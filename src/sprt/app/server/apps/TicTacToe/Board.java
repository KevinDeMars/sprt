/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server.apps.TicTacToe;

/**
 * A tic-tac-toe board
 */
public class Board {
    private static final int BOARD_SIZE = 3;
    private char turn = 'X';
    private final char[][] squares = {
            {' ', ' ', ' '},
            {' ', ' ', ' '},
            {' ', ' ', ' '},
    };

    /**
     * Gets whose turn it is
     * @return 'X' or 'O'
     */
    public char getTurn() {
        return turn;
    }

    /**
     * Place a piece at the given position for the player whose turn it currently is
     * @param row destination row (0-2)
     * @param col destination col (0-2)
     */
    public void move(int row, int col) {
        if (!canMove(row, col)) {
            throw new IllegalArgumentException("Position already filled");
        }
        squares[row][col] = turn;
        turn = (turn == 'X') ? 'O' : 'X';
    }

    /**
     * Gets whether the current player can place a piece at the given location.
     * @param row row to check
     * @param col column to check
     * @return true if legal move; else, false.
     */
    public boolean canMove(int row, int col) {
        return squares[row][col] == ' ';
    }

    /**
     * Gets whether the board is full.
     * @return true if board is full; else, false.
     */
    public boolean isFull() {
        for (var row : squares) {
            for (char x : row) {
                if (x == ' ')
                    return false;
            }
        }
        return true;
    }

    /**
     * Gets the winner of the game, if there is one.
     * @return 'X' or 'O' if there is a winner; else, null.
     */
    public Character winner() {
        // Check horizontal
        for (int row = 0; row < BOARD_SIZE; ++row) {
            if (checkRow(row))
                return squares[row][0];
        }
        // check vertical
        for (int col = 0; col < BOARD_SIZE; ++col) {
            if (checkCol(col))
                return squares[0][col];
        }
        if (checkDiag1())
            return squares[0][0];
        if (checkDiag2())
            return squares[BOARD_SIZE - 1][0];
        return null;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("| ");
        for (var row : squares) {
            for (char c : row) {
                if (c == ' ')
                    sb.append('_');
                else
                    sb.append(c);
                sb.append(' ');
            }
            sb.append(" | ");
        }
        return sb.toString();
    }

    private boolean checkRow(int r) {
        char piece = squares[r][0];
        if (piece == ' ')
            return false;
        for (int c = 0; c < BOARD_SIZE; ++c) {
            if (squares[r][c] != piece)
                return false;
        }
        return true;
    }
    private boolean checkCol(int c) {
        char piece = squares[0][c];
        if (piece == ' ')
            return false;
        for (int r = 0; r < BOARD_SIZE; ++r) {
            if (squares[r][c] != piece)
                return false;
        }
        return true;
    }
    private boolean checkDiag1() {
        char piece = squares[0][0];
        if (piece == ' ')
            return false;
        for (int i = 0; i < BOARD_SIZE; ++i) {
            if (squares[i][i] != piece)
                return false;
        }
        return true;
    }
    private boolean checkDiag2() {
        char piece = squares[BOARD_SIZE - 1][0];
        if (piece == ' ')
            return false;
        for (int r = BOARD_SIZE - 1; r >= 0; --r) {
            for (int c = 0; c < BOARD_SIZE; ++c) {
                if (squares[r][c] != piece)
                    return false;
            }
        }
        return true;
    }
}
