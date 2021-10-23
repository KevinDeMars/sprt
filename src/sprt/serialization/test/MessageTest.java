/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Program 1
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization.test;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import shared.serialization.test.EqualsAndHashCodeTests;
import shared.serialization.test.reflection.CheckArgumentsTestFactory.Check;
import shared.serialization.test.reflection.CheckNullTestFactory;
import shared.serialization.test.reflection.CheckTokenTestFactory;
import shared.serialization.test.reflection.ConstructorInvocation;
import shared.serialization.test.reflection.MethodInvocation;
import sprt.serialization.*;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static sprt.serialization.test.MessageInputTest.makeTestMessageInput;

public class MessageTest {
    private final CookieList cookies = new CookieList(new MessageInput(new StringReader("foo=1\r\nbar=2\r\n\r\n")));
    private final Message msg = messages().findFirst().get();

    private final List<Message> equal = List.of(
            new Request("fn", new String[]{"p1", "p2"}, new CookieList().add("foo", "1").add("bar", "2")),
            new Request("fn", new String[]{"p1", "p2"}, new CookieList().add("bar", "2").add("foo", "1"))
    );

    private final List<Message> unequal = List.of(
            new Request("fn", new String[]{"p1"}, new CookieList().add("foo", "1").add("bar", "2")),
            new Request("notFn", new String[]{"p1", "p2"}, new CookieList().add("foo", "1").add("bar", "2")),
            new Request("fn", new String[]{"p1", "p2"}, new CookieList().add("foo", "2").add("bar", "1")),
            new Response(Status.OK, "fn", "msg", new CookieList()),
            new Response(Status.ERROR, "fn", "msg", new CookieList()),
            new Response(Status.OK, "notFn", "msg", new CookieList()),
            new Response(Status.OK, "fn", "notMsg", new CookieList()),
            new Response(Status.OK, "fn", "notMsg", new CookieList().add("foo", "1"))
    );

    public MessageTest() throws ValidationException, IOException {
    }

    @TestFactory
    Stream<DynamicNode> testEquals() {
        return EqualsAndHashCodeTests.testEquals(equal, unequal);
    }
    @TestFactory
    Stream<DynamicNode> testHashCode() {
        return EqualsAndHashCodeTests.testHashCode(equal, unequal);
    }

    @TestFactory
    Stream<DynamicNode> checkNull() throws NoSuchMethodException {
        var msg = messages().findFirst().get();
        var checks = Stream.of(
                Check.allParams(new ConstructorInvocation(Request.class.getConstructor(String.class, String[].class, CookieList.class), "fn", new String[0], new CookieList())),
                Check.allParams(new MethodInvocation(Message.class.getMethod("decode", MessageInput.class), msg, validRequestInputs().findFirst().get())),
                Check.allParams(new MethodInvocation(Message.class.getMethod("encode", MessageOutput.class), msg, new MessageOutput())),
                Check.allParams(new MethodInvocation(Message.class.getMethod("setFunction", String.class), msg, "fn")),
                Check.allParams(new MethodInvocation(Message.class.getMethod("setCookieList", CookieList.class), msg, new CookieList()))
        );
        var factory = new CheckNullTestFactory();
        return checks.map(factory::testCheckArguments);
    }

    @TestFactory
    Stream<DynamicNode> checkToken() throws NoSuchMethodException {
        var msg = messages().findFirst().get();
        var checks = Stream.of(
                Check.allOfType(String.class, new ConstructorInvocation(Request.class.getConstructor(String.class, String[].class, CookieList.class),
                        "fn", new String[] {"p1"}, new CookieList()
                )),
                Check.allParams(new MethodInvocation(Message.class.getMethod("setFunction", String.class),
                                msg, "fn"
                ))
        );
        var factory = new CheckTokenTestFactory();
        return checks.map(factory::testCheckArguments);
    }

