"""
RAG Demo with PostgreSQL (pgvector) + DeepSeek API
2026-07-17
"""

# fundamental
import os,json
from typing import List, Dict, Optional
from datetime import datetime

import psycopg2,sqlalchemy,openai
from pgvector.psycopg2 import register_vector
from sqlalchemy import create_engine, Column, String, Text, DateTime, Integer, Float
from sqlalchemy.orm import sessionmaker
from sqlalchemy.dialects.postgresql import JSONB, ARRAY

os.environ['HF_ENDPOINT'] = 'https://hf-mirror.com'
from sentence_transformers import SentenceTransformer

# customized
from config import Config
from knowledge_base.test_questions_digital_products import test_questions
from SemanticChunker import SemanticChunker
from Log import Log
from util import load_documents, load_documents_from_file

Base = sqlalchemy.orm.declarative_base()

class Document(Base):
    """文档表"""
    __tablename__ = 'documents'

    id = Column(Integer, primary_key=True, autoincrement=True)
    content = Column(Text, nullable=False)
    extra_metadata = Column(JSONB, default={})
    created_at = Column(DateTime, default=datetime.now)

class Chunk(Base):
    """文档块表"""
    __tablename__ = 'chunks'

    id = Column(Integer, primary_key=True, autoincrement=True)
    document_id = Column(Integer, nullable=False)
    content = Column(Text, nullable=False)
    chunk_index = Column(Integer, nullable=False)
    extra_metadata = Column(JSONB, default={})
    created_at = Column(DateTime, default=datetime.now)

