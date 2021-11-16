/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.app.client;

import n4m.serialization.*;
import shared.app.AppUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Client implementing the N4M protocol.
 */
public class N4MClient {

    /**
     * Creates and runs a client.
     * @param args serverNameOrAddress, serverPort, businessName
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: N4MClient <serverNameOrAddress> <serverPort> <businessName>");
            return;
        }
        int serverPort = AppUtil.parseIntOrExit("serverPort", args[1], 1, 65535, 1);

        try {
            var client = new N4MClient(args[0], serverPort);
            var response = client.query(args[2]);
            if (response.getErrorCode() != ErrorCode.NOERROR)
                System.out.println("Non-zero error code!");

            System.out.println(response);
        } catch (UnknownHostException e) {
            System.err.println("Couldn't resolve server name.");
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        } catch (ECException e) {
            System.err.println("Invalid response: " + e.getMessage());
        }
    }

    /**
     * Creates a new client with the given server info
     * @param srvName domain name or IP address of server
     * @param srvPort port of server
     * @throws UnknownHostException if invalid or unreachable srvName
     * @throws SocketException if failed to create socket
     */
    public N4MClient(String srvName, int srvPort) throws UnknownHostException, SocketException {
        this.srvPort = srvPort;
        this.srvName = srvName;
        this.srvAddr = InetAddress.getByName(srvName);
        this.socket = new UDPSocketPlus();
    }

    private final int srvPort;
    private final String srvName;
    private final InetAddress srvAddr;
    private final UDPSocketPlus socket;

    /**
     * Queries the client's associated N4M server with the given business name.
     * @param businessName name of requesting business
     * @return App usage info
     * @throws IOException if I/O error
     * @throws ECException if invalid response
     */
    public N4MResponse query(String businessName) throws IOException, ECException {
        // Prepare and send message
        int msgId = Util.randomInt(0, N4MMessage.MAX_MSG_ID);
        var query = new N4MQuery(msgId, businessName);
        socket.send(query.encode(), srvAddr, srvPort);

        // Receive response
        DatagramPacket response = socket.receive(srvAddr);

        // Decode and verify response
        // Truncate buffer to actual length of received data
        var data = Arrays.copyOf(response.getData(), response.getLength());
        var msg = N4MMessage.decode(data);

        if (!(msg instanceof N4MResponse resp)) {
            throw new ECException("Not an N4MResponse", ErrorCode.BADMSG);
        }

        if (resp.getMsgId() != query.getMsgId()) {
            System.out.println("Non-matching ID");
        }

        return resp;
    }
}
