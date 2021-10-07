/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Prog0.2
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization.test;

import org.junit.jupiter.api.Test;
import sprt.serialization.PeekableReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PeekableReaderTest {
    private final PeekableReader digits = new PeekableReader(new InputStreamReader(
            new ByteArrayInputStream("0123456789".getBytes(StandardCharsets.US_ASCII))
    ));

    @Test
    void readNChars() throws IOException {
        assertEquals("01234", digits.readNChars(5));
        assertEquals('5', digits.peek());
    }

    @Test
    void readNCharsPastEnd() throws IOException {
        assertEquals("0123456789", digits.readNChars(50));
        assertEquals(-1, digits.peek());
    }

    @Test
    void readExactlyNChars() throws IOException {
        assertEquals("01234", digits.readExactlyNChars(5));
        assertEquals('5', digits.peek());
    }

    @Test
    void readExactlyNCharsPastEnd() {
        assertThrows(IOException.class, () -> digits.readExactlyNChars(50));
    }

    @Test
    void peekNChars() throws IOException {
        assertEquals("01234", digits.peekNChars(5));
        assertEquals('0', digits.peek());
    }

    @Test
    void peekNCharsPastEnd() throws IOException {
        assertEquals("0123456789", digits.peekNChars(50));
        assertEquals('0', digits.peek());
    }

    @Test
    void peekExactlyNChars() throws IOException {
        assertEquals("01234", digits.peekExactlyNChars(5));
        assertEquals('0', digits.peek());
    }

    @Test
    void peekExactlyNCharsPastEnd() {
        assertThrows(IOException.class, () -> digits.peekExactlyNChars(50));
    }


}
