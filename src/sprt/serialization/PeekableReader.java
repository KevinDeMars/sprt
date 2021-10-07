/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Prog0
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization;

import java.io.*;
import java.nio.charset.Charset;

/**
 * A reader that supports the peek operation (reading a character without advancing the stream position).
 */
public class PeekableReader extends BufferedReader {

    /**
     * Wraps an existing reader in a PeekableReader, using a buffer of a given size.
     * @param in existing reader
     * @param sz size of the buffer
     */
    public PeekableReader(Reader in, int sz) {
        super(in, sz);
    }

    /**
     * Wraps an existing reader in a PeekableReader.
     * @param in existing reader
     */
    public PeekableReader(Reader in) {
        super(in);
    }

    /**
     * Wraps an input stream in a PeekableReader by applying the given character encoding.
     * @param in existing input stream
     * @param cs character encoding to use
     */
    public PeekableReader(InputStream in, Charset cs) {
        super(new InputStreamReader(in, cs));
    }

    /**
     * Gets the next character in the stream without advancing the stream position.
     * @return the next character in the stream position, or -1 if EOS.
     * @throws IOException if an I/O error occurs
     */
    public int peek() throws IOException {
        mark(1);
        try {
            return read();
        }
        finally {
            reset();
        }
    }

    /**
     * Reads into a String as many characters as possible, up to n.
     * @param n max number of characters to read
     * @return a possibly-empty String containing at most n characters
     * @throws IOException if an I/O error occurs
     */
    public String readNChars(int n) throws IOException {
        char[] buf = new char[n];
        int read;
        int totalRead = 0;
        // Keep reading until read n chars or EOF
        do {
            read = this.read(buf, totalRead, n);
            if (read != -1) {
                n -= read;
                totalRead += read;
            }
        }
        while (n > 0 && read != -1);

        return new String(buf, 0, totalRead);
    }

    /**
     * Reads exactly n characters into a String
     * @param n number of characters to read
     * @return a String containing exactly n characters.
     * @throws IOException if an I/O error occurs
     * @throws EOFException if less than n characters could be read.
     */
    public String readExactlyNChars(int n) throws IOException {
        var read = readNChars(n);
        if (read.length() != n) {
            throw new EOFException("Expected to read " + n + " chars; got " + read.length());
        }
        return read;
    }

    /**
     * Gets at most the next n characters without advancing the stream position.
     * @param n maximum number of characters to read
     * @return a possibly-empty String containing at most n chars
     * @throws IOException if an I/O error occurs
     */
    public String peekNChars(int n) throws IOException {
        mark(n);
        String result;
        try {
            result = readNChars(n);
        }
        finally {
            reset();
        }
        return result;
    }

    /**
     * Gets exactly n characters without advancing the stream position.
     * @param n number of characters to read
     * @return a String containing exactly n chars
     * @throws IOException if an I/O error occurs
     * @throws EOFException if less than n characters are available
     */
    public String peekExactlyNChars(int n) throws IOException {
        mark(n);
        String result;
        try {
            result = readExactlyNChars(n);
        }
        finally {
            reset();
        }
        return result;
    }

    /**
     * Reads the entire stream into a string.
     * Usage of this method is not recommended except for debugging and testing.
     * @return all data in stream as a String
     * @throws IOException if I/O error
     */
    public String readAll() throws IOException {
        StringBuilder sb = new StringBuilder();
        String s = readNChars(1024);
        while (s.length() > 0) {
            sb.append(s);
            s = readNChars(1024);
        }
        return sb.toString();
    }


}
