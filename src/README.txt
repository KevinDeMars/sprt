Server applications:

- Poll: Runs as described in Program 2

- TicTacToe: Play tic-tac-toe, crammed into one line.
Usage: Designed for two players. The player whose turn is indicated inputs where to place their piece
by writing the row, a space, then the column. Coordinates are between 1 and 3.

States: InitialState:

Examples:
Function> TicTacToe
| _ _ _  | _ _ _  | _ _ _  |  X's turn (row col)> 1 1
| X _ _  | _ _ _  | _ _ _  |  O's turn (row col)> 3 1
| X _ _  | _ _ _  | O _ _  |  X's turn (row col)> 1 2
| X X _  | _ _ _  | O _ _  |  O's turn (row col)> 3 2
| X X _  | _ _ _  | O O _  |  X's turn (row col)> 1 3
| X X X  | _ _ _  | O O _  | Winner: X

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
