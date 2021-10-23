/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Prog0
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization.test;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import shared.serialization.test.EqualsAndHashCodeTests;
import shared.serialization.test.reflection.CheckArgumentsTestFactory.Check;
import shared.serialization.test.reflection.CheckNullTestFactory;
import shared.serialization.test.reflection.CheckTokenTestFactory;
import shared.serialization.test.reflection.Invocation;
import shared.serialization.test.reflection.MethodInvocation;
import sprt.serialization.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static shared.serialization.test.reflection.TestAssertions.assertCollectionContains;
import static sprt.serialization.test.MessageInputTest.makeTestMessageInput;

class CookieListTest {
    private final Collection<CookieList> equalLists = List.of(
            new CookieList().add("foo", "1").add("bar", "2"),
            new CookieList().add("foo", "1").add("bar", "2"),
            new CookieList().add("bar", "2").add("foo", "1")
    );
    private final Collection<CookieList> unequalLists = List.of(
            new CookieList(),
            new CookieList().add("foo", "2").add("bar", "1"),
            new CookieList().add("foo", "1")
    );


    CookieListTest() throws ValidationException {
    }

    @TestFactory
    Stream<DynamicNode> testEquals() {
        return EqualsAndHashCodeTests.testEquals(equalLists, unequalLists);
    }
    @TestFactory
    Stream<DynamicNode> testHashCode() {
        return EqualsAndHashCodeTests.testHashCode(equalLists, unequalLists);
    }

