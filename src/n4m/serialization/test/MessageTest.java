/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package n4m.serialization.test;

import n4m.serialization.*;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import shared.serialization.test.EqualsAndHashCodeTests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static n4m.serialization.N4MResponse.dateToTimestamp;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    private final List<N4MMessage> equal = List.of(
            new N4MQuery(123, "Bus1"),
            new N4MQuery(100 + 23, "Bus" + "1")
    );
    private final List<N4MMessage> unequal = List.of(
            new N4MQuery(100, "Bus1"),
            new N4MQuery(123, "Bus2"),
            new N4MResponse(ErrorCode.NOERROR, 123, 12345, List.of()),
            new N4MResponse(ErrorCode.INCORRECTHEADER, 123, 12345, List.of()),
            new N4MResponse(ErrorCode.NOERROR, 100, 12345, List.of()),
            new N4MResponse(ErrorCode.NOERROR, 123, 111, List.of()),
            new N4MResponse(ErrorCode.NOERROR, 123, 12345, List.of(new ApplicationEntry("Poll", 1337)))
    );

    public MessageTest() throws ECException {
    }

    @TestFactory
    Stream<DynamicNode> testEquals() {
        return EqualsAndHashCodeTests.testEquals(equal, unequal);
    }
    @TestFactory
    Stream<DynamicNode> testHashCode() {
        return EqualsAndHashCodeTests.testHashCode(equal, unequal);
    }

    @Test
    void queryRejectBadParams() {
        var badIds = List.of(-1, 256, 100000, Integer.MAX_VALUE);
        String longName = "1234567890".repeat(500);
        var badNames = List.of(longName, "piñata");

        for (String name : badNames) {
            assertThrows(ECException.class, () -> new N4MQuery(123, name).encode(), name);
        }
        for (int id : badIds) {
            assertThrows(ECException.class, () -> new N4MQuery(id, "Bus1").encode());
        }
    }

    @Test
    void responseRejectBadParams() {
        var badIds = List.of(-1, 256, 100000, Integer.MAX_VALUE);
        var badTimestamps = List.of(-1);

        var badAppNames = List.of("1234567890".repeat(500), "piñata", "Hola, ¿Cómo estás?");
        var badAccessCounts = List.of(-1, 100000);

        for (int id : badIds) {
            assertThrows(ECException.class, () -> makeN4MResponse(id, null, null, null, null).encode());
        }
        for (long ts : badTimestamps)
            assertThrows(ECException.class, () -> makeN4MResponse(null, ts, null, null, null).encode());
        for (String name : badAppNames)
            assertThrows(ECException.class, () -> makeN4MResponse(null, null, null, name, null).encode(), name);
        for (int count : badAccessCounts)
            assertThrows(ECException.class, () -> makeN4MResponse(null, null, null, null, count).encode());
    }

    N4MResponse makeN4MResponse(Integer id, Long timestamp, ErrorCode ec, String appName, Integer accessCount) throws ECException {
        if (id == null)
            id = 123;
        if (timestamp == null)
            timestamp = dateToTimestamp(new Date()) - 100;
        if (ec == null)
            ec = ErrorCode.NOERROR;
        if (appName == null)
            appName = "Poll";
        if (accessCount == null)
            accessCount = 100;
        return new N4MResponse(ec, id, timestamp, List.of(new ApplicationEntry(appName, accessCount)));
    }

    @ParameterizedTest
    @MethodSource("validMessages")
    void encodeAndDecode(N4MMessage m) throws ECException {
        var encoded = m.encode();
        var decoded = N4MMessage.decode(encoded);
        assertEquals(m, decoded);
    }

    @ParameterizedTest
    @MethodSource("invalidMessages")
    void testBadMsgBytes(BadMsg msg) {
        try {
            N4MMessage.decode(msg.data);
        } catch (ECException e) {
            assertEquals(msg.expectedErr, e.getErrorCodeType());
            return;
        }

        fail("Should have thrown ECException");
    }

    @ParameterizedTest
    @MethodSource("validMessages")
    void testChopUpMsg(N4MMessage msg) {
        var bytes = msg.encode();
        for (int size = 0; size < bytes.length; ++size) {
            byte[] subBytes = new byte[size];
            System.arraycopy(bytes, 0, subBytes, 0, size);
            assertThrows(ECException.class, () -> N4MMessage.decode(subBytes));
        }
    }

    @Test
    void testTooManyEntries() {
        try {
            var entry = new ApplicationEntry("Poll", 123);
            var list = new ArrayList<ApplicationEntry>();
            for (int i = 0; i < 256; ++i)
                list.add(entry);
            new N4MResponse(ErrorCode.NOERROR, 111, 111, list);
        } catch (ECException e) {
            assertEquals(e.getErrorCodeType(), ErrorCode.BADMSG);
            return;
        }

        fail("Should have thrown BADMSG exception");
    }

    @Test
    void testToString() throws ECException {
        var msg1 = new N4MQuery(111, "Bus1");
        assertEquals("N4M QUERY: MsgID=111, BusName=Bus1", msg1.toString());
        var c = Calendar.getInstance();
        c.set(2021, Calendar.OCTOBER, 31, 11, 23, 45);
        var ts = dateToTimestamp(c.getTime());

        var msg2 = new N4MResponse(ErrorCode.NOERROR, 111, ts, List.of(
                new ApplicationEntry("Poll", 222),
                new ApplicationEntry("Guess", 333)
        ));
        assertEquals(String.format("N4M RESPONSE: MsgID=111, Error=NOERROR, Time=%s: Poll(222) Guess(333) ", c.getTime()),
                msg2.toString()
        );
    }

    static Stream<N4MQuery> validQueries() throws ECException {
        return Stream.of(
            new N4MQuery(100, "MyBusiness"),
            new N4MQuery(0, "Business1"),
            new N4MQuery(255, "Business1"),
            new N4MQuery(100, "Business2"),
            new N4MQuery(100, "a".repeat(200)),
            new N4MQuery(100, "")
        );
    }

    static Stream<N4MResponse> validResponses() throws ECException {
        var entry = new ApplicationEntry("Poll", 1234);
        var longList = new ArrayList<ApplicationEntry>();
        for (int i = 0; i < 150; ++i)
            longList.add(entry);
        var shortList = List.of(entry);
        var medList = List.of(entry, new ApplicationEntry("Yeet", 123), new ApplicationEntry("Hello", 65535));
        var recent = dateToTimestamp(new Date()) - 100;

        return Stream.of(
                new N4MResponse(ErrorCode.NOERROR, 100, recent, shortList),
                new N4MResponse(ErrorCode.NOERROR, 100, 145, List.of()),
                new N4MResponse(ErrorCode.INCORRECTHEADER, 100, 100, List.of()),
                new N4MResponse(ErrorCode.SYSTEMERROR, 100, 111, List.of()),
                new N4MResponse(ErrorCode.NOERROR, 0, 111, longList),
                new N4MResponse(ErrorCode.NOERROR, 0, 0xF0_00_00_00L, medList)
        );
    }

    static Stream<N4MMessage> validMessages() throws ECException {
        return concat(validQueries(), validResponses());
    }

    record BadMsg(ErrorCode expectedErr, byte[] data) {

    }

    static Stream<BadMsg> invalidMessages() throws IOException {
        return Stream.of(
                // Wrong version
                new BadMsg(ErrorCode.INCORRECTHEADER, makeFakeN4MMessage(1, false, 0, (byte)111, new byte[] {2, 'h', 'i'})),
                // query has error code
                new BadMsg(ErrorCode.INCORRECTHEADER, makeFakeN4MMessage(2, false, 2, (byte)111, new byte[] {2, 'h', 'i'})),
                // name length too short
                new BadMsg(ErrorCode.BADMSGSIZE, makeFakeN4MMessage(2, false, 0, (byte)111, new byte[] {0, 'h', 'i'})),
                // name length too long
                new BadMsg(ErrorCode.BADMSGSIZE, makeFakeN4MMessage(2, false, 0, (byte)111, new byte[] {127, 'h', 'i'})),
                // empty data
                new BadMsg(ErrorCode.BADMSGSIZE, makeFakeN4MMessage(2, false, 0, (byte)111, new byte[] {})),
                // invalid chars
                new BadMsg(ErrorCode.BADMSG, makeFakeN4MMessage(2, false, 0, (byte)111, new byte[] {2, (byte)0x90, (byte)0xFF})),


                // invalid response error code
                new BadMsg(ErrorCode.INCORRECTHEADER, makeFakeN4MMessage(2, true, 6, (byte)111, new byte[] {1,1,1,1, 0})),
                // only half of timestamp
                new BadMsg(ErrorCode.BADMSGSIZE, makeFakeN4MMessage(2, true, 6, (byte)111, new byte[] {1,1})),
                // empty data
                new BadMsg(ErrorCode.BADMSGSIZE, makeFakeN4MMessage(2, true, 6, (byte)111, new byte[] {})),
                // not enough app entries
                new BadMsg(ErrorCode.BADMSGSIZE, makeFakeN4MMessage(2, true, 0, (byte)111, new byte[] {1,1,1,1, 2})),
                // too many app entries
                new BadMsg(ErrorCode.BADMSGSIZE, makeFakeN4MMessage(2, true, 0, (byte)111, new byte[] {1,1,1,1, 1,   3,3, 2,'h','i', 3,3, 2,'h','i'})),
                // app entry name too short
                new BadMsg(ErrorCode.BADMSGSIZE, makeFakeN4MMessage(2, true, 0, (byte)111, new byte[] {1,1,1,1, 1,   3,3, 10,'h','i'})),
                // app entry name invalid chars
                new BadMsg(ErrorCode.BADMSG, makeFakeN4MMessage(2, true, 0, (byte)111, new byte[] {1,1,1,1, 1,   3,3, 2,(byte)0xFF,(byte)0x91})),
                // app entry name too long
                new BadMsg(ErrorCode.BADMSGSIZE, makeFakeN4MMessage(2, true, 0, (byte)111, new byte[] {1,1,1,1, 1,   3,3, 2,'a','a','a','a','a'}))
        );
    }

    static byte[] makeFakeN4MMessage(int version, boolean isResponse, int errCode, byte msgId, byte[] data) throws IOException {
        var out = new ByteArrayOutputStream();
        var writer = new BinaryWriter(out);
        writer.writeBits(version, 4);
        writer.writeBit(isResponse ? 1 : 0);
        writer.writeBits(errCode, 3);
        writer.writeByte(msgId);
        writer.writeBytes(data);
        return out.toByteArray();
    }

}
