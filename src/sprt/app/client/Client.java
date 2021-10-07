/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Project 2 (Test only)
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.client;

import sprt.serialization.*;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import static sprt.app.client.ClientParseArgs.*;
import static sprt.serialization.Util.checkNull;

/** A client that exchanges messages with a SPRT server. */
public class Client {
    // Contains exit codes
    protected enum Error {
        BadPort(0), BadCookieFile(1), BadSocket(2), IOError(3), ValidationError(4);
        public final int code;
        Error(int code) {
            this.code = code;
        }
    }

    /**
     * Creates and runs a client.
     * @param args server identity, server port, cookie file path
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: Client <serverIdentity> <serverPort> <cookieFile>");
            return;
        }

        int port = getPortNumOrExit(args[1]);
        var cookies = loadCookieListOrExit(args[2]);
        var socket = createSocketOrExit(args[0], port, cookies);
        int err = 0;
        Client c = null;

        try {
            c = new Client(socket, cookies);
            c.go();
        }
        catch (IOException e) {
            System.err.println("Error sending or receiving data");
            err = Error.IOError.code;
        }
        catch (ValidationException e) {
            System.err.println("Server returned invalid data");
            err = Error.ValidationError.code;
        }
        finally {
            if (c != null) {
                persistCookies(c.getCookies(), args[2]);
            }
        }

        System.exit(err);
    }

    // Scanner which wraps System.in for input
    private final ClientIO console;
    // connection to server
    private final Socket socket;
    // current cookie list
    private final CookieList cookies;
    // wraps socket's input stream
    private final MessageInput in;
    // wraps socket's output stream
    private final MessageOutput out;

    /**
     * Creates client with the given connection to the server and list of cookies
     * @param socket connection with server
     * @param cookies initial cookie list
     * @throws IOException if in/out streams can't be obtained from socket
     */
    public Client(Socket socket, CookieList cookies) throws IOException {
        this.console = new ClientIO(System.in, System.out, System.err);
        this.socket = checkNull(socket, "socket");
        checkNull(cookies, "cookies");
        this.cookies = new CookieList(cookies);
        in = new MessageInput(socket.getInputStream());
        out = new MessageOutput(socket.getOutputStream());
    }

    /**
     * Gets client's cookie list
     * @return cookie list
     */
    public CookieList getCookies() {
        return cookies;
    }

    /**
     * Communicates with the server according to the SPRT protocol
     *   with each parameter list and the initial function read
     *   from standard input.
     * @throws IOException if error communicating with server
     * @throws ValidationException if server returns invalid data
     */
    public void go() throws IOException, ValidationException {
        String function = console.readToken("Function> ");
        String[] params = new String[0];
        boolean done;

        do {
            var req = new Request(function, params, this.cookies);
            req.encode(out);

            var resp = (Response) Message.decodeType(in, MessageType.Response);

            cookies.addAll(resp.getCookieList());
            PrintStream out = (resp.getStatus().equals(Status.OK)) ? System.out : System.err;
            out.print(resp.getMessage());

            function = resp.getFunction();
            done = "NULL".equals(function);
            if (!done)
                params = console.readMultiTokens("");
        } while (!done);

    }

    private static void persistCookies(CookieList cookies, String path) {
        try {
            var f = Files.newOutputStream(Path.of(path));
            cookies.encode(new MessageOutput(f));
        }
        catch (IOException e) {
            System.err.println("Couldn't persist cookies.");
        }
    }
}
