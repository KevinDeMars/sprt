/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server.apps.TicTacToe;

import sprt.app.server.ServerApp;
import sprt.app.server.State;
import sprt.app.server.StateResult;
import sprt.serialization.Request;
import sprt.serialization.Response;
import sprt.serialization.Status;
import sprt.serialization.ValidationException;

public class TicTacToe extends ServerApp {
    public class InitialState extends State {
        @Override
        public String name() {
            return "InitialState";
        }

        @Override
        public String prompt() {
            return null;
        }

        public StateResult doHandleRequest(Request req) throws ValidationException {
            return new StateResult(new PlayerMoveState());
        }
    }

    public class PlayerMoveState extends State {
        @Override
        public String name() {
            return "MoveState";
        }

        @Override
        public String prompt() {
            return board + " " + board.getTurn() + "'s turn (row col)> ";
        }

        public StateResult doHandleRequest(Request req, String sRow, String sCol) throws ValidationException {
            int row, col;
            try {
                row = Integer.parseInt(sRow);
                col = Integer.parseInt(sCol);
            }
            catch (NumberFormatException e) {
                return new StateResult(this, new Response(Status.ERROR, name(),
                        "position must be 2 integers. " + prompt())
                );
            }
            if (row < 1 || col < 1 || row > 3 || col > 3)
                return new StateResult(this, new Response(Status.ERROR, name(), "position must be between 1 and 3. " + prompt()));

            // adjust from 1-based index to 0-based
            row--;
            col--;
            if (!board.canMove(row, col))
                return new StateResult(this, new Response(Status.ERROR, name(), "Can't move there. " + prompt()));
            board.move(row, col);
            if (board.winner() != null)
                return new StateResult(null, new Response(Status.OK, "NULL", board + "Winner: " + board.winner()));
            if (board.isFull()) {
                return new StateResult(null, new Response(Status.OK, "NULL", board + "It's a draw."));
            }
            return new StateResult(this);
        }
    }

    private final Board board = new Board();

    public TicTacToe() {
        gotoState(new InitialState());
    }
}
