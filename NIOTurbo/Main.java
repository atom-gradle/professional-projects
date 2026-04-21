package NIOTurbo;

/**
* @author: Qian
*/

public class Main {

    public static void main(String[] args) throws Exception {

        MainReactor reactor = new MainReactor();
        new Thread(reactor).start();

    }
}