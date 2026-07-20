# professional-projects

Welcome to my professional projects repository! This is a collection of programming projects for job-seeking.

## About Me

- 🎓 华中农业大学 - 计算机科学与技术（2024.9 - 2028.6）
- 📝 CET-6 638分
- 📍 上海户口 | 可全职实习（每周5天）
- 🔭 正在寻找AI后端、后端开发、全栈开发日常实习

---

## 📁 Projects

### 1. [AI 跨境电商合规助手](./my-ai-agent/)

**技术栈：**
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M6-6DB33F?logo=spring)](https://spring.io/projects/spring-ai)
[![DeepSeek](https://img.shields.io/badge/DeepSeek-API-4A6CF7)](https://deepseek.com)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7-FF4438?logo=redis)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://www.docker.com/)

**项目描述：**
借助Cursor快速开发，基于 Spring AI + DeepSeek 构建的 AI Agent 应用，实现多轮对话、工具调用和结构化报告生成。用户输入商品描述，Agent 自主决策调用合规检查、风险检测、税费估算三个工具，输出合规报告。

**核心功能：**
- **多轮对话记忆**：基于 `ChatMemory` + **Redis** 实现会话上下文管理，按 sessionId 隔离
- **品类合规检查**：检查商品品类在 US/EU/CN/JP/UK/AU 等国家是否受限，内置受限品类规则库
- **营销用语风险检测**：检测宣传文案中的绝对化用语和风险关键词，返回低/中/高三档风险等级及修改建议
- **跨境税费估算**：根据品类和目标国家估算综合税率，计算税费和含税总价

**项目亮点：**
- **Agent 工具编排**：使用 **Spring AI** 的 **@Tool** 注解 + **MethodToolCallbackProvider** 注册 3 个工具函数；LLM 根据用户输入自主决策调用顺序和组合，实现 **Function Calling** 完整链路
- **对话记忆管理**：基于 **ChatMemory** + **Redis** 实现会话上下文隔离，支持多轮连续对话；**MySQL** 持久化存储会话和消息历史
- **工程化落地**：Docker Compose 一键启动 MySQL + Redis；Flyway 管理数据库版本
- **性能验证**：三个工具并行调用，减少端到端响应时间；Redis 缓存会话上下文，减少重复 LLM 调用

**API 示例：**
```bash
# 创建会话
curl -X POST http://localhost:8080/api/sessions -H "Content-Type: application/json" -d "{}"
```
```
# 发送消息（Agent 自动调用三个工具）
curl -X POST http://localhost:8080/api/agent/sessions/1/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "智能手表，电子产品，卖到美国，299美元。宣传语：'\''全网最好、销量第一'\''"}'
```

---

**技术栈：**
[![Python](https://img.shields.io/badge/Python-3.11-3776AB?logo=python)](https://www.python.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?logo=postgresql)](https://www.postgresql.org/)
[![pgvector](https://img.shields.io/badge/pgvector-0.8.2-4169E1)](https://github.com/pgvector/pgvector)
[![DeepSeek](https://img.shields.io/badge/DeepSeek-API-4D6BFE?logo=deepseek)](https://platform.deepseek.com/)
[![Sentence-Transformers](https://img.shields.io/badge/Sentence--Transformers-2.2.2-FFD43B)](https://www.sbert.net/)
[![FAISS](https://img.shields.io/badge/FAISS-1.7.4-009688)](https://github.com/facebookresearch/faiss)

**项目描述：**
RAG（检索增强生成）智能问答系统，基于 PostgreSQL + pgvector 向量数据库与 DeepSeek 大语言模型构建。支持多格式知识库导入（JSON/TXT/Markdown）、语义检索与生成式问答。本人独立完成从架构设计、向量化引擎开发到生产环境部署的全链路交付。

**核心功能：**
- **向量化检索与存储**：基于 `pgvector` 扩展实现向量数据的持久化存储与索引，结合 `IVFFlat` 或 `HNSW` 算法加速近似最近邻搜索。通过 `cosine` 距离计算语义相似度，在 **8 个文档块** 规模下实现毫秒级检索响应
- **混合 Embedding 引擎**：采用本地 `sentence-transformers` 模型（`shibing624/text2vec-base-chinese`）生成文本向量，无需依赖外部 API，保障数据隐私与稳定性。兼容多种 Embedding 模型，支持热切换
- **智能文档处理流水线**：实现文档自动切分（`chunk_size=300`，`overlap=50`），支持句子边界感知的智能分割。通过 `JSON/TXT/Markdown` 多格式加载器，支持批量导入与增量更新，并内置去重机制避免数据污染
- **LLM 集成与对话生成**：基于 `OpenAI SDK` 调用 `DeepSeek-v4-flash` 模型生成高质量回答。通过精心设计的 `System Prompt` 约束模型行为，实现「仅基于上下文回答」与「拒绝幻觉」的可靠机制
- **数据库性能优化**：通过 `EXPLAIN ANALYZE` 分析查询计划，为向量相似度搜索建立 `IVFFlat` 索引（`lists=100`）。结合 `JSONB` 字段存储元数据，支持灵活的文档分类与过滤
- **异步与容错机制**：实现数据库连接池管理（`psycopg2` 事务控制），提供完整的错误捕获与事务回滚。Embedding 生成失败时自动重试，保证数据一致性
- **服务部署与运维**：基于 `Docker Compose` 一键启动 PostgreSQL + pgvector 容器，配置持久化存储。通过 `.env` 环境变量管理敏感配置，支持 `Docker` 镜像打包与 `ECS` 云端部署

---
2. RAG-System (RAG 知识库系统)
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

---

### 2. [玉米表型采析系统-后端](./CornPhenoUltra/)

**技术栈：**
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)](https://www.mysql.com/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-FF6600?logo=rabbitmq)](https://www.rabbitmq.com/)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.7-000000)](https://baomidou.com/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker)](https://www.docker.com/)

**项目描述：**
玉米表型采析系统后端，支持微信登录、采集记录上传、算法服务调用、分析报告生成、文件下载等功能。本人独立完成从数据库设计、接口开发到生产环境部署的全链路交付。

**核心功能：**
- **用户认证与鉴权**：基于 `Interceptor` + `JWT` 实现无状态认证，结合 `ThreadLocal` 完成用户身份线程级绑定，拦截未登录请求，保障接口安全性
- **数据库索引优化**：通过 `EXPLAIN` 分析执行计划，结合 `SHOW INDEX` 中的 `cardinality` 字段评估索引选择性，针对复杂统计查询（多条件筛选 + 分组聚合）设计覆盖索引。在 **51万条数据** 规模下，查询耗时从 **1.15s 降至 0.33s**，降低约 **70%**
- **异步处理与解耦**：使用 `DeferredResult` + `RabbitMQ` 异步调用独立部署的 Python 算法服务，释放请求线程，提升系统吞吐量；通过消息确认机制保证任务不丢失，超时时长 30s，失败时进入死信队列，实现更健全的处理机制
- **服务部署与运维**：基于 `Docker` 打包后端服务，部署至阿里云 ECS；配置域名、DNS 服务及 TLS 证书，通过 `Nginx` 反向代理实现 HTTPS 访问

**项目链接：** https://github.com/atom-gradle/professional-projects/tree/main/CornPhenoUltra

---

### 3. [NIO-Turbo](./NIOTurbo/)

**技术栈：**
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk)](https://www.java.com/)
[![NIO](https://img.shields.io/badge/Java%20NIO-ED8B00)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/package-summary.html)

**项目描述：**
从 0 手写实现的基于 Reactor 模式、NIO API 的高性能 TCP 服务器，自定义消息体，支持高并发场景下的消息解析与处理。

**核心设计：**
- **Reactor 多线程模型**：`MainReactor` 负责监听处理连接请求，`SubReactor` 负责读写事件 & 业务处理，有效分离连接建立与业务处理
- **自定义消息协议**：Msg 消息体采用 `4字节长度 + 多字段定长头 + 变长内容体` 设计，同时支持文字 & 文件传输；加入 MD5 校验字段，支持消息分隔与完整性校验
- **对象池优化**：借鉴享元模式思想，设计 Msg 消息体对象池 `MsgPool`，通过复用已有对象，有效减少对象在新生代和老年代之间的拷贝和 GC 停顿，提升系统吞吐量

**性能验证：**
模拟 2000 个并发连接，每连接发送 500 条消息（含 MD5 校验 + ACK 响应），在 R7 8845H 轻薄本，localhost 测得：

| 指标 | 数值 |
|------|------|
| 端到端吞吐量 | **52,000+ QPS** |
| 平均响应延迟 | **25.37 ms** |
| 成功率 | **100%**（100万消息0丢失） |

---

## 🛠️ 技术能力总览

| 领域 | 技术 |
|------|------|
| 后端框架 | Spring Boot, MyBatis-Plus, Spring AI |
| 中间件 | MySQL, Redis, RabbitMQ, PostgreSQL |
| 语言 | Java, Python |
| AI 工具 | Cursor, DeepSeek |
| 运维部署 | Docker, Linux, Nginx, 阿里云 ECS |
| 构建和管理 | Maven, Gradle, Git |

---

## 📫 Contact Me

- 📧 Email: pro.gradle@outlook.com
