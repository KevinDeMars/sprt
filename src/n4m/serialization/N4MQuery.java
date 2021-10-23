/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization;

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

    }

    /**
     * Returns a string representation in the form:
     *
     * N4M QUERY: MsgID=56, BusName=Mine
     * @return String representation of query
     */
    @Override
    public String toString() {
        return super.toString();
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
