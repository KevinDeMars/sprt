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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger LOG = Logger.getLogger(Server.class.getName());
    // Number of times socket.accept() is allowed to fail in a row before server gives up
    private static final int NUM_ACCEPT_TRIES = 10;

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

        try {
            var hnd = new FileHandler("connections.log");
            //hnd.setFormatter(new SimpleFormatter());
            LOG.addHandler(hnd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            new Server(port, numThreads).go();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "I/O error creating server instance", e);
        }
    }


    private ExecutorService threadPool;
    private ServerSocket socket;

    public Server(int port, int numThreads) throws IOException {
        socket = new ServerSocket(port);
        threadPool = Executors.newFixedThreadPool(numThreads);
    }

    public void go() {
        LOG.info("Listening on port " + this.socket.getLocalPort());

        int numAcceptFails = 0;
        while (true) {
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
        logClient(cliSock, "Client connected");
        try {
            doHandleClient(cliSock);
        }
        catch (IOException e) {
            logClient(cliSock, Level.INFO, "I/O error", e);
        }
        catch (Throwable t) {
            logClient(cliSock, Level.SEVERE, "Uncaught exception handling client", t);
        }
    }

    private void doHandleClient(Socket cliSock) throws IOException, ValidationException {
        var in = new MessageInput(cliSock.getInputStream());
        var out = new MessageOutput(cliSock.getOutputStream());

        var req = (Request) Message.decodeType(in, MessageType.Request);
        // For initial function, instantiate the class corresponding to that function's name

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
            response = app.handleRequest(req);
            response.setCookieList(
                    req.getCookieList().addAll(response.getCookieList())
            );
            response.encode(out);
        }
        while (!"NULL".equals(response.getFunction()));

    }

    private Optional<ServerApp> getApp(String name) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName("sprt.app.server.apps." + name + "." + name);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            LOG.info("Invalid app name: " + name);
            return Optional.empty();
        }

        Object instance = null;
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


    private void logClient(Socket cliSock, Level level, String msg, Throwable throwable) {
        LOG.log(level, cliSock.getRemoteSocketAddress() + "-" + Thread.currentThread().getId() + " " + msg, throwable);
    }
    // Why doesn't Java have default parameters
    private void logClient(Socket cliSock, Level level, String msg) {
        logClient(cliSock, level, msg, null);
    }
    private void logClient(Socket cliSock, String msg) {
        logClient(cliSock, Level.INFO, msg);
    }
}
