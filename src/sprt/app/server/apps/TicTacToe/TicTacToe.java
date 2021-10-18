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
import sprt.serialization.Status;
import sprt.serialization.ValidationException;

/**
 * A simple two-player tic-tac-toe application.
 */
public class TicTacToe extends ServerApp {
    /**
     * Initial state, waits for request
     */
    public class InitialState extends State {
        @Override
        public String name() {
            return "InitialState";
        }

        @Override
        public String prompt() {
            return null;
        }

        /**
         * Handles a client's request
         * @param req client's request
         * @return next state + response pair. Goes to PlayerMoveState.
         * @throws ValidationException if response data is invalid
         */
        public StateResult doHandleRequest(Request req) throws ValidationException {
            return new StateResult(new PlayerMoveState(), Status.OK);
        }
    }

    /**
     * Asks player to move
     */
    public class PlayerMoveState extends State {
        @Override
        public String name() {
            return "MoveState";
        }

        @Override
        public String prompt() {
            return board + " " + board.getTurn() + "'s turn (row col)> ";
        }

        /**
         * Handles a client's request
         * @param req client's request
         * @param sRow String representation of row to move to (valid: 1-3)
         * @param sCol String representation of column to move to (valid: 1-3)
         * @return next state + response pair. Stays in this state until game is over, then displays winner.
         * @throws ValidationException if response data is invalid
         */
        public StateResult doHandleRequest(Request req, String sRow, String sCol) throws ValidationException {
            int row, col;
            try {
                row = Integer.parseInt(sRow);
                col = Integer.parseInt(sCol);
            }
            catch (NumberFormatException e) {
                return new StateResult(this, Status.ERROR, "Position must be two integers. ");
            }
            if (row < 1 || col < 1 || row > 3 || col > 3)
                return new StateResult(this, Status.ERROR, "Position must be between 1 and 3. ");

            // adjust from 1-based index to 0-based
            row--;
            col--;
            if (!board.canMove(row, col))
                return new StateResult(this, Status.ERROR, "Can't move there. ");
            board.move(row, col);
            if (board.winner() != null)
                return StateResult.exit(Status.OK, board + " Winner: " + board.winner());
            if (board.isFull()) {
                return StateResult.exit(Status.OK, board + " It's a draw.");
            }
            return new StateResult(this, Status.OK);
        }
    }

    private final Board board = new Board();

    /**
     * Creates new Tic-Tac-Toe application.
     */
    public TicTacToe() {
        gotoState(new InitialState());
    }
}
