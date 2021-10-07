/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Project 2 (Test only)
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization.test.reflection;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;

import java.util.ArrayList;
import java.util.Arrays;


public abstract class CheckArgumentsTestFactory {
    public static record Check(Invocation invocation, int... paramIndices) {
        public static Check allParams(Invocation inv) {
            int[] indices = new int[inv.params.length];
            for (int i = 0; i < indices.length; ++i) {
                indices[i] = i;
            }
            return new Check(inv, indices);
        }
        public static <T> Check allOfType(Class<T> clazz, Invocation inv) {
            var indices = new ArrayList<Integer>();
            for (int i = 0; i < inv.getExecutable().getParameterCount(); ++i) {
                if (clazz.isAssignableFrom(inv.getExecutable().getParameters()[i].getType())) {
                    indices.add(i);
                }
            }
            return new Check(inv, indices.stream().mapToInt(x -> x).toArray());
        }
    }

    protected abstract DynamicTest testBadInvocation(Invocation validInvocation, int idx);

    public DynamicContainer testCheckArguments(Check check) {
        var tests = Arrays.stream(check.paramIndices)
                .mapToObj(idx -> testBadInvocation(check.invocation, idx));
        return DynamicContainer.dynamicContainer(check.invocation.displayName(), tests);
    }

}
