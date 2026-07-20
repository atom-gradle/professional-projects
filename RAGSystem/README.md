**技术栈：**
[![Python](https://img.shields.io/badge/Python-3.12-3776AB?logo=python)](https://www.python.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?logo=postgresql)](https://www.postgresql.org/)
[![pgvector](https://img.shields.io/badge/pgvector-0.8.2-4169E1)](https://github.com/pgvector/pgvector)
[![DeepSeek](https://img.shields.io/badge/DeepSeek-API-4D6BFE?logo=deepseek)](https://platform.deepseek.com/)
[![Sentence-Transformers](https://img.shields.io/badge/Sentence--Transformers-2.2.2-FFD43B)](https://www.sbert.net/)

**项目描述：**
RAG（检索增强生成）智能问答系统，基于 PostgreSQL + pgvector 向量数据库与 DeepSeek 大语言模型构建。支持多格式知识库导入（JSON/TXT/Markdown）、语义检索与生成式问答。本人独立开发。

**核心功能：**
- **向量化检索与存储**：基于 `pgvector` 扩展实现向量数据的持久化存储与索引，结合 `IVFFlat` 或 `HNSW` 算法加速近似最近邻搜索。通过 `cosine` 距离计算语义相似度
- **混合 Embedding 引擎**：采用本地 `sentence-transformers` 模型（`shibing624/text2vec-base-chinese`）生成文本向量，无需依赖外部 API，保障数据隐私与稳定性。
- **智能文档处理流水线**：实现文档自动切分（`min_chunk_size=150`，`max_chunk_size=500`），支持句子边界感知的智能分割。支持 `JSON` 格式，支持批量导入与增量更新
- **LLM 集成与对话生成**：基于 `OpenAI SDK` 调用 `DeepSeek-v4-flash` 模型生成高质量回答。通过精心设计的 `System Prompt` 约束模型行为，实现「仅基于上下文回答」与「拒绝幻觉」的可靠机制
- **数据库性能优化**：通过 `EXPLAIN ANALYZE` 分析查询计划，为向量相似度搜索建立 `IVFFlat` 索引（`lists=100`）。结合 `JSONB` 字段存储元数据，支持灵活的文档分类与过滤
- **异步与容错机制**：实现数据库连接池管理（`psycopg2` 事务控制），提供完整的错误捕获与事务回滚。Embedding 生成失败时自动重试，保证数据一致性