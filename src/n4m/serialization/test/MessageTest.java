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
import org.junit.jupiter.api.TestFactory;
import shared.serialization.test.EqualsAndHashCodeTests;

import java.util.List;
import java.util.stream.Stream;

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

}
