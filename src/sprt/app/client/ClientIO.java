/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.client;

import sprt.serialization.Parsing;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Predicate;

// Internal class for reading/writing to the console (or other stream)
class ClientIO {
    private final Scanner in;
    private final PrintStream out;
    private final PrintStream err;

    ClientIO(InputStream in, PrintStream out, PrintStream err) {
        this.in = new Scanner(in);
        this.out = out;
        this.err = err;
    }

    // Repeatedly prompt for a line until the line is valid, as defined by the predicate
    String read(String prompt, Predicate<String> checkLine, String errMsg) {
        out.print(prompt);
        String line = in.nextLine();
        while (!checkLine.test(line)) {
            err.println("Bad user input: " + errMsg);
            out.print(prompt);
            line = in.nextLine();
        }
        return line;
    }

    // Reads a single token from console
    String readToken(String prompt) {
        return read("Function> ", Parsing::isToken, "Function not a proper token (alphanumeric)");
    }

    // Reads 0 or more tokens separated by a space
    String[] readMultiTokens(String prompt) {
        String tokens = read(prompt,
                line -> line.length() == 0 || Arrays.stream(line.split(" ")).allMatch(Parsing::isToken),
                "All parameters must be tokens (alphanumeric)"
        );
        if (tokens.length() == 0)
            return new String[0];
        else
            return tokens.split(" ");
    }
}
