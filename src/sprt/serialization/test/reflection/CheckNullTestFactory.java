/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Project 2 (Test only)
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization.test.reflection;

import org.junit.jupiter.api.DynamicTest;

import static sprt.serialization.test.TestAssertions.assertThrowsWithCause;

public class CheckNullTestFactory extends CheckArgumentsTestFactory {
    @Override
    protected DynamicTest testBadInvocation(Invocation validInvocation, int idx) {
        var inv = validInvocation.withParam(idx, null);
        return DynamicTest.dynamicTest(inv.getParamName(idx) + " = null",
                () -> assertThrowsWithCause(NullPointerException.class, inv::invoke)
        );
    }
}