/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.app.client;

import java.io.IOException;
import java.io.Serial;

/**
 * Thrown if a packet was too large to fit in a buffer
 */
public class PacketTruncatedException extends IOException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Create exception with given message and cause
     * @param msg message
     * @param cause what caused this exception
     */
    public PacketTruncatedException(String msg, Throwable cause) {
        super(msg, cause);
    }
    /**
     * Create exception with given message
     * @param msg message
     */
    public PacketTruncatedException(String msg) {
        super(msg);
    }
}
