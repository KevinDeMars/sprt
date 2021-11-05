package sprt.app.donatest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * P3 Server test
 * @version 1.0
 */
@TestMethodOrder(OrderAnnotation.class)
class ServerTest {

    private static final String SERVER = "localhost";
    private static final int PORT = 12345;
    private static final Charset ENC = StandardCharsets.US_ASCII;
    private static final int SLOWDELAYMS = 100;

    private Socket clientSocket;
    private Scanner in;

    @BeforeEach
    protected void before() throws IOException {
        clientSocket = new Socket(SERVER, PORT);
        in = new Scanner(clientSocket.getInputStream(), ENC);
        in.useDelimiter("\r\n\r\n");
    }

    @AfterEach
    protected void after() throws IOException {
        in.close();
        clientSocket.close();
    }

    @BeforeAll
    protected static void announcement() {
        System.out.println("1.  Ignore order of cookies in response.");
    }

    @DisplayName("Basic")
    @Test
    @Order(1)
    protected void testBasic() throws IOException, InterruptedException {
        printTest("BC [Basic] - 20 points");

        sendSlowly(clientSocket, "SPRT/1.0 Q RUN Poll\r\n\r\n");
        printExpected("SPRT/1.0 R OK NameStep Name (First Last)>");

        send(clientSocket, "SPRT/1.0 Q RUN NameStep Bob Smith\r\n\r\n");
        printExpected("SPRT/1.0 R OK FoodStep Bob's Food Mood>\nFName=Bob\nLName=Smith");

        send(clientSocket, "SPRT/1.0 Q RUN FoodStep Italian\r\nLName=Smith\r\nFName=Bob\r\n\r\n");
        printExpected("SPRT/1.0 R OK NULL 25% + 1% off at Pastatic\nFName=Bob\nLName=Smith\nRepeat=1");
    }

    @DisplayName("Name")
    @Test
    @Order(2)
    protected void testName() throws IOException, InterruptedException {
        printTest("NM [Name] - 10 points");

        send(clientSocket, "SPRT/1.0 Q RUN Poll\r\nFName=Bob\r\nLName=Smith\r\nRepeat=1\r\n\r\n");
        printExpected("SPRT/1.0 R OK FoodStep Bob's Food Mood>\nFName=Bob\nLName=Smith\nRepeat=1");

        send(clientSocket, "SPRT/1.0 Q RUN FoodStep Mexican\r\nFName=Bob\r\nLName=Smith\r\nRepeat=1\r\n\r\n");
        printExpected("SPRT/1.0 R OK NULL 20% + 2% off at Tacopia\nFName=Bob\nLName=Smith\nRepeat=2");
    }

    @DisplayName("Repeat")
    @Test
    @Order(3)
    protected void testRepeat() throws IOException, InterruptedException {
        printTest("RP [Repeat] - 10 points");

        send(clientSocket, "SPRT/1.0 Q RUN Poll\r\nFName=Bob\r\nLName=Smith\r\nRepeat=1\r\n\r\n");
        printExpected("SPRT/1.0 R OK FoodStep Bob's Food Mood>\nFName=Bob\nLName=Smith\nRepeat=1");

        send(clientSocket, "SPRT/1.0 Q RUN FoodStep X\r\nFName=Bob\r\nLName=Smith\r\nRepeat=1\r\n\r\n");
        printExpected("SPRT/1.0 R OK NULL 10% + 2% off at McDonalds\nFName=Bob\nLName=Smith\nRepeat=2");
    }

    @DisplayName("Bad Transition")
    @Test
    @Order(4)
    protected void testTransitionBad() throws IOException, InterruptedException {
        printTest("TB [Transition Bad] - 10 points");

        send(clientSocket, "SPRT/1.0 Q RUN Poll\r\n\r\n");
        printExpected("SPRT/1.0 R OK NameStep Name (First Last)>");

        send(clientSocket, "SPRT/1.0 Q RUN FoodStep Chicken\r\nFName=Bob\r\nLName=Smith\r\n\r\n");
        printExpected("SPRT/1.0 R ERROR NULL <Some message about unexpected>");
    }

    @DisplayName("Bad Command")
    @Test
    @Order(5)
    protected void testCommandBad() throws IOException, InterruptedException {
        printTest("CB [Command Bad] - 10 points");

        send(clientSocket, "SPRT/1.0 Q YURP Poll\r\n\r\n");
        printExpected("SPRT/1.0 R ERROR NULL <Some message about unexpected command>");
    }

    @DisplayName("Bad Parameter")
    @Test
    @Order(6)
    protected void testParameterBad() throws IOException, InterruptedException {
        printTest("PB [Parameter Bad] - 10 points");

        send(clientSocket, "SPRT/1.0 Q RUN Poll\r\n\r\n");
        printExpected("SPRT/1.0 R OK NameStep Name (First Last)>");

        send(clientSocket, "SPRT/1.0 Q RUN NameStep Yurp\r\n\r\n");
        printExpected("SPRT/1.0 R ERROR NameStep <Some error about name>. Name (First Last)>");

        send(clientSocket, "SPRT/1.0 Q RUN NameStep Bob Smith\r\n\r\n");
        printExpected("SPRT/1.0 R OK FoodStep Bob's Food Mood>\nFName=Bob\nLName=Smith");

        send(clientSocket, "SPRT/1.0 Q RUN FoodStep Italian\r\nFName=Bob\r\nLName=Smith\r\n\r\n");
        printExpected("SPRT/1.0 R OK NULL 25% + 1% off at Pastatic\nFName=Bob\nLName=Smith\nRepeat=1");
    }

    @DisplayName("Bad Repeat")
    @Test
    @Order(7)
    protected void testRepeatBad() throws IOException, InterruptedException {
        printTest("RB [Repeat Bad] - 10 points");

        send(clientSocket, "SPRT/1.0 Q RUN Poll\r\nFName=Bob\r\nLName=Smith\r\n\r\n");
        printExpected("Should get FoodStep");
        send(clientSocket, "SPRT/1.0 Q RUN FoodStep Mexican\r\nFName=Bob\r\nLName=Smith\r\nRepeat=Z\r\n\r\n");
        printExpected("Repeat should be something reasonable like 0 or 1");
    }

    private static synchronized void printTest(String testName) {
        System.err.println("***************************");
        System.err.println(testName);
        System.err.println("***************************");
    }

    private static synchronized void sendSlowly(Socket clientSocket, String msg)
            throws IOException, InterruptedException {
        for (byte b : msg.getBytes(ENC)) {
            clientSocket.getOutputStream().write(b);
            TimeUnit.MILLISECONDS.sleep(SLOWDELAYMS);
        }
    }

    private static synchronized void send(Socket clientSocket, String msg) throws IOException, InterruptedException {
        clientSocket.getOutputStream().write(msg.getBytes(ENC));
        TimeUnit.MILLISECONDS.sleep(10);
    }
    
    private void dumpResponse() {
        System.out.println(in.next());
    }

    private synchronized void printExpected(String msg) {
        System.err.println(msg);
        dumpResponse();
        System.out.println();
    }
}
