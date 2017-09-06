package Client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        Client client = null;
        try {
            InetAddress address = null;
            try {
                address = InetAddress.getByName("localhost");
            } catch (UnknownHostException e) {
                System.err.println("Unknown host");
            }
            client = new Client(address, 5000);
            String initMsg = client.initialize();
            if (!initMsg.equals("BUSY")) {
                System.out.println(initMsg);
                String message = "";
                Scanner scanner = new Scanner(System.in);
                while (!message.equals("LOSE") && !message.equals("WON")) {
                    message = client.game(scanner.next());
                    System.out.println(message);
                }
            } else
                System.out.println("The server is busy");
        } catch (Exception e) {
        } finally {
            if (client != null)
                client.closeConnection();
        }
    }
}
