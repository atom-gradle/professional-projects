# NIO-Turbo

> 从零实现的基于Java NIO的高性能TCP服务器，支持自定义消息协议、MD5校验和对象池优化

## 🎯 项目背景

深入学习Netty设计思想的实践项目，通过手写Reactor模式、自定义协议和性能优化，理解NIO编程的核心原理。

## ✨ 核心特性

- **Reactor多线程模型**：MainReactor负责连接，SubReactor负责IO读写
- **自定义消息协议**：支持文本和文件传输，内置MD5完整性校验
- **对象池优化**：复用Msg对象，减少GC压力
- **虚拟线程支持**：使用Java 21虚拟线程处理业务逻辑
- **完整的压测验证**：自研压测客户端，测量端到端性能

## 🛠️ 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 核心语言 |
| NIO | - | 非阻塞IO |
| Virtual Thread | - | 高并发业务处理 |
| Maven | 3.8+ | 构建工具 |

## 📁 项目结构
```bash
NIOTurbo/
├── MainReactor.java # 主Reactor（处理连接）
├── SubReactor.java # 从Reactor（处理IO）
├── Msg.java # 消息实体
├── MsgPool.java # 对象池
├── Util.java # 工具类（MD5校验）
├── HighConcurrentTestClient.java # 高并发压测客户端
└── Main.java # 服务端启动类
```

## 🚀 快速开始

### 启动服务器

```bash
# 编译
javac NIOTurbo/*.java

# 运行
java NIOTurbo.MainReactor
```

输出：
```bash
Server launches, listening for port 8000
SubReactor launched
SubReactor launched
...
```

### 启动高并发压测客户端
```bash
# 运行
java NIOTurbo.HighConcurrentTestClient
```

## 自定义协议
### 消息格式
+----------+--------------------+
| 4 bytes  |    variable        |
| 消息长度  |    消息体          |
+----------+--------------------+

### 消息示例
```bash
Msg {
    from='VT_923', 
    to='Server', 
    when='2026-04-22T22:01:47', 
    type=1, 
    state=0, 
    fileExt='txt', 
    content='Msg_923_98', 
    MD5Check='26fd1aa197c82f0d75f67f6a1b26eafd'
}
```

### 1.多线程模型
```java
// MainReactor：处理连接
public void accept(SelectionKey key) {
    SocketChannel client = server.accept();
    client.configureBlocking(false);
    subReactors[nextIndex].register(client);
}

// SubReactor：处理读写
public void read(SelectionKey key) {
    // 读取消息 → 校验 → 提交到线程池
    subExecutor.execute(() -> handleMessage());
}
```

### 2.对象池优化
```java
public class MsgPool {
    private final Queue<Msg> pool = new ConcurrentLinkedQueue<>();
    
    public Msg borrow() {
        Msg msg = pool.poll();
        return msg != null ? msg : new Msg();
    }
    
    public void release(Msg msg) {
        msg.clear();  // 重置状态
        pool.offer(msg);
    }
}
```

### 3.消息完整性校验
```java

```

## 性能测试


### 压测结果
========== 测试结果 ==========
总耗时: 14.84 秒
虚拟线程数: 1000
总消息数: 100000
成功消息: 100000
失败消息: 0
成功率: 100.00%
平均延迟: 24.57 ms
QPS: 6,737.18 消息/秒
================================
