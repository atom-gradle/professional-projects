# AI Agent Demo

Spring Boot 3 + Spring AI + DeepSeek + MySQL + Redis 的 AI Agent 演示项目。

## 技术栈

- Java 17
- Spring Boot 3.3.5
- Spring AI 1.0.0（DeepSeek OpenAI 兼容 API）
- MySQL 8.0 + Flyway
- Redis 7

## 快速开始

### 1. 启动基础设施

```bash
cd docker
docker compose up -d
```

### 2. 配置环境变量

复制 `.env.qian` 为 `.env`，填入 DeepSeek API Key：

```bash
DEEPSEEK_API_KEY=sk-your-key-here
```

在 PowerShell 中加载环境变量：

```powershell
Get-Content .env | ForEach-Object {
  if ($_ -match '^([^#=]+)=(.*)$') { [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process') }
}
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

### 4. 访问 API 文档

http://localhost:8080/swagger-ui.html

## 核心 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/health` | 健康检查 |
| POST | `/api/sessions` | 创建会话 |
| GET | `/api/sessions` | 会话列表 |
| GET | `/api/sessions/{id}/messages` | 消息历史 |
| POST | `/api/agent/sessions/{id}/chat` | 同步对话 |
| POST | `/api/agent/sessions/{id}/chat/stream` | SSE 流式对话 |

## 示例调用

```bash
# 创建会话
curl -X POST http://localhost:8080/api/sessions -H "Content-Type: application/json" -d "{}"

# 对话（替换 {sessionId}）
curl -X POST http://localhost:8080/api/agent/sessions/1/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"北京今天天气怎么样？\"}"
```

## 项目结构

```
src/main/java/com/qian/agent/
├── config/          # Spring / AI / Redis 配置
├── controller/      # REST 接口
├── service/         # 业务逻辑
├── agent/
│   ├── tools/       # Agent 工具（天气、计算器）
│   └── prompt/      # Prompt 模板加载
├── entity/          # JPA 实体
├── repository/      # 数据访问
├── redis/           # Redis 缓存
├── model/           # DTO
├── common/          # 通用类
└── exception/       # 异常处理
```

## Agent 工具

- **getWeather**：查询城市天气（Demo 模拟数据）
- **calculate**：数学表达式计算

## 下一步

- [ ] Redis 持久化 ChatMemory
- [ ] RAG 知识库问答
- [ ] Vue 3 聊天前端
