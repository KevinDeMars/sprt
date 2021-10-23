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
 * Represents one application and its access count
 */
public class ApplicationEntry {
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
