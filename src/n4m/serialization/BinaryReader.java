/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization;

import java.io.EOFException;
import java.nio.charset.Charset;

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

    public byte readInt8() throws EOFException {
        checkAligned();
        checkRange();
        return data[bytePos++];
    }
    public int readUInt8() throws EOFException {
        return readInt8();
    }

    public short readInt16() throws EOFException {
        return (short) ((readInt8() << 8) | readInt8());
    }
    public int readUInt16() throws EOFException {
        return readInt16();
    }

    public int readInt32() throws EOFException {
        return (readInt16() << 16) | readInt16();
    }
    public long readUInt32() throws EOFException {
        return readInt32();
    }

    public byte[] readBytes(int len) throws EOFException {
        byte[] result = new byte[len];
        for (int i = 0; i < len; ++i) {
            result[i] = readInt8();
        }
        return result;
    }

    public String readLpStr(Charset cs) throws EOFException {
        int len = readUInt8();
        return new String(readBytes(len), cs);
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
