/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package shared;

import java.util.Random;

/**
 * Contains miscellaneous methods such as RNG methods.
 */
public class MathUtil {
    private static Random rand = new Random();

    /**
     * Gets a random integer between min and max, inclusive.
     * @param min minimum number (inclusive)
     * @param max maximum number (inclusive)
     * @return the random integer
     */
    public static int randomInt(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }
}
