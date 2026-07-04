package com.qian.feather;

import java.net.*;
import java.io.*;

import com.qian.feather.item.FixedMsg;

@Deprecated
public class Client {

    private Socket socket;
    String host = "192.168.1.108";
    int port = 9000;

    private BufferedInputStream bis;
    private BufferedOutputStream bos;
    private static volatile Client instance;

    private Client() {
    }

    public static Client getInstance() {
        if (instance == null) {
            synchronized (Client.class) {
                if (instance == null) {
                    instance = new Client();
                }
            }
        }
        return instance;
    }

    public void connectToServer(OnMsgReceivedListener onMsgReceivedListener) throws IOException {
        socket = new Socket(host,port);
        bis = new BufferedInputStream(socket.getInputStream(),4096);
        bos = new BufferedOutputStream(socket.getOutputStream(),4096);
        Runnable r = new ServerHandler(socket,onMsgReceivedListener);
        new Thread(r).start();
    }

    public interface OnMsgReceivedListener {
        void onMsgReceived(FixedMsg msg);
    }

    class ServerHandler implements Runnable {
        OnMsgReceivedListener listener;

        public ServerHandler(Socket socket,OnMsgReceivedListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            while(socket.isConnected()) {
                int bytesRead;
                byte[] buffer = new byte[8192];
                try {
                    while((bytesRead = bis.read(buffer)) != -1) {
                        FixedMsg msg = FixedMsg.createFixedMsg(buffer,bytesRead);
                        System.out.println(msg);
                        listener.onMsgReceived(msg);
                    }
                } catch(Exception e) {}
            }

        }

    }

    public boolean isConnected()  {
        if(socket != null) {
            return socket.isConnected();
        }
        return false;
    }

    public void sendMsg(FixedMsg msg) throws IOException {
        if(socket != null && socket.isConnected()) {
            bos.write(msg.getBytes());
            bos.flush();
        }
    }
    public void sendMsg(FixedMsg[] msgs) throws IOException {
        for(FixedMsg msg : msgs) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            sendMsg(msg);
        }
    }

    public void disconnectFromServer() throws IOException {
        if(socket != null) {
            socket.close();
        }
    }
}