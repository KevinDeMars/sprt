/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Prog0
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization;

import java.io.*;
import java.nio.CharBuffer;

import static sprt.serialization.Util.checkNull;

/**
 * Deserialization input source for messages
 */
public class MessageInput {
    private final PeekableReader reader;

    /**
     * Creates an empty MessageInput.
     */
    public MessageInput() {
        reader = new PeekableReader(new ByteArrayInputStream(new byte[0]), Message.SPRT_CHARSET);
    }

    /**
     * Creates a MessageInput from the given stream of raw bytes, assumed to represent ASCII text.
     * @param in input stream
     * @throws NullPointerException if in is null
     */
    public MessageInput(InputStream in) throws NullPointerException {
        reader = new PeekableReader(checkNull(in, "in"), Message.SPRT_CHARSET);
    }

    /**
     * Creates a MessageInput from an existing reader.
     * @param in input reader
     * @throws NullPointerException if in is null
     */
    public MessageInput(Reader in) throws NullPointerException {
        reader = new PeekableReader(checkNull(in, "in"));
    }

    /**
     * Wrapper for {@link PeekableReader#read()}
     * @return Next character in stream
     * @throws IOException if I/O problem
     */
    public int read() throws IOException {
        return reader.read();
    }

    /**
     * Reads into a String as many characters as possible, up to n.
     * @param n max number of characters to read
     * @return a possibly-empty String containing at most n characters
     * @throws IOException if an I/O error occurs
     */
    public String readNChars(int n) throws IOException {
        return reader.readNChars(n);
    }

    /**
     * Reads exactly n characters into a String
     * @param n number of characters to read
     * @return a String containing exactly n characters.
     * @throws IOException if an I/O error occurs
     * @throws EOFException if less than n characters could be read.
     */
    public String readExactlyNChars(int n) throws IOException {
        return reader.readExactlyNChars(n);
    }

    /**
     * Gets at most the next n characters without advancing the stream position.
     * @param n maximum number of characters to read
     * @return a possibly-empty String containing at most n chars
     * @throws IOException if an I/O error occurs
     */
    public String peekNChars(int n) throws IOException {
        return reader.peekNChars(n);
    }

    /**
     * Gets exactly n characters without advancing the stream position.
     * @param n number of characters to read
     * @return a String containing exactly n chars
     * @throws IOException if an I/O error occurs
     * @throws EOFException if less than n characters are available
     */
    public String peekExactlyNChars(int n) throws IOException {
        return reader.peekExactlyNChars(n);
    }

    /**
     * Skips exactly n characters from the underlying stream.
     * @param n the number of characters to skip
     * @throws IOException if exactly n characters could not be skipped.
     */
    public void skip(int n) throws IOException {
        if (reader.skip(n) != n) {
            throw new IOException("Couldn't skip " + n + " characters in stream");
        }
    }

    /**
     * Reads into a String containing all characters up until the predicate tests false.
     * The stream position is advanced by the length of the returned String.
     * The character that tests false in the predicate is NOT consumed.
     * @param predicate a function that returns true if the character should be added to the result and
     *                  the stream should continue to be read; else, false.
     * @return a (possibly-empty) String containing all characters from the stream until the predicate
     *    tested false, or the stream ran out of data, whichever came first.
     * @throws IOException if the stream failed to be read
     */
    public String readWhile(ExceptionPredicate<Character, IOException> predicate) throws IOException {
        StringBuilder sb = new StringBuilder();
        // Peek then test, so that stream pos is only changed
        // if we want to add the character we read
        int c = reader.peek();
        while (c != -1 && predicate.test((char)c)) {
            sb.append((char)c);
            skip(1);
            c = reader.peek();
        }
        return sb.toString();
    }

    /**
     * Reads into a String containing all characters up until the predicate tests true.
     * The stream position is advanced by the length of the returned String.
     * The character that tests true in the predicate is NOT consumed.
     * @param predicate a function that returns false if the character should be added to the result and
     *                  the stream should continue to be read; else, true.
     * @return a (possibly-empty) String containing all characters from the stream until the predicate
     *    tested true, or the stream ran out of data, whichever came first.
     * @throws IOException if the stream failed to be read
     */
    public String readUntil(ExceptionPredicate<Character, IOException> predicate) throws IOException {
        return readWhile(predicate.negate());
    }

    /**
     * Reads from the stream until one of the given delimiters is found.
     * The stream position is advanced by the length of the returned String.
     * The delimiter is not consumed.
     * @param delimiters the characters that mark the end of the data to be read.
     * @return a (possibly-empty) String containing all characters before the first occurrence
     *   of a delimiter (or until the end of the stream).
     * @throws IOException if the stream couldn't be read from.
     */
    public String readToDelimiter(char[] delimiters) throws IOException {
        var stream = CharBuffer.wrap(delimiters).chars();
        return readUntil(c -> stream.anyMatch(strCh -> c == strCh));
    }

    /**
     * Reads from the stream until the given delimiter is found.
     * The stream position is advanced by the length of the returned String.
     * The delimiter is not consumed.
     * @param delimiter the character that marks the end of the data to be read.
     * @return a (possibly-empty) String containing all characters before the first occurrence
     *   of the delimiter (or until the end of the stream).
     * @throws IOException if the stream couldn't be read from.
     */
    public String readToDelimiter(char delimiter) throws IOException {
        return readUntil(c -> c == delimiter);
    }

    /**
     * Same as {@link #readToDelimiter(char)}, but with a String delimiter.
     * @param delimiter marks the end of the data to be read.
     * @return a (possibly-empty) String containing all characters before the first occurrence
     *   of the delimiter (or until the end of the stream).
     * @throws IOException if the stream couldn't be read from.
     */
    public String readToDelimiter(String delimiter) throws IOException {
        return readUntil(_unused -> this.peekNChars(delimiter.length()).equals(delimiter));
    }

    /**
     * Consumes and returns the next token (alphanumeric String).
     * @return a String of at least one alphanumeric character.
     * @throws ValidationException if the next character is not alphanumeric
     * @throws IOException if the underlying stream couldn't be read
     * @throws EOFException if EOS was reached and no valid token was read
     */
    public String nextToken() throws ValidationException, IOException {
        if (reader.peek() == -1) {
            throw new EOFException("Couldn't read token from input stream");
        }
        String tok = readWhile(Util::isAlnum);
        if (tok.length() == 0) {
            String badStr = reader.peekNChars(10);
            throw new ValidationException("Expected token, got non-token String at \"" + badStr + "\"", badStr);
        }

        return tok;
    }

    /**
     * Indicates whether a token can be consumed from the current position
     * using {@link #nextToken()}
     *
     * The stream position is unchanged.
     * @return true if a token can be read; false otherwise
     */
    public boolean hasNextToken() {
        try {
            return Util.isAlnum((char) reader.peek());
        }
        catch (IOException e) {
            return false;
        }
    }

    /**
     * Indicates whether the given String matches exactly
     * the substring from the stream, starting from the stream position.
     *
     * The stream position is unchanged.
     * @param str the String to search for.
     * @return true if the next characters in the stream match str; false otherwise
     */
    public boolean nextStringMatches(String str) {
        try {
            return reader.peekExactlyNChars(str.length()).equals(str);
        }
        catch (IOException e) {
            return false;
        }
    }

    /**
     * Wrapper for {@link PeekableReader#readAll()}.
     * Usage of this method is not recommended except for debugging and testing.
     * @return all data as a String
     * @throws IOException if I/O error
     */
    public String readAll() throws IOException {
        return reader.readAll();
    }

}
