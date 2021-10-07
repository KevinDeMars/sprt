/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Program 1
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization.test;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sprt.serialization.*;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ResponseTest {
    private final CookieList cookies = new CookieList(new MessageInput(new StringReader("foo=1\r\nbar=2\r\n\r\n")));
    private final Response response = new Response(Status.OK, "function", "msg",  cookies);

    public ResponseTest() throws ValidationException, IOException {
    }

    @Nested
    class NullParams {
        @Test
        void nullConstructorMessage() {
            assertThrows(NullPointerException.class, () -> new Response(Status.OK, "fn", null, cookies));
        }
        @Test
        void nullSetStatus() {
            assertThrows(NullPointerException.class, () -> response.setStatus(null));
        }
        @Test
        void nullSetMessage() {
            assertThrows(NullPointerException.class, () -> response.setMessage(null));
        }
    }

    @Nested
    class Setters {
        @Test
        void setMessageUnprintable() {
            assertThrows(ValidationException.class, () -> response.setMessage("\007")); // bell character
        }

    }

    @Test
    void testToString() {
        assertEquals("RESPONSE: OK function msg Cookies=[bar=2,foo=1]", response.toString());
    }
}
