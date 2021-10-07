/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Prog0
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization.test;

import org.junit.jupiter.api.function.Executable;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class TestAssertions {
    /**
     * Asserts that Collection c contains at least every item in collection toSearchFor, in any order.
     * @param c The collection to test
     * @param toSearchFor The expected values that c should contain
     * @param <T> The type of the elements in each collection
     */
    public static <T> void assertCollectionContains(Collection<T> c, Collection<T> toSearchFor) {
        for (T thing : toSearchFor) {
            assertTrue(c.contains(thing), "Collection " + c + " does not contain expected value " + thing);
        }
    }

    public static void assertThrowsWithCause(Class<? extends Throwable> expectedCause, Executable ex) {
        try {
            ex.execute();
        } catch (Throwable throwable) {
            assertNotNull(throwable.getCause(), "Exception has no cause: " + throwable);
            assertEquals(expectedCause, throwable.getCause().getClass());
            return;
        }
        fail("Expected to get Throwable with a cause of " + expectedCause.getName() + ", but not exception was thrown");
    }

}
