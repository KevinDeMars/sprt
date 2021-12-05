/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server;

import n4m.app.server.N4MServer;
import shared.app.AppUtil;
import sprt.serialization.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static shared.app.AppUtil.logToFile;

/**
 * Server implementing SPRT Protocol.
 */
public class Server {
    private static final Logger LOG = logToFile(Server.class, "connections.log");
    // Number of times socket.accept() is allowed to fail in a row before server gives up
    private static final int NUM_ACCEPT_TRIES = 10;
    // Time client has to send data before it gets kicked off
    private static final int TIMEOUT_MS = 20 * 1000;

    protected enum Error {
        TooManyAcceptFails(0), BadArg(1);

        public final int code;
        Error(int code) {
            this.code = code;
        }
    }

    /**
     * Runs both a SPRT server and an N4M server with the given port and # threads.
     * @param args port (1-65535), numThreads (1+)
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: Server <port> <numThreads>");
            return;
        }

        int port = AppUtil.parseIntOrExit("port", args[0], 1, 65535, Error.BadArg.code);
        int numThreads = AppUtil.parseIntOrExit("numThreads", args[1], 1, Integer.MAX_VALUE, Error.BadArg.code);

        Server sprtServer;
        N4MServer n4mServer = null;
        try {
            sprtServer = new Server(port, numThreads);
            n4mServer = new N4MServer(sprtServer.appStats, port, numThreads);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "I/O error creating server instance", e);
            return;
        }

        // Run n4m server in separate thread
        new Thread(n4mServer::go).start();
        // Run sprt server in this thread
        sprtServer.go();
    }

    private final ExecutorService threadPool;
    private final ServerSocket socket;
    private final AppStats appStats = new AppStats();

    /**
     * Creates server with the given port and # threads.
     * @param port port to run on, between 1 and 65535
     * @param numThreads Size of thread pool.
     * @throws IOException if I/O error occurs.
     */
    public Server(int port, int numThreads) throws IOException {
        socket = new ServerSocket();
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(port));
        threadPool = Executors.newFixedThreadPool(numThreads);
    }

    /**
     * Runs the server forever.
     */
    public void go() {
        LOG.info("Listening on port " + this.socket.getLocalPort());

        while (true) {
            try {
                Socket clientSock = socket.accept();
                threadPool.execute(() -> handleClient(clientSock));
            }
            catch (IOException e) {
                LOG.log(Level.WARNING, "Error accepting socket", e);
            }
        }
    }

    // Runs doHandleClient and deals with exceptions + closing socket
    private void handleClient(Socket cliSock) {
        LOG.fine(logPrefix(cliSock) + "Client connected");

        try {
            cliSock.setSoTimeout(TIMEOUT_MS);
            doHandleClient(cliSock);
        }
        catch (SocketTimeoutException e) {
            LOG.fine(logPrefix(cliSock) + "Client timed out");
        }
        catch (IOException e) {
            LOG.log(Level.INFO, logPrefix(cliSock) + "I/O Error");
        }
        catch (Throwable t) {
            LOG.log(Level.SEVERE, logPrefix(cliSock) + "Uncaught exception handling client", t);
        }
        finally {
            try {
                cliSock.close();
                LOG.fine(logPrefix(cliSock) + "Client connection closed");
            }
            catch (IOException e) {
                LOG.log(Level.INFO, logPrefix(cliSock) + "Couldn't close client socket", e);
            }

        }
    }

    private void doHandleClient(Socket cliSock) throws IOException, ValidationException {
        var in = new MessageInput(cliSock.getInputStream());
        var out = new MessageOutput(cliSock.getOutputStream());

        Request req;
        try {
            req = (Request) Message.decodeType(in, MessageType.Request);
        }
        catch (ValidationException e) {
            new Response(Status.ERROR, Response.NO_NEXT_FUNCTION, "Bad initial request")
                    .encode(out);
            return;
        }

        // If app w/ given name exists, run it
        var app = getApp(req.getFunction());
        if (app.isPresent()) {
            // Run app. Send error message if ValidationException or server causes these two exceptions
            try {
                appStats.appWasRun(app.get());
                runApp(cliSock, in, out, app.get(), req);
            } catch (ValidationException e) {
                LOG.log(Level.INFO, logPrefix(cliSock) + "Bad data: " + e.getMessage(), e);
                new Response(Status.ERROR, Response.NO_NEXT_FUNCTION, "Bad data.")
                        .encode(out);
            }
        } else {
            new Response(Status.ERROR, Response.NO_NEXT_FUNCTION, "Unexpected function").encode(out);
        }
    }

    private void runApp(Socket cliSock, MessageInput in, MessageOutput out, ServerApp app, Request request)
            throws ValidationException, IOException
    {
        Response response;
        String expectedFunction = request.getFunction();
        boolean done;
        do {
            LOG.finer(logPrefix(cliSock) + "Received: " + request);
            if (!request.getFunction().equals(expectedFunction)) {
                response = new Response(Status.ERROR, expectedFunction, "Incorrect function " + request.getFunction() + ". Should be " + expectedFunction);
            }
            else {
                response = app.handleRequest(request);
                expectedFunction = response.getFunction();
            }

            response.setCookieList(
                    request.getCookieList().addAll(response.getCookieList())
            );
            response.encode(out);
            LOG.finer(logPrefix(cliSock) + "Sent: " + response);

            done = Response.NO_NEXT_FUNCTION.equals(response.getFunction());
            if (!done)
                request = (Request) Message.decodeType(in, MessageType.Request);
        }
        while (!done);

    }

    /**
     * Gets the server app with the given name, if it exists.
     *
     * The app should be in the package sprt.app.server.apps.(name), and
     *    the class name should be (name).
     * @param name name of app to get
     * @return Specified app, or Optional.empty if not found.
     */
    public static Optional<ServerApp> getApp(String name) {
        Class<?> clazz;
        try {
            clazz = Class.forName("sprt.app.server.apps." + name + "." + name);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            LOG.info("Invalid app name: " + name);
            return Optional.empty();
        }

        Object instance;
        try {
            instance = clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOG.log(Level.WARNING, "Couldn't create app of type " + clazz, e);
            return Optional.empty();
        }

        if (!(instance instanceof ServerApp)) {
            LOG.warning("App does not implement ServerApp: " + clazz);
            return Optional.empty();
        }
        return Optional.of((ServerApp) instance);
    }

    /**
     * Gets app usage statistics
     * @return app stats
     */
    public AppStats getAppStats() {
        return appStats;
    }

    private static String logPrefix(Socket cliSock) {
        return cliSock.getRemoteSocketAddress() + "-" + Thread.currentThread().getId() + " ";
    }
}
