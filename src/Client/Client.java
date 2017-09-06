package Client;

import java.io.IOException;
import java.net.*;

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
            System.err.println("Error: Connection was not possible");
        }
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.err.println("Error: The host is unknown");
        }

    }
    /**
     * Initialiserar anslutningen till servern
     * @return Returnerar om anslutningen till servern lyckas eller ej
     */
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

    /**
     * Klientents kommunikation till servern vid spelet.
     * @param input Input med gissning från användaren.
     * @return Returnerar svaret från servern.
     */
    public String game(String input) {
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[128];
        DatagramPacket messageFromServer = new DatagramPacket(buf, buf.length);
        sendMessage("GUESS " + input);
        receiveData(messageFromServer);
        sb.append(getMessageWithoutNull(messageFromServer)).append("\n");
        receiveData(messageFromServer);
        if(getMessageWithoutNull(messageFromServer).equals("WON") || getMessageWithoutNull(messageFromServer).equals("LOSE")) {
            clientSocket.close();
            return getMessageWithoutNull(messageFromServer);
        }
        sb.append(getMessageWithoutNull(messageFromServer));
        return sb.toString();
    }

    /**
     * Används för att skicka ett specifikt meddelanden.
     * @param msg Meddelandet som ska skickas
     *
     */
    public void sendMessage(String msg) {
        byte[] buf = msg.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, serverPort);
        try {
            clientSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: The message was not sent");
        }
    }

    /**
     * Tar emot ny data från servern.
     * @param receive Tar emot ett datagrampacket
     * @return Returnerar datan som finns i paketet
     */
    private byte[] receiveData(DatagramPacket receive) {
        try {
            clientSocket.receive(receive);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return receive.getData();
    }

    /**
     * Plockar ut datan från datagrampaketet
     * @param packet Ett datagrampaket
     * @return Returnerar en sträng av datan.
     */
    private String getMessageWithoutNull(DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength());
    }
}
