/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.app.server;

import n4m.app.client.UDPSocketPlus;
import n4m.serialization.*;
import sprt.app.server.Server;
import sprt.app.server.ServerApp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static shared.app.AppUtil.setupLogger;

/**
 * Server implementing N4M protocol (depends on sprt server).
 */
public class N4MServer {
    private static final Logger LOG = Logger.getLogger(N4MServer.class.getName());
    static {
        setupLogger(LOG, "n4m.log");
    }

    private final UDPSocketPlus socket;
    private final ExecutorService threadPool;
    private final Server sprtServer;

    /**
     * Create N4M server that responds to queries about the given sprt server.
     * @param sprtServer Server to get usage data from
     * @param port Port to listen for incoming requests from
     * @param numThreads maximum threads for handling N4M queries
     * @throws SocketException if can't bind to given port.
     */
    public N4MServer(Server sprtServer, int port, int numThreads) throws SocketException {
        socket = new UDPSocketPlus(port);
        threadPool = Executors.newFixedThreadPool(numThreads);
        this.sprtServer = sprtServer;
    }

    /**
     * Listens for requests forever.
     */
    public void go() {
        while (true) {
            try {
                var pkt = socket.receive();
                threadPool.execute(() -> handlePkt(pkt));
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Error receiving UDP segment", e);
            }
        }
    }

    private void handlePkt(DatagramPacket pkt) {
        try {
            doHandlePkt(pkt);
        } catch (Throwable e) {
            LOG.log(Level.WARNING, "Failed to handle packet from " + pkt.getAddress(), e);
            maybeSendError(pkt, ErrorCode.SYSTEMERROR);
        }

    }

    private void doHandlePkt(DatagramPacket pkt) throws ECException, IOException {
        byte[] data = Arrays.copyOf(pkt.getData(), pkt.getLength()); // extract actual data from buffer
        // Decode message. If we get ECException, send a reply with that error code.
        N4MMessage msg;
        try {
            msg = N4MMessage.decode(data);
        }
        catch (ECException e) {
            reply(pkt, new N4MResponse(e.getErrorCodeType(), 0, 0, List.of()));
            return;
        }

        // If got a non-query N4MMessage, send BADMSG error
        if (!(msg instanceof N4MQuery q)) {
            reply(pkt, new N4MResponse(ErrorCode.BADMSG, msg.getMsgId(), 0, List.of()));
            return;
        }

        LOG.fine(String.format("Business \"%s\" queried using address %s", q.getBusinessName(), pkt.getAddress()));

        // Send the data
        var accessCts = sprtServer.getAccessCounts();
        var entries = accessCts.entrySet().stream()
                .map(this::mapEntryToApplicationEntry)
                .collect(Collectors.toList());
        reply(pkt, new N4MResponse(ErrorCode.NOERROR, q.getMsgId(), sprtServer.getLastAppTimestamp(), entries));
    }

    private void reply(DatagramPacket pkt, N4MResponse reply) throws IOException {
        socket.send(reply.encode(), pkt.getAddress(), pkt.getPort());
    }

    private ApplicationEntry mapEntryToApplicationEntry(Map.Entry<ServerApp, Integer> entry) {
        // App name is the name of the class of the app (e.g. sprt.app.server.apps.Poll.Poll becomes "Poll")
        var name = entry.getKey().getClass().getSimpleName();
        int accessCt = entry.getValue();
        try {
            return new ApplicationEntry(name, accessCt);
        } catch (ECException e) {
            throw new RuntimeException("SPRT server returned bad data, should never happen");
        }
    }

    // Tries to send an error. On failure, logs error and ignores it.
    private void maybeSendError(DatagramPacket pkt, ErrorCode err) {
        try {
            reply(pkt, new N4MResponse(err, 0, 0, List.of()));
        } catch (IOException | ECException e) {
            LOG.log(Level.WARNING, "maybeSendError failed to send", e);
        }
    }
}
