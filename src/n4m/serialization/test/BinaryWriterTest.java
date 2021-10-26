/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization.test;

import n4m.serialization.BinaryWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import shared.serialization.test.TestUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BinaryWriterTest {
    private byte[] data;
    private ByteArrayOutputStream bOut;
    private BinaryWriter out;

    public BinaryWriterTest() {
        data = TestUtil.randomBytes(300);
        bOut = new ByteArrayOutputStream();
        out = new BinaryWriter(bOut);
    }

    @Test
    void testWriteByte() throws IOException {
        for (byte b : data)
            out.writeByte(b);
        assertArrayEquals(data, bOut.toByteArray());
    }

    @Test
    void testWriteBytes() throws IOException {
        out.writeByte(data[0]);
        out.writeBytes(data, 1, data.length - 1);
        assertArrayEquals(data, bOut.toByteArray());

        assertThrows(IndexOutOfBoundsException.class, () -> out.writeBytes(data, 0, 1000000));
    }

    @Test
    void testWriteBits() throws IOException {
        for (int i = 0; i < data.length; ++i) {
            int byt = data[i];
            out.writeBits((byt & 0b11111000) >> 3, 5);
            out.writeBits(byt & 0b00000111, 3);
        }

        assertArrayEquals(data, bOut.toByteArray());
    }

    @ParameterizedTest
    @MethodSource("strSrc")
    void testWriteLpStr(String str) throws IOException {
        out.writeLpStr(str, StandardCharsets.US_ASCII.newEncoder());

        // Copy everything except the first byte (length prefix)
        byte[] theStringPart = new byte[str.length()];
        System.arraycopy(bOut.toByteArray(), 1, theStringPart, 0, str.length());

        assertEquals(str, new String(theStringPart, StandardCharsets.US_ASCII));
    }
    static Stream<String> strSrc() {
        return Stream.of("", "Hello", "a".repeat(200));
    }

    @Test
    void testLpStrTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
                out.writeLpStr("a".repeat(300), StandardCharsets.US_ASCII.newEncoder())
        );
    }
}
