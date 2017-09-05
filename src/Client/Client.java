package Client;

import java.io.IOException;
import java.net.*;

/**
 * Created by timothy on 2017-09-05, Client.
 */
public class Client {
    private DatagramSocket clientSocket;
    private InetAddress address;
    private int serverPort;

    public Client(int serverPort) {
        this.serverPort = serverPort;
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public void sendMessage(String msg) {
        byte[] buf = msg.getBytes();
        System.out.println(clientSocket.getPort());

        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, serverPort);
        try {
            clientSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String initialize() {
        byte[] buf = new byte[128];
        DatagramPacket rec = new DatagramPacket(buf, buf.length);
        sendMessage("HELLO");
        receiveData(rec); // Will wait here
        if(getMessageWithoutNull(rec).equals("OK")) {
            sendMessage("START");
            receiveData(rec); // Will wait here
        }
        else {
            clientSocket.close();
            return getMessageWithoutNull(rec);
        }

        return getMessageWithoutNull(rec);
    }

    public String game(String input) {
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[128];
        DatagramPacket messageFromServer = new DatagramPacket(buf, buf.length);
        sendMessage("GUESS " + input);
        receiveData(messageFromServer);
        sb.append(getMessageWithoutNull(messageFromServer) + "\n");
        receiveData(messageFromServer);
        sb.append(getMessageWithoutNull(messageFromServer));
        return sb.toString();
    }

    private byte[] receiveData(DatagramPacket receive) {
        try {
            clientSocket.receive(receive);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return receive.getData();
    }

    private String getMessageWithoutNull(DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength());
    }
}
