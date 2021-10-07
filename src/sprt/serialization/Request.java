/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Program 1
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static sprt.serialization.Util.checkNull;
import static sprt.serialization.Util.deepCheckNull;

/** Represents a request and provides serialization/deserialization */
public class Request extends Message {
    /** Values for the parameters associated with the function for this message */
    private String[] params;

    @Override
    protected MessageType getType() {
        return MessageType.Request;
    }

    /**
     * Creates a request with the given data.
     * @param function name of function to run
     * @param params parameters for function
     * @param cookieList cookies for request
     * @throws ValidationException if params contains a non-token
     */
    public Request(String function, String[] params, CookieList cookieList)
            throws ValidationException
    {
        super(function, cookieList);
        setParams(params);
    }

    /**
     * Creates a request by deserializing the portion of an input stream AFTER the "SPRT/1.0 Q " header.
     * @param in partially-consumed input
     * @throws ValidationException if a value is invalid
     * @throws IOException if I/O error occurs
     */
    Request(MessageInput in) throws ValidationException, IOException {
        checkNull(in, "in");

        String command = in.nextToken();
        if (!"RUN".equals(command))
            throw new ValidationException("Invalid command", command);
        Parsing.expectNextString(in, " ");
        String function = in.nextToken();
        var params = new ArrayList<String>();
        while (in.nextStringMatches(" ")) {
            in.skip(1);
            params.add(in.nextToken());
        }
        Parsing.expectNextString(in, NEWLINE);
        var cookies = new CookieList(in);

        setFunction(function);
        setParams(params.toArray(new String[0]));
        setCookieList(cookies);
    }

    @Override
    protected void doEncode(MessageOutput out) throws IOException {
        out.write("Q RUN ", getFunction());
        for (String param : params) {
            out.write(" ", param);
        }
        out.write(NEWLINE);
    }

    /**
     * Returns the parameter list
     * @return parameter list
     */
    public String[] getParams() {
        return Arrays.copyOf(params, params.length);
    }

    /**
     * Sets the parameter list
     * @param params new parameters
     * @return this Request with new parameters
     * @throws ValidationException if invalid params
     * @throws NullPointerException if null array or array element
     */
    public Request setParams(String[] params) throws ValidationException {
        deepCheckNull(params, "params");
        for (String p : params) {
            if (!Parsing.isToken(p)) {
                throw new ValidationException("Invalid token", p);
            }
        }
        this.params = Arrays.copyOf(params, params.length);
        return this;
    }

    /**
     * Returns a String representation a form like:
     * "REQUEST: GO p1 p2 Cookies=[a=1,b=2]"
     * @return String representation of the request
     */
    @Override
    public String toString() {
        return "REQUEST: GO "
                + getFunction() + " "
                + String.join(" ", params)  + " "
                + getCookieList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Request request = (Request) o;
        return Arrays.equals(params, request.params);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(params);
        return result;
    }
}
