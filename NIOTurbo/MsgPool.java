package NIOTurbo;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MsgPool {
    private final Queue<Msg> pool = new ConcurrentLinkedQueue<>();

    public Msg borrow() {
        Msg msg = pool.poll();
        return msg != null ? msg : new Msg();
    }

    public void release(Msg msg) {
        msg.clear();
        pool.offer(msg);
    }
}
