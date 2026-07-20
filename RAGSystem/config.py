from dataclasses import dataclass

@dataclass
class Config:

    # PostgreSQL配置
    pg_host: str = "localhost"
    pg_port: int = 5432
    pg_database: str = "rag_demo"
    pg_user: str = "postgres"
    pg_password: str = "123456"

    # DeepSeek API配置
    deepseek_api_key: str = "your_api_key"
    deepseek_base_url: str = "https://api.deepseek.com/v1"
    chat_model: str = "deepseek-v4-flash"

    # Embedding 配置 - 使用本地模型替代
    use_local_embedding: bool = True  # True: 使用本地模型, False: 使用API
    embedding_model_name: str = "shibing624/text2vec-base-chinese"  # 本地embedding模型
    embedding_dimension: int = 768  # text2vec-base-chinese 的维度
    model_path = "./models/text2vec-base-chinese"
    model_name = "shibing624/text2vec-base-chinese"

    # RAG配置
    chunk_size: int = 300
    chunk_overlap: int = 50
    top_k: int = 3
    similarity_threshold: float = 0.6

    # 源配置
    mirror = "https://hf-mirror.com"
