/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  Project 2 (Test only)
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.serialization.test.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static sprt.serialization.Util.concat;
import static sprt.serialization.Util.iterate;

public abstract class Invocation {
    protected final Object[] params;

    public static <T> List<Invocation> allMethodsDefaultParams(Class<T> clazz) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        var result = new ArrayList<Invocation>();
        List<String> excluded = Arrays.stream(Object.class.getMethods()).map(Method::getName).collect(Collectors.toList());
        for (Executable ex : iterate(concat(clazz.getMethods(), clazz.getConstructors()))) {
            // Exclude methods added by default in Object class (wait, toString, equals)
            if (!excluded.contains(ex.getName())) {
                result.add(Invocation.withDefaultParams(ex));
            }
        }
        return result;
    }

    public static Invocation withDefaultParams(Executable ex) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Object[] params = new Object[ex.getParameters().length];
        for (int i = 0; i < params.length; ++i) {
            var constructor = ex.getParameters()[i].getType().getConstructor();
            var obj = constructor.newInstance();
            params[i] = obj;
        }

        if (ex instanceof Constructor c)
            return new ConstructorInvocation(c, params);
        else {
            var m = (Method) ex;
            Object instance;
            try {
                instance = m.getDeclaringClass().getConstructor().newInstance();
            }
            catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("No default constructor for " + m.getDeclaringClass().getSimpleName(), e);
            }
            return new MethodInvocation(m, instance, params);
        }
    }

    public Invocation(Object... params) {
        this.params = Arrays.copyOf(params, params.length);
    }

    public abstract void invoke() throws Exception;

    public abstract Invocation withParam(int idx, Object p);

    public abstract String displayName();

    public abstract String getParamName(int idx);

    public abstract Executable getExecutable();
}