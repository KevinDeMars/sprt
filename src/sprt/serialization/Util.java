/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Prog0
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Contains common utility methods for SPRT serialization. */
public class Util {

    /**
     * Requires that x is non-null.
     * @param x the object to ensure non-null
     * @param name name of the object, for the exception message
     * @param <T> type of x
     * @return x if it is non-null
     * @throws NullPointerException if x is null
     */
    public static <T> T checkNull(T x, String name) {
        Objects.requireNonNull(x, name + " must not be null");
        return x;
    }

    /**
     * Requires that x is non-null and contains no null elements.
     * @param x collection to check
     * @param name name of collection (for error message)
     * @param <T> type of elements in collection
     * @return x
     * @throws NullPointerException if x is null or contains a null value
     */
    public static <T> Iterable<T> deepCheckNull(Iterable<T> x, String name) {
        checkNull(x, name);
        int i = 0;
        for (T item : x) {
            Objects.requireNonNull(item, "Invalid null item at index " + i + " in collection " + name);
            ++i;
        }
        return x;
    }

    /**
     * Requires that x is non-null and contains no null elements.
     * @param x collection to check
     * @param name name of collection (for error message)
     * @param <T> type of elements in collection
     * @return x
     * @throws NullPointerException if x is null or contains a null value
     */
    public static <T> T[] deepCheckNull(T[] x, String name) {
        deepCheckNull(
                () -> Arrays.stream(x).iterator(),
                name
        );
        return x;
    }

    /**
     * Determines whether c is an ASCII alphabetical character or digit.
     * Does not depend on localization.
     * Non-ASCII alphabetical characters return false.
     * @param c character to check
     * @return true if c is alphanumeric; else, false.
     */
    public static boolean isAlnum(char c) {
        return (c >= 'A' && c <= 'Z')
                || (c >= 'a' && c <= 'z')
                || (c >= '0' && c <= '9');
    }

    /**
     * Creates stream containing the contents of each stream, in order.
     * @param streams streams to stream over
     * @param <T> type of each element in streams
     * @return Stream containing all items in all streams
     */
    @SafeVarargs
    public static <T> Stream<T> concat(Stream<? extends T>... streams) {
        Stream<T> result = Stream.empty();
        for (var s : streams) {
            result = Stream.concat(result, s);
        }
        return result;
    }

    /**
     * Creates stream containing the contents of each iterable, in order.
     * @param itrs iterables to stream
     * @param <T> type of element in iterables
     * @return Stream containing all items in all iterables
     */
    @SafeVarargs
    public static <T> Stream<T> concat(Iterable<? extends T>... itrs) {
        Stream<T> s = Stream.empty();
        for (var itr : itrs) {
            s = Stream.concat(s, stream(itr));
        }
        return s;
    }

    /**
     * Creates stream containing the contents of each array, in order.
     * @param arrays arrays to stream
     * @param <T> type of item in array
     * @return Stream containing all items in all arrays
     */
    @SafeVarargs
    public static <T> Stream<T> concat(T[]... arrays) {
        Stream<T> s = Stream.empty();
        for (var arr : arrays) {
            s = Stream.concat(s, Arrays.stream(arr));
        }
        return s;
    }

    /**
     * Creates a stream of the items in the given iterable.
     * @param iterable items to stream over
     * @param <T> type of each item
     * @return stream containing all items in iterable
     */
    public static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * Converts a stream to an iterable, to use with a for-each loop.
     * The Iterable may be invalidated after iterating through it once.
     * @param s stream to create iterable
     * @param <T> type of item to iterate over
     * @return iterable over the items in s
     */
    public static <T> Iterable<T> iterate(Stream<T> s) {
        return s::iterator;
    }

}
