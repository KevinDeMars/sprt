/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.app.client;

import java.io.IOException;

public class PacketTruncatedException extends IOException {
    public PacketTruncatedException(String msg, Throwable cause) {
        super(msg, cause);
    }
    public PacketTruncatedException(String msg) {
        super(msg);
    }
}
