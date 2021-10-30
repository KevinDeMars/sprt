/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;

/**
 * Stream writer for binary bytes or bits
 */
public class BinaryWriter {
    DataOutputStream dout;
    byte partialByte = 0;
    int bitIdx = 0;

    /**
     * Wraps the given output stream to write to it
     * @param out stream to wrap
     */
    public BinaryWriter(OutputStream out) {
        dout = new DataOutputStream(out);
    }

    /**
     * Writes part of the given array to the stream in order.
     * @param b array to write
     * @param off offset from start of b to write
     * @param len number of bytes to write
     * @throws IOException if I/O error
     * @throws IllegalStateException if not aligned with byte boundary
     */
    public void writeBytes(byte[] b, int off, int len) throws IOException {
        checkAligned();
        dout.write(b, off, len);
    }

    /**
     * Writes the entire byte array to the stream in order.
     * @param bytes array to write
     * @throws IOException if I/O error
     * @throws IllegalStateException if not aligned with byte boundary
     */
    public void writeBytes(byte[] bytes) throws IOException {
        writeBytes(bytes, 0, bytes.length);
    }

    /**
     * Writes the given byte to the stream.
     * @param v byte to write (upper 3 bytes are ignored)
     * @throws IOException if I/O error
     * @throws IllegalStateException if not aligned with byte boundary
     */
    public void writeByte(int v) throws IOException {
        checkAligned();
        dout.writeByte(v);
    }

    /**
     * Writes the given byte to the stream in big-endian order.
     * @param v short to write (upper 2 bytes are ignored)
     * @throws IOException if I/O error
     * @throws IllegalStateException if not aligned with byte boundary
     */
    public void writeShort(int v) throws IOException {
        checkAligned();
        dout.writeShort(v);
    }

    /**
     * Writes the given int to the stream in big-endian order.
     * @param v int to write
     * @throws IOException if I/O error
     * @throws IllegalStateException if not aligned with byte boundary
     */
    public void writeInt(int v) throws IOException {
        checkAligned();
        dout.writeInt(v);
    }

    /**
     * Writes a single bit to the stream.
     * @param v bit to write (upper 31 bits are ignored)
     * @throws IOException if I/O error
     */
    public void writeBit(int v) throws IOException {
        partialByte = (byte)((partialByte << 1) | (v & 1));
        bitIdx++;
        if (bitIdx == 8) {
            bitIdx = 0;
            writeByte(partialByte);
            partialByte = 0;
        }
    }

    /**
     * Writes an n-bit number to the stream (in big-endian order, if applicable).
     * @param v bits to write (upper bits are ignored)
     * @param numBits number of bits to write
     * @throws IOException if I/O error
     */
    public void writeBits(int v, int numBits) throws IOException {
        int numShifts = numBits - 1;
        for (int i = 0; i < numBits; ++i, --numShifts) {
            int mask = 1 << numShifts;
            int val = (v & mask) >> numShifts;
            writeBit(val);
        }
    }

    /**
     * Writes a string to the stream using the given encoder.
     * The length (in Unicode code units) is written first, as a single unsigned byte.
     *
     * @param str String to write
     * @param encoder encoder to use to convert to bytes
     * @throws CharacterCodingException if str can't be represented using the encoder's encoding
     * @throws IOException if I/O error
     *
     */
    public void writeLpStr(String str, CharsetEncoder encoder) throws IOException {
        checkAligned();
        if (str.length() > 0xFF) {
            throw new IllegalArgumentException("String too long");
        }
        writeByte(str.length());
        var bytes = encoder.encode(CharBuffer.wrap(str.toCharArray()))
                .array();
        writeBytes(bytes, 0, bytes.length);
    }

    private void checkAligned() {
        if (bitIdx != 0) {
            throw new IllegalStateException("Can't write entire bytes unless aligned with byte boundary");
        }
    }
}
