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
            client = new Client(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
            String initMsg = client.initialize();
            if (!initMsg.equals("BUSY") && !initMsg.split(" ")[0].equals("ERROR")) {
                System.out.println(initMsg);
                String message = "";
                Scanner scanner = new Scanner(System.in);
                while (!message.equals("LOSE") && !message.equals("WON")) {
                    message = client.game(scanner.nextLine());
                    System.out.println(message);

                }
            } else {
                System.out.println(initMsg);
                System.out.println("The server could not handle you");
            }
        } catch (Exception e) {
        } finally {
            if (client != null)
                client.closeConnection(0);
        }
    }
}
