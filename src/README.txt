Server applications:

Poll: Runs as described in Program 2

TicTacToe: Play tic-tac-toe, crammed into one line.
    Usage: Designed for two players. The player whose turn is indicated enters where to place their piece
    by entering the row, a space, then the column. Coordinates are between 1 and 3.

    States:
       InitialState: No input. Always goes to PlayerMoveState.
       PlayerMoveState: Has row and col as inputs. Stays in this state until one player wins or the game is drawn.
          Then, prints the winner (or that it's a draw) and goes to the empty state.

    Examples:
        Game where X wins:
        Function> TicTacToe
        | _ _ _  | _ _ _  | _ _ _  |  X's turn (row col)> 1 1
        | X _ _  | _ _ _  | _ _ _  |  O's turn (row col)> 3 1
        | X _ _  | _ _ _  | O _ _  |  X's turn (row col)> 1 2
        | X X _  | _ _ _  | O _ _  |  O's turn (row col)> 3 2
        | X X _  | _ _ _  | O O _  |  X's turn (row col)> 1 3
        | X X X  | _ _ _  | O O _  | Winner: X

        Game with draw:
        Function> TicTacToe
        | _ _ _  | _ _ _  | _ _ _  |  X's turn (row col)> 1 2
        | _ X _  | _ _ _  | _ _ _  |  O's turn (row col)> 1 1
        | O X _  | _ _ _  | _ _ _  |  X's turn (row col)> 1 3
        | O X X  | _ _ _  | _ _ _  |  O's turn (row col)> 2 2
        | O X X  | _ O _  | _ _ _  |  X's turn (row col)> 2 1
        | O X X  | X O _  | _ _ _  |  O's turn (row col)> 2 3
        | O X X  | X O O  | _ _ _  |  X's turn (row col)> 3 1
        | O X X  | X O O  | X _ _  |  O's turn (row col)> 3 2
        | O X X  | X O O  | X O _  |  X's turn (row col)> 3 3
        | O X X  | X O O  | X O X  | It's a draw.

        Non-happy cases:
        Function> TicTacToe
        | _ _ _  | _ _ _  | _ _ _  |  X's turn (row col)> yeet oof
        Position must be two integers. | _ _ _  | _ _ _  | _ _ _  |  X's turn (row col)> 200 0
        Position must be between 1 and 3. | _ _ _  | _ _ _  | _ _ _  |  X's turn (row col)> hello
        Invalid number of parameters. | _ _ _  | _ _ _  | _ _ _  |  X's turn (row col)> 1 1
        | X _ _  | _ _ _  | _ _ _  |  O's turn (row col)> 1 1
        Can't move there. | X _ _  | _ _ _  | _ _ _  |  O's turn (row col)>