/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Project 2 (Test only)
 * Class:       CSI 4321
 *
 * ***********************************************/

package shared.serialization.test.reflection;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodInvocation extends Invocation {
    private final Method method;
    private final Object instance;

    public MethodInvocation(Method m, Object instance, Object... params) {
        super(params);
        this.method = m;
        if (!this.method.getDeclaringClass().isInstance(instance)) {
            throw new IllegalArgumentException("instance must be instance of " + this.method.getDeclaringClass().getSimpleName());
        }
        this.instance = instance;
    }

    @Override
    public void invoke() throws Exception {
        method.invoke(instance, params);
    }

    @Override
    public Invocation withParam(int idx, Object p) {
        var params = Arrays.copyOf(this.params, this.params.length);
        params[idx] = p;
        return new MethodInvocation(method, instance, params);
    }

    @Override
    public String displayName() {
        return method.getName();
    }

    @Override
    public String getParamName(int idx) {
        return method.getParameters()[idx].getName();
    }

    @Override
    public Method getExecutable() {
        return method;
    }
}
