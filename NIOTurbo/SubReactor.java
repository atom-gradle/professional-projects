package NIOTurbo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Qian
 * handle business IO
 */

public class SubReactor implements Runnable {

    private Selector selector;
    private static volatile boolean running = true;

    //private ThreadLocal<ByteBuffer> readBuffer = ThreadLocal.withInitial(() -> ByteBuffer.allocate(8192));

    private final ExecutorService subExecutor;

    public SubReactor(ExecutorService subExecutor) throws IOException {
        selector = Selector.open();
        this.subExecutor = subExecutor;
    }

    public void register(SocketChannel client) throws ClosedChannelException {
        if (client == null || !client.isOpen()) {
            return;
        }

        synchronized (this) {
            SelectionKey key = client.register(selector, SelectionKey.OP_READ);
            System.out.println("Registering...");

            selector.wakeup();
        }

    }

    @Override
    public void run() {
        System.out.println("SubReactor launched");

        while(running) {
            try {
                if(!running) {
                    break;
                }
                int readyChannels = selector.select(100);
                if(readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while(keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if(key.isReadable()) {
                        try {
                            read(key);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

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
            ByteBuffer msgBuffer = ByteBuffer.allocate(msgLength);
            while (msgBuffer.hasRemaining()) {
                bytesRead = client.read(msgBuffer);
                if (bytesRead == -1) {
                    return;
                }
            }

            msgBuffer.flip();
            byte[] data = new byte[msgLength];
            msgBuffer.get(data);

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

                    // 响应回复客户端
                    sendResponse(client, response);

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

    private void sendResponse(SocketChannel client, String message) {
        try {
            byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);

            // 构建响应消息
            ByteBuffer responseBuffer = ByteBuffer.allocate(4 + responseBytes.length);
            responseBuffer.putInt(responseBytes.length);
            responseBuffer.put(responseBytes);
            responseBuffer.flip();

            // 发送响应
            while (responseBuffer.hasRemaining()) {
                client.write(responseBuffer);
            }

            System.out.println("Sent response to client: " + message);

        } catch (IOException e) {
            System.err.println("Failed to send response: " + e.getMessage());
        }
    }

    /*
    public void read(SelectionKey key) throws Exception {
        SocketChannel client = (SocketChannel) key.channel();
        int bytesRead = 0;
        if((bytesRead = client.read(readBuffer)) != -1) {
            System.out.printf("read %d bytes\n", bytesRead);
            byte[] data = readBuffer.array();
            readBuffer.clear();

            subExecutor.execute(() -> {
                List<Msg> decodedMsgs = msgDecoder.decode(data);
                System.out.println(decodedMsgs);
            });
        } else {
            key.cancel();
            client.close();
            if(clientsMap.containsKey(client)) {
                clientsMap.remove(client);
            }
            System.out.println("A client disconnected! "+clientsMap);
        }
    }

     */
}