    // Test that empty/invalid params in constructor throws ValidationException
    @Nested
    class Constructor {
        @Test
        void functionEmpty() {
            assertThrows(ValidationException.class,
                    () -> new Request("", new String[0], new CookieList())
            );
        }
        @Test
        void functionNonToken() {
            assertThrows(ValidationException.class,
                    () -> new Request("hello there", new String[0], msg.getCookieList())
            );
        }
    }

    @Nested
    class Decode {
        // Tests that valid request inputs deserialize successfully to Request objects
        @ParameterizedTest
        @MethodSource("sprt.serialization.test.MessageTest#validRequestInputs")
        void requestInstanceof(MessageInput in) throws ValidationException, IOException {
            var msg = Message.decode(in);
            assertNotNull(msg);
            assertTrue(msg instanceof Request, "Should be Request; got " + msg.getClass().getName());
        }
        // Tests that valid response inputs deserialize successfully to Response objects
        @ParameterizedTest
        @MethodSource("sprt.serialization.test.MessageTest#validResponseInputs")
        void responseInstanceOf(MessageInput in) throws ValidationException, IOException {
            var msg = Message.decode(in);
            assertNotNull(msg);
            assertTrue(msg instanceof Response, "Should be Response; got " + msg.getClass().getName());
        }
        // Tests that invalid inputs cause a VE
        @ParameterizedTest
        @MethodSource({"sprt.serialization.test.MessageTest#invalidRequestInputs", "sprt.serialization.test.MessageTest#invalidResponseInputs"})
        void invalidMessages(MessageInput in) {
            assertThrows(ValidationException.class, () -> Message.decode(in));
        }
        // Tests that if any token is missing in the input, a EOFException is thrown
        @ParameterizedTest
        @MethodSource({"sprt.serialization.test.MessageTest#validRequestInputs", "sprt.serialization.test.MessageTest#validResponseInputs"})
        void prematureEndOfStream(MessageInput in) throws IOException, ValidationException {
            String newData = "";
            while (in.hasNextToken()) {
                newData += in.nextToken();
                newData += in.readUntil(Util::isAlnum);
                if (in.hasNextToken()) {
                    var newIn = makeTestMessageInput(newData);
                    assertThrows(EOFException.class, () -> Message.decode(newIn));
                }
            }
        }
        // Tests that if the wrong line ending is used, a ValidationException is thrown
        @ParameterizedTest
        @MethodSource({"sprt.serialization.test.MessageTest#validRequestInputs", "sprt.serialization.test.MessageTest#validResponseInputs"})
        void wrongLineEnding(MessageInput in) throws IOException {
            var str = in.readAll();
            var str2 = str.replace("\r\n", "\n");
            var in2 = makeTestMessageInput(str2);
            assertThrows(ValidationException.class, () -> Message.decode(in2));
        }
        // Tests that two Messages can be deserialized from a single stream
        @ParameterizedTest
        @MethodSource({"sprt.serialization.test.MessageTest#validResponseInputs", "sprt.serialization.test.MessageTest#validRequestInputs"})
        void twoInOneStream(MessageInput in) throws IOException, ValidationException {
            var str = in.readAll();
            str += str;
            var in2 = makeTestMessageInput(str);
            var msg1 = Message.decode(in2);
            var msg2 = Message.decode(in2);
            assertNotNull(msg1);
            assertEquals(msg1, msg2);
        }
    }

    @Nested
    class Getters {
        // Tests that mutating the result of a getter doesn't mutate it within the Message
        @Test
        void cookieListEncapsulation() throws ValidationException {
            CookieList c2 = msg.getCookieList();
            c2.add("mycoolcookie", "123");
            assertNotEquals(c2, msg.getCookieList());
        }
    }

    // Tests that setters with invalid values throw a VE
    @Nested
    class Setters {
        @Test
        void setFunctionNonToken() {
            assertThrows(ValidationException.class, () -> msg.setFunction("hello there"));
        }
        @Test
        void setFunctionEmpty() {
            assertThrows(ValidationException.class, () -> msg.setFunction(""));
        }
    }

