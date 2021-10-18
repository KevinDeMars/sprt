/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Program 1
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization;

import java.io.IOException;
import java.util.Objects;

import static sprt.serialization.Util.checkNull;

/** Represents a response and provides serialization/deserialization */
public class Response extends Message {
    /** The type of this response (e.g. OK, ERROR) */
    private Status status;
    /** Message provided to the client */
    private String message;
    /** What to set the function to if there is no next function (e.g. app decides to exit) */
    public static final String NO_NEXT_FUNCTION = "NULL";

    @Override
    protected MessageType getType() {
        return MessageType.Response;
    }

    /**
     * Constructs response using given values
     * @param status response status
     * @param function response function
     * @param message response message
     * @param cookies response cookie list
     * @throws ValidationException if error with given values
     * @throws NullPointerException if null parameter
     */
    public Response(Status status, String function, String message, CookieList cookies)
            throws ValidationException
    {
        super(function, cookies);
        setStatus(status);
        setMessage(message);
    }

    /**
     * Constructs response using given values
     * @param status response status
     * @param function response function
     * @param message response message
     * @throws ValidationException if error with given values
     * @throws NullPointerException if null parameter
     */
    public Response(Status status, String function, String message)
            throws ValidationException
    {
        this(status, function, message, new CookieList());
    }

    /**
     * Constructs response by deserializing from the given input.
     *
     * The "SPRT/1.0 R " portion of the message must already be consumed.
     * @param in partially-consumed input source
     * @throws ValidationException if error with given values
     * @throws IOException if I/O problem
     * @throws NullPointerException if in is null
     */
    Response(MessageInput in) throws ValidationException, IOException {
        checkNull(in, "in");

        String statusStr = in.nextToken();
        Status status;
        try {
            status = Status.valueOf(statusStr);
        }
        catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid status", statusStr, e);
        }
        setStatus(status);
        Parsing.expectNextString(in, " ");

        String function = in.nextToken();
        setFunction(function); // & validate
        Parsing.expectNextString(in, " ");

        String message = in.readToDelimiter(NEWLINE);
        setMessage(message); // & validate
        Parsing.expectNextString(in, NEWLINE);

        var cookies = new CookieList(in);
        setCookieList(cookies);
    }

    @Override
    protected void doEncode(MessageOutput out) throws IOException {
        out.writeLine("R ", status.toString(), " ", getFunction(), " ", message);
    }

    /**
     * Returns the status
     * @return status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets new status
     * @param status new status
     * @return this Response with new status
     * @throws NullPointerException if null status
     */
    public Response setStatus(Status status) {
        this.status = checkNull(status, "status");
        return this;
    }

    /**
     * Returns the message
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets new message
     * @param message new message
     * @return this Response with new message
     * @throws ValidationException if invalid message
     * @throws NullPointerException if null message
     */
    public Response setMessage(String message) throws ValidationException
    {
        checkNull(message, "message");
        if (message.chars().anyMatch(c -> c < 0x20 || c > 0x7F))
            throw new ValidationException("message contains unprintable character", message);

        this.message = message;
        return this;
    }

    /**
     * Returns a String representation in a form like:
     * "RESPONSE: OK function Things went well Cookies=[a=1,b=2]"
     * @return String representation of response
     */
    @Override
    public String toString() {
        return "RESPONSE: " + status + " " + getFunction() + " " + message + " " + getCookieList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Response response = (Response) o;
        return status == response.status && Objects.equals(message, response.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), status, message);
    }
}
