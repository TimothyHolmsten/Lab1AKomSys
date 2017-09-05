package Client;

/**
 * Created by timothy on 2017-09-05, Client.
 */
public class Main {
    public static void main(String[] args) {
        Client c = new Client(4000, 5000);
        String initMsg = c.initialize();
        if(!initMsg.equals("BUSY")) {
            System.out.println(initMsg);
            String message = "lol";/*
            while(!message.equals("LOSE") || !message.equals("WIN")) {
                // scanner input
                // message = game(input)
                // sout message
            }*/
            System.out.println(message);
        }

    }
}
