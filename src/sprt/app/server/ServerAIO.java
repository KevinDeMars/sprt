/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server;

import n4m.app.server.N4MServer;
import sprt.app.server.AsyncServerSocketChannelWrapper.Peer;
import sprt.serialization.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static shared.app.AppUtil.logToFile;
import static shared.app.AppUtil.parseIntOrExit;
import static sprt.app.server.Server.getApp;

/**
 * Implementation of SPRT server protocol using Java NIO
 */
public class ServerAIO {
    private static final Logger LOG = logToFile(ServerAIO.class, "connections.log");
    private static final int N4M_THREAD_POOL_SIZE = 5;

    /**
     * Creates and runs a SPRT server and an N4M server.
     * @param args port number
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: ServerAIO <port>");
            return;
        }
        int port = parseIntOrExit("port", args[0], 1, 65535, 1);

        // start SPRT server
        ServerAIO sprtSrv;
        try {
            sprtSrv = new ServerAIO(port);
        } catch (IOException e) {
            System.err.println("Couldn't create SPRT server");
            return;
        }
        sprtSrv.start();

        // start N4M server in separate thread
        N4MServer n4mSrv;
        try {
            n4mSrv = new N4MServer(sprtSrv.stats, port, N4M_THREAD_POOL_SIZE);
        } catch (SocketException e) {
            System.err.println("Couldn't create N4M server");
            return;
        }
        var n4mThread = new Thread(n4mSrv::go);
        n4mThread.start();

        // Wait forever for n4m thread to finish
        try {
            n4mThread.join();
        } catch (InterruptedException e) {
            System.err.println("N4M thread interrupted");
        }
    }

    private final AsyncServerSocketChannelWrapper channel;
    // Tracks usage data for n4m server
    private final AppStats stats = new AppStats();

    /**
     * Creates server and binds to the given port
     * @param port port to bind to
     * @throws IOException If channel creation fails
     */
    public ServerAIO(int port) throws IOException {
        channel = new AsyncServerSocketChannelWrapper(port);
    }

    /**
     * Starts the server and accepts peer connections
     */
    public void start() {
        channel.beginAccept(peer -> {
            LOG.fine(logPrefix(peer) + "Connected");
            readRequest(peer, request -> handleFirstRequest(peer, request));
            return true; // keep accepting connections
        });
    }

    private void handleFirstRequest(Peer peer, Request req) {
        var optApp = getApp(req.getFunction());
        if (optApp.isEmpty()) {
            fail(peer, "Unexpected function");
            return;
        }
        var app = optApp.get();

        stats.appWasRun(app);
        appHandleRequest(peer, app, req);
    }

    private void appHandleRequest(Peer peer, ServerApp app, Request request) {
        try {
            doAppHandleRequest(peer, app, request);
        } catch (ValidationException e) {
            fail(peer, e.getMessage());
        }
        catch (Throwable t) {
            fail(peer, "Unexpected error");
        }
    }

    private void doAppHandleRequest(Peer peer, ServerApp app, Request request) throws ValidationException {
        Response response;
        LOG.finer(logPrefix(peer) + "Received: " + request);
        var expectedFunction = app.getState().name();
        if (!request.getFunction().equals(expectedFunction)) {
            fail(peer, "Unexpected message");
            return;
        }

        response = app.handleRequest(request);
        response.setCookieList(
                request.getCookieList().addAll(response.getCookieList())
        );
        sendResponse(peer, response, () -> {
            LOG.finer(logPrefix(peer) + "Sent: " + response);
            // If app is not done, then begin reading another request. When done reading, call this function again.
            if (!Response.NO_NEXT_FUNCTION.equals(response.getFunction())) {
                readRequest(peer, req -> appHandleRequest(peer, app, req));
            }
            else {
                peer.close();
            }
        });

    }

    private void fail(Peer peer, String msg) {
        LOG.info(logPrefix(peer) + "Closing peer with error: " + msg);

        Response response = null;
        try {
            response = new Response(Status.ERROR, Response.NO_NEXT_FUNCTION, msg);
        } catch (ValidationException e) {
            LOG.info("Can't create response");
            return;
        }

        sendResponse(peer, response, peer::close);
    }

    private void readRequest(Peer peer, Consumer<Request> callback) {
        peer.beginRead(data -> {
            try {
                var request = (Request) Message.decodeType(new MessageInput(new ByteArrayInputStream(data)), MessageType.Request);
                callback.accept(request);
            } catch (ValidationException e) {
                fail(peer, e.getMessage());
            }
            catch (IOException e) {
                fail(peer, "I/O Error");
            }
        });
    }

    private void sendResponse(Peer peer, Response response, VoidFunction callback) {
        var baos = new ByteArrayOutputStream();
        var out = new MessageOutput(baos);
        try {
            response.encode(out);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't write to ByteArrayOutputStream in memory, should never happen");
        }

        peer.beginWrite(baos.toByteArray(), callback);
    }

    private static String logPrefix(Peer peer) {
        String remoteAddress = "???";
        try {
            remoteAddress = peer.getRemoteAddress().toString();
        }
        catch (IOException e) {
            // Leave as "???"
        }
        return remoteAddress + "-" + Thread.currentThread().getId() + " ";
    }
}
