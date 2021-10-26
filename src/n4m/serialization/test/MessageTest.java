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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static n4m.serialization.N4MResponse.dateToTimestamp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    static Stream<N4MQuery> validQueries() throws ECException {
        return Stream.of(
            new N4MQuery(100, "MyBusiness"),
            new N4MQuery(0, "Business1"),
            new N4MQuery(11, "Business1"),
            new N4MQuery(100, "Business2"),
            new N4MQuery(100, "a".repeat(200))
        );
    }

    static Stream<N4MResponse> validResponses() throws ECException {
        var entry = new ApplicationEntry("Poll", 1234);
        var longList = new ArrayList<ApplicationEntry>();
        for (int i = 0; i < 150; ++i)
            longList.add(entry);
        var shortList = List.of(entry);
        var recent = dateToTimestamp(new Date()) - 100;

        return Stream.of(
                new N4MResponse(ErrorCode.NOERROR, 100, recent, shortList),
                new N4MResponse(ErrorCode.NOERROR, 100, 145, List.of()),
                new N4MResponse(ErrorCode.INCORRECTHEADER, 100, 100, List.of()),
                new N4MResponse(ErrorCode.SYSTEMERROR, 100, 111, List.of()),
                new N4MResponse(ErrorCode.NOERROR, 0, 111, longList)
        );
    }

    static Stream<N4MMessage> validMessages() throws ECException {
        return concat(validQueries(), validResponses());
    }

    static byte[] makeFakeN4MMessage() {

    }
}
