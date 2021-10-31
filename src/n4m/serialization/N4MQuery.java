/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.util.Objects;

import static sprt.serialization.Util.checkNull;

/** Represents an N4M query and provides serialization/deserialization */
public class N4MQuery extends N4MMessage {
    /** Maximum length for businessName */
    public static final int MAX_BUSINESS_NAME_LENGTH = 0xFF;
    // Name of business making the request
    private String businessName;

    /**
     * Creates a new N4M query using given values
     * @param msgId message ID
     * @param businessName business name
     * @throws ECException if validation fails (BADMSG)
     * @throws NullPointerException if business name is null
     */
    public N4MQuery(int msgId, String businessName) throws ECException {
        super(msgId);
        setBusinessName(businessName);
    }

    protected static N4MQuery doDecode(BinaryReader reader, int msgId, int errCode) throws ECException {
        if (errCode != ErrorCode.NOERROR.getErrorCodeNum())
            throw new ECException("Query can't have error code", ErrorCode.INCORRECTHEADER);

        String busName;
        try {
            busName = reader.readLpStr(N4M_CHARSET_DECODER);
        }
        catch (EOFException e) {
            throw new ECException("Message too short", ErrorCode.BADMSGSIZE, e);
        }
        catch (CharacterCodingException e) {
            throw new ECException("Business name has invalid chars", ErrorCode.BADMSG, e);
        }

        if (reader.hasNext()) {
            throw new ECException("Business name is shorter than length prefix", ErrorCode.BADMSGSIZE);
        }

        return new N4MQuery(msgId, busName);
    }

    @Override
    protected void doEncode(BinaryWriter out) throws IOException {
        // Finish writing header
        out.writeBit(0); // 0 for Query
        out.writeBits(0, 3); // error code: 3 zero bits
        out.writeByte(getMsgId());
        // Data
        try {
            out.writeLpStr(businessName, N4M_CHARSET_ENCODER);
        }
        catch (CharacterCodingException e) {
            throw new IllegalStateException("Invalid business name. Should never happen", e);
        }
    }

    /**
     * Returns a string representation in the form:
     *
     * N4M QUERY: MsgID=56, BusName=Mine
     * @return String representation of query
     */
    @Override
    public String toString() {
        return String.format("N4M QUERY: MsgID=%d, BusName=%s", msgId, businessName);
    }

    /**
     * Returns business name
     * @return business name
     */
    public String getBusinessName() {
        return businessName;
    }

    /**
     * Sets business name
     * @param businessName business name
     * @throws ECException if validation fails (BADMSG)
     * @throws NullPointerException if business name is null
     */
    public void setBusinessName(String businessName) throws ECException {
        checkNull(businessName, "businessName");
        if (businessName.length() > MAX_BUSINESS_NAME_LENGTH) {
            throw new ECException("Business name too long", ErrorCode.BADMSG);
        }
        if (!N4M_CHARSET_ENCODER.canEncode(businessName)) {
            throw new ECException("Invalid business name for N4M charset", ErrorCode.BADMSG);
        }
        this.businessName = businessName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        N4MQuery n4MQuery = (N4MQuery) o;
        return Objects.equals(businessName, n4MQuery.businessName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), businessName);
    }
}
