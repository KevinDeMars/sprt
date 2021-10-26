/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization.test;

import n4m.serialization.BinaryReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import shared.serialization.test.TestUtil;

import java.io.EOFException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BinaryReaderTest {
    private byte[] data;
    private BinaryReader reader;

    public BinaryReaderTest() {
        data = TestUtil.randomBytes(200);
        reader = new BinaryReader(data);
    }

    // Checks that correct byte is returned and bounds are checked.
    @Test
    void testReadByte() throws EOFException {
        for (byte byt : data) {
            int a = reader.readByte();
            assertEquals(byt, a);
        }
        assertThrows(EOFException.class, reader::readByte);
    }

    // Tests that unsigned reads are positive. For corresponding signed/unsigned reads S and U:
    //    A) U == S, if S is non-negative
    // or B) U == 2^(numBits) + S, if S is negative
    //        e.g. if readByte() is -3, then readUByte() must be 2^8 + (-3) = 253
    @Test
    void testSignedUnsignedBehavior() throws EOFException {
        var r1 = new BinaryReader(data);
        var r2 = new BinaryReader(data);
        for (int i = 0; i < data.length; ++i) {
            byte a = r1.readByte();
            int unsigned = r2.readUByte();
            assertTrue(unsigned >= 0, "unsigned read must be non-negative, got " + unsigned);
            if (a >= 0)
                assertEquals(a, unsigned);
            else
                assertEquals(unsigned, (1 << 8) + a);
        }

        r1 = new BinaryReader(data);
        r2 = new BinaryReader(data);
        for (int i = 0; i < data.length / 2; ++i) {
            short a = r1.readShort();
            int unsigned = r2.readUShort();
            assertTrue(unsigned >= 0, "unsigned read must be non-negative, got " + unsigned);
            if (a >= 0)
                assertEquals(a, unsigned);
            else
                assertEquals(unsigned, (1 << 16) + a);
        }

        r1 = new BinaryReader(data);
        r2 = new BinaryReader(data);
        for (int i = 0; i < data.length / 4; ++i) {
            int a = r1.readInt();
            long unsigned = r2.readUInt();
            assertTrue(unsigned >= 0, "unsigned read must be non-negative, got " + unsigned);
            if (a >= 0)
                assertEquals(a, unsigned);
            else
                assertEquals(unsigned, (1L << 32L) + a);
        }
    }

    @Test
    void testReadBits() throws EOFException {
        var r2 = new BinaryReader(data);

        for (int i = 0; i < data.length / 2; ++i) {
            int num = 0;
            for (int j = 0; j < 8; ++j)
                num = (num << 2) | reader.readBits(2);
            assertEquals(num, r2.readUShort());
        }

        assertThrows(EOFException.class, reader::readBit);
    }

    @Test
    void testReadBytes() throws EOFException {
        reader.readByte();
        assertThrows(IllegalArgumentException.class, () -> reader.readBytes(Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> reader.readBytes(-1));
        for (int i = 1; i < data.length; ++i) {
            assertEquals(data[i], reader.readBytes(1)[0]);
        }
    }

    @ParameterizedTest
    @MethodSource("stringSrc")
    void testReadLpStr(String str) throws EOFException, CharacterCodingException {
        byte[] bytes = new byte[str.length() + 1];
        bytes[0] = (byte) str.length();
        var strBytes = str.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(strBytes, 0, bytes, 1, str.length());
        reader = new BinaryReader(bytes);

        assertEquals(str, reader.readLpStr(StandardCharsets.US_ASCII.newDecoder()));
    }
    static Stream<String> stringSrc() {
        return Stream.of("", "hello world", "a".repeat(200));
    }

    @Test
    void testBadAlign() throws EOFException {
        reader.readBit();
        assertThrows(IllegalStateException.class, reader::readByte);
    }
}
