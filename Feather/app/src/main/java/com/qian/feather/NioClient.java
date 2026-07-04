package com.qian.feather;

import android.util.Log;

import com.qian.feather.item.Msg;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * @author Qian Yanrun
 */

public class NioClient {
    private static final String host = "115.29.241.155";
    //private static final String host = "192.168.1.110";
    private static final int port = 8000;
    private SocketChannel socketChannel;
    private Selector selector;
    public static String clientIP;
    private static volatile NioClient instance = null;
    private static volatile ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private static final ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
    private MsgListener msgListener;
    private static final InetSocketAddress address = new InetSocketAddress(host,port);
    public void setMsgListener(MsgListener msgListener) {
        this.msgListener = msgListener;
        if(this.msgListener != null) {
            msgListener.onConnectionStatusChanged(true);
        }
    }

    private NioClient() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(address);
        selector = Selector.open();
        socketChannel.register(selector,SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
        clientIP = socketChannel.getLocalAddress().toString();
    }
    //Designing Pattern - Singleton Pattern
    public static NioClient getInstance() throws IOException {
        if (instance == null) {
            synchronized (Client.class) {
                if (instance == null) {
                    instance = new NioClient();
                }
            }
        }
        return instance;
    }
    public boolean isConnected() {
        if(socketChannel != null) {
            return socketChannel.isConnected();
        }
        return false;
    }
    public void handle() throws IOException {
        while(true) {
            int select = selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            while(keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                if (!key.isValid()) {
                    continue;
                }
                if(key.isConnectable()) {
                    Log.d("NioClient","isConnectable");
                    if(socketChannel.isConnectionPending()) {
                        socketChannel.finishConnect();
                    }
                } else if(key.isReadable()) {
                    Log.d("NioClient","isReadable");
                    Msg msg = read(key);
                    msgListener.onMsgReceived(msg);
                }
            }
        }
    }
    public Msg read(SelectionKey key) throws IOException {
        Msg msg = null;
        SocketChannel client = (SocketChannel) key.channel();
        int length = 0;
        lengthBuffer.clear();
        if((client.read(lengthBuffer)) != -1) {
            lengthBuffer.flip();
            length = lengthBuffer.getInt();
            System.out.printf("Length is: %d",length);
            ByteBuffer buffer = ByteBuffer.allocate(length-4);
            int bytesRead = client.read(buffer);
            if(bytesRead != -1) {
                buffer.flip();
                byte[] msgBytes = new byte[buffer.limit()];
                buffer.get(msgBytes);
                msg = new Msg(msgBytes);
                System.out.println(msg);
            }
        }
        return msg;
    }
    public boolean sendMsg(Msg msg) {
        try {
            if(socketChannel != null) {
                if(socketChannel.isConnected()) {
                    ByteBuffer buffer = msg.getByteBuffer();
                    SelectionKey key = socketChannel.keyFor(selector);
                    key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                    while(buffer.hasRemaining()) {
                        socketChannel.write(buffer);
                        if (!buffer.hasRemaining()) {
                            //数据全部写完后才取消WRITE兴趣
                            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                        }
                    }
                    return true;
                }
            }
        } catch(Exception e) {
            return false;
        }
        return false;
    }
    public void disconnectFromServer() {
        if(socketChannel != null) {
            try {
                socketChannel.close();
                if(this.msgListener != null) {
                    msgListener.onConnectionStatusChanged(false);
                }
            } catch (IOException e) {

            }
        }
    }

    public interface MsgListener {
        void onMsgReceived(Msg msg);
        void onConnectionStatusChanged(boolean connected);
        void onException(Exception exception);
    }

}