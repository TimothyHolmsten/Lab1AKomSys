package Client;

import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        Client c = new Client(5000);
        String initMsg = c.initialize();
        if(!initMsg.equals("BUSY")) {
            System.out.println(initMsg);
            String message = "";
            Scanner scanner = new Scanner(System.in);
            while(!message.equals("LOSE") && !message.equals("WON")) {
                message = c.game(scanner.next());
                System.out.println(message);
            }
        }
    }
}
