/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Represents an N4M response and provides serialization/deserialization */
public class N4MResponse extends N4MMessage {
    // One entry for each application that the requesting business owns
    private List<ApplicationEntry> applications;
    // Time of the most recent application execution
    // Seconds since epoch (1970-01-01)
    private long timestamp;
    // Type of error, or no error (e.g. INVALIDHEADER)
    private ErrorCode errorCode;

    /**
     * Creates a new N4M response using given values
     * @param errorCode type of error (or no error)
     * @param msgId message ID
     * @param timestamp Time of last app execution, in seconds since 1970-01-01.
     *                  If no app has been executed, then timestamp is 0.
     * @param applications list of applications
     * @throws ECException if validation fails (BADMSG)
     * @throws NullPointerException if applications is null
     */
    public N4MResponse(ErrorCode errorCode, int msgId, long timestamp, List<ApplicationEntry> applications)
        throws ECException
    {
        super(msgId);
    }

    protected static N4MResponse decode(BinaryReader reader, int msgId, int errorCode) throws ECException {
        long timestamp;
        int appCount;
        List<ApplicationEntry> entries = new ArrayList<>();
        try {
            timestamp = reader.readUInt32();
            appCount = reader.readUInt8();
            for (int i = 0; i < appCount; ++i) {
                entries.add(readAppEntry(reader));
            }
        }
        catch (EOFException e) {
            throw new ECException("Message too short", ErrorCode.BADMSGSIZE, e);
        }

        var ec = ErrorCode.valueOf(errorCode);
        return new N4MResponse(ec, msgId, timestamp, entries);
    }

    protected static ApplicationEntry readAppEntry(BinaryReader reader) throws EOFException, ECException {
        int useCount = reader.readUInt16();
        String name = reader.readLpStr(N4M_CHARSET);
        return new ApplicationEntry(name, useCount);
    }

    /**
     * Returns String representation in the form:
     *
     * N4M RESPONSE: MsgID=&lt;msgid&gt;, Error=&lt;error&gt;, Time=&lt;timestamp&gt;: (&lt;app name&gt;(&lt;count&gt;)&lt;space&gt;)*
     *
     * Example:
     * N4M RESPONSE: MsgID=56, Error=NOERROR, Time=Wed Oct 20 23:46:51 CDT 2021: Guess(97) Poll(141)
     * @return String representation
     */
    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * get list of applications
     * @return list of applications
     */
    public List<ApplicationEntry> getApplications() {
        return applications;
    }

    /**
     * Sets list of applications
     * @param applications list of applications
     * @throws ECException if too many applications (BADMSG)
     * @throws NullPointerException if applications is null
     */
    public void setApplications(List<ApplicationEntry> applications) throws ECException {
        this.applications = applications;
    }

    /**
     * Return timestamp
     * @return timestamp (time of last execution, in seconds since 1970-01-01)
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets timestamp
     * @param timestamp Time of last application execution, given in
     *                  seconds since 1/1/1970. If no application has
     *                  been executed then time should be 0.
     * @throws ECException if validation fails (BADMSG)
     */
    public void setTimestamp(long timestamp) throws ECException {
        this.timestamp = timestamp;
    }

    /**
     * Return error code
     * @return error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Set error code
     * @param errorCode new error code
     * @throws NullPointerException if errorCode is null
     */
    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        N4MResponse that = (N4MResponse) o;
        return timestamp == that.timestamp && Objects.equals(applications, that.applications) && errorCode == that.errorCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), applications, timestamp, errorCode);
    }
}
