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

    public Client(int port, int serverPort) {
        this.serverPort = serverPort;
        try {
            clientSocket = new DatagramSocket(port);
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
        if(getMessageWithoutNull(rec.getData()).equals("OK")) {
            sendMessage("START");
            receiveData(rec); // Will wait here
        }
        else {
            clientSocket.close();
            return getMessageWithoutNull(rec.getData());
        }

        return getMessageWithoutNull(rec.getData());
    }

    public String game(String input) {
        return "Hej";
    }

    private byte[] receiveData(DatagramPacket receive) {
        try {
            clientSocket.receive(receive);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return receive.getData();
    }

    private String getMessageWithoutNull(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b: data) {
            if(b == '\0')
                return sb.toString();
            sb.append((char) b);
        }
        return sb.toString();
    }
}
