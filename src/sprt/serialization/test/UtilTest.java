/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Prog0.2
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static sprt.serialization.Util.checkNull;
import static sprt.serialization.Util.isAlnum;


public class UtilTest {
    @Test
    void checkNullOk() {
        String x = "oof";
        checkNull(x, "x");
    }

    @Test
    void checkNullBad() {
        String x = null;
        assertThrows(NullPointerException.class, () -> checkNull(x, "x"));
    }

    @ParameterizedTest
    @CsvSource({"A", "a", "0"})
    void isAlnumOk(char c) {
        assertTrue(isAlnum(c));
    }
    @ParameterizedTest
    @CsvSource({"é", "!"})
    void isAlnumBad(char c) {
        assertFalse(isAlnum(c));
    }

    @Test
    void isAlnumWhitespace() {
        assertFalse(isAlnum(' '));
        assertFalse(isAlnum('\t'));
    }


}
