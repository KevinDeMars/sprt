/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Project 2 (Test only)
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization.test.reflection;

import org.junit.jupiter.api.DynamicTest;
import sprt.serialization.ValidationException;

import static sprt.serialization.test.TestAssertions.assertThrowsWithCause;

public class CheckTokenTestFactory extends CheckArgumentsTestFactory {
    @Override
    protected DynamicTest testBadInvocation(Invocation validInvocation, int idx) {
        var inv = validInvocation.withParam(idx, "non token");
        return DynamicTest.dynamicTest(inv.getParamName(idx) + " = non token",
                () -> assertThrowsWithCause(ValidationException.class, inv::invoke)
        );
    }
}
