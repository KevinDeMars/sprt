/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization;

import java.util.Objects;

import static sprt.serialization.Util.checkNull;

/**
 * Represents one application and its access count
 */
public class ApplicationEntry {
    public static final int MAX_ACCESS_COUNT = 0xFFFF;
    public static final int MAX_APPLICATION_NAME_LENGTH = 0xFF;

    // number of times this app was accessed
    private int accessCount;
    // name of app, e.g. Poll
    private String applicationName;

    /**
     * Create application entry
     * @param applicationName name of application
     * @param accessCt application's access count
     * @throws ECException if validation fails (BADMSG)
     * @throws NullPointerException if applicationName is null
     */
    public ApplicationEntry(String applicationName, int accessCt)
            throws ECException
    {
        setApplicationName(applicationName);
        setAccessCount(accessCt);
    }

    /**
     * Gets app's access count
     * @return access count
     */
    public int getAccessCount() {
        return accessCount;
    }

    /**
     * Set app's access count
     * @param accessCount access count
     * @throws ECException if validation fails (BADMSG)
     */
    public void setAccessCount(int accessCount) throws ECException {
        if (accessCount > MAX_ACCESS_COUNT || accessCount < 0) {
            throw new ECException("Access count out of range", ErrorCode.BADMSG);
        }
        this.accessCount = accessCount;
    }

    /**
     * Returns app's name
     * @return app name
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Sets app name
     * @param applicationName app name
     * @throws ECException if validation fails, e.g. name too long (BADMSG)
     * @throws NullPointerException if applicationName is null
     */
    public void setApplicationName(String applicationName) throws ECException {
        checkNull(applicationName, "applicationName");
        if (applicationName.length() > MAX_APPLICATION_NAME_LENGTH) {
            throw new ECException("Application name too long", ErrorCode.BADMSG);
        }
        if (!N4MMessage.N4M_CHARSET_ENCODER.canEncode(applicationName)) {
            throw new ECException("Invalid application name for N4M charset", ErrorCode.BADMSG);
        }
        this.applicationName = applicationName;
    }

    /**
     * Returns a String representation, e.g. "Poll(40)"
     * @return String representation of this application and access count
     */
    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationEntry that = (ApplicationEntry) o;
        return accessCount == that.accessCount && Objects.equals(applicationName, that.applicationName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessCount, applicationName);
    }
}
