/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static sprt.serialization.Util.checkNull;

/**
 * Buffers a message until a delimiter is found.
 */
public class NIODeframer {
    /**
     * Delimiter to end a message
     */
    public static final String DELIMITER = "\r\n\r\n";
    private static final byte[] DELIM_BYTES = DELIMITER.getBytes(Message.SPRT_CHARSET);
    // Growable buffer to hold message
    private byte[] data = new byte[1024];
    // Idx in data where the next byte should be put.
    private int dataPos = 0;

    /**
     * See {@link #getMessage(byte[])}.
     * @param buffer (possibly) partial message to buffer
     * @return Full message (including delimiter) if one exists
     * @throws NullPointerException If buffer is null
     */
    public byte[] getMessage(ByteBuffer buffer) throws NullPointerException {
        checkNull(buffer, "buffer");
        var buf = new byte[buffer.remaining()];
        buffer.get(buf);
        return getMessage(buf);
    }

    /**
     * Appends the given partial message to the back of the buffer and, if possible, removes
     * a full delimited message from the front of the buffer.
     *
     * If a full message exists after this message is added, it is removed from the buffer and returned,
     *   including the delimiter. This only happens once per method call.
     * @param message (possibly) partial message to buffer
     * @return Full message (including delimiter) if one exists
     * @throws NullPointerException If message is null
     */
    public byte[] getMessage(byte[] message) throws NullPointerException {
        checkNull(message, "message");
        if (dataPos + message.length >= data.length)
            growBuffer();
        System.arraycopy(message, 0, this.data, dataPos, message.length);
        int oldDataPos = dataPos;
        dataPos += message.length;

        int delimiterIdx = findDelimiter(Math.max(0, oldDataPos - DELIM_BYTES.length));

        if (delimiterIdx != -1) {
            // Message is from start of data to delimiterIdx + delimiter length, exclusive.
            var output = new byte[delimiterIdx + DELIM_BYTES.length];
            System.arraycopy(data, 0, output, 0, delimiterIdx + DELIM_BYTES.length);
            // Truncate data buffer to whatever is after the delimiter
            int remainingDataIdx = delimiterIdx + DELIM_BYTES.length;
            int lengthAfterDelimiter = dataPos - remainingDataIdx;
            System.arraycopy(data, delimiterIdx + DELIM_BYTES.length, data, 0, lengthAfterDelimiter);
            dataPos = lengthAfterDelimiter;
            return output;
        }
        return null;
    }

    /**
     * Gets the size of the currently buffered data (not the capacity of the underlying array).
     * @return data size
     */
    public int dataSize() {
        return dataPos;
    }

    // Finds index of delimiter, searching from given start, or -1 if not found
    private int findDelimiter(int searchStart) {
        for (int start = searchStart; start <= dataPos - DELIM_BYTES.length; ++start) {
            if (-1 == Arrays.mismatch(data, start, start + DELIM_BYTES.length, DELIM_BYTES, 0, DELIM_BYTES.length))
            {
                return start;
            }
        }
        return -1;
    }

    // Doubles buffer size
    private void growBuffer() {
        var newData = new byte[data.length * 2];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }

}
