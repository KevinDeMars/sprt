/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Program 1
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static sprt.serialization.Util.checkNull;

/**
 * Represents generic portion of a message and provides serialization/deserialization.
 */
public abstract class Message {
    /** The end-of-line sequence in the SPRT protocol. */
    public static final String NEWLINE = "\r\n";
    /** The charset used in the SPRT protocol. */
    public static final Charset SPRT_CHARSET = StandardCharsets.US_ASCII;
    /** All SPRT 1.0 messages start with this */
    public static final String SPRT_VERSION_HEADER = "SPRT/1.0 ";

    /** The name of the function to run, e.g. "Poll" */
    private String function;
    /** List of cookies (key/val pairs) associated with this request */
    private CookieList cookieList;
    /** The type of message (e.g. MessageType.Request) */
    protected abstract MessageType getType();

    /**
     * Creates message using given values
     * @param function function
     * @param cookieList cookie list
     * @throws ValidationException if error with given values
     * @throws NullPointerException if null parameter
     */
    public Message(String function, CookieList cookieList)
            throws ValidationException
    {
        checkNull(function, "function");
        if (!Parsing.isToken(function))
            throw new ValidationException("Invalid token", function);
        setFunction(function);
        setCookieList(cookieList);
    }

    /**
     * Creates a Message in an invalid, uninitialized state.
     */
    protected Message() {

    }

    // Consumes header and gets the message type. Throws ValidationException for invalid message type.
    protected static MessageType consumeHeader(MessageInput in) throws ValidationException, IOException {
        checkNull(in, "in");
        Parsing.expectNextString(in, SPRT_VERSION_HEADER);
        String typeTok = in.nextToken();
        Parsing.expectNextString(in, " ");
        return MessageType.fromToken(typeTok);
    }

    /**
     * Creates a new message by deserializing from the given input
     * according to the specified serialization.
     * @param in user input source
     * @return new message
     * @throws ValidationException if validation fails
     * @throws IOException if I/O problem
     * @throws NullPointerException if in is null
     */
    public static Message decode(MessageInput in)
            throws ValidationException, IOException
    {
        return decodeType(in, null);
    }

    /**
     * Deserializes message and also checks that the received message is of the given type.
     * If the expected type is null, any type is accepted.
     * @param in user input source
     * @param expectedType Type that should be received
     * @return new message
     * @throws ValidationException if wrong message type or validation fails
     * @throws IOException if I/O problem
     * @throws NullPointerException if in is null
     */
    public static Message decodeType(MessageInput in, MessageType expectedType) throws ValidationException, IOException {
        MessageType type = consumeHeader(in);
        if (expectedType != null && !type.equals(expectedType)) {
            throw new ValidationException("Expected type " + expectedType + ", got " + type, type.token);
        }
        return switch (type) {
            case Response -> new Response(in);
            case Request -> new Request(in);
        };
    }

    /**
     * Encode the entire message
     * @param out serialization output sink
     * @throws IOException if I/O problem
     * @throws NullPointerException if out is null
     */
    public void encode(MessageOutput out)
            throws IOException
    {
        checkNull(out, "out");
        out.write(SPRT_VERSION_HEADER);
        doEncode(out);
        cookieList.encode(out);
    }

    protected abstract void doEncode(MessageOutput out) throws IOException;

    /**
     * Gets the function
     * @return function
     */
    public String getFunction() {
        return function;
    }

    /**
     * Sets the function
     * @param function new function
     * @return this message with new function
     * @throws NullPointerException if null function
     * @throws ValidationException if invalid function
     */
    public Message setFunction(String function)
            throws ValidationException
    {
        checkNull(function, "function");
        if (!Parsing.isToken(function))
            throw new ValidationException("Invalid token", function);
        this.function = function;
        return this;
    }

    /**
     * Gets the cookie list
     * @return cookie list
     */
    public CookieList getCookieList() {
        return new CookieList(cookieList);
    }

    /**
     * Sets the cookie list
     * @param cookieList new cookie list
     * @return this Message with new cookie list
     * @throws NullPointerException if null cookie list
     */
    public Message setCookieList(CookieList cookieList) {
        checkNull(cookieList, "cookieList");
        this.cookieList = new CookieList(cookieList);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return function.equals(message.function) && cookieList.equals(message.cookieList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(function, cookieList);
    }
}
