/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Prog0.2
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization.test;

import org.junit.jupiter.api.Test;
import sprt.serialization.MessageOutput;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageOutputTest {
    private final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
    private final MessageOutput out = new MessageOutput(byteArray);

    private void checkResult(String x) throws IOException {
        out.flush();
        assertEquals(x, byteArray.toString(StandardCharsets.US_ASCII));
    }

    @Test
    void write() throws IOException {
        out.write("Hello world");
        checkResult("Hello world");
    }

    // writing null should fail
    @Test
    void writeNull() {
        assertThrows(NullPointerException.class, () -> out.write((String) null));
    }

    // writing an empty string should have no effect
    @Test
    void writeEmpty() throws IOException {
        out.write("a b c");
        out.write("");
        checkResult("a b c");
    }

    @Test
    void writeLine() throws IOException {
        out.writeLine("aaa");
        checkResult("aaa\r\n");
    }

    @Test
    void writeLineEmpty() throws IOException {
        out.writeLine("");
        checkResult("\r\n");
    }
}