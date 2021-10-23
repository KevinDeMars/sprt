/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Program 1
 * Class:       CSI 4321
 *
 * ***********************************************/

package shared.serialization.test;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static sprt.serialization.Util.concat;
import static sprt.serialization.Util.iterate;

public class EqualsAndHashCodeTests {
    public static <T> Stream<DynamicNode> testEquals(Collection<T> equal, Collection<T> unequal) {
        // Reflexivity
        var reflexTests = concat(equal, unequal)
                .map(obj -> DynamicTest.dynamicTest(
                        obj + " == self",
                        () -> assertEquals(obj, obj)
                ));
        DynamicContainer reflexive = DynamicContainer.dynamicContainer("Reflexive", reflexTests);

        // Symmetric
        var symTests = new ArrayList<DynamicTest>();
        for (var a : iterate(concat(equal, unequal))) {
            for (var b : iterate(concat(equal, unequal))) {
                if (a.equals(b)) {
                    symTests.add(DynamicTest.dynamicTest(b + " == " + a, () -> assertEquals(b, a)));
                }
                else {
                    symTests.add(DynamicTest.dynamicTest(b + " != " + a, () -> assertNotEquals(b, a)));
                }
            }
        }
        DynamicContainer symmetric = DynamicContainer.dynamicContainer("Symmetric", symTests);

        // Transitive
        var transTests = new ArrayList<DynamicTest>();
        for (var a : iterate(concat(equal, unequal))) {
            for (var b : iterate(concat(equal, unequal))) {
                for (var c : iterate(concat(equal, unequal))) {
                    if (a.equals(b) && b.equals(c)) {
                        transTests.add(DynamicTest.dynamicTest(a + " == " + c, () -> assertEquals(a, c)));
                    }
                }
            }
        }
        var transitive = DynamicContainer.dynamicContainer("Transitive", transTests);

        // not equals null
        var nullTests = new ArrayList<DynamicTest>();
        for (var x : iterate(concat(equal, unequal))) {
            nullTests.add(DynamicTest.dynamicTest(x + " != null", () -> assertNotEquals(x, null)));
        }
        var notEqualsNull = DynamicContainer.dynamicContainer("Doesn't Equal Null", nullTests);

        // equals different type
        var typeTests = new ArrayList<DynamicTest>();
        for (var x : iterate(concat(equal, unequal))) {
            typeTests.add(
                    DynamicTest.dynamicTest(x + " != string", () -> assertNotEquals("yeet", x))
            );
        }
        var notEqualsOtherType = DynamicContainer.dynamicContainer("Doesn't equal other type", typeTests);

        // consistent
        var consistentTests = new ArrayList<DynamicTest>();
        for (var a : iterate(concat(equal, unequal))) {
            for (var b : iterate(concat(equal, unequal))) {
                consistentTests.add(DynamicTest.dynamicTest("consistent: " + a + " , " + b, () -> {
                    boolean eq = a.equals(b);
                    for (int i = 0; i < 10; ++i) {
                        assertEquals(eq, a.equals(b));
                    }
                }));
            }
        }
        var consistent = DynamicContainer.dynamicContainer("Consistent result", consistentTests);

        return Stream.of(reflexive, symmetric, transitive, notEqualsNull, notEqualsOtherType, consistent);
    }

    @TestFactory
    public static <T> Stream<DynamicNode> testHashCode(Collection<T> equal, Collection<T> unequal) {
        // Whenever it is invoked on the same object more than once during an execution of a Java application,
        // the hashCode method must consistently return the same integer, provided no information used in equals
        // comparisons on the object is modified
        var consistencyTests = new ArrayList<DynamicTest>();
        for (var x : iterate(concat(equal, unequal))) {
            consistencyTests.add(DynamicTest.dynamicTest(x.toString(), () -> {
                int hash = x.hashCode();
                for (int i = 0; i < 10; ++i) {
                    assertEquals(hash, x.hashCode());
                }
            }));
        }
        var consistency = DynamicContainer.dynamicContainer("Consistent", consistencyTests);

        // If two objects are equal according to the equals(Object) method, then calling the hashCode method on
        // each of the two objects must produce the same integer result.
        var equalTests = new ArrayList<DynamicTest>();
        for (var x : iterate(concat(equal, unequal))) {
            for (var y : iterate(concat(equal, unequal))) {
                if (x.equals(y)) {
                    equalTests.add(DynamicTest.dynamicTest("hash(" + x + ") == hash(" + y + ")",
                            () -> assertEquals(x.hashCode(), y.hashCode())
                    ));
                }
            }
        }
        var equality = DynamicContainer.dynamicContainer("Equal objects -> equal hash", equalTests);

        return Stream.of(consistency, equality);
    }


}
