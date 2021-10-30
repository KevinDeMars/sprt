/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization;

/** Type of error in N4M protocol */
public enum ErrorCode {
    /** No error */
    NOERROR(0),
    /** Problem with header (e.g. bad version) */
    INCORRECTHEADER(1),
    /** Incorrect message size (too long/short) */
    BADMSGSIZE(2),
    /** Incorrect message contents (e.g. illegal error code) */
    BADMSG(3),
    /** Problem with sending or receiving */
    SYSTEMERROR(4);

    private final int errorCodeNum;

    ErrorCode(int num) {
        this.errorCodeNum = num;
    }

    /**
     * Return error code number corresponding to the error code
     * @return error code number
     */
    public int getErrorCodeNum() {
        return errorCodeNum;
    }

    /**
     * Gets error code that corresponds to number
     * @param errorCodeNum error code number to find
     * @return corresponding error code
     * @throws ECException if invalid error code number (INCORRECTHEADER)
     */
    public static ErrorCode valueOf(int errorCodeNum) throws ECException {
        for (var err : ErrorCode.values())
            if (err.errorCodeNum == errorCodeNum)
                return err;
        throw new ECException("Invalid error code number", INCORRECTHEADER);
    }
}
