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
import java.util.Objects;

/**
 * An application that can run on a SPRT server
 */
public abstract class ServerApp {
    // Current state, or null for the empty state
    protected State state;

    /**
     * Handles the request by passing it to the app's State.
     * @param req Request to handle
     * @return Response to give to client
     * @throws ValidationException if invalid data
     */
    public Response handleRequest(Request req) throws ValidationException {
        if (getState() == null) {
            return new Response(Status.ERROR, Response.NO_NEXT_FUNCTION, "App has already exited");
        }

        try {
            var result = state.handleRequest(req);
            gotoState(result.nextState());
            return result.resp();
        } catch (InvocationTargetException | IllegalAccessException e) {
            System.err.println("Server error running request:");
            e.printStackTrace();
            return new Response(Status.ERROR, Response.NO_NEXT_FUNCTION, "A server error occurred processing the request.");
        }
        catch (IllegalArgumentException e) {
            return new Response(Status.ERROR, getState().name(), "Invalid number of parameters. " + getState().prompt());
        }
    }

    /**
     * Transitions to the given state.
     * @param nextState state to go to. The empty/final state is represented as null.
     */
    public void gotoState(State nextState) {
        // if no transition (state is same pointer) then do nothing
        if (nextState == state)
            return;
        if (state != null)
            state.onExit();
        this.state = nextState;
        if (nextState != null)
            nextState.onEnter();
    }

    /**
     * Get current state.
     * @return the current state
     */
    public State getState() {
        return this.state;
    }

    /**
     * Returns whether this is the "same app" as o, meaning the classes
     *  are the same. (e.g. a Poll instance equals a Poll instance; a Poll instance != a Guess instance)
     * @param o other object
     * @return whether these are the same apps
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getClass());
    }
}
