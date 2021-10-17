/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server;

import sprt.serialization.CookieList;
import sprt.serialization.Response;
import sprt.serialization.Status;
import sprt.serialization.ValidationException;

/**
 * next state + response pair obtained when handling a Request
 */
public record StateResult(State nextState, Response resp) {
    /**
     * Creates an OK response with the given next state and cookie list.
     * @param nextState next state to go to
     * @param cookies new cookies to send to client
     * @throws ValidationException if invalid cookie list
     */
    public StateResult(State nextState, CookieList cookies) throws ValidationException {
        this(nextState, new Response(Status.OK, nextState.name(), nextState.prompt(), cookies));
    }
    /**
     * Creates an OK response with the given next state and no additional cookies.
     * @param nextState next state to go to
     * @throws ValidationException if invalid cookie list
     */
    public StateResult(State nextState) throws ValidationException {
        this(nextState, new CookieList());
    }
}