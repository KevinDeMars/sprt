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

public record StateResult(State nextState, Response resp) {
    public StateResult(State nextState, CookieList cookies) throws ValidationException {
        this(nextState, new Response(Status.OK, nextState.name(), nextState.prompt(), cookies));
    }
    public StateResult(State nextState) throws ValidationException {
        this(nextState, new CookieList());
    }
}