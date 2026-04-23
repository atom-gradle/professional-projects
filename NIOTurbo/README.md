# NIO-Turbo

> 从零实现的基于Java NIO的高性能TCP服务器，支持自定义消息协议、MD5校验和对象池优化

## 🎯 项目背景

深入学习Netty设计思想的实践项目，通过手写Reactor模式、自定义协议和性能优化，理解NIO编程的核心原理。

## ✨ 核心特性

- **Reactor多线程模型**：MainReactor负责连接，SubReactor负责IO读写
- **自定义消息协议**：支持文本和文件传输，内置MD5完整性校验
- **对象池优化**：复用Msg对象，减少GC压力
- **虚拟线程支持**：使用Java 21虚拟线程处理业务逻辑
- **完整的压测验证**：高并发压测客户端，测量端到端性能

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
| 长度 | 4 B | 30 B | B | 19 B | 4B | 4B | 3B | variable | 32 B |
|------|------|------|------|------|------|------|------|------|------|
| 字段 | length | from | to | when | type | state | fileExt | content | md5check |

### 消息示例
```json
Msg {
    length='144',
    from='张三', 
    to='李四', 
    when='2026-04-22T22:01:47', 
    type=1, 
    state=0, 
    fileExt='txt', 
    content='Hello.This is ZhangSan', 
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
借鉴享元模式和池化思想，复用Msg对象，减少对象在新生代之间、新生代和老年代之间的拷贝，减少GC停顿

### 3.消息完整性校验
```java
subExecutor.execute(() -> {
    try {
        Msg recoveredMsg = new Msg(data);
        recoveredMsg.setLength(msgLength);

        String response;
        if(!Util.verifyMsg(recoveredMsg)) {
            System.out.println("Validation Failed");
            response = "FAIL: Invalid message checksum";
        } else {
            System.out.println(recoveredMsg);
            response = "ACK: Message received and verified successfully";
        }

        // 发送响应给客户端
        sendResponse(client, response);

    } catch (Exception e) {
        e.printStackTrace();
    }
});
```
调用`Util.verifyMsg(String msg)`对接收到的Msg进行完整性校验，完整则响应客户端`"ACK: Message received and verified successfully"`，否则响应`"FAIL: Invalid message checksum"`

## 性能测试
### 测试环境
| 配置 | 参数 |
|------|------|
| Java | 21 |
| CPU | R7 8845H |
| OS | Windows 11 25H2 |
| 网络 | localhost |
| 客户端总数 | 1000 |
| 每个客户端发送消息数 | 100 |
| 总消息数 | 100,000 |

### 压测结果
| 指标 | 数值 | 说明 |
|------|------|------|
| 总耗时 | 14.84秒 | 包含连接间隔 |
| 成功接收并解析消息数 | 100,000 |
| 成功率 | 100.00& |
| 平均延迟 | 24.57ms |
| QPS | 6,737,18 消息/秒 |

### 测试说明
本测试要求服务端接收消息，并校验MD5值之后，向客户端返回ACK消息，客户端发送这条消息的线程收到ACK响应后，才发送下一条消息，所以客户端所测得QPS=服务端QPS
