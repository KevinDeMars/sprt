/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.app.client;

import n4m.serialization.ECException;
import n4m.serialization.N4MMessage;
import n4m.serialization.N4MQuery;
import n4m.serialization.N4MResponse;
import shared.app.AppUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class N4MClient {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: N4MClient <serverNameOrAddress> <serverPort> <businessName>");
            return;
        }
        int serverPort = AppUtil.parseIntOrExit("serverPort", args[1], 1, 65535, 1);

        try {
            new N4MClient(args[0], serverPort, args[2]).go();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public N4MClient(String srvName, int srvPort, String businessName) {
        this.srvPort = srvPort;
        this.srvName = srvName;
        this.businessName = businessName;
    }

    private final int srvPort;
    private final String srvName;
    private final String businessName;

    public void go() throws IOException, UnknownHostException, SocketException, ECException {
        var srvAddr = InetAddress.getByName(srvName);
        var socket = new UDPSocketPlus();

        int msgId = Util.randomInt(0, N4MMessage.MAX_MSG_ID);
        var query = new N4MQuery(msgId, businessName);

        socket.send(query.encode(), srvAddr, srvPort);
        var resp = socket.receive(1000, srvAddr);

        try {
            // Truncate buffer to actual length of received data
            var data = Arrays.copyOf(resp.getData(), resp.getLength());
            var msg = N4MMessage.decode(data);

            if (msg instanceof N4MResponse r) {
                System.out.println(r);
            }
            else {
                System.out.println("oh no");
            }
        }
        catch (ECException e) {
            System.out.println("oh no2");
        }

    }
}
