# 玉米表型采析系统后端 (CornPhenoUltra)

> 玉米表型数据采集与分析系统的后端服务，支持微信登录、采集记录管理、算法服务调用、报告生成等功能

## 📋 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [核心实现](#核心实现)
- [性能优化案例](#性能优化案例)
- [部署](#部署)
- [API 文档](#api-文档)
- [TODO](#todo)

## ✨ 功能特性

| 模块 | 功能 | 状态 |
|------|------|------|
| 用户认证 | 微信小程序登录 / JWT 无状态认证 | ✅ 完成 |
| 采集记录 | 上传、查询、修改、删除 | ✅ 完成 |
| 文件管理 | 图片/视频上传、存储、下载 | ✅ 完成 |
| 算法调用 | RabbitMQ 异步调用分析服务 | ✅ 完成 |
| 报告生成 | 异步生成 PDF 分析报告 | ✅ 完成 |
| 数据统计 | 多维度聚合统计（地块/品种/时间） | ✅ 完成 |

## 🛠️ 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 后端主语言 |
| Spring Boot | 3.x | 应用框架 |
| Spring Security | 6.x | 认证授权 |
| MySQL | 8.0 | 数据存储 |
| MyBatis-Plus | 3.5 | ORM 框架 |
| RabbitMQ | 3.x | 消息队列 |
| Docker | 24.x | 容器化部署 |
| Nginx | 1.24 | 反向代理 / HTTPS |

## 📁 项目结构
src/main/java/com/qian/
├── config/ # 配置类
│ ├── SecurityConfig # Spring Security 配置
│ ├── RabbitMQConfig # 消息队列配置
│ └── WebConfig # 跨域等配置
├── controller/ # 控制器层
│ ├── AdminController # 管理端
│ ├── AnalysisController # 报告生成
│ ├── AuthController # 微信登录 / JWT
│ ├── RecordController # 采集记录 CRUD
│ ├── FileController # 文件上传下载
│ └── UserController # 用户管理
├── service/ # 业务逻辑层
│ ├── impl/ # 实现类
├── mapper/ # MyBatis-Plus Mapper
├── pojo/ # 实体类
├── util/ # 工具类
│ ├── JwtUtil # JWT 工具
│ └── ThreadLocalUtil # 用户上下文绑定
└── exception/ # 全局异常处理
├── BusinessException # 自定义异常
└── GlobalExceptionHandler

## 🚀 快速开始

### 环境要求

- JDK 21+
- MySQL 8.0+
- Maven 3.8+
- RabbitMQ 3.x（可选，可关闭）

### 配置

#### 1. 克隆项目

```bash
git clone https://github.com/atom-gradle/CornPhenod.git
cd CornPheno

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db_cornpheno?useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

# JWT 配置
jwt:
  secret: your-jwt-secret-key
  expiration: 86400000  # 24小时

# 微信小程序配置
wx:
  app-id: your-app-id
  app-secret: your-app-secret
```

# 开发环境
mvn spring-boot:run

# 生产环境
mvn clean package
java -jar target/CornPheno.jar

# 构建镜像
docker build -t CornPheno .

# 运行容器
docker run -d \
  --name CornPheno \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  CornPheno

