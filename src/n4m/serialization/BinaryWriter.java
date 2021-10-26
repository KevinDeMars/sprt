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
import java.nio.charset.CharsetEncoder;

public class BinaryWriter {
    DataOutputStream dout;
    byte partialByte = 0;
    int bitIdx = 0;

    public BinaryWriter(OutputStream out) {
        dout = new DataOutputStream(out);
    }

    public void writeBytes(byte[] b, int off, int len) throws IOException {
        checkAligned();
        dout.write(b, off, len);
    }

    public void writeByte(int v) throws IOException {
        checkAligned();
        dout.writeByte(v);
    }

    public void writeShort(int v) throws IOException {
        checkAligned();
        dout.writeShort(v);
    }

    public void writeInt(int v) throws IOException {
        checkAligned();
        dout.writeInt(v);
    }

    public void writeBit(int v) throws IOException {
        partialByte = (byte)((partialByte << 1) | (v & 1));
        bitIdx++;
        if (bitIdx == 8) {
            bitIdx = 0;
            writeByte(partialByte);
            partialByte = 0;
        }
    }

    public void writeBits(int v, int numBits) throws IOException {
        int numShifts = numBits - 1;
        for (int i = 0; i < numBits; ++i, --numShifts) {
            int mask = 1 << numShifts;
            int val = (v & mask) >> numShifts;
            writeBit(val);
        }
    }

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
