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

public class Server {
    private static final Logger LOG = Logger.getLogger(Server.class.getName());
    // Number of times socket.accept() is allowed to fail in a row before server gives up
    private static final int NUM_ACCEPT_TRIES = 10;
    private static final int TIMEOUT_MS = 5 * 1000;

    protected enum Error {
        TooManyAcceptFails(0);
        public final int code;
        Error(int code) {
            this.code = code;
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: Server <port> <numThreads>");
            return;
        }

        int port = 12345;
        int numThreads = 10;

        setupLogger();

        try {
            new Server(port, numThreads).go();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "I/O error creating server instance", e);
        }
    }

    private static void setupLogger() {
        try {
            LOG.setLevel(Level.ALL);

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
            e.printStackTrace();
        }
    }


    private ExecutorService threadPool;
    private ServerSocket socket;

    public Server(int port, int numThreads) throws IOException {
        socket = new ServerSocket();
        //LOG.info(socket.getLocalSocketAddress() + " " + socket.getLocalPort());
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), port));
        threadPool = Executors.newFixedThreadPool(numThreads);
    }

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
            // TODO
            cliSock.close();
            return;
        }
        try {
            runApp(cliSock, in, out, app.get(), req);
        }
        catch (InvocationTargetException | IllegalAccessException e) {
            LOG.log(Level.INFO,"Server error running app " + app.get().getClass(), e);
        }



        cliSock.close();
    }

    private void runApp(Socket cliSock, MessageInput in, MessageOutput out, ServerApp app, Request initialRequest)
            throws InvocationTargetException, IllegalAccessException, ValidationException, IOException
    {
        app.handleRequest(initialRequest).encode(out);

        Response response;
        do {
            var req = (Request) Message.decodeType(in, MessageType.Request);
            LOG.finer(logPrefix(cliSock) + "Received: " + req);

            response = app.handleRequest(req);
            response.setCookieList(
                    req.getCookieList().addAll(response.getCookieList())
            );
            response.encode(out);
            LOG.finer(logPrefix(cliSock) + "Sent: " + response);
        }
        while (!"NULL".equals(response.getFunction()));

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