    // Tests that serializing/deserializing a message results in the original message
    @ParameterizedTest
    @MethodSource("messages")
    void encodedEqualsOriginal(Message msg) throws IOException, ValidationException {
        var s = new ByteArrayOutputStream();
        var out = new MessageOutput(s);
        msg.encode(out);
        // Decoding the encoded message should get the original message
        var in = new MessageInput(new ByteArrayInputStream(s.toByteArray()));
        var decodedMsg = Message.decode(in);
        assertEquals(msg, decodedMsg);
    }

    // Gets a stream of MessageInputs that represent valid Requests
    static Stream<MessageInput> validRequestInputs() {
        return Stream.of(
                "SPRT/1.0 Q RUN Fn\r\n\r\n",
                "SPRT/1.0 Q RUN Fn param1\r\nfoo=1\r\n\r\n",
                "SPRT/1.0 Q RUN Fn 1 2 3\r\n1=1\r\n2=3\r\n\r\n"
        )
        .map(MessageInputTest::makeTestMessageInput);
    }

    // Gets a stream of MessageInputs that should cause a ValidationException
    // when decoded
    static Stream<MessageInput> invalidRequestInputs() {
        return Stream.of(
                "SPRT/3.1 Q RUN Fn\r\n\r\n",
                " Q RUN Fn\r\n\r\n",
                "SPRT/1.0 Z RUN Fn\r\n\r\n",
                "SPRT/1.0 Q OOF Fn\r\n\r\n",
                "SPRT/1.0 Q RUN not_a_token\r\n\r\n",
                "SPRT/1.0 R RUN Fn\r\n\r\n",
                "SPRT/1.0 Q RUN Fn not_a_token\r\nfoo=1\r\n\r\n",
                "SPRT/1.0 Q RUN Fn \r\nfoo=1\r\n\r\n",
                "SPRT/1.0 Q RUN Fn param1 \r\nfoo=1\r\n\r\n"
        )
        .map(MessageInputTest::makeTestMessageInput);
    }

    // Gets a stream of MessageInputs that represent valid Requests
    static Stream<MessageInput> validResponseInputs() {
        return Stream.of(
                "SPRT/1.0 R OK Fn This is a message.\r\n\r\n",
                "SPRT/1.0 R OK Fn \r\n\r\n",
                "SPRT/1.0 R OK Fn Message\r\nfoo=1\r\nbar=2\r\n\r\n",
                "SPRT/1.0 R OK Fn \r\nfoo=1\r\nbar=2\r\n\r\n",
                "SPRT/1.0 R ERROR Fn You forgot the thing\r\n\r\n",
                "SPRT/1.0 R ERROR Fn \r\n\r\n"
        )
        .map(MessageInputTest::makeTestMessageInput);
    }

    // Gets a stream of MessageInputs that should cause a ValidationException
    // when decoded
    static Stream<MessageInput> invalidResponseInputs() {
        return Stream.of(
                "SPRT/1.0 R OK Fn\r\n\r\n", // needs space even if no message
                "SPRT/3.1 R OK Fn Message\r\n\r\n",
                "R OK Fn Message\r\n\r\n",
                "SPRT/1.0 OK Fn Message\r\n\r\n",
                "SPRT/1.0 R YEET Fn Message\r\n\r\n",
                "SPRT/1.0 Z OK Fn Message\r\n\r\n",
                "SPRT/1.0 R OK not_a_token Message\r\n\r\n",
                "SPRT/1.0 R OK Fn unprintable \007 chars\r\n\r\n"
        )
        .map(MessageInputTest::makeTestMessageInput);
    }

    static Stream<Message> messages() {
        return Stream.concat(
                validRequestInputs().map(MessageTest::decodeNoException),
                validResponseInputs().map(MessageTest::decodeNoException)
        );
    }

    // Checked exceptions don't play nice with the stream interface, so this
    // converts them to unchecked instead
    static Message decodeNoException(MessageInput in) {
        try {
            return Message.decode(in);
        } catch (ValidationException | IOException e) {
            throw new RuntimeException("Couldn't create message for testing", e);
        }
    }
}
