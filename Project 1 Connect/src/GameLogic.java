import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameLogic implements IGameLogic, Serializable {
    int x = 0;
    int y = 0;
    private int playerID;

    //In order to know on what level of Y the to be inserted coin ends up on we need to keep track of the current stack of coins in each column
    // e.g. coinsPerCol[3] = 6 means there are 7 coins in column 4.
    int[] coinsPerCol;

    int[][] playBoard;
    int lastMoveCol;
    int lastMovePlayerID;

    //directions to move on XY-field e.g. xNeighbour[0],yNeighbour[0] would be the values needed to move from
    //the current coin field to the top left diagonal neighbour of the current position (global XY Pointer)
    private int[] xNeighbour = new int[]{-1, -1, -1, 0, 1, 1, 1, 0};
    private int[] yNeighbour = new int[]{1, 0, -1, -1, -1, 0, 1, 1};
    int globalXPointer;
    int globalYPointer;

    /**
     * Initialize gameLogic by setting the games dimensions (x & y) and which player the class is supposed to represent (blue or red).
     *
     * @param x        number of columns
     * @param y        number of rows
     * @param playerID 1 = blue (player1), 2 = red (player2)
     */
    public void initializeGame(int x, int y, int playerID) {
        this.x = x;
        this.y = y;
        this.playerID = playerID;
        playBoard = new int[x][y];
        coinsPerCol = new int[x];
        for (int i = 0; i < coinsPerCol.length; i++) {
            coinsPerCol[i] = -1;
        }
    }

    /**
     * @return one of the constants of the enum Winner depending on whether player1 or 2 has won or whether the game
     * is not finished yet or a draw.
     */
    public Winner gameFinished() {
        if (lastMovePlayerID == 0) return Winner.NOT_FINISHED;
        for (int i = 0; i < 4; i++) {

            int counter = 0;
            int localPointerX = globalXPointer;
            int localPointerY = globalYPointer;

            //check whether the neighbour in direction i (@see xNeighbour[i],yNeighbour[i]) has the same player ID,
            // if so go to that neighbour and check for its neighbour in the same direction.
            while (checkNeighbours(globalXPointer, globalYPointer, i, lastMovePlayerID)) {
                goToNeighbour(i);
                counter++;
            }

            //reset the globalPointer to the field where we have started to look for whining combination.
            globalYPointer = localPointerY;
            globalXPointer = localPointerX;

            //check the neighbour and its neighbour lying on the opposite direction.
            while (checkNeighbours(globalXPointer, globalYPointer, i + 4, lastMovePlayerID)) {
                goToNeighbour(i + 4);
                counter++;
            }

            //reset the globalPointer to the field where we have started to look for whining combination
            globalYPointer = localPointerY;
            globalXPointer = localPointerX;

            if (counter > 2) {
                // note that >1 means connect 3 and > 2 means connect 4 etc.
                //once we have found more than COUNTER coins witht he same PlayerIDvalue in some direction
                // (including both sides from the starting coin Field) we know that this is a winning situation.

                if (lastMovePlayerID == 1) return Winner.PLAYER1;
                else return Winner.PLAYER2;
            }
        }

        //if the previous for loop has not found any winning combination we know that none of the players has connected four
        // Now we check whether all columns are full. If so it is a tie. Otherwise we can stop checking when we see the first nonfull column.
        for (int i = 0; i < coinsPerCol.length; i++) {
            if (coinsPerCol[i] != y - 1) {
                return Winner.NOT_FINISHED;
            }
        }
        return Winner.TIE;
    }

    /**
     *
     * @param x xValue of the field we want to check the neighbours of
     * @param y yValue of the field we want to check the neighbours of
     * @param dirNeighBour direction of the neighbour to be checked {@see xNeighbour and yNeighbour arrays}
     * @param pID player / tokenID
     * @return boolean is the neighbouring token the same (id-wise)
     */
    private boolean checkNeighbours(int x, int y, int dirNeighBour, int pID) {
        if (x <= 0 && (dirNeighBour < 3)) return false;
        if (x == this.x - 1 && dirNeighBour > 3) return false;
        if (y <= 0 && (dirNeighBour > 1 && dirNeighBour < 5)) return false;
        if (y == this.y - 1 && (dirNeighBour == 0 || dirNeighBour > 5)) return false;

        int xdir = xNeighbour[dirNeighBour];
        int ydir = yNeighbour[dirNeighBour];

        if (playBoard[x + xdir][y + ydir] == pID) {
            return true;
        }
        return false;
    }



    /**
     Move the two global Pointers to the indicated field
     [0][8][6]
     [1][.][5]
     [2][3][4]
     where [.] indicates the current field.
     careful!! Out of bound is not checked.
     * @param neighBourDir direction of the neighbour to be checked {@see xNeighbour and yNeighbour arrays}
     */
    private void goToNeighbour(int neighBourDir) {
        globalXPointer = globalXPointer + xNeighbour[neighBourDir];
        globalYPointer = globalYPointer + yNeighbour[neighBourDir];
    }

    /**
     * @param column The column where the coin is inserted.
     * @param playerID The ID of the current player.
     */
    public void insertCoin(int column, int playerID) {
        coinsPerCol[column] = coinsPerCol[column] + 1;
        lastMoveCol = column;
        lastMovePlayerID = playerID;
        playBoard[column][coinsPerCol[column]] = playerID;
        globalXPointer = column;
        globalYPointer = coinsPerCol[column];
    }

    /**
     *
     * @return column index (x-axis) of playboard where the next coin should be inserted.
     */
    public int decideNextMove() {
        int toRet = new MiniMax(this).minimaxDecision();
        return toRet;
    }

    /**
     *
     * @return int array of all possible Moves / array with column indexes (x-axis) which are not full yet.
     */
    public int[] getPossibleMoves(){
        List<Integer> toRet = new ArrayList<>();
        for(int i = 0; i < coinsPerCol.length; i++){
            if(coinsPerCol[i] < this.y -1) toRet.add(i);
        }
        int[] tooRet = toRet.stream().mapToInt(i->i).toArray();
        return tooRet;
    }

    /**
     *
     * @return return playerID of the GameLogic instance.
     */
    public int getPlayerID() {
        return playerID;
    }

    /**
     *
     * @param playerID set the playerID of the GameLogic instance.
     */
    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    /**
     * This function is used to estimate the winner based current state of the game board.
     * For every field (x and y) we determine its connection value. In other words we estimate how
     * likely a connect four combination with this field is.
     *
     * @param x x-axis value
     * @param y y-axis value
     * @param playerID id of player
     * @return value of indicated play board (x and y) in terms of possible connect four winning combinations.
     */
    public double countConnectionOfCoins(int x, int y, int playerID){
        int xAxisPointer = x;
        int yAxisPointer = y;
        double util = 0;
        double toRet = 0;
        for(int i = 0; i < 8; i++){
            //check whether the neighbour in direction i (@see xNeighbour[i],yNeighbour[i]) has the same player ID,
            // if so go to that neighbour and check for its neighbour in the same direction.
            int counter = 0;
            while (neighbourSameIdOrEmpty(xAxisPointer, yAxisPointer, i, playerID)) {
                xAxisPointer = xAxisPointer + xNeighbour[i];
                yAxisPointer = yAxisPointer + yNeighbour[i];
                if(playBoard[xAxisPointer][yAxisPointer]!=0){
                    counter++; //count the number of non-empty slots with the right coin id in it.
                    if(inBounds(xAxisPointer + xNeighbour[i],yAxisPointer + yNeighbour[i])) util = util + 1;
                    else util = util + 0.0;
                }
                else util = util + 0.3;
            }
            if(counter < 1) util = 0;
            toRet = toRet + util;
            xAxisPointer = x;
            yAxisPointer = y;
        }
        return toRet;
    }

    /**
     *
     * @param x x-axis value
     * @param y y-axis value
     * @param neighBourDir direction of the neighbour to be checked {@see xNeighbour and yNeighbour arrays}
     * @param pID ID of player
     * @return boolean is neighbour same ID or 0?
     */
    private boolean neighbourSameIdOrEmpty(int x, int y, int neighBourDir, int pID) {
        if (x <= 0 && (neighBourDir < 3)) return false;
        if (x == this.x - 1 && neighBourDir > 3) return false;
        if (y <= 0 && (neighBourDir > 1 && neighBourDir < 5)) return false;
        if (y == this.y - 1 && (neighBourDir == 0 || neighBourDir > 5)) return false;

        int xdir = xNeighbour[neighBourDir];
        int ydir = yNeighbour[neighBourDir];

        if (playBoard[x + xdir][y + ydir] == pID || playBoard[x + xdir][y + ydir] == 0) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param x x-axis value
     * @param y y-axis value
     * @return boolean if indicate field is in bound of the playboard.
     */
    private boolean inBounds (int x, int y){
        return (x >= 0) && (x < playBoard.length) && (y >= 0) && (y < playBoard[x].length);
    }
}