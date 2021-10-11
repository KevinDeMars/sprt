/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server;

import sprt.serialization.Request;
import sprt.serialization.ValidationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public abstract class State {
    public abstract String name();
    public abstract String prompt();
    protected void onEnter() {}
    protected void onExit() {}

    StateResult handleRequest(ServerApp app, Request req, String[] params) throws ValidationException, InvocationTargetException, IllegalAccessException {
        var method = getHandler(params);
        if (method.isPresent()) {
            var realParams = new Object[params.length + 2];
            System.arraycopy(params, 0, realParams, 2, params.length);
            realParams[0] = app;
            realParams[1] = req;
            return (StateResult) method.get().invoke(this, realParams);
        }
        else {
            throw new IllegalArgumentException("Incorrect number of arguments");
        }
    }

    private Optional<Method> getHandler(String[] paramList) {
        var paramTypes = new Class[paramList.length + 2];
        Arrays.fill(paramTypes, String.class);
        paramTypes[0] = ServerApp.class;
        paramTypes[1] = Request.class;
        try {
            var m = this.getClass().getMethod("doHandleRequest", paramTypes);
            if (!StateResult.class.isAssignableFrom(m.getReturnType()))
                return Optional.empty();
            return Optional.of(m);
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }
}
