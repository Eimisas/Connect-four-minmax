# Connect-four-minmax
Project was created during ITU university course and uploaded for Eimantas Jazukevicius portfolio post purpose.

In this project we made our own implementation of the game ”Connect Four”.The game was implemented in Java. It is possible for a human player to play against the computer, and moreover the computer is making reasonable moves which was achieved by implementing famous Minimax algorithm with Alpha Beta search and evaluation function.
The computer’s response time on a board with 7 columns and 6 rows on average is not longer than 10 seconds although few moves takes up to 30 seconds.

To start the program run `ShowGame.java` file with program arguments:

`human GameLogic 6 6`

* human - human player
* GameLogic - min max algorithm
* [col, row] parameter to set the game board
