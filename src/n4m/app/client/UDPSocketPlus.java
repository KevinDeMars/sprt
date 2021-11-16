/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.app.client;

import java.io.IOException;
import java.net.*;

/**
 * Wraps a UDP socket (DatagramSocket) and adds extra functionality.
 */
public class UDPSocketPlus {
    // Max size for a UDP segment.
    private static final int MAX_DATAGRAM_SIZE = 64 * 1024;
    // wrapped plain socket
    private final DatagramSocket socket;

    /**
     * Creates new UDP socket bound to an ephemeral port.
     * @throws SocketException if socket couldn't be opened
     */
    public UDPSocketPlus() throws SocketException {
        this(0);
    }

    /**
     * Creates new UDP socket bound to given port.
     * @param port port to bind to
     * @throws SocketException if socket can't be opened or bound to given port
     */
    public UDPSocketPlus(int port) throws SocketException {
        socket = new DatagramSocket(null);
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(port));
    }

    /**
     * See {@link DatagramSocket#send(DatagramPacket)}.
     * @param pkt Packet to send
     * @throws IOException if I/O error occurs
     */
    public void send(DatagramPacket pkt) throws IOException {
        socket.send(pkt);
    }

    /**
     * Convenience method that creates and sends a DatagramPacket for the data.
     * @param data data to send
     * @param peerAddr IP address of peer
     * @param peerPort port number of peer
     * @throws IOException if I/O error occurs
     */
    public void send(byte[] data, InetAddress peerAddr, int peerPort) throws IOException {
        var pkt = new DatagramPacket(data, data.length, peerAddr, peerPort);
        this.send(pkt);
    }

    /**
     * Blocks and receives a packet containing at most maxLength bytes.
     * @param maxLength maximum size of data to receive
     * @return the received packet
     * @throws IOException if I/O error occurs
     * @throws PacketTruncatedException if the received packet is larger than maxLength
     */
    public DatagramPacket receive(int maxLength) throws IOException, PacketTruncatedException {
        // Extra byte is to detect truncation
        DatagramPacket pkt = new DatagramPacket(new byte[maxLength + 1], maxLength + 1);
        socket.receive(pkt);
        if (pkt.getLength() > maxLength) {
            throw new PacketTruncatedException("Got packet exceeding max length of " + maxLength);
        }
        return pkt;
    }

    /**
     * Blocks and receives a packet with up to the maximum UDP segment size.
     * @return received packet
     * @throws IOException if I/O error occurs
     */
    public DatagramPacket receive() throws IOException {
        return receive(MAX_DATAGRAM_SIZE);
    }

    /**
     * Blocks and receives a packet from a specific peer containing at most maxLength bytes.
     * @param maxLength maximum size of data to receive
     * @param peerAddress expected address to receive from.
     * @return the received packet
     * @throws IOException if I/O error occurs, or a packet from a different peer is received
     * @throws PacketTruncatedException if the received packet is larger than maxLength
     */
    public DatagramPacket receive(int maxLength, InetAddress peerAddress) throws IOException {
        var pkt = receive(maxLength);
        if (!pkt.getAddress().equals(peerAddress)) {
            throw new IOException("Got packet from unknown source");
        }
        return pkt;
    }
    /**
     * Blocks and receives a packet from a specific peer up to the maximum UDP segment size.
     * @param peerAddress expected address to receive from.
     * @return received packet
     * @throws IOException if I/O error occurs, or a packet from a different peer is received
     */
    public DatagramPacket receive(InetAddress peerAddress) throws IOException {
        return receive(MAX_DATAGRAM_SIZE, peerAddress);
    }
}
