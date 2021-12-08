/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.client;

import sprt.serialization.CookieList;
import sprt.serialization.MessageInput;
import sprt.serialization.ValidationException;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

// Internal utility methods for parsing arguments for Client
class ClientParseArgs {
    // Tries to load cookies from filePath. If file not found, creates new list.
    static CookieList loadCookieListOrExit(String filePath) {
        Path path = null;
        try {
            path = Path.of(filePath);
        }
        catch (InvalidPathException e) {
            System.err.println("Invalid path: " + filePath);
            System.exit(Client.Error.BadCookieFile.code);
        }

        // Create new cookie list if file doesn't exist
        if (!Files.exists(Path.of(filePath))) {
            return new CookieList();
        }

        CookieList cookies = null;
        try {
            cookies = new CookieList(new MessageInput(Files.newInputStream(path)));
        } catch (IOException | SecurityException e) {
            System.err.println("Can't open file: " + filePath);
            System.exit(Client.Error.BadCookieFile.code);
        }
        catch (ValidationException e) {
            System.err.println("Invalid cookie file contents at token \"" + e.getToken() + "\"");
            System.exit(Client.Error.BadCookieFile.code);
        }
        return cookies;
    }

    static int getPortNumOrExit(String portStr) {
        int port = 0;
        try {
            port = Integer.parseInt(portStr);
        }
        catch (NumberFormatException e) {
            System.err.println("Port number must be integer");
            System.exit(Client.Error.BadPort.code);
        }
        if (port < 0 || port > 65535) {
            System.err.println("Invalid port number");
            System.exit(Client.Error.BadPort.code);
        }
        return port;
    }

    static Socket createSocketOrExit(String host, int port) {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
        } catch (UnknownHostException e) {
            System.err.println("Invalid or unresolvable host: " + host);
            System.exit(Client.Error.BadSocket.code);
        } catch (IOException | SecurityException e) {
            System.err.println("Error creating socket: " + e.getMessage());
            System.exit(Client.Error.BadSocket.code);
        }
        return socket;
    }
}
