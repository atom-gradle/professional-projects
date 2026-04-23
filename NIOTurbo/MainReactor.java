package NIOTurbo;

import java.io.IOException;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author Qian
 * handle connections
 */

public class MainReactor implements Runnable {

    // SubReactors
    private SubReactor[] subReactors;
    private int nextSubReactorIndex;

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private static volatile boolean running = true;
    // Port
    public static final int port = 8000;

    // ThreadPools
    private static final int availableCores = Runtime.getRuntime().availableProcessors();
    private final ExecutorService subBusinessPool;

    public MainReactor() throws Exception {
        subBusinessPool = new ThreadPoolExecutor(availableCores, 2 * availableCores, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000), new ThreadPoolExecutor.CallerRunsPolicy());

        // assign and start subReactors
        subReactors = new SubReactor[5];
        for(int i = 0;i < 5;i++) {
            subReactors[i] = new SubReactor(subBusinessPool);
            new Thread(subReactors[i]).start();
        }

        // Configuration for Server
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);

    }

    @Override
    public void run(){
        System.out.printf("Server launches, listening for port %d\n", port);
        while(running) {
            if(!running) {
                break;
            }
            int readyChannels = 0;
            try {
                readyChannels = selector.select();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //int readyChannels = selector.select(500);
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
                if(key.isAcceptable()) {
                    accept(key);
                    System.out.println("Accept a connection");
                }
            }
        }
    }

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

}
