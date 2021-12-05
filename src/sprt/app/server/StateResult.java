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
     * Transitions to the next state and returns to the client the given status, prefixed message, and cookie list.
     * @param nextState state to transition to
     * @param status status code to give client
     * @param beforeMsg String prepended to the next state's prompt. Useful for error messages.
     * @param cookieList list of new cookies to add (or update) to the client's cookie list.
     * @throws ValidationException if invalid data
     */
    public StateResult(State nextState, Status status, String beforeMsg, CookieList cookieList) throws ValidationException {
        this(nextState, new Response(status, nextState.name(), beforeMsg + nextState.prompt(), cookieList));
    }
    /**
     * Transitions to the next state and returns to the client the given status and prefixed message.
     * @param nextState state to transition to
     * @param status status code to give client
     * @param beforeMsg String prepended to the next state's prompt. Useful for error messages.
     * @throws ValidationException if invalid data
     */
    public StateResult(State nextState, Status status, String beforeMsg) throws ValidationException {
        this(nextState, status, beforeMsg, new CookieList());
    }
    /**
     * Transitions to the next state and returns to the client the given status and cookie list.
     * @param nextState state to transition to
     * @param status status code to give client
     * @param cookies list of new cookies to add (or update) to the client's cookie list.
     * @throws ValidationException if invalid data
     */
    public StateResult(State nextState, Status status, CookieList cookies) throws ValidationException {
        this(nextState, status, "", cookies);
    }
    /**
     * Transitions to the next state and returns to the client the given status.
     * @param nextState state to transition to
     * @param status status code to give client
     * @throws ValidationException if invalid data
     */
    public StateResult(State nextState, Status status) throws ValidationException {
        this(nextState, status, "", new CookieList());
    }


    /**
     * Creates a transition to the empty/final state with the given status code, message, and new/updated cookies.
     * @param status status to return to client (OK/ERROR)
     * @param msg message to return to client
     * @param cookies new/updated cookies to give to client
     * @return A StateResult representing the specified transition.
     * @throws ValidationException if invalid data
     */
    public static StateResult exit(Status status, String msg, CookieList cookies) throws ValidationException {
        return new StateResult(null, new Response(status, Response.NO_NEXT_FUNCTION, msg, cookies));
    }
    /**
     * Creates a transition to the empty/final state with the given status code and message.
     * @param status status to return to client (OK/ERROR)
     * @param msg message to return to client
     * @return A StateResult representing the specified transition.
     * @throws ValidationException if invalid data
     */
    public static StateResult exit(Status status, String msg) throws ValidationException {
        return StateResult.exit(status, msg, new CookieList());
    }

}