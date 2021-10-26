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
 * Stateful reader for big-endian binary data
 */
public class BinaryReader {
    int bitPos;
    int bytePos;
    byte[] data;

    public BinaryReader(byte[] data) {
        this.data = data;
    }

    public byte readByte() throws EOFException {
        checkAligned();
        checkRange();
        return data[bytePos++];
    }
    public int readUByte() throws EOFException {
        return 0xFF & readByte();
    }

    public short readShort() throws EOFException {
        return (short) readUShort();
    }
    public int readUShort() throws EOFException {
        int val = readUByte() << 8;
        val |= readUByte();
        return val;
    }

    public int readInt() throws EOFException {
        int val = readUByte() << 24;
        val |= readUByte() << 16;
        val |= readUByte() << 8;
        val |= readUByte();
        return val;
    }
    public long readUInt() throws EOFException {
        return 0xFFFFFFFFL & readInt();
    }

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

    public String readLpStr(CharsetDecoder decoder) throws EOFException, CharacterCodingException {
        int len = readUByte();
        var bytes = ByteBuffer.wrap(readBytes(len));
        return decoder.decode(bytes).toString();
    }

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
