**项目描述：**
RAG（检索增强生成）智能问答系统，基于 PostgreSQL + pgvector 向量数据库与 DeepSeek 大语言模型构建。支持多格式知识库导入（JSON/TXT）、语义检索与生成式问答。本人独立开发。

### 📁 项目结构
```
RAGSystem/
├── knowledge_base # 知识文档存放位置
├───── knowledge-digital-products.json # 知识文档
├── models # sentence-transformer模型
├── config.py # 配置类
├── Log.py # 日志打印
├── RAG-System.py # RAG系统
├── SemanticChunker.py # 语义分块器
└── util.py # 工具类
```

**技术栈：**
[![Python](https://img.shields.io/badge/Python-3.12-3776AB?logo=python)](https://www.python.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?logo=postgresql)](https://www.postgresql.org/)
[![pgvector](https://img.shields.io/badge/pgvector-0.8.2-4169E1)](https://github.com/pgvector/pgvector)
[![DeepSeek](https://img.shields.io/badge/DeepSeek-API-4D6BFE?logo=deepseek)](https://platform.deepseek.com/)
[![Sentence-Transformers](https://img.shields.io/badge/Sentence--Transformers-2.2.2-FFD43B)](https://www.sbert.net/)

**核心实现**
- **向量化检索与存储引擎**
基于 PostgreSQL **pgvector** 扩展实现向量数据的持久化存储，支持 768 维向量字段及余弦相似度（`<=>` 操作符）语义检索。采用 **IVFFlat** 索引（`lists=100`）加速近似最近邻搜索，结合相似度阈值过滤（默认 0.6）与 Top-K 动态配置，平衡召回率与查询性能
- **智能文档处理与语义分块**
基于 **sentence-transformers** 本地模型（`paraphrase-multilingual-MiniLM-L12-v2`）生成文本向量，无需依赖外部 API，保障数据隐私与稳定性。实现语义感知的智能分块器（**SemanticChunker**）：通过相邻句子余弦相似度计算（阈值 0.6）结合长度约束（150-500 字符），实现句子边界感知的动态切分，相比固定长度切分，语义完整性提升显著。
- **RAG 生成与 LLM 集成**
基于 **OpenAI SDK** 统一接口调用 **deepseek-v4-flash** API，通过精心设计的系统约束 Prompt（「仅基于上下文回答」+「拒绝幻觉」）保障生成内容的可控性与可溯源性。检索结果自动注入上下文窗口，构建结构化知识片段，支持引用来源追溯
- **数据库连接与会话管理**
采用 **psycopg2** 原生驱动管理 PostgreSQL 连接，通过 `autocommit=False` 事务模式保障文档导入与分块写入的原子性。支持 JSONB 元数据灵活存储，实现文档分类、来源等维度的扩展过滤

### 配置和启动
**方式一**

1.下载所需依赖
```bash
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
```

2.安装PostgreSQL和pgvector拓展

3.替换config.py中的deepseek-api-key
```
# config.py

# DeepSeek API配置
deepseek_api_key: str = "YOUR_API_KEY"
deepseek_base_url: str = "https://api.deepseek.com/v1"
chat_model: str = "deepseek-v4-flash"
```

4.运行
```bash
python RAG-System
```

**方式二 (Docker)**

1.安装Docker

2.运行Docker Compose
```bash
docker compose up -d
```