/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Prog0.2
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization.test;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sprt.serialization.Parsing;
import sprt.serialization.ValidationException;

import java.io.EOFException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static sprt.serialization.test.MessageInputTest.makeTestMessageInput;

public class ParsingTest {
    @Nested
    class ExpectNextStrings {
        // Tests that if expectNextStrings finds a match, it returns that match
        @Test
        void expectNextStringsFound() throws ValidationException, IOException {
            var in = makeTestMessageInput("SPRT/1.0");
            String protocol = Parsing.expectNextStrings(in, "SPRT/1.1", "SPRT/1.0");
            assertEquals("SPRT/1.0", protocol);
        }

        // Tests that if expectNextStrings finds a match and the input strings are different sizes,
        // it returns that match
        @Test
        void expectNextStringsWithDifferentSizes() throws ValidationException, IOException {
            var in = makeTestMessageInput("Hello there");
            Parsing.expectNextStrings(in, "foo", "This is a really long string", "Hello there");
        }

        // Should throw exception if none of the expected strings are found
        @Test
        void expectNextStringsNotFound() {
            var in = makeTestMessageInput("SPRT/3.5");
            assertThrows(ValidationException.class, () -> Parsing.expectNextStrings(in, "SPRT/1.0", "SPRT/1.1"));
        }

        @Test
        void expectNextStringsEof() {
            var in = makeTestMessageInput("x");
            assertThrows(EOFException.class, () -> Parsing.expectNextStrings(in, "foo", "bar"));
        }


        @Test
        void expectNextStringsWithDifferentSizesNotFound() {
            var in = makeTestMessageInput("Hello there");
            assertThrows(ValidationException.class, () -> Parsing.expectNextStrings(in, "oof", "This is a really long string", "billy mays"));
        }
    }

    @Nested
    class ExpectNextString {
        @Test
        void expectNextStringOk() throws ValidationException, IOException {
            var in = makeTestMessageInput("SPRT/1.0 Q");
            Parsing.expectNextString(in, "SPRT/1.0");
        }

        // Should throw exception if expecting a newline and not finding one
        @Test
        void expectNewlineError() {
            var in = makeTestMessageInput("\rfoo=1\r\nbar=2\r\n\r\n");
            assertThrows(ValidationException.class, () -> Parsing.expectNextString(in, "\r\n"));
        }

        @Test
        void expectNextStringEof() {
            var in = makeTestMessageInput("yeet");
            assertThrows(EOFException.class, () -> Parsing.expectNextString(in, "Hello there"));
        }


    }




    @Test
    void isToken() {
        assertTrue(Parsing.isToken("hello"));
    }

    @Test
    void isTokenWhitespace() {
        assertFalse(Parsing.isToken("hello world"));
    }

    @Test
    void isTokenNonAscii() {
        assertFalse(Parsing.isToken("\u0389")); // unicode capital Greek omega
    }

}