    @TestFactory
    Stream<DynamicNode> testNull() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var checks = Invocation.allMethodsDefaultParams(CookieList.class)
                .stream()
                .map(Check::allParams);
        var factory = new CheckNullTestFactory();
        return checks.map(factory::testCheckArguments);
    }

    @TestFactory
    Stream<DynamicNode> testToken() throws ValidationException, NoSuchMethodException {
        var cl = new CookieList().add("foo", "1");
        var checks = Stream.of(
                Check.allParams(new MethodInvocation(
                        CookieList.class.getMethod("add", String.class, String.class),
                        cl, "myName", "myVal"
                ))
        );
        var factory = new CheckTokenTestFactory();
        return checks.map(factory::testCheckArguments);
    }

    /**
     * Makes a new CookieList containing cookies setting "foo" to "1" and "bar" to "2", for testing.
     * @return The new CookieList.
     */
    public static CookieList fooBarList() {
        try {
            return new CookieList(makeTestMessageInput("foo=1\r\nbar=2\r\n\r\n"));
        }
        catch (ValidationException | IOException e) {
            // Fail the test
            throw new RuntimeException("Couldn't make testing cookie list.", e);
        }
    }

    /**
     * Asserts that a valid CookieList can NOT be read from the current position of the given MessageInput.
     * A validation exception should be thrown.
     * @param in The assumed-invalid message input.
     */
    static void assertInvalid(MessageInput in) {
        assertThrows(ValidationException.class, () -> new CookieList(in));
    }

    /**
     * Asserts that a valid CookieList can be read from the current position of the given MessageInput.
     * @param in the assumed-valid message input.
     * @throws ValidationException if the assertion fails with a protocol or syntax error
     * @throws IOException if the assertion fails due to a problem reading from in.
     */
    static void assertValid(MessageInput in) throws ValidationException, IOException {
        new CookieList(in);
    }

    @Nested
    class Parsing {
        // Should successfully create a CookieList from a valid MessageInput containing cookies
        @Test
        void parseOk() throws ValidationException, IOException {
            var in = makeTestMessageInput("x=1\r\ny=2\r\n\r\n");
            assertValid(in);
        }

        @Test
        void parseByteArray() throws ValidationException, IOException {
            // ascii for x=1\r\ny=2\r\n\r\n
            var bytes = new byte[] {120, 61, 49, 13, 10, 121, 61, 50, 13, 10, 13, 10};
            var in = new MessageInput(new ByteArrayInputStream(bytes));
            assertValid(in);
        }

        // Should fail when creating a CookieList from an empty MessageInput
        @Test
        void parseEmpty() {
            var in = makeTestMessageInput("");
            assertThrows(EOFException.class, () -> new CookieList(in));
        }

        // Should succeed to create a CookieList with no cookies
        @Test
        void parseNoCookies() throws ValidationException, IOException {
            var in = makeTestMessageInput(Message.NEWLINE);
            assertValid(in);
        }

        // Should fail to create a CookieList with non-ascii chars
        @Test
        void parseContainsNonAscii() {
            var in = makeTestMessageInput("foo=piñata\r\nbar=Hola, ¿Cómo estás?\r\n\r\n");
            assertInvalid(in);
        }

        // Should fail to create a CookieList with a cookie containing a CRLF
        @Test
        void parseContainsCRLF() {
            var in = makeTestMessageInput("foo=This is a \r\nreally long\r\nmulti-line value.\r\nbar=1\r\n\r\n");
            assertInvalid(in);
        }

        // Should fail if MessageInput uses \n instead of \r\n
        @Test
        void parseWrongLineEnding() {
            var in = makeTestMessageInput("foo=1\nbar=2\n\n");
            assertInvalid(in);
        }

        // Should fail if a cookie has no name
        @Test
        void parseNoName() {
            var in = makeTestMessageInput("foo=1\r\n=2\r\n\r\n");
            assertInvalid(in);
        }

        // Should fail if a cookie has no value
        @Test
        void parseNoValue() {
            var in = makeTestMessageInput("foo=1\r\nbar=\r\n\r\n");
            assertInvalid(in);
        }

        // Should fail if the cookie's key/val are not separated by "="
        @Test
        void parseNoEquals() {
            var in = makeTestMessageInput("foo=1\r\nbar 2\r\n\r\n");
            assertInvalid(in);
        }

        // Should fail if there is whitespace around the cookie's key and val
        @Test
        void parseWithSpaceAroundEquals() {
            var in = makeTestMessageInput("foo = 1\r\nbar = 2\r\n\r\n");
            assertInvalid(in);
        }

        @Test
        void parseEarlyEOF() {
            String data = "foo=1\r\nbar=2\r\n\r\n";
            for (int i = 0; i < data.length() - 1; ++i) {
                var data2 = data.substring(0, i);
                var badIn = makeTestMessageInput(data2);
                assertThrows(EOFException.class, () -> new CookieList(badIn));
            }
        }

        @Test
        void parseNoEndingCRLF() {
            var in = makeTestMessageInput("foo=1\r\nbar=2\r\n");
            assertThrows(EOFException.class, () -> new CookieList(in));
        }

        @Test
        void twoCookieLists() throws ValidationException, IOException {
            String data = "foo=1\r\nbar=2\r\n\r\n";
            data += data;
            var in = makeTestMessageInput(data);
            var c1 = new CookieList(in);
            var c2 = new CookieList(in);
            assertEquals(c1, c2);
        }
    }

    @Nested
    class Getters {
        // list.getNames() should return every cookie from the MessageInput and
        // from manually adding, and only those cookies.
        @Test
        void getNamesHasAllNames() throws ValidationException {
            var list = fooBarList();
            list.add("baz", "3");
            assertCollectionContains(list.getNames(), Set.of("foo", "bar", "baz"));
            assertEquals(3, list.getNames().size());
        }

        // list.getValue() should return the same value that was added by
        // messageInput or by .add()
        @Test
        void getValueFindsCorrectValues() throws ValidationException {
            var list = fooBarList();
            list.add("baz", "3");
            assertEquals("1", list.getValue("foo"));
            assertEquals("2", list.getValue("bar"));
            assertEquals("3", list.getValue("baz"));
        }

        // list.getValue() should return null if the cookie name was not found
        @Test
        void getValueNotFound() {
            assertNull(fooBarList().getValue("aaaaa"));
        }

        // toString should return all cookies in alphabetical order, and their corresponding values
        @Test
        void testToString() {
            assertEquals("Cookies=[bar=2,foo=1]", fooBarList().toString());
        }

        // toString should return this specific string if there are no cookies.
        @Test
        void testToStringEmpty() {
            assertEquals("Cookies=[]", new CookieList().toString());
        }
    }

    @Nested
    class Mutators {
        // Should successfully add a new cookie with valid name/val
        @Test
        void addNewCookie() throws ValidationException {
            var list = fooBarList();
            list.add("baz", "1337");
            assertCollectionContains(list.getNames(), Set.of("foo", "bar", "baz"));
            assertEquals(3, list.getNames().size());
        }

        // Should successfully replace a cookie if adding a cookie with an existing name
        @Test
        void addReplaceCookie() throws ValidationException {
            var list = fooBarList();
            var oldVal = list.getValue("foo");
            list.add("foo", "1337");
            var newVal = list.getValue("foo");
            assertNotEquals(oldVal, newVal);
            assertCollectionContains(list.getNames(), Set.of("foo", "bar"));
            assertEquals(2, list.getNames().size());
        }

        // Should fail to add a cookie with a non-alnum name or value
        @Test
        void addNonAlphanumeric() {
            var list = fooBarList();
            assertThrows(ValidationException.class, () -> list.add("my_cookie", "1337"));
            assertThrows(ValidationException.class, () -> list.add("myCookie", "hello_world"));
        }

    }

    @Nested
    class OtherMethods {
        @Test
        void encode() throws IOException {
            var stream = new ByteArrayOutputStream();
            var out = new MessageOutput(stream);
            var list = fooBarList();
            list.encode(out);
            assertEquals("bar=2\r\nfoo=1\r\n\r\n", stream.toString(StandardCharsets.US_ASCII));
        }
    }
}