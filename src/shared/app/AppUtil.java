/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package shared.app;

/**
 * Functionality common between SPRT and N4M clients and servers.
 */
public class AppUtil {
    /**
     * Parse and bounds-check a String, exiting the program on failure.
     * @param varname name of variable (for error message)
     * @param str String representation of variable
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @param exitCode code to exit with on failure
     * @return given value, parsed as an integer
     */
    public static int parseIntOrExit(String varname, String str, int min, int max, int exitCode) {
        int x = 0;
        try {
            x = Integer.parseInt(str);
        }
        catch (NumberFormatException e) {
            System.err.println(varname + " must be an integer");
            System.exit(exitCode);
        }

        if (x < min || x > max) {
            System.err.println(varname + " must be >" + min + " and <" + max);
            System.exit(exitCode);
        }

        return x;
    }
}
