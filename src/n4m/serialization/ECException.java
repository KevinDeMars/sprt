/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization;

import java.io.Serial;

/** Exception with an error code, used by N4M */
public class ECException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    /** type of error, e.g. INCORRECTHEADER */
    private final ErrorCode errorCodeType;

    /**
     * Construct N4M validation exception
     * @param msg exception messsage (not null)
     * @param errorCode type of error (not null)
     * @param cause exception cause (nullable)
     * @throws NullPointerException if msg or errorCode is null
     */
    public ECException(String msg, ErrorCode errorCode, Throwable cause) {
        super(msg, cause);
        this.errorCodeType = errorCode;
    }

    /**
     * Construct N4M validation exception
     * @param msg exception messsage (not null)
     * @param errorCode type of error (not null)
     * @throws NullPointerException if msg or errorCode is null
     */
    public ECException(String msg, ErrorCode errorCode) {
        this(msg, errorCode, null);
    }

    /**
     * Return error code type
     * @return error code type
     */
    public ErrorCode getErrorCodeType() {
        return errorCodeType;
    }
}
