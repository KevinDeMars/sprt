/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package shared.serialization.test;

import java.util.Random;

public class TestUtil {
    public static byte[] randomBytes(int n) {
        var rng = new Random();
        var data = new byte[n];
        rng.nextBytes(data);
        return data;
    }
}
