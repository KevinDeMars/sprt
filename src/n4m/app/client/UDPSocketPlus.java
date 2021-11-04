/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.app.client;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;

public class UDPSocketPlus {
    //private final int port;
    //private final String peer;
    //private final InetAddress peerAddress;
    private final DatagramSocket socket;
    private int maxTries = 5;

    public UDPSocketPlus() throws UnknownHostException, SocketException {
        //this.port = port;
        //this.peer = peer;
        //peerAddress = InetAddress.getByName(peer);
        socket = new DatagramSocket();
    }

    public void send(DatagramPacket pkt) throws IOException {
        socket.send(pkt);
    }

    public void send(byte[] data, InetAddress peerAddr, int peerPort) throws IOException {
        var pkt = new DatagramPacket(data, data.length, peerAddr, peerPort);
        this.send(pkt);
    }

    public DatagramPacket receive(int maxLength) throws IOException {
        DatagramPacket pkt = new DatagramPacket(new byte[maxLength + 1], maxLength + 1);

        int tries = 0;
        while (tries < maxTries) {
            try {
                socket.receive(pkt);

                //if (!pkt.getAddress().equals(peerAddress)) {
                //    throw new IOException("Got ")
                //}

                if (pkt.getLength() > maxLength) {
                    throw new PacketTruncatedException("Got packet above max length of " + maxLength);
                }

                return pkt;
            }
            catch (InterruptedIOException e) {
                ++tries;
                System.out.println("Timed out. " + (maxTries - tries) + " more tries.");
            }
        }
        throw new SocketTimeoutException("Couldn't receive message.");
    }
    public DatagramPacket receive(int maxLength, InetAddress peerAddress) throws IOException {
        var pkt = receive(maxLength);
        if (!pkt.getAddress().equals(peerAddress)) {
            throw new IOException("Got packet from unknown source");
        }
        return pkt;
    }
}
