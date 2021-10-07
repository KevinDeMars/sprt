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
import sprt.serialization.CookieList;
import sprt.serialization.MessageInput;
import sprt.serialization.Request;
import sprt.serialization.ValidationException;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class RequestTest {
    private final CookieList cookies = new CookieList(new MessageInput(new StringReader("foo=1\r\nbar=2\r\n\r\n")));
    private final Request request = new Request("function", new String[] {"p1", "p2"},  cookies);

    public RequestTest() throws ValidationException, IOException {

    }

    @Nested
    class Constructor {
        @Test
        void emptyParam() {
            assertThrows(ValidationException.class, () -> new Request("fn", new String[]{""}, cookies));
        }
        @Test
        void nonTokenParam() {
            assertThrows(ValidationException.class, () -> new Request("fn", new String[]{"hi mom"}, cookies));
        }
    }

    @Nested
    class NullParams {
        @Test
        void nullConstructorParams() {
            assertThrows(NullPointerException.class, () -> new Request("function", null, cookies));
        }
        @Test
        void nullSetParams() {
            assertThrows(NullPointerException.class, () -> request.setParams(null));
        }
        @Test
        void nullElementConstructorParams() {
            assertThrows(NullPointerException.class, () -> new Request("function", new String[] {null}, cookies));
        }
        @Test
        void nullElementSetParams() {
            assertThrows(NullPointerException.class, () -> request.setParams(new String[]{null}));
        }
    }

    @Nested
    class Getters {
        @Test
        void paramsEncapsulation() {
            var params = request.getParams();
            params[0] = "aaaaaa";
            assertNotEquals(params[0], request.getParams()[0]);
        }
    }

    @Nested
    class Setters {
        @Test
        void setParamNonToken() {
            assertThrows(ValidationException.class, () -> request.setParams(new String[]{"hello there"}));
        }
        @Test
        void setParamEmpty() {
            assertThrows(ValidationException.class, () -> request.setParams(new String[]{""}));
        }
    }

    @Test
    void testToString() {
        assertEquals("REQUEST: GO function p1 p2 Cookies=[bar=2,foo=1]", request.toString());
    }
}
