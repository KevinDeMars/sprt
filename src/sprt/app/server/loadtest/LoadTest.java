/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server.loadtest;

import sprt.serialization.*;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static shared.app.AppUtil.parseIntOrExit;
import static sprt.app.server.loadtest.LoadTestingLib.measurePerformance;

/**
 * Measures performance running a SPRT server with many concurrent connections.
 */
public class LoadTest {
    private static final Logger LOG = Logger.getLogger("LoadTest");

    /**
     * Runs the load test.
     * @param args server name/ip, port number, connections per second, time to test (sec)
     */
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: LoadTest <server> <port> <connections/sec> <test time (sec)>");
            return;
        }

        String host = args[0];
        int port = parseIntOrExit("port", args[1], 1, 65535, 1);
        int connPerSec = parseIntOrExit("connPerSec", args[2], 1, Integer.MAX_VALUE, 1);
        int testTime = parseIntOrExit("testTime", args[3], 1, Integer.MAX_VALUE, 1);

        var stats = measurePerformance(() -> runClient(host, port), connPerSec, testTime);

        System.out.printf("Tested with %d function calls in total%n", stats.count);
        System.out.printf("Min time: %s%n", stats.minTime);
        System.out.printf("Max time: %s%n", stats.maxTime);
        System.out.printf("Avg time: %s%n", stats.avgTime);
    }

    // The function that has its runtime measured
    private static Void runClient(String host, int port) throws IOException, ValidationException {
        new MockClient(host, port, requests, expectedResponses).go();
        return null;
    }

    // Utility methods for requests/responses to use in testing

    private static Request makeRequest(String fn, String... params) {
        try {
            return new Request(fn, params, new CookieList());
        }
        catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }
    private static Response makeResponse(String fn, String msg, String... cookies) {
        try {
            CookieList clist = new CookieList();
            for (int i = 0; i < cookies.length; i += 2)
                clist.add(cookies[i], cookies[i+1]);
            return new Response(Status.OK, fn, msg, clist);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }
    private static List<Request> requests = List.of(
            makeRequest("Poll"),
            makeRequest("NameStep", "Bob", "Smith"),
            makeRequest("FoodStep", "Mexican")
    );
    private static List<Response> expectedResponses = List.of(
            makeResponse("NameStep", "Name (First Last)> "),
            makeResponse("FoodStep", "Bob's Food Mood> ", "FName", "Bob", "LName", "Smith"),
            makeResponse("NULL", "20% + 1% off at Tacopia", "FName", "Bob", "LName", "Smith", "Repeat", "1")
    );

    // server port
    private final int port;
    // server name or IP
    private final String srvAddr;

    /**
     * Creates a load tester with the given server info
     * @param port server port
     * @param srvAddr server name or ip
     */
    public LoadTest(int port, String srvAddr) {
        this.port = port;
        this.srvAddr = srvAddr;
    }

}
