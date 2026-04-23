package NIOTurbo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Qian
 * 基于虚拟线程的高并发测试客户端
 */
public class HighConcurrentTestClient {

    private final String host;
    private final int port;
    private final int virtualThreadCount; // 虚拟线程数量
    private final int messagesPerThread;  // 每个线程发送消息数

    private final LongAdder totalMessages = new LongAdder();
    private final LongAdder successMessages = new LongAdder();
    private final LongAdder failedMessages = new LongAdder();
    private final LongAdder totalLatencyNanos = new LongAdder();

    private final MsgPool msgPool;

    private final CountDownLatch startLatch;
    private final CountDownLatch finishLatch;

    private volatile long testStartTime;
    private volatile long testEndTime;

    public HighConcurrentTestClient(String host, int port,
                                   int virtualThreadCount,
                                   int messagesPerThread) {
        this.host = host;
        this.port = port;
        this.virtualThreadCount = virtualThreadCount;
        this.messagesPerThread = messagesPerThread;
        this.msgPool = new MsgPool();
        this.startLatch = new CountDownLatch(1);
        this.finishLatch = new CountDownLatch(virtualThreadCount);
    }

    public void start() throws InterruptedException {
        System.out.println("========== 虚拟线程并发测试 ==========");
        System.out.printf("目标服务器: %s:%d\n", host, port);
        System.out.printf("虚拟线程数: %d\n", virtualThreadCount);
        System.out.printf("每线程消息数: %d\n", messagesPerThread);
        System.out.printf("总消息量: %d\n", (long) virtualThreadCount * messagesPerThread);
        System.out.println("======================================\n");

        testStartTime = System.currentTimeMillis();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            for (int i = 0; i < virtualThreadCount; i++) {
                final int clientId = i;
                executor.submit(() -> runVirtualClient(clientId));
                if(i % 50 == 0) {
                    TimeUnit.MILLISECONDS.sleep(500);
                }
            }

            System.out.println("Waiting for all virtual threads to come online...");
            TimeUnit.SECONDS.sleep(2);

            System.out.println("Start sending messages...\n");
            startLatch.countDown();

            finishLatch.await();
            testEndTime = System.currentTimeMillis();

        } catch (Exception e) {
            e.printStackTrace();
        }

        printResults();
    }

    private void runVirtualClient(int clientId) {
        SocketChannel channel = null;

        try {
            channel = SocketChannel.open();
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress(host, port));

            System.out.printf("[VT-%05d] 连接成功\n", clientId);

            startLatch.await();

            // 发送消息
            long threadStartTime = System.nanoTime();
            int localSuccess = 0;
            int localFailed = 0;

            for (int i = 0; i < messagesPerThread; i++) {
                Msg msg = msgPool.borrow();
                msg.setTextMsg("VT_" + clientId, "Server", "Msg_" + clientId + "_" + i);

                long sendStart = System.nanoTime();

                // 发送请求
                ByteBuffer buffer = msg.getReadableByteBuffer();
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }

                // 等待响应，先读取响应长度
                ByteBuffer lenBuffer = ByteBuffer.allocate(4);
                while (lenBuffer.hasRemaining()) {
                    int read = channel.read(lenBuffer);
                    if (read == -1) throw new IOException("Connection closed");
                }
                lenBuffer.flip();
                int respLength = lenBuffer.getInt();

                // 读取响应体
                ByteBuffer respBuffer = ByteBuffer.allocate(respLength);
                while (respBuffer.hasRemaining()) {
                    int read = channel.read(respBuffer);
                    if (read == -1) throw new IOException("Connection closed");
                }
                respBuffer.flip();
                byte[] respData = new byte[respLength];
                respBuffer.get(respData);
                String response = new String(respData, StandardCharsets.UTF_8);

                long latency = System.nanoTime() - sendStart;

                // 验证响应
                if (response.startsWith("ACK")) {
                    totalLatencyNanos.add(latency);
                    successMessages.increment();
                    localSuccess++;
                } else {
                    failedMessages.increment();
                    localFailed++;
                    System.err.println("Received error: " + response);
                }

                msgPool.release(msg);
                totalMessages.increment();
            }

            long threadEndTime = System.nanoTime();
            double threadDuration = (threadEndTime - threadStartTime) / 1_000_000.0;

            System.out.printf("[VT-%05d] 完成: 成功=%d, 失败=%d, 耗时=%.2fms, QPS=%.2f\n",
                    clientId, localSuccess, localFailed, threadDuration,
                    localSuccess / (threadDuration / 1000));

            channel.close();

        } catch (Exception e) {
            System.err.printf("[VT-%05d] Exception: %s\n", clientId, e.getMessage());
            e.printStackTrace();
        } finally {
            finishLatch.countDown();
        }
    }

    private void printResults() {
        long totalDuration = testEndTime - testStartTime;
        long totalSent = successMessages.sum();
        long totalFailed = failedMessages.sum();
        long totalMsg = totalMessages.sum();

        double qps = (totalSent * 1000.0) / totalDuration;
        double avgLatency = totalLatencyNanos.sum() / (double) Math.max(1, totalSent) / 1_000_000.0;
        double successRate = (totalSent * 100.0) / Math.max(1, totalMsg);

        System.out.println("\n========== 测试结果 ==========");
        System.out.printf("总耗时: %.2f 秒\n", totalDuration / 1000.0);
        System.out.printf("虚拟线程数: %d\n", virtualThreadCount);
        System.out.printf("总消息数: %d\n", totalMsg);
        System.out.printf("成功消息: %d\n", totalSent);
        System.out.printf("失败消息: %d\n", totalFailed);
        System.out.printf("成功率: %.2f%%\n", successRate);
        System.out.printf("平均延迟: %.2f ms\n", avgLatency);
        System.out.printf("QPS: %,.2f 消息/秒\n", qps);
        System.out.println("================================");
    }

    public static void main(String[] args) throws InterruptedException {
        String host = "localhost";
        int port = 8000;
        int virtualThreadCount = 1000;// 1000个虚拟线程
        int messagesPerThread = 100;// 每个线程发送100条消息

        HighConcurrentTestClient client = new HighConcurrentTestClient(
                host, port, virtualThreadCount, messagesPerThread
        );

        client.start();
    }
}