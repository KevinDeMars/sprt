/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Program 1
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization;

/**
 * Represents a predicate (boolean-returning) function whose test can throw an exception.
 * @param <T> Type of value the predicate tests
 * @param <E> Type of error the test could throw
 */
@FunctionalInterface
public interface ExceptionPredicate<T, E extends Throwable> {
    /**
     * Tests the predicate
     * @param val value to test
     * @return whether the value satisfies this predicate
     * @throws E if exception occurs
     */
    boolean test(T val) throws E;

    /**
     * Creates a new predicate representing the negation of this one.
     * @return the negated predicate
     */
    default ExceptionPredicate<T,E> negate() {
        return val -> !test(val);
    }

    /**
     * Creates a new predicate representing the logical AND of this one and another.
     * @param other other predicate to AND
     * @return the logical AND of this and other
     */
    default ExceptionPredicate<T,E> and(ExceptionPredicate<T,E> other) {
        return val -> other.test(val) && test(val);
    }

    /**
     * Creates a new predicate representing the logical OR of this and another.
     * @param other other predicate to OR
     * @return the logical OR of this and other
     */
    default ExceptionPredicate<T,E> or(ExceptionPredicate<T,E> other) {
        return val -> other.test(val) || test(val);
    }
}
