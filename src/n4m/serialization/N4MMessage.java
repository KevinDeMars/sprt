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
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Represents generic portion of a N4M message and provides serialization/deserialization.
 */
public abstract class N4MMessage {
    public static final Charset N4M_CHARSET = StandardCharsets.US_ASCII;

    // randomly generated number that client uses to map server responses to outstanding requests
    // range: 0-255
    int msgId;

    protected N4MMessage(int msgId) throws ECException {
        if (msgId > 255 || msgId < 0) {
            throw new ECException("msgId must be between 0 and 255", ErrorCode.INCORRECTHEADER);
        }
        this.msgId = msgId;
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
        var reader = new BinaryReader(in);

        int version;
        boolean isResponse;
        int errCode;
        int msgId;
        try {
            version = reader.readBits(4);
            isResponse = reader.readBit() == 1;
            errCode = reader.readBits(3);
            msgId = reader.readUInt8();
        }
        catch (EOFException e) {
            throw new ECException("Header too short", ErrorCode.BADMSGSIZE, e);
        }

        if (version != 0b0010) {
            throw new ECException("Bad version: " + version, ErrorCode.INCORRECTHEADER);
        }

        if (isResponse) {
            return N4MResponse.decode(reader, msgId, errCode);
        }
        else {
            return N4MQuery.decode(reader, msgId, errCode);
        }
    }

    /**
     * Return encoded N4M message
     * @return message encoded in byte array
     */
    public byte[] encode() {
        // TODO
        return new byte[0];
    }

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
