/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import shared.MathUtil;
import sprt.serialization.Message;
import sprt.serialization.NIODeframer;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class NIODeframerTest {
    private final NIODeframer deframer = new NIODeframer();
    private static final String delim = NIODeframer.DELIMITER;

    @ParameterizedTest
    @MethodSource("completeMessages")
    void fullMsg(String input) {
        var msg = deframer.getMessage(bytes(input));
        assertNotNull(msg);
        assertEquals(0, deframer.dataSize());
    }

    @ParameterizedTest
    @MethodSource("completeMessages")
    void msgInChunks(String input) {
        readInChunks(bytes(input), bytes(input).length);
    }

    @ParameterizedTest
    @MethodSource("completeMessages")
    void doubleMsg(String input) {
        input += input;
        var msg1 = deframer.getMessage(bytes(input));
        var msg2 = deframer.getMessage(new byte[0]);
        assertNotNull(msg1);
        assertArrayEquals(msg1, msg2);
    }

    @ParameterizedTest
    @MethodSource("completeMessages")
    void doubleMsgChunks(String input) {
        input += input;
        readInChunks(bytes(input), bytes(input).length / 2);
    }

    private void readInChunks(byte[] bytes, int msgLen) {
        for (int trial = 0; trial < 100; ++trial) {
            int pos = 0;
            int msgNumber = 0;
            while (pos < bytes.length) {
                int remainingLen = bytes.length - pos;
                int chunkSize = MathUtil.randomInt(1, remainingLen);
                var chunk = Arrays.copyOfRange(bytes, pos, pos + chunkSize);
                pos += chunkSize;
                var msg = deframer.getMessage(chunk);

                int newMsgNumber = pos / msgLen;
                if (newMsgNumber > msgNumber) {
                    int expectedNumMessages = newMsgNumber - msgNumber - 1;
                    assertNotNull(msg);
                    for (int i = 0; i < expectedNumMessages; ++i) {
                        assertNotNull(deframer.getMessage(new byte[0]));
                    }
                    msgNumber = newMsgNumber;
                }
                else {
                    assertNull(msg);
                }
            }
        }
    }

    private static Stream<String> completeMessages() {
        return Stream.of(
                delim,
                "Hello" + delim,
                "Billy mays\r\nBuy Oxiclean" + delim,
                "SPRT/1.0 Q RUN Poll\r\nFoo=1" + delim
        );
    }

    private static byte[] bytes(String str) {
        return str.getBytes(Message.SPRT_CHARSET);
    }
}
