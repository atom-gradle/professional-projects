package NIOTurbo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
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

            System.out.println("等待所有虚拟线程启动...");
            TimeUnit.SECONDS.sleep(2);

            System.out.println("开始发送消息...\n");
            startLatch.countDown();

            // 等待所有线程完成
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
            // 1. 创建连接
            channel = SocketChannel.open();
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress(host, port));

            System.out.printf("[VT-%05d] 连接成功\n", clientId);

            startLatch.await();

            // 2. 发送消息
            long threadStartTime = System.nanoTime();
            int localSuccess = 0;
            int localFailed = 0;

            for (int i = 0; i < messagesPerThread; i++) {
                // 借用Msg对象
                Msg msg = msgPool.borrow();
                msg.setTextMsg(
                        "VT_" + clientId,
                        "Server",
                        String.format("Msg_%d_%d", clientId, i)
                );

                long sendStart = System.nanoTime();

                try {
                    // 发送消息
                    ByteBuffer buffer = msg.getReadableByteBuffer();
                    while (buffer.hasRemaining()) {
                        channel.write(buffer);
                    }

                    long sendEnd = System.nanoTime();
                    totalLatencyNanos.add(sendEnd - sendStart);

                    localSuccess++;
                    successMessages.increment();

                } catch (IOException e) {
                    localFailed++;
                    failedMessages.increment();
                    System.err.printf("[VT-%05d] 发送失败: %s\n", clientId, e.getMessage());
                } finally {
                    // 归还对象
                    msgPool.release(msg);
                    totalMessages.increment();
                }

                // 控制发送速率
                // Thread.sleep(1);
            }

            long threadEndTime = System.nanoTime();
            double threadDuration = (threadEndTime - threadStartTime) / 1_000_000.0;

            System.out.printf("[VT-%05d] 完成: 成功=%d, 失败=%d, 耗时=%.2fms, QPS=%.2f\n",
                    clientId, localSuccess, localFailed, threadDuration,
                    localSuccess / (threadDuration / 1000));

            channel.close();

        } catch (Exception e) {
            System.err.printf("[VT-%05d] 异常: %s\n", clientId, e.getMessage());
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
        int virtualThreadCount = 1000;      // 1000个虚拟线程
        int messagesPerThread = 100;         // 每个线程发送100条消息

        HighConcurrentTestClient client = new HighConcurrentTestClient(
                host, port, virtualThreadCount, messagesPerThread
        );

        client.start();
    }
}