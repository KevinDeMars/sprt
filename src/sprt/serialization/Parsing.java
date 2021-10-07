/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Prog0.2
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

/** Contains common methods useful for parsing from a MessageInput for the SPRT protocol. */
public class Parsing {
    /**
     * Determines whether the given String is a token (non-empty and containing only alphanumeric characters).
     * @param str the String to test.
     * @return true if str is a token; else, false.
     */
    public static boolean isToken(String str) {
        return str != null && str.matches("[A-Za-z0-9]+");
    }

    /**
     * Asserts that the next characters in the stream match str exactly.
     * Advances the stream position by the length of str.
     * @param in input source
     * @param str the String that the stream should contain
     * @throws ValidationException if the stream does not immediately contain str.
     * @throws IOException if IO error
     */
    public static void expectNextString(MessageInput in, String str) throws ValidationException, IOException {
        String read = in.readNChars(str.length());
        if (read.length() < str.length()) {
            throw new EOFException("Premature EOS when expecting string \"" + str + "\"");
        }
        if (!str.equals(read)) {
            throw new ValidationException("Expected to get " + str + "; got " + read, read);
        }
    }

    /**
     * Asserts that the next characters in the stream exactly match one
     * of the given Strings.
     *
     * The stream position is advanced by the length of the matching String.
     * @param validStrings the possible Strings that could be the next characters in the stream.
     * @param in input source
     * @return The matching String contained by the stream
     * @throws IOException if the stream couldn't be read.
     * @throws ValidationException if none of the given strings matched
     */
    public static String expectNextStrings(MessageInput in, String... validStrings) throws IOException, ValidationException {
        // Read enough chars to compare to the longest valid string
        var stats = Arrays.stream(validStrings)
                .mapToInt(String::length)
                .summaryStatistics();
        int maxLen = stats.getMax();
        int minLen = stats.getMin();

        String read = in.peekNChars(maxLen);
        if (read.length() < minLen) {
            throw new EOFException("Not enough chars to match any string. Got: \"" + read
                    + "\", expected one of: " + Arrays.toString(validStrings));
        }

        for (String validStr : validStrings) {
            // If the first part of read matches validStr, we found a match
            if (read.length() >= validStr.length()
                    && read.regionMatches(0, validStr, 0, validStr.length())
            ) {
                // Advance by the length of the matching string from the initial position
                in.skip(validStr.length());
                return validStr;
            }
        }
        throw new ValidationException("Got none of the expected strings: " + Arrays.toString(validStrings), read);
    }
}
