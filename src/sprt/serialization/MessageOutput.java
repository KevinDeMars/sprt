/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Prog0
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization;

import java.io.*;

import static sprt.serialization.Message.NEWLINE;
import static sprt.serialization.Message.SPRT_CHARSET;
import static sprt.serialization.Util.checkNull;

/**
 * Serialization output source for messages
 */
public class MessageOutput {
    private final BufferedWriter writer;

    /**
     * Wraps the given OutputStream, assumed to represent ASCII text.
     * @param out stream to wrap
     * @throws NullPointerException if out is null
     */
    public MessageOutput(OutputStream out) throws NullPointerException {
        this.writer = new BufferedWriter(
                new OutputStreamWriter(checkNull(out, "out"), SPRT_CHARSET)
        );
    }

    /**
     * Creates a MessageOutput that writes to a String.
     */
    public MessageOutput() {
        this.writer = new BufferedWriter(new StringWriter());
    }

    /**
     * Wrapper for {@link BufferedWriter#write(String)}.
     * @param str String to write
     * @throws IOException If I/O Error occurs
     */
    public void write(String str) throws IOException {
        writer.write(str);
    }

    /**
     * Writes each string using {@link BufferedWriter#write(String)} in order.
     * @param strings collection of Strings to write
     * @throws IOException if I/O error occurs
     */
    public void write(String... strings) throws IOException {
        for (var s : strings) {
            writer.write(s);
        }
    }

    /**
     * Writes the given String, then writes a CRLF.
     * @param str string to write
     * @throws IOException if an I/O error occurs
     */
    public void writeLine(String str) throws IOException {
        writer.write(str);
        writer.write(NEWLINE);
    }

    /**
     * Writes each of the strings in order, then writes a CRLF.
     * @param strings strings to write
     * @throws IOException if an I/O error occurs
     */
    public void writeLine(String... strings) throws IOException {
        for (var s : strings) {
            writer.write(s);
        }
        writer.write(NEWLINE);
    }

    /**
     * Wrapper for {@link BufferedWriter#flush()}.
     * @throws IOException if I/O error occurs.
     */
    public void flush() throws IOException {
        writer.flush();
    }

}
