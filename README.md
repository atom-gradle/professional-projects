# professional-projects

欢迎来到professional-projects仓库！这里放着我的求职作品。
Welcome to my professional projects repository! This is a collection of programming projects for job-seeking.

## About Me

- 🎓 华中农业大学 (211) 计算机科学与技术 2024级
- 📝 CET-6 638分
- 🔭 正在寻找后端开发日常实习

## 📁 Projects

### 1.[玉米表型采析系统-后端](./CCNUpromax/)
**技术栈：** 
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk)](https://www.java.com/) [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot) [![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)](https://www.mysql.com/) [![Redis](https://img.shields.io/badge/Redis-6.0-DC382D?logo=redis)](https://redis.io/) [![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.4.3-000000)](https://baomidou.com/)

**项目描述：**  
玉米表型采析系统后端，支持微信登录、采集记录上传、算法服务调用、分析报告生成、文件下载等功能。本人独立完成后端代码编写、迭代优化及生产环境部署。

-   **用户认证与鉴权**：基于 `Interceptor` + `JWT` 实现无状态认证，结合 `ThreadLocal` 完成用户身份线程级绑定，拦截未登录请求，**保障接口安全性**
    
- **数据库索引优化**：通过 `EXPLAIN` 分析执行计划，结合 `SHOW INDEX` 中的 `cardinality` 字段评估索引选择性，针对复杂统计查询（多条件筛选 + 分组聚合）设计**覆盖索引**。在 **51万条数据** 规模下，查询耗时从 **1.15s 降至 0.37s**，降低约 **60%**
    
-   **异步处理与解耦**：使用 `DeferredResult` + `RabbitMQ` 异步调用算法服务，释放请求线程，**提升系统吞吐量**；通过消息确认机制保证任务不丢失，加入死信队列，实现更健全的处理机制
    
-   **服务部署与运维**：基于 `Docker` 打包后端服务，部署至阿里云 ECS；配置域名、DNS服务 及 TLS 证书，通过 `Nginx` 反向代理实现 HTTPS 访问

### 2.[NIO-Turbo](./NIOTurbo/)（从0实现的基于NIO的高性能TCP服务器）
**技术栈：** `Java 21` `NIO` `Reactor模式`

**项目描述：**  
从0手写实现基于 Reactor 模式的 TCP 服务器，自定义消息体，支持高并发场景下的消息解析与处理。

-   **Reactor 多线程模型**：`MainReactor` 负责监听处理连接请求，`SubReactor` 负责读写事件&业务处理，有效分离连接建立与业务处理

-   **自定义消息协议**：Msg消息体实体采用 `长度字段` + `多字段消息体` 协议，同时支持文字&文件，加入采用MD5算法的校验字段，支持消息分隔与完整性校验
    
-   **对象池优化**：借鉴享元模式思想，涉及Msg消息体对象池MsgPool，通过复用已有对象，**有效减少对象在新生代和老年代之间的拷贝和GC停顿**，提升系统吞吐量
    
- **性能验证**：模拟1000个并发连接，每连接发送100条消息（含MD5校验+ACK响应），在R7 8845H轻薄本，localhost测得：
  - **端到端吞吐量**：6,600+ QPS
  - **平均响应延迟**：25.37 ms
  - **成功率**：100%（10万消息0丢失）

## Contact Me

- 📧 pro.gradle@outlook.com
