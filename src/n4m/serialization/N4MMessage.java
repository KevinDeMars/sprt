/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.*;
import java.util.Objects;

import static sprt.serialization.Util.checkNull;

/**
 * Represents generic portion of a N4M message and provides serialization/deserialization.
 */
public abstract class N4MMessage {
    /** Maximum value for message ID */
    public static final int MAX_MSG_ID = 0xFF;
    /** Current protocol version */
    public static final byte VERSION = 0b0010;

    /** Charset used for Strings within messages */
    public static final Charset N4M_CHARSET = StandardCharsets.US_ASCII;
    // Used to convert Strings to bytes
    protected static final CharsetEncoder N4M_CHARSET_ENCODER = N4M_CHARSET.newEncoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);
    // used to convert bytes to Strings
    protected static final CharsetDecoder N4M_CHARSET_DECODER = N4M_CHARSET.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);



    // randomly generated number that client uses to map server responses to outstanding requests
    // range: 0-255
    protected int msgId;

    protected N4MMessage(int msgId) throws ECException {
        setMsgId(msgId);
    }

    /**
     * Creates a new N4M message by deserializing from the given byte array according to the specified serialization.
     * @param in buffer of received packet
     * @return new N4M message
     * @throws ECException if validation fails such as bad version, incorrect packet size, etc.
     *    with error code types: header problems (INCORRECTHEADER)
     *    too many/few bytes (BADMSGSIZE)
     * @throws NullPointerException if in is null
     */
    public static N4MMessage decode(byte[] in) throws ECException {
        var reader = new BinaryReader(checkNull(in, "in"));

        // Read portion of header
        int version;
        boolean isResponse;
        int errCode;
        int msgId;
        try {
            version = reader.readBits(4);
            isResponse = reader.readBit() == 1;
            errCode = reader.readBits(3);
            msgId = reader.readUByte();
        }
        catch (EOFException e) {
            throw new ECException("Header too short", ErrorCode.BADMSGSIZE, e);
        }

        if (version != VERSION) {
            throw new ECException("Bad version: " + version, ErrorCode.INCORRECTHEADER);
        }

        // Delegate the rest to subclasses
        if (isResponse) {
            return N4MResponse.doDecode(reader, msgId, errCode);
        }
        else {
            return N4MQuery.doDecode(reader, msgId, errCode);
        }
    }

    /**
     * Return encoded N4M message
     * @return message encoded in byte array
     */
    public byte[] encode() {
        var bOut = new ByteArrayOutputStream();
        var out = new BinaryWriter(bOut);
        try {
            out.writeBits(VERSION, 4);
            doEncode(out);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't write to byte output stream. This should never happen");
        }
        return bOut.toByteArray();
    }

    protected abstract void doEncode(BinaryWriter out) throws IOException;

    /**
     * Return message ID
     * @return message ID
     */
    public int getMsgId() {
        return msgId;
    }

    /**
     * Set message ID
     * @param msgId new message ID
     * @throws ECException if validation fails (BADMSG)
     */
    public void setMsgId(int msgId) throws ECException {
        if (msgId > MAX_MSG_ID || msgId < 0) {
            throw new ECException("msgId out of range", ErrorCode.INCORRECTHEADER);
        }
        this.msgId = msgId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        N4MMessage that = (N4MMessage) o;
        return msgId == that.msgId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(msgId);
    }
}
