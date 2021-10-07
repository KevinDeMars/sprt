/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Prog0
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization.test;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sprt.serialization.MessageInput;
import sprt.serialization.ValidationException;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;


public class MessageInputTest {
    public static MessageInput makeTestMessageInput(String inputStr) {
        return new MessageInput(new ByteArrayInputStream(inputStr.getBytes(StandardCharsets.US_ASCII)));
    }

    @Nested
    class HasNext {
        @Test
        void hasNextTokenOk() {
            var in = makeTestMessageInput("Hello there");
            assertTrue(in.hasNextToken());
        }

        @Test
        void hasNextTokenInvalid() {
            var in = makeTestMessageInput("/1.0");
            assertFalse(in.hasNextToken());
        }

        @Test
        void hasNextTokenEmpty() {
            var in = makeTestMessageInput("");
            assertFalse(in.hasNextToken());
        }

        @Test
        void hasNextStringOk() {
            var in = makeTestMessageInput("Hello there");
            assertTrue(in.nextStringMatches("Hello there"));
        }

        @Test
        void hasNextStringPartialMatch() {
            var in = makeTestMessageInput("Hello ");
            assertFalse(in.nextStringMatches("Hello world"));
        }

        @Test
        void hasNextStringEmpty() {
            var in = makeTestMessageInput("");
            assertFalse(in.nextStringMatches("Hello"));
        }

    }

    @Nested
    class Reading {
        @Test
        void readWhile() throws IOException {
            var in = makeTestMessageInput("abcdefg");
            assertEquals("abcd", in.readWhile(c -> c <= 'd'));
        }

        @Test
        void readWhileEmpty() throws IOException {
            var in = makeTestMessageInput("abcdefg");
            assertEquals("", in.readWhile(c -> false));
        }

        @Test
        void readToDelimiter() throws IOException {
            var in = makeTestMessageInput("hello this is a line\nline2");
            assertEquals("hello this is a line", in.readToDelimiter('\n'));
        }

        @Test
        void readToDelimiterNoDelimiter() throws IOException {
            var in = makeTestMessageInput("hello there");
            assertEquals("hello there", in.readToDelimiter('\n'));
        }

        // nextToken should return the appropriate token regardless of whether it's the
        // beginning of the message input
        @Test
        void nextTokenMultiple() throws ValidationException, IOException {
            var in = makeTestMessageInput("foo=1\r\nbar=2\r\n\r\n");
            assertEquals("foo", in.nextToken());
            in.skip(1); // skip "="
            assertEquals("1", in.nextToken());
        }

        // nextToken should stop at any non-alnum character
        @Test
        void readTokenNonAlphanumeric() throws ValidationException, IOException {
            var in = makeTestMessageInput("my_var=1\r\n\r\n");
            assertEquals("my", in.nextToken());
        }

        // nextToken should throw an exception if the next input
        // is not a valid token
        @Test
        void cantReadTokenSpecialChar() {
            var in = makeTestMessageInput("/1.0");
            assertThrows(ValidationException.class, in::nextToken);
        }

        // nextToken should throw an exception if there is no more input
        @Test
        void cantReadTokenEmpty() {
            var in = makeTestMessageInput("");
            assertThrows(EOFException.class, in::nextToken);
        }
    }


}
