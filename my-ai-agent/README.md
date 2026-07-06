## AI 跨境电商合规助手 - 技术 Demo

## 一、项目简介

本项目是一个基于 Spring AI 的跨境电商合规 Agent Demo。用户输入一段商品描述或"种草笔记"，Agent 会自动提取商品信息，调用多个工具进行合规检查、风险检测和税费估算，最终生成结构化的合规报告。

**技术亮点：**
- AI 自主决策调用工具（Function Calling）
- 多轮对话上下文记忆
- 完整工程化（Docker Compose + Flyway + Swagger）

---

## 二、技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.5.6 |
| AI 框架 | Spring AI | 1.1.3 |
| LLM | DeepSeek (API) | deepseek-chat |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | 7-alpine |
| ORM | Spring Data JPA | - |
| 构建工具 | Maven | 3.9+ |
| API 文档 | SpringDoc OpenAPI | 2.6.0 |

---

## 三、核心功能

### 1. 多轮对话
- 基于 `ChatMemory` 实现会话上下文管理
- 按 sessionId 隔离不同会话
- MySQL 持久化存储会话和消息历史

### 2. 品类合规检查（ComplianceCheckTool）
- 检查商品品类在目标国家是否受限
- 支持国家：US、EU、CN、JP、UK、AU
- 内置受限品类规则库

### 3. 营销用语风险检测（RiskKeywordTool）
- 检测宣传文案中的绝对化用语和风险关键词
- 返回风险等级：低/中/高
- 提供修改建议

### 4. 跨境税费估算（TaxEstimateTool）
- 根据品类和目标国家估算综合税率
- 计算税费金额和含税总价
- 提供税负水平提示

---

## 四、架构设计

```
用户输入（种草笔记）
        ↓
   Agent Controller
        ↓
    Agent Service（编排层）
        ↓
   Spring AI ChatClient
        ↓
     DeepSeek LLM
        ↓
   Function Calling
   ┌─────┼─────┐
   ↓     ↓     ↓
合规检查 风险检测 税费估算
   │     │     │
   └─────┼─────┘
        ↓
   结构化合规报告
```

**数据流：**
1. 用户发送消息 → Controller
2. AgentService 调用 ChatClient
3. LLM 根据用户输入自主决定调用哪些 Tool
4. Tool 执行后返回结果给 LLM
5. LLM 汇总生成结构化报告返回给用户
6. 会话和消息持久化到 MySQL

---

## 五、快速启动

### 1. 启动基础设施

```bash
cd docker
docker compose up -d
```

### 2. 配置 API Key

```bash
export DEEPSEEK_API_KEY="sk-你的密钥"
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

### 4. 访问接口

- Swagger UI: http://localhost:8080/swagger-ui.html
- 创建会话: `POST /api/sessions`
- 发送消息: `POST /api/agent/sessions/{id}/chat`

---

## 六、API 示例

### 创建会话

```bash
curl -X POST http://localhost:8080/api/sessions \
  -H "Content-Type: application/json" -d "{}"
```

### 发送消息

```bash
curl -X POST http://localhost:8080/api/agent/sessions/1/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "我们有一款智能手表，品类是电子产品，想卖到美国。价格299美元。宣传语：'\''全网最好、销量第一、绝对安全'\''，请帮我检查是否合规。"}'
```

### 响应示例（结构化报告）

```json
{
  "code": 200,
  "data": {
    "sessionId": 1,
    "reply": "## 合规报告\n\n### 品类合规 ✅\n- 品类：电子产品\n- 目标国家：美国\n- 状态：合规\n\n### 风险检测 ⚠️\n- 风险等级：高风险\n- 发现关键词：最好、第一、绝对安全\n- 建议：修改宣传语\n\n### 税费估算\n- 税率：10%\n- 税费：$29.90\n- 含税总价：$328.90"
  }
}
```

---

## 七、项目亮点

1. **AI 自主决策**：LLM 根据用户输入自动判断需要调用哪些工具，无需硬编码

2. **完整工程化**：
   - Docker Compose 一键启动基础设施
   - Flyway 管理数据库版本
   - Swagger 自动生成 API 文档
   - 配置分离（dev/prod）

3. **可扩展性**：
   - 新增 Tool 只需实现 `@Tool` 注解方法
   - 合规规则库可从 Redis 缓存读取
   - 支持切换到其他 LLM（OpenAI/Ollama）

---

## 八、目录结构

```
my-ai-agent/
├── docker/
│   └── docker-compose.yml
├── src/
│   └── main/
│       ├── java/com/qian/agent/
│       │   ├── AgentApplication.java
│       │   ├── config/
│       │   │   ├── AiConfig.java
│       │   │   └── ...
│       │   ├── agent/tools/
│       │   │   ├── ComplianceCheckTool.java
│       │   │   ├── RiskKeywordTool.java
│       │   │   └── TaxEstimateTool.java
│       │   ├── controller/
│       │   ├── service/
│       │   ├── entity/
│       │   └── repository/
│       └── resources/
│           ├── application.yml
│           ├── prompts/
│           │   └── system-agent.st
│           └── db/migration/
│               └── V1__init_schema.sql
├── pom.xml
└── README.md
```