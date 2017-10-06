package Server;


import java.io.IOException;
import java.net.*;

public class Server {
    private DatagramSocket serverSocket;
    private InetAddress servingClientAddress;
    private int servingClientPort;
    private Game game = new Game();
    private long lastTime;
    private byte[] rec = new byte[1024];
    private DatagramPacket receivePacket = new DatagramPacket(rec, rec.length);
    //state=0:waiting for hello
    //state=1:waiting for start
    //state=2:waiting for guess
    private int state = 0;


    public Server(int port) {
        try {
            serverSocket = new DatagramSocket(port);
            System.out.println("Server inizialised");
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        lastTime = System.currentTimeMillis();
        System.out.println("Server started at:" + lastTime);
        while (true) {
            //Om vi inte timeoutat: kör logik
            //Annars reseta state och meddela ev spelare
            //System.out.println("STATE:"+state);
            System.out.println("waitig for packet");
            try {
                serverSocket.receive(receivePacket);
                if(System.currentTimeMillis() - lastTime > 10000){
                    if(servingClientPort != 0) {
                        byte[] msgBuf = "BUSY".getBytes();
                        System.out.println("Timeout");
                        DatagramPacket packet = new DatagramPacket(msgBuf, msgBuf.length, servingClientAddress, servingClientPort);
                        try {
                            serverSocket.send(packet);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    resetState();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
            lastTime = System.currentTimeMillis();
            System.out.println("packet reccieved");
            switch (state) {
                //state=1:waiting for start
                case 1:
                    if (getMessageWithoutNull(receivePacket).equals("START")
                            && receivePacket.getAddress() == servingClientAddress
                            && receivePacket.getPort() == servingClientPort) {
                        int len = game.getSecret().length();
                        sendMessage("READY " + len + "", receivePacket);
                        state = 2;
                    } else {
                        sendMessage("BUSY", receivePacket);
                        if(receivePacket.getAddress() == servingClientAddress
                                && receivePacket.getPort() == servingClientPort){
                            resetState();
                        }
                    }
                    break;

                //state=2:waiting for guess
                case 2:
                    //om paketet är från den aktiva användaren: kör spellogik
                    //om paketet är från någon annan: svara busy
                    if (receivePacket.getAddress() == servingClientAddress
                            && receivePacket.getPort() == servingClientPort) {
                        String[] gameMessage = getMessageWithoutNull(receivePacket).split(" ");

                        //Om paketet innehåller en gissning: kör spellogik
                        //Annars: svara "wierd guess"
                        if (gameMessage[0].equals("GUESS")) {

                            //Om gissningen är korrekt formaterad: kör spellogik
                            //Annars: svara "wierd guess"
                            if (gameMessage.length > 1 && !gameMessage[1].equals("\n")) {
                                game.play(gameMessage[1].toCharArray()[0]);
                                game.setGuesses(game.getGuesses() + 1);

                                //Om vi använt alla gissningar eller vi gissat korrekt: avsluta spelet
                                //Annars: skicka tillbaka info till användaren
                                if (game.getGuesses() == 10 || game.getSecret().equals(game.getGuessedString())) {
                                    if (game.getSecret().equals(game.getGuessedString())) {
                                        System.out.println("PlayerWin");
                                        sendMessage(game.getGuessedString(), receivePacket);
                                        sendMessage("WON", receivePacket);
                                    } else {
                                        System.out.println("PlayerWin");
                                        sendMessage(game.getGuessedString(), receivePacket);
                                        sendMessage("LOSE", receivePacket);
                                    }
                                    resetState();
                                } else {
                                    sendMessage(game.getGuessedString(), receivePacket);
                                    sendMessage(String.format("You have %d guesses left", 10 - game.getGuesses()), receivePacket);
                                }

                            } else {
                                sendMessage("Weird guess, try again", receivePacket);
                                sendMessage(String.format("You have %d guesses left", 10 - game.getGuesses()), receivePacket);
                            }
                        } else {
                            sendMessage("Weird guess, try again", receivePacket);
                            sendMessage(String.format("You have %d guesses left", 10 - game.getGuesses()), receivePacket);
                        }

                    } else {
                        sendMessage("BUSY", receivePacket);
                    }
                    break;

                //state=0:waiting for hello
                default:
                    if (getMessageWithoutNull(receivePacket).equals("HELLO")) {
                        initialize(receivePacket);
                        state = 1;
                    } else {
                        sendMessage("BUSY", receivePacket);
                    }
                    break;
            }

        }
    }

    public void initialize(DatagramPacket packet) {
        servingClientAddress = packet.getAddress();
        servingClientPort = packet.getPort();
        sendMessage("OK", packet);
    }

    public void resetState() {
        servingClientPort = 0;
        servingClientAddress = null;
        state = 0;
        game = new Game();
    }

    private void sendMessage(String msg, DatagramPacket datagramPacket) {
        byte[] msgBuf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(msgBuf, msgBuf.length, datagramPacket.getAddress(), datagramPacket.getPort());
        try {
            serverSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getMessageWithoutNull(DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength());
    }
}
