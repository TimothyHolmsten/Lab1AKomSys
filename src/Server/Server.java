package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by timothy on 2017-09-05, Server.
 */
public class Server {
    private DatagramSocket serverSocket;
    private boolean busy = false;
    private InetAddress servingClientAddress;
    private int servingClientPort;
    private Game game;

    public Server(int port) {
        try {
            serverSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        byte[] rec = new byte[1024];

        while(true) {
            DatagramPacket receivePacket = new DatagramPacket(rec, rec.length);
            try {
                serverSocket.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(busy &&
                receivePacket.getAddress().equals(servingClientAddress) &&
                receivePacket.getPort() == servingClientPort) {

                // SPELLOGIK
                String[] gameMessage = getMessageWithoutNull(receivePacket).split(" ");
                if(gameMessage[0].equals("GUESS"))
                    game.play(gameMessage[1].toCharArray()[0]);
                sendMessage(game.getGuessedString(), receivePacket);
                game.setGuesses(game.getGuesses() + 1);
                if(game.getGuesses() == 10 || game.getSecret().equals(game.getGuessedString())) {
                    busy = false;
                    servingClientPort = 0;
                    servingClientAddress = null;
                    if(game.getSecret().equals(game.getGuessedString()))
                        sendMessage("WON", receivePacket);
                    else
                        sendMessage("LOSE", receivePacket);
                    continue;
                }
                sendMessage(String.format("You have %d guesses left", 10 - game.getGuesses()), receivePacket);
                continue;
            }

            if(!busy)
                System.out.println(new String(receivePacket.getData(), 0, receivePacket.getLength()));
                if(getMessageWithoutNull(receivePacket).equals("HELLO"))
                    initialize(receivePacket);
            else
                sendMessage("BUSY", receivePacket);
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
        byte[] buffer = new byte[128];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        sendMessage("OK",packet);
        receiveData(receivePacket);
        if(getMessageWithoutNull(receivePacket).equals("START")) {
            sendMessage("READY 5", packet);
            servingClientAddress = packet.getAddress();
            servingClientPort = packet.getPort();
            busy = true;
            game = new Game();
        }
    }

    private String getMessageWithoutNull(DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength());
    }
}
