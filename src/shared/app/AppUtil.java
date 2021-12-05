/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package shared.app;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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

    /**
     * Creates a new logger for the class that writes to the given file.
     * @param cls Class used to set logger's name
     * @param filename path to write log
     * @return new logger
     */
    public static Logger logToFile(Class<?> cls, String filename) {
        var log = Logger.getLogger(cls.getSimpleName());
        log.setUseParentHandlers(false);
        try {
            log.setLevel(Level.ALL);

            var hnd = new FileHandler(filename);
            hnd.setLevel(Level.ALL);
            hnd.setFormatter(new SimpleFormatter());
            log.addHandler(hnd);

            /*var hnd2 = new ConsoleHandler();
            hnd2.setLevel(Level.ALL);
            log.addHandler(hnd2);*/

        } catch (IOException e) {
            System.err.println("Couldn't set up log: " + e);
        }
        return log;
    }
}
