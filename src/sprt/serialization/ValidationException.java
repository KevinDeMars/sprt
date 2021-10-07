/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Prog0
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization;

import java.io.Serial;

/** Validation exception containing the token failing validation */
public class ValidationException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    /** token that caused the exception */
    private final String token;

    /**
     * Constructs validation exception
     * @param msg Exception message (non-null)
     * @param token Token causing validation failure (non-null)
     */
    public ValidationException(String msg, String token) throws NullPointerException {
        this(msg, token, null);
    }

    /**
     * Constructs validation exception
     * @param msg Exception message (non-null)
     * @param token Token causing validation failure (non-null)
     * @param cause Exception cause
     */
    public ValidationException(String msg, String token, Throwable cause) throws NullPointerException {
        super(msg, cause);
        this.token = token;
    }

    /**
     * Gets the token that caused the error.
     * @return a (possibly-invalid) token
     */
    public String getToken() {
        return token;
    }
}
