package Server;

/**
 * Created by timothy on 2017-09-05, Server.
 */
public class Main {
    public static void main(String[] args) {
        Server s = new Server(5000);
        s.start();
    }
}
