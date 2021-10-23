/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization;

import java.io.EOFException;
import java.util.Objects;

/** Represents an N4M query and provides serialization/deserialization */
public class N4MQuery extends N4MMessage {
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
    }

    protected static N4MQuery decode(BinaryReader reader, int msgId, int errCode) throws ECException {
        if (errCode != ErrorCode.NOERROR.getErrorCodeNum())
            throw new ECException("Query can't have error code", ErrorCode.INCORRECTHEADER);

        String busName;
        try {
            busName = reader.readLpStr(N4M_CHARSET);
        }
        catch (EOFException e) {
            throw new ECException("Message too short", ErrorCode.BADMSGSIZE, e);
        }
        return new N4MQuery(msgId, busName);
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
