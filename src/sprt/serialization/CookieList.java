/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Prog0
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static sprt.serialization.Message.NEWLINE;
import static sprt.serialization.Util.checkNull;

/** Set of cookies (name/value pairs) */
public class CookieList {
    /** key/value pairs contained by this list */
    private final Map<String, String> cookies;

    /**
     * Creates a new, empty cookie list
     */
    public CookieList() {
        cookies = new HashMap<>();
    }

    /**
     * Creates a new CookieList by deserializing from the given input according to the specified serialization.
     * @param in input stream from which to deserialize the name/value list
     * @throws ValidationException if validation problem such as illegal name and/or value, etc.
     * @throws IOException if I/O problem (EOFException for EoS)
     * @throws NullPointerException if input stream is null
     */
    public CookieList(MessageInput in) throws ValidationException, IOException {
        this();
        checkNull(in, "in");

        while (in.hasNextToken()) {
            String key = in.nextToken();
            Parsing.expectNextString(in, "=");
            String val = in.nextToken();
            add(key, val);
            Parsing.expectNextString(in, NEWLINE);
        }
        Parsing.expectNextString(in, NEWLINE);
    }

    /**
     * Creates a copy of an existing CookieList
     * @param other list to copy
     */
    public CookieList(CookieList other) {
        cookies = new HashMap<>(other.cookies);
    }

    /**
     * Encode the name-value list according to the specified serialization to the given output.
     * The name-value pair serialization must be in sort order (alphabetically by name in increasing order).
     * For example, a=1 b=2 ...
     * @param out serialization output sink
     * @throws IOException if I/O problem
     * @throws NullPointerException if out is null
     */
    public void encode(MessageOutput out) throws IOException {
        checkNull(out, "out");
        // Lambdas don't let you use functions that throw exceptions, so I can't use .forEach.
        // So I have to waste time+space with .toList(). Suggestions are appreciated.
        var ascendingCookies = cookies.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());
        for (var c : ascendingCookies) {
            writeCookie(out, c);
        }
        out.write(NEWLINE);
        out.flush();
    }

    protected void writeCookie(MessageOutput out, Map.Entry<String, String> entry) throws IOException {
        out.writeLine(entry.getKey(), "=", entry.getValue());
    }

    /**
     * Adds the new name/value pair to the list of cookies.
     * If the name already exists, the new value replaces the old value
     * @param name name to be added
     * @param value value to be associated with the name
     * @return this CookieList with new name/value pair
     * @throws ValidationException if validation failure for name or value
     * @throws NullPointerException if name or value is null
     */
    public CookieList add(String name, String value) throws ValidationException {
        checkNull(name, "name");
        checkNull(value, "value");
        if (!Parsing.isToken(name))
            throw new ValidationException("name must be a token", name);
        if (!Parsing.isToken(value))
            throw new ValidationException("value must be a token", value);
        cookies.put(name, value);
        return this;
    }

    /**
     * Adds all cookies from another cookie list
     * @param other other cookie list
     * @return this CookieList with new cookies
     * @throws NullPointerException if other is null
     */
    public CookieList addAll(CookieList other) {
        checkNull(other, "other");
        try {
            for (var key : other.getNames())
                this.add(key, other.getValue(key));
        }
        catch (ValidationException e) {
            throw new IllegalStateException("Current cookie list is invalid", e);
        }
        return this;
    }

    /**
     * Gets the set of names
     * @return Set (potentially empty) of names (strings) for this list
     */
    public Set<String> getNames() {
        return Set.copyOf(cookies.keySet());
    }

    /**
     * Gets the value associated with the given name
     * @param name cookie name
     * @return Value associated with the given name or null if no such name
     * @throws NullPointerException if name is null
     */
    public String getValue(String name) {
        checkNull(name, "name");
        return cookies.get(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CookieList that = (CookieList) o;
        return cookies.equals(that.cookies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cookies);
    }

    /**
     * Returns string representation of cookie list.
     * The name-value pair serialization must be in sort order (alphabetically by name in increasing order).
     * For example, Cookies=[a=1,b=2]
     * Note: Space (or lack thereof) is important.
     * @return string representation of cookie list
     */
    @Override
    public String toString() {
        return "Cookies=[" + cookies.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","))
            + "]";
    }
}
