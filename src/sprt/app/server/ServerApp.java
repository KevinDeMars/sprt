/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server;

import sprt.serialization.Request;
import sprt.serialization.Response;
import sprt.serialization.Status;
import sprt.serialization.ValidationException;

import java.lang.reflect.InvocationTargetException;

public abstract class ServerApp {
    // Current state
    protected State state;
    // Keep last response sent for re-sending prompt for parameters
    protected Response lastResponseSent;

    protected ServerApp(State initalState) {
        this.state = initalState;
    }

    public Response handleRequest(Request req) throws ValidationException {
        if (getState() == null) {
            return new Response(Status.ERROR, "NULL", "App has already exited");
        }

        try {
            var result = state.handleRequest(this, req, req.getParams());
            gotoState(result.nextState());
            return result.resp();
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return new Response(Status.ERROR, "NULL", "A server error occurred processing the request.");
        }
        catch (IllegalArgumentException e) {
            return new Response(Status.ERROR, getState().name(), "Invalid number of parameters. " + getState().prompt());
        }

        /*
        try {
            lastResponseSent = state.handleRequest(this, req, req.getParams());
            return lastResponseSent;
        } catch (InvocationTargetException | IllegalAccessException e) {
            return new Response(Status.ERROR, "NULL", "Server error occurred while handling request", new CookieList());
        } catch (IllegalArgumentException e) {
            return new Response(Status.ERROR, this.state.name(), "Incorrect number of arguments " + lastResponseSent.getMessage(), new CookieList());
        }*/
    }

    public void gotoState(State nextState) {
        // if no transition (state is same pointer) then do nothing
        if (nextState == state)
            return;
        state.onExit();
        this.state = nextState;
        if (nextState != null)
            nextState.onEnter();
    }

    public State getState() {
        return this.state;
    }
}
