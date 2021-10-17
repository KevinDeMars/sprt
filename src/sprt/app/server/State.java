/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server;

import sprt.serialization.Request;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * A state that a ServerApp is in at any given time.
 */
public abstract class State {
    /**
     * Name of the state
     * @return state name
     */
    public abstract String name();

    /**
     * Prompt to give to client
     * @return the prompt
     */
    public abstract String prompt();

    // By default, do nothing on enter/exit
    protected void onEnter() {}
    protected void onExit() {}

    /**
     * Handles the request by finding a method in the current state called doHandleRequest with the correct parameters.
     * @param req request to handle
     * @return Result of handling the request (next state + response pair)
     * @throws InvocationTargetException if the state's handler throws an exception
     * @throws IllegalAccessException if no permission to run state's handler
     * @throws IllegalArgumentException if no handler exists for the appropriate parameters.
     */
    StateResult handleRequest(Request req) throws InvocationTargetException, IllegalAccessException {
        var params = req.getParams();
        var method = getHandler(params);
        if (method.isPresent()) {
            var realParams = new Object[params.length + 1];
            System.arraycopy(params, 0, realParams, 1, params.length);
            realParams[0] = req;
            return (StateResult) method.get().invoke(this, realParams);
        }
        else {
            throw new IllegalArgumentException("Incorrect number of arguments");
        }
    }

    private Optional<Method> getHandler(String[] paramList) {
        var paramTypes = new Class[paramList.length + 1];
        Arrays.fill(paramTypes, String.class);
        paramTypes[0] = Request.class;
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
