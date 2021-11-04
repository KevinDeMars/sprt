/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package shared.app;

public class AppUtil {
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
