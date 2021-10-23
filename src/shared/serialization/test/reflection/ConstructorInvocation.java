/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Project 2 (Test only)
 * Class:       CSI 4321
 *
 * ***********************************************/

package shared.serialization.test.reflection;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public class ConstructorInvocation extends Invocation {
    private final Constructor<?> c;

    public ConstructorInvocation(Constructor<?> c, Object... params) {
        super(params);
        this.c = c;
    }

    @Override
    public void invoke() throws Exception {
        c.newInstance(params);
    }

    @Override
    public Invocation withParam(int idx, Object p) {
        var params = Arrays.copyOf(this.params, this.params.length);
        params[idx] = p;
        return new ConstructorInvocation(c, params);
    }

    @Override
    public String displayName() {
        return "constructor" + Arrays.toString(c.getParameters());
    }

    @Override
    public String getParamName(int idx) {
        return c.getParameters()[idx].getName();
    }

    @Override
    public Constructor<?> getExecutable() {
        return c;
    }
}
