/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;

/**
 * Stateful reader for big-endian binary bytes or bits
 */
public class BinaryReader {
    int bitPos;
    int bytePos;
    byte[] data;

    /**
     * Creates new reader that reads from the given data
     * @param data data to read
     */
    public BinaryReader(byte[] data) {
        this.data = data;
    }

    /**
     * Checks whether there is more data to be read.
     * @return true if data can be read; else, false
     */
    public boolean hasNext() {
        return bytePos < data.length;
    }

    /**
     * Reads a signed byte.
     * @return byte read
     * @throws EOFException if no more data exists
     * @throws IllegalStateException if not aligned with byte boundary
     */
    public byte readByte() throws EOFException {
        checkAligned();
        checkRange();
        return data[bytePos++];
    }
    /**
     * Reads an unsigned byte.
     * @return byte read
     * @throws EOFException if no more data exists
     * @throws IllegalStateException if not aligned with byte boundary
     */
    public int readUByte() throws EOFException {
        return 0xFF & readByte();
    }

    /**
     * Reads a signed 2-byte big-endian short.
     * @return short read
     * @throws EOFException if no more data exists
     * @throws IllegalStateException if not aligned with byte boundary
     */
    public short readShort() throws EOFException {
        return (short) readUShort();
    }
    /**
     * Reads an unsigned 2-byte big-endian short.
     * @return short read
     * @throws EOFException if no more data exists
     * @throws IllegalStateException if not aligned with byte boundary
     */
    public int readUShort() throws EOFException {
        int val = readUByte() << 8;
        val |= readUByte();
        return val;
    }

    /**
     * Reads a signed 4-byte big-endian int.
     * @return int read
     * @throws EOFException if no more data exists
     * @throws IllegalStateException if not aligned with byte boundary
     */
    public int readInt() throws EOFException {
        int val = readUByte() << 24;
        val |= readUByte() << 16;
        val |= readUByte() << 8;
        val |= readUByte();
        return val;
    }
    /**
     * Reads an unsigned 4-byte big-endian int.
     * @return int read
     * @throws EOFException if no more data exists
     * @throws IllegalStateException if not aligned with byte boundary
     */
    public long readUInt() throws EOFException {
        return 0xFFFFFFFFL & readInt();
    }

    /**
     * Reads the given number of bytes.
     * @param len number of bytes to read
     * @return bytes read
     * @throws EOFException if no more data exists
     * @throws IllegalArgumentException if len is negative or extremely large (wraps around int limit)
     * @throws IllegalStateException if not aligned with byte boundary
     */
    public byte[] readBytes(int len) throws EOFException {
        if (bytePos + len > data.length)
            throw new EOFException("Tried to read out of bounds of data");
        // check for negative or extremely large (wraps over int limit) len
        if (bytePos + len < bytePos)
            throw new IllegalArgumentException("Bad length");
        byte[] result = new byte[len];
        for (int i = 0; i < len; ++i) {
            result[i] = readByte();
        }
        return result;
    }

    /**
     * Reads a length-prefixed string, interpreted using the given decoder.
     * The length is prefixed using an unsigned 1-byte value.
     *
     * @param decoder decoder to interpret bytes as chars
     * @return String read
     * @throws EOFException if the length prefix says that there are more characters in
     *    the String than there are to read from the data.
     * @throws CharacterCodingException if the decoder is unable to decode the bytes to chars.
     * @throws IllegalStateException if not aligned with byte boundary
     */
    public String readLpStr(CharsetDecoder decoder) throws EOFException, CharacterCodingException {
        int len = readUByte();
        var bytes = ByteBuffer.wrap(readBytes(len));
        return decoder.decode(bytes).toString();
    }

    /**
     * Reads a single bit.
     * @return bit read
     * @throws EOFException if no data can be read
     */
    public int readBit() throws EOFException {
        checkRange();
        int mask = 0b1000_0000 >> bitPos;
        int numShifts = 7 - bitPos;
        int result = (data[bytePos] & mask) >> numShifts;
        ++bitPos;
        if (bitPos == 8) {
            bitPos = 0;
            ++bytePos;
        }
        return result;
    }

    /**
     * Reads an n-bit integer (assumed to be big endian if applicable).
     * @param numBits number of bits to read
     * @return bits read
     * @throws EOFException if no data can be read
     * @throws IllegalArgumentException if numBits is less than 1 or greater than 32
     */
    public int readBits(int numBits) throws EOFException {
        if (numBits > 32 || numBits < 1) {
            throw new IllegalArgumentException("numBits must be between 1 and 32");
        }
        int result = 0;
        for (int i = 0; i < numBits; ++i) {
            result = (result << 1) | readBit();
        }
        return result;
    }

    private void checkAligned() {
        if (bitPos != 0) {
            throw new IllegalStateException("Can't call readByte/Short/Int if not aligned with byte boundary");
        }
    }
    private void checkRange() throws EOFException {
        if (bytePos >= data.length) {
            throw new EOFException("Tried to read out of bounds of data");
        }
    }
}
