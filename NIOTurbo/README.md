# NIO-Turbo

> 从0实现的基于Java NIO的高性能TCP服务器，使用自定义消息协议，包含MD5校验

## 🎯 项目背景

深入学习**Redis** 和 **Netty** 设计思想的实践项目，通过手写 **Reactor** 模式、自定义协议和性能优化，理解NIO编程的核心原理。

## ✨ 核心特性

- **Reactor多线程模型**：MainReactor负责连接，SubReactor负责IO读写
- **自定义消息协议**：支持文本和文件传输，内置MD5完整性校验
- **对象池优化**：复用Msg对象，减少GC压力
- **高并发**：使用Java 21虚拟线程处理业务逻辑
- **完整的压测验证**：高并发压测客户端，测量端到端性能

## 📁 项目结构
```bash
NIOTurbo/
├── HighConcurrentTestClient.java # 高并发压测客户端
├── Main # 服务器启动类
├── MainReactor.java # 主Reactor（处理连接）
├── Msg.java # 消息实体
├── MsgPool.java # 对象池
├── SubReactor.java # 从Reactor（处理IO）
└── Util.java # 工具类（MD5校验）
```

## 🚀 快速开始
### 启动服务器

```bash
# 编译
javac NIOTurbo/*.java

# 运行
java NIOTurbo.Main
```

输出：
```
SubReactor launched
SubReactor launched
SubReactor launched
SubReactor launched
SubReactor launched
Server launches, listening for port 8000
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
122+ Bytes

### 消息示例
```
Msg {
    length='145',
    from='张三', 
    to='李四', 
    when='2026-04-22T22:01:47', 
    type=1, 
    state=0, 
    fileExt='', 
    content='Hello.This is ZhangSan.', 
    MD5Check='26fd1aa197c82f0d75f67f6a1b26eafd'
}
```

### 1.多线程模型
```java
// MainReactor：处理连接
public void accept(SelectionKey key) {
    ServerSocketChannel server = (ServerSocketChannel)key.channel();
    SocketChannel client = null;
    try {
        client = server.accept();
        client.configureBlocking(false);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
    SubReactor subReactor = subReactors[nextSubReactorIndex];
    nextSubReactorIndex = (nextSubReactorIndex + 1) % subReactors.length;
    try {
        subReactor.register(client);
    } catch (ClosedChannelException e) {
        throw new RuntimeException(e);
    }
}

// SubReactor：处理读写
public void read(SelectionKey key) {
    SocketChannel client = (SocketChannel) key.channel();

    try {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        int bytesRead = client.read(lengthBuffer);

        if (bytesRead == -1) {
            // 连接关闭
            key.cancel();
            client.close();
            System.out.println("Client disconnected");
            return;
        }

        if (bytesRead < 4) {
            // 没读完整，继续等待
            return;
        }

        lengthBuffer.flip();
        int msgLength = lengthBuffer.getInt();

        if (msgLength <= 0 || msgLength > 8192) {
            System.out.println("Invalid message length: " + msgLength);
            key.cancel();
            client.close();
            return;
        }

        // 读取消息体
        ByteBuffer msgBuffer = ByteBuffer.allocateDirect(msgLength);
        while (msgBuffer.hasRemaining()) {
            bytesRead = client.read(msgBuffer);
            if (bytesRead == -1) {
                return;
            }
        }

        msgBuffer.flip();

        Thread.startVirtualThread(() -> {
            try {
                //Msg recoveredMsg = new Msg(data);
                //recoveredMsg.setLength(msgLength);
                Msg recoveredMsg = new Msg(msgBuffer, msgLength);

                if(!Util.verifyMsg(recoveredMsg)) {
                    //if(!Util.verifyMsg(data)) {
                    //System.out.println("Validation Failed");
                } else {
                    sendSuccessResponse(client);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    } catch (IOException e) {
        System.out.println("Error reading from client: " + e.getMessage());
        try {
            key.cancel();
            client.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
```
分离连接请求和业务IO，采用Java 21+提供的虚拟线程，极大提升了系统吞吐量

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
### 通用测试环境
| 配置 | 参数 |
|------|------|
| Java | 21 |
| CPU | R7 8845H (16T) |
| OS | Windows 11 25H2 |
| 网络 | localhost |


**（连接数，每连接消息数）分别为（2000，500），（1500，200），（1000，100）**

测试条件1
| 指标 | 数值 | 
|------|------|
| 模拟客户端总数 | 2,000 |
| 每个客户端发送消息数 | 500 |
| 总消息数 | 1,000,000 |

压测结果1
| 指标 | 数值 |
|------|------|
| 总耗时 | 14.93秒 |
| 成功接收并解析消息数 | 1,000,000 |
| 成功率 | 100% |
| 平均延迟 | 25.37ms |
| QPS | 52,000+ |

测试条件2
| 指标 | 数值 |
|------|------|
| 模拟客户端总数 | 1,500 |
| 每个客户端发送消息数 | 200 |
| 总消息数 | 300,000 |

压测结果2
| 指标 | 数值 |
|------|------|
| 总耗时 | 10.83秒 |
| 成功接收并解析消息数 | 300,000 |
| 成功率 | 100% |
| 平均延迟 | 20.16ms |
| QPS | ~28,915.66 |

测试条件3
| 指标 | 数值 |
|------|------|
| 模拟客户端总数 | 1,000 |
| 每个客户端发送消息数 | 100 |
| 总消息数 | 100,000 |

压测结果3
| 指标 | 数值 |
|------|------|
| 总耗时 | 6.83秒 |
| 成功接收并解析消息数 | 100,000 |
| 成功率 | 100% |
| 平均延迟 | 12.40ms |
| QPS | ~14,652 |

### 测试说明
本测试要求服务端接收消息，并校验MD5值之后，向客户端返回ACK消息，客户端发送这条消息的线程收到ACK响应后，才发送下一条消息，所以客户端所测得QPS=服务端QPS
本项目根目录下有压力测试视频 `NOITurbo压力测试.mp4` 和 测试结果截图 `测试结果.jpg`