class PgVectorRAG:

    def __init__(self, config: Config):
        self.config = config

        # 初始化DeepSeek客户端
        self.client = openai.OpenAI(
            api_key=config.deepseek_api_key,
            base_url=config.deepseek_base_url
        )

        # 初始化本地 Embedding 模型
        if config.use_local_embedding:
            Log.info(f"正在加载本地 Embedding 模型: {config.embedding_model_name}")
            if not os.path.exists(config.model_path):
                self.embedding_model = SentenceTransformer(config.model_name)
                self.embedding_model.save(config.model_path)
            else:
                Log.debug("直接从本地加载")
                self.embedding_model = SentenceTransformer(config.model_path)  # 直接从本地加载
            Log.info("本地 Embedding 模型加载完成")

        # 初始化PostgreSQL连接
        self.conn = self._init_database()

        # 注册pgvector
        register_vector(self.conn)

        self._create_tables()

        self.semantic_chunker = SemanticChunker()

        Log.info("RAG系统初始化完成")
        Log.info(f"   - PostgreSQL: {config.pg_host}:{config.pg_port}/{config.pg_database}")
        Log.info(f"   - Chat模型: {config.chat_model}")
        Log.info(f"   - Embedding模型: {'本地 ' + config.embedding_model_name if config.use_local_embedding else 'API'}")
        Log.info(f"   - Chunker: {self.semantic_chunker.__class__.__name__}")


    def _init_database(self) -> psycopg2.extensions.connection:
        try:
            dsn = f"host={self.config.pg_host} port={self.config.pg_port} dbname={self.config.pg_database} user={self.config.pg_user} password={self.config.pg_password} client_encoding=UTF8"
            conn = psycopg2.connect(dsn)
            conn.autocommit = False

            with conn.cursor() as cur:
                cur.execute("SELECT version()")
                version = cur.fetchone()[0]
                if isinstance(version, bytes):
                    version = version.decode('utf-8', errors='replace')
                Log.debug(f"Successfully connected to database: {version[:50]}...")

            return conn
        except Exception as e:
            Log.error(f"数据库连接失败: {e}")
            raise

    def _create_tables(self):
        """创建必要的表和索引"""
        cursor = self.conn.cursor()

        # 创建文档表 - 修正字段名
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS documents (
                id SERIAL PRIMARY KEY,
                content TEXT NOT NULL,
                extra_metadata JSONB DEFAULT '{}'::jsonb,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)

        # 创建文档块表（包含向量）
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS chunks (
                id SERIAL PRIMARY KEY,
                document_id INTEGER NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
                content TEXT NOT NULL,
                chunk_index INTEGER NOT NULL,
                extra_metadata JSONB DEFAULT '{}'::jsonb,
                embedding vector(768),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)

        # 创建向量索引（使用IVFFlat）
        cursor.execute("""
            CREATE INDEX IF NOT EXISTS idx_chunks_embedding 
            ON chunks 
            USING ivfflat (embedding vector_cosine_ops)
            WITH (lists = 100)
        """)

        self.conn.commit()
        cursor.close()
        Log.info("Successfully created database and tables")

    def get_embedding(self, text: str) -> List[float]:
        """获取文本的 Embedding 向量"""
        if self.config.use_local_embedding and self.embedding_model:
            # 使用本地模型
            try:
                embedding = self.embedding_model.encode(text, normalize_embeddings=True)
                return embedding.tolist()
            except Exception as e:
                Log.info(f" 本地Embedding生成失败: {e}")
                raise

    def add_document(self, content: str, extra_metadata: Optional[Dict] = None) -> int:
        """添加文档到知识库"""
        cursor = self.conn.cursor()

        try:
            # 1. 插入文档 - 修正字段名
            cursor.execute(
                "INSERT INTO documents (content, extra_metadata) VALUES (%s, %s) RETURNING id",
                (content, json.dumps(extra_metadata or {}))
            )
            doc_id = cursor.fetchone()[0]

            # 2. 切分文本
            # chunks = self.chunk_text(content)

            chunks = self.semantic_chunker.chunk(content)
            Log.info(f"文档切分为 {len(chunks)} 个块")

            # 3. 生成并存储每个块的embedding
            for idx, chunk_text in enumerate(chunks):
                Log.info(f"   - 处理块 {idx + 1}/{len(chunks)}...")
                embedding = self.get_embedding(chunk_text)

                # 将向量转换为PostgreSQL vector格式
                embedding_str = '[' + ','.join(map(str, embedding)) + ']'

                cursor.execute(
                    """
                    INSERT INTO chunks (document_id, content, chunk_index, extra_metadata, embedding)
                    VALUES (%s, %s, %s, %s, %s)
                    """,
                    (doc_id, chunk_text, idx, json.dumps({}), embedding_str)  # 修正：extra_metadata
                )

            self.conn.commit()
            Log.info(f" 文档添加成功 (ID: {doc_id})")
            return doc_id

        except Exception as e:
            self.conn.rollback()
            Log.error(f"添加文档失败: {e}")
            raise
        finally:
            cursor.close()

    def add_documents(self, documents: List[Dict[str, str]]):
        """批量添加文档"""
        for doc in documents:
            content = doc.get('content', '')
            extra_metadata = doc.get('extra_metadata', {})
            self.add_document(content, extra_metadata)

    def retrieve(self, query: str, top_k: Optional[int] = None) -> List[Dict]:
        """检索最相关的文档块"""
        if top_k is None:
            top_k = self.config.top_k

        cursor = self.conn.cursor()

        try:
            # 生成查询向量
            query_embedding = self.get_embedding(query)
            query_vector = '[' + ','.join(map(str, query_embedding)) + ']'

            # 使用pgvector的余弦相似度搜索
            cursor.execute(
                """
                SELECT 
                    c.id,
                    c.content,
                    c.document_id,
                    c.chunk_index,
                    c.extra_metadata,
                    1 - (c.embedding <=> %s::vector) AS similarity
                FROM chunks c
                WHERE 1 - (c.embedding <=> %s::vector) > %s
                ORDER BY c.embedding <=> %s::vector
                LIMIT %s
                """,
                (query_vector, query_vector, self.config.similarity_threshold, query_vector, top_k)
            )

            results = cursor.fetchall()

            formatted_results = []
            for row in results:
                formatted_results.append({
                    'id': row[0],
                    'content': row[1],
                    'document_id': row[2],
                    'chunk_index': row[3],
                    'extra_metadata': row[4],
                    'similarity': float(row[5])
                })

            return formatted_results

        except Exception as e:
            Log.error(f"检索失败: {e}")
            raise
        finally:
            cursor.close()

    def generate_answer(self, query: str, context: str) -> str:
        system_prompt = """你是一个基于知识库回答问题的专业助手。请遵循以下规则：
1. 仅基于提供的上下文信息回答问题
2. 如果上下文信息不充分，请如实告知
3. 回答要简洁、准确、有条理
4. 可以引用上下文中的具体内容
5. 不要添加上下文之外的信息
"""

        user_prompt = f"""
        上下文信息：
        {context}

        用户问题：{query}

        请基于上述上下文信息回答问题：
        """

        try:
            response = self.client.chat.completions.create(
                model=self.config.chat_model,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt}
                ],
                temperature=0.7,
                max_tokens=500,
                top_p=0.9
            )

            return response.choices[0].message.content

        except Exception as e:
            Log.error(f"[Error]  生成回答失败: {e}")
            return "抱歉，生成回答时出现错误。"

    def query(self, question: str, top_k: Optional[int] = None, verbose: bool = True) -> Dict:
        """完整的RAG查询流程"""
        if verbose:
            Log.info(f"问题: {question}")

        # 1. 检索相关文档
        if verbose:
            Log.debug("Searching for relative docs...")

        retrieved_docs = self.retrieve(question, top_k)

        if verbose:
            Log.info(f"检索到 {len(retrieved_docs)} 个相关片段:")
            for i, doc in enumerate(retrieved_docs, 1):
                Log.info(f"{i}. 相似度: {doc['similarity']:.4f}")
                Log.info(f"{doc['content'][:150]}...")

        # 2. 构建上下文
        context = "\n\n---\n\n".join([
            f"片段 {i + 1}:\n{doc['content']}"
            for i, doc in enumerate(retrieved_docs)
        ])

        # 3. 生成回答
        if verbose:
            print("正在生成回答...")

        answer = self.generate_answer(question, context)

        result = {
            'question': question,
            'answer': answer,
            'context': context,
            'retrieved_documents': retrieved_docs,
            'num_retrieved': len(retrieved_docs)
        }

        if verbose:
            print(f"最终回答:")
            print(answer)
            print()

        return result

    def get_stats(self) -> Dict:
        """获取系统统计信息"""
        cursor = self.conn.cursor()

        try:
            cursor.execute("SELECT COUNT(*) FROM documents")
            doc_count = cursor.fetchone()[0]

            cursor.execute("SELECT COUNT(*) FROM chunks")
            chunk_count = cursor.fetchone()[0]

            cursor.execute("""
                SELECT schemaname, tablename, indexname 
                FROM pg_indexes 
                WHERE tablename = 'chunks' 
                AND indexname LIKE '%embedding%'
            """)
            indexes = cursor.fetchall()

            return {
                'document_count': doc_count,
                'chunk_count': chunk_count,
                'has_vector_index': len(indexes) > 0,
                'vector_indexes': [idx[2] for idx in indexes]
            }

        except Exception as e:
            Log.error(f"获取统计信息失败: {e}")
            return {}
        finally:
            cursor.close()

    def delete_document(self, doc_id: int):
        """删除文档"""
        cursor = self.conn.cursor()
        try:
            cursor.execute("DELETE FROM documents WHERE id = %s", (doc_id,))
            self.conn.commit()
            Log.info(f"文档 {doc_id} 删除成功")
        except Exception as e:
            self.conn.rollback()
            Log.error(f"删除文档失败: {e}")
            raise
        finally:
            cursor.close()

    def close(self):
        if self.conn:
            self.conn.close()
            Log.info("Database connection closed")

def main():
    # 1. 配置
    config = Config()
    # 2. 初始化RAG系统
    rag = PgVectorRAG(config)
    # 3. 准备知识库文档
    documents = load_documents_from_file(config.file_path)

    # 4. 构建知识库
    Log.info("开始构建知识库...")

    for doc in documents:
        rag.add_document(doc['content'], doc['extra_metadata'])
    # 5. 查看统计信息
    stats = rag.get_stats()
    Log.info(f"系统统计:")
    Log.info(f"   - 文档数量: {stats['document_count']}")
    Log.info(f"   - 块数量: {stats['chunk_count']}")
    Log.info(f"   - 向量索引: {stats['has_vector_index']}")

    # 6. 测试问答
    Log.info("开始问答测试")

    for question in test_questions:
        rag.query(question, top_k=config.top_k)

    rag.close()

if __name__ == "__main__":
    main()