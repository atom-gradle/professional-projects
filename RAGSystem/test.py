import os,json,re
from typing import List, Dict, Optional, Tuple
from dataclasses import dataclass
from datetime import datetime

import openai

import psycopg2

# 在 import 之前设置环境变量
import os

from config import Config

os.environ['HF_ENDPOINT'] = 'https://hf-mirror.com'  # 使用镜像站
# 安装依赖：pip install sentence-transformers
from sentence_transformers import SentenceTransformer
config = Config()
model = SentenceTransformer(config.model_path)

def test_model():
    # 使用免费且高效的中文embedding模型

    # 或者英文模型
    # model = SentenceTransformer('all-MiniLM-L6-v2')

    client = openai.OpenAI(
        api_key="your_api_key",
        base_url="https://api.deepseek.com"
    )

    try:
        # 测试聊天模型
        response = client.chat.completions.create(
            model="deepseek-v4-flash",
            messages=[{"role": "user", "content": "Hello"}],
            max_tokens=5
        )
        print("✅ Chat 模型测试成功")
    except Exception as e:
        print(f"❌ Chat 模型测试失败: {e}")

    try:
        test_embedding = model.encode(['测试文本'])
        print("✅ Embedding 模型测试成功")
    except Exception as e:
        print(f"❌ Embedding 模型测试失败: {e}")

def test_db_connection():
    try:
        conn = psycopg2.connect(
            host="localhost",
            port=5432,
            database="rag_demo",
            user="postgres",
            password="123456",
            client_encoding='UTF8'
        )
        cursor = conn.cursor()
        cursor.execute("SELECT version()")
        version = cursor.fetchone()[0]
        print(f"✅ PostgreSQL连接成功: {version[:50]}...")

        cursor.execute("SELECT extversion FROM pg_extension WHERE extname = 'vector'")
        result = cursor.fetchone()
        if result:
            print(f"✅ pgvector版本: {result[0]}")
        else:
            print("⚠️ pgvector扩展未安装，请运行: CREATE EXTENSION vector;")

        cursor.close()
        conn.close()
        return True
    except Exception as e:
        print(f"❌ 连接失败: {e}")
        return False

def fixed_size_chunk(text : str,chunk_size=100, overlap=20) -> List[str]:
    """简单粗暴按字符数切"""
    chunks = []
    for i in range(0, len(text), chunk_size - overlap):
        chunk = text[i:i + chunk_size]
        chunks.append(chunk)
    return chunks


def recursive_chunk2(text, chunk_size=100, overlap=20):
    """按优先级递归切分：段落 > 句子 > 词"""
    separators = ["\n\n", "\n", "。", "！", "？", "；", "，", " ", ""]

    def _split(text, separators):
        if not separators:
            # 最后按字符切
            return [text[i:i + chunk_size] for i in range(0, len(text), chunk_size)]

        sep = separators[0]
        if sep == "":
            # 按字符切
            return [text[i:i + chunk_size] for i in range(0, len(text), chunk_size)]

        # 用当前分隔符切分
        splits = text.split(sep)
        chunks = []
        current_chunk = ""

        for split in splits:
            # 如果当前块+新片段超过限制，递归处理
            if len(current_chunk) + len(split) > chunk_size:
                if current_chunk:
                    chunks.append(current_chunk)
                # 如果单个片段就超长，用下一级分隔符再切
                if len(split) > chunk_size:
                    chunks.extend(_split(split, separators[1:]))
                    current_chunk = ""
                else:
                    current_chunk = split
            else:
                current_chunk += sep + split if current_chunk else split

        if current_chunk:
            chunks.append(current_chunk)

        return chunks

    return _split(text, separators)


def semantic_chunk(text, model, max_chunk_size=100):
    """基于语义相似度动态切分"""
    from sklearn.metrics.pairwise import cosine_similarity

    # 1. 先用句子拆（中文用句号/问号/感叹号）
    sentences = re.split(r'[。！？；\n]+', text)

    # 2. 计算每个句子的embedding
    embeddings = [model.encode(s) for s in sentences if s.strip()]

    # 3. 计算相邻句子的相似度
    similarities = []
    for i in range(len(embeddings) - 1):
        sim = cosine_similarity([embeddings[i]], [embeddings[i + 1]])[0][0]
        similarities.append(sim)

    # 4. 在相似度低的地方切分（话题转换）
    chunks = []
    current_chunk = sentences[0]

    for i in range(1, len(sentences)):
        # 如果相似度低于阈值 或 当前块太长
        if (similarities[i - 1] < 0.5 and len(current_chunk) > 100) or \
                len(current_chunk) + len(sentences[i]) > max_chunk_size:
            chunks.append(current_chunk)
            current_chunk = sentences[i]
        else:
            current_chunk += sentences[i]

    if current_chunk:
        chunks.append(current_chunk)

    return chunks

if __name__ == '__main__':
    text = "智能手机是集成了通信、计算和多媒体功能的便携式设备。主流操作系统包括Android和iOS。手机的核心硬件包括处理器（CPU/SoC）、内存（RAM）、存储（ROM）、屏幕、电池和摄像头。手机处理器主要厂商有高通（Snapdragon系列）、联发科（Dimensity系列）、苹果（A系列芯片）、华为海思（Kirin系列）和三星（Exynos系列）。"

    # 测试小 chunk_size
    chunks = recursive_chunk2(text, chunk_size=50, overlap=10)
    for i, c in enumerate(chunks):
        print(f"[{i}] len={len(c)}: {c}")
    print(f"\n总共 {len(chunks)} 块")

    semantic_chunk(text, model)
