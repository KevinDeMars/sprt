/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server;

import sprt.serialization.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

/**
 * Server implementing SPRT 1.0 Protocol.
 */
public class Server {
    private static final Logger LOG = Logger.getLogger(Server.class.getName());
    // Number of times socket.accept() is allowed to fail in a row before server gives up
    private static final int NUM_ACCEPT_TRIES = 10;
    private static final int TIMEOUT_MS = 20 * 1000;

    protected enum Error {
        TooManyAcceptFails(0), BadArg(1);

        public final int code;
        Error(int code) {
            this.code = code;
        }
    }

    /**
     * Runs a server with the given port and # threads.
     * @param args port (1-65535), numThreads (1+)
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: Server <port> <numThreads>");
            return;
        }

        int port = parseIntOrExit("port", args[0], 1, 65535);
        int numThreads = parseIntOrExit("numThreads", args[1], 1, Integer.MAX_VALUE);
        setupLogger();

        try {
            new Server(port, numThreads).go();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "I/O error creating server instance", e);
        }
    }

    private static int parseIntOrExit(String varname, String str, int min, int max) {
        int x = 0;
        try {
            x = Integer.parseInt(str);
        }
        catch (NumberFormatException e) {
            System.err.println(varname + " must be an integer");
            System.exit(Error.BadArg.code);
        }

        if (x < min || x > max) {
            System.err.println(varname + " must be >" + min + " and <" + max);
            System.exit(Error.BadArg.code);
        }

        return x;
    }

    private static void setupLogger() {
        try {
            LOG.setLevel(Level.ALL);

            // Remove default console handler
            var defaultHandlers = Logger.getLogger("").getHandlers();
            for (var hnd : defaultHandlers) {
                Logger.getLogger("").removeHandler(hnd);
            }

            var hnd = new FileHandler("connections.log");
            hnd.setLevel(Level.ALL);
            //System.setProperty("java.util.logging.SimpleFormatter.format", "")
            hnd.setFormatter(new SimpleFormatter());
            LOG.addHandler(hnd);

            var hnd2 = new ConsoleHandler();
            hnd2.setLevel(Level.ALL);
            LOG.addHandler(hnd2);
        } catch (IOException e) {
            System.err.println("Couldn't set up log: " + e);
            e.printStackTrace();
            // Continue without logging
        }
    }

    private ExecutorService threadPool;
    private ServerSocket socket;

    /**
     * Creates server with the given port and # threads.
     * @param port port to run on, between 1 and 65535
     * @param numThreads Size of thread pool.
     * @throws IOException if I/O error occurs.
     */
    public Server(int port, int numThreads) throws IOException {
        socket = new ServerSocket();
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), port));
        threadPool = Executors.newFixedThreadPool(numThreads);
    }

    /**
     * Runs the server forever.
     */
    public void go() {
        LOG.info("Listening on port " + this.socket.getLocalPort());

        int numAcceptFails = 0;
        while (true) {
            // Repeatedly accept connections and handle in thread pool.
            // If too many socket.accept() fails in a row, exit.
            try {
                Socket clientSock = socket.accept();
                numAcceptFails = 0;
                threadPool.execute(() -> handleClient(clientSock));
            }
            catch (IOException e) {
                ++numAcceptFails;
                if (numAcceptFails < NUM_ACCEPT_TRIES)
                    LOG.log(Level.WARNING, "Error accepting socket", e);
                else {
                    LOG.log(Level.SEVERE, "Error accepting socket; exiting", e);
                    System.exit(Error.TooManyAcceptFails.code);
                }
            }
        }
    }

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

        var req = (Request) Message.decodeType(in, MessageType.Request);

        var app = getApp(req.getFunction());
        if (app.isEmpty()) {
            new Response(Status.ERROR, Response.NO_NEXT_FUNCTION, "Unexpected function").encode(out);
            return;
        }
        try {
            runApp(cliSock, in, out, app.get(), req);
        }
        catch (ValidationException e) {
            LOG.log(Level.INFO, logPrefix(cliSock) + "Bad data: " + e.getMessage(), e);
            new Response(Status.ERROR, Response.NO_NEXT_FUNCTION, "Bad data.")
                    .encode(out);
        }
        catch (InvocationTargetException | IllegalAccessException e) {
            LOG.log(Level.INFO,logPrefix(cliSock) + "Server error running app " + app.get().getClass(), e);
        }
    }

    private void runApp(Socket cliSock, MessageInput in, MessageOutput out, ServerApp app, Request request)
            throws InvocationTargetException, IllegalAccessException, ValidationException, IOException
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

    private Optional<ServerApp> getApp(String name) {
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


    private static String logPrefix(Socket cliSock) {
        return cliSock.getRemoteSocketAddress() + "-" + Thread.currentThread().getId() + " ";
    }
}
