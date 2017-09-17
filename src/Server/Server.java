package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class Server {
    private DatagramSocket serverSocket;
    private boolean busy = false;
    private InetAddress servingClientAddress;
    private int servingClientPort;
    private Game game;
    private long lastTime;

    public Server(int port) {
        try {
            serverSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        lastTime = System.currentTimeMillis();
    }

    public void start() {
        byte[] rec = new byte[1024];

        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(rec, rec.length);
            try {
                serverSocket.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (System.currentTimeMillis() - lastTime > 9000 && getMessageWithoutNull(receivePacket).equals("HELLO")) {
                busy = false;
            }

            if (!getMessageWithoutNull(receivePacket).equals("HELLO") && !busy) {
                sendMessage("BUSY", receivePacket);
                continue;
            }

            if (busy &&
                    receivePacket.getAddress().equals(servingClientAddress) &&
                    receivePacket.getPort() == servingClientPort) {
                lastTime = System.currentTimeMillis();
                // SPELLOGIK
                String[] gameMessage = getMessageWithoutNull(receivePacket).split(" ");
                if (gameMessage[0].equals("GUESS"))
                    if (gameMessage.length > 1 && !gameMessage[1].equals("\n"))
                        game.play(gameMessage[1].toCharArray()[0]);
                    else {
                        sendMessage("Weird guess, try again", receivePacket);
                        sendMessage(String.format("You have %d guesses left", 10 - game.getGuesses()), receivePacket);
                        continue;
                    }

                sendMessage(game.getGuessedString(), receivePacket);
                game.setGuesses(game.getGuesses() + 1);
                // Spelet avslutas
                if (game.getGuesses() == 10 || game.getSecret().equals(game.getGuessedString())) {
                    busy = false;
                    servingClientPort = 0;
                    servingClientAddress = null;
                    if (game.getSecret().equals(game.getGuessedString()))
                        sendMessage("WON", receivePacket);
                    else
                        sendMessage("LOSE", receivePacket);
                    continue;
                }
                sendMessage(String.format("You have %d guesses left", 10 - game.getGuesses()), receivePacket);
                continue;
            }

            if (!busy) {
                System.out.println(getMessageWithoutNull(receivePacket));
                if (getMessageWithoutNull(receivePacket).equals("HELLO"))
                    initialize(receivePacket);
            } else {
                sendMessage("BUSY", receivePacket);
            }
        }
    }
    private void sendMessage(String msg, DatagramPacket datagramPacket) {
        byte[] msgBuf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(msgBuf, msgBuf.length, datagramPacket.getAddress(), datagramPacket.getPort());
        try {
            serverSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //serverSocket.close();
    }

    private byte[] receiveData(DatagramPacket receive) {
        try {
            serverSocket.receive(receive);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return receive.getData();
    }

    public void initialize(DatagramPacket packet) {
        lastTime = System.currentTimeMillis();
        busy = true;
        byte[] buffer = new byte[128];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        sendMessage("OK", packet);
        servingClientAddress = packet.getAddress();
        servingClientPort = packet.getPort();

        while (receivePacket.getAddress() == null || !receivePacket.getAddress().equals(servingClientAddress) && receivePacket.getPort() != servingClientPort ) {
            receiveData(receivePacket);
            if(receivePacket.getAddress().equals(servingClientAddress) &&
                    receivePacket.getPort() == packet.getPort() &&
                    !getMessageWithoutNull(receivePacket).equals("START")
                    ) {
                sendMessage("ERROR Wrong command, try again", receivePacket);
                busy = false;
                return;
            }
            if(System.currentTimeMillis() - lastTime < 9000)
                if (receivePacket.getAddress() != servingClientAddress && receivePacket.getPort() != servingClientPort) {
                    sendMessage("BUSY", receivePacket);
                }
                else if (getMessageWithoutNull(receivePacket).equals("HELLO")) {
                packet.setAddress(receivePacket.getAddress());
                packet.setPort(receivePacket.getPort());
                receivePacket = new DatagramPacket(buffer, buffer.length);
                lastTime = System.currentTimeMillis();
                }
        }
         if (getMessageWithoutNull(receivePacket).equals("START") && receivePacket.getAddress().equals(servingClientAddress) && receivePacket.getPort() == servingClientPort) {
            sendMessage("READY 5", packet);

            game = new Game();
        } else {
            busy = false;
            sendMessage("BUSY", packet);
        }
    }

    private String getMessageWithoutNull(DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength());
    }
}
