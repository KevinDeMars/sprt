/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.app.client;

import java.util.Random;

public class Util {
    private static Random rand = new Random();

    public static int randomInt(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }
}
