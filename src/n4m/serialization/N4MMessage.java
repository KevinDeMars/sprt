/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization;

import java.util.Objects;

/**
 * Represents generic portion of a N4M message and provides serialization/deserialization.
 */
public abstract class N4MMessage {
    // randomly generated number that client uses to map server responses to outstanding requests
    // range: 0-255
    int msgId;

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

    }

    /**
     * Return encoded N4M message
     * @return message encoded in byte array
     */
    public byte[] encode() {

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
