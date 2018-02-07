import java.util.ArrayList;
import java.util.List;

public class MiniMax {
    boolean printOUT = true;
    IGameLogic.Winner compValue;
    double val = 1;
    // weight of depth - each recursive call of min- or max-value is weighted
    // less and less the further down in the decision tree (for 0 < val < 1)
    int depthToGo = 9;
    GameLogic gl;

    /**
     * instantiage MinMax Class with a GameLogic object as its only parameter.
     * This instance of the GameLogic serves as the root state of the minmaxDecision() call.
     * @param gl GameLogic Object --> root state of decision tree.
     */
    public MiniMax(GameLogic gl) {
        this.gl = gl;
    }

    /**
     * With this method we enter the recurive calculation of the min and max values for the corresponding deciion tree nodes.
     * The way it is set up, the minimaxDecision starts always with a max decision node as a root.
     * The player of the GameLogic which is passed to the instantiation of the MinMax Class is set a the Max player
     * and the other player is set as the Min player.
     * @return int action as a column index which says where the next coin has to be inserted.
     */
    public int minimaxDecision() {
        System.out.println("Calculating next Move...");
        long startTime = System.nanoTime();
        if (gl.getPlayerID() == 1) compValue = IGameLogic.Winner.PLAYER1;
        if (gl.getPlayerID() == 2) compValue = IGameLogic.Winner.PLAYER2;
        int maxAction = -1;
        double v = -100000000;
        int depth = 1;
        List<Double> tempStore = new ArrayList<>();

        for (int a : Actions(gl)) {
            //for some reason it is not doing the correct decision when using Double.Max and Double.Min instead of large values
            double utility = minValue(result(gl, a), -1000000, 1000000, depth) * val;
            tempStore.add(utility);
            if (utility > v) {
                v = utility;
                maxAction = a;
            }
        }
        System.out.println("Calc took " + ((System.nanoTime() - startTime) / 1000000));
        return maxAction;
    }

    /**
     *
     * @param s state of decision node (object).
     * @param alpha values on which we prune away branches that cannot possibly influence the final decision.
     * @param beta values on which we prune away branches that cannot possibly influence the final decision.
     * @param depth depth of the tree (current depth the recursive calculation is in)
     * @return double utility value of the state for MAX
     */
    private double maxValue(GameLogic s, double alpha, double beta, int depth) {
        depth++;
        if (cutOffTest(s, depth)) return evaluation(s);
        double v = -100000;
        List<Double> tempStore = new ArrayList<>();
        for (int a : Actions(s)) {
            double temp = minValue(result(s, a), alpha, beta, depth) * val;
            tempStore.add(temp);
            if (temp > v) {
                v = temp;
            }
            if (v >= beta) return v;
            if (v > alpha) alpha = v;
        }
        return v;
    }


    /**
     *
     * @param s state of decision node (object).
     * @param alpha values on which we prune away branches that cannot possibly influence the final decision.
     * @param beta values on which we prune away branches that cannot possibly influence the final decision.
     * @param depth depth of the tree (current depth the recursive calculation is in)
     * @return
     */
    private double minValue(GameLogic s, double alpha, double beta, int depth) {
        depth++;
        if (cutOffTest(s, depth)) return evaluation(s);
        double v = 100000;
        List<Double> tempStore = new ArrayList<>();
        for (int a : Actions(s)) {
            double temp = maxValue(result(s, a), alpha, beta, depth) * val;
            tempStore.add(temp);
            if (temp < v) {
                v = temp;
            }
            if (v <= alpha) return v;
            if (beta > v) beta = v;
        }
        return v;
    }

    /**
     * check if the state passed as an argument is a terminal state -> game finished.
     * @param s state to bet tested for its terminality
     * @return boolean
     */
    private boolean terminalTest(GameLogic s) {
        if (s.gameFinished().equals(GameLogic.Winner.NOT_FINISHED)) return false;
        return true;
    }

    /**
     *
     * @param s current state we are in
     * @param a action as int which is an index for a game boad column
     * @return GameLogic as a new instance which is in a new state (token of action a added).
     * It is important to note that this method returns a new instance of GameLogic as a state becuase if this was not
     * the case, then further changes on child nodes would change the parrents because the point to the same place in memory.
     */
    private GameLogic result(GameLogic s, int a) {
        GameLogic toRet = stateCopier(s);
        toRet.insertCoin(a, s.getPlayerID());
        int pIDtoSet = 1;
        if (s.getPlayerID() == 1) pIDtoSet = 2;
        toRet.setPlayerID(pIDtoSet);
        return toRet;
    }

    /**
     *
     * @param s state on which the utility has to be assessed.
     * @return double utility -1 win for Min, 0 for draw and 1 win for Max.
     */
    private double utility(GameLogic s) {
        IGameLogic.Winner comp = s.gameFinished();
        if (comp.equals(compValue)) return 1.0;
        else if (comp.equals(IGameLogic.Winner.TIE)) return 0.0;
        else if (!comp.equals(compValue)) return -1.0;
        System.out.println("this line should never be reached");
        return 0.0;
    }

    /**
     *
     * @param s current node in decision tree
     * @return int[] array of allowed moves, where each array element represents one index for a column in the play board.
     */
    private int[] Actions(GameLogic s) {
        return s.getPossibleMoves();
    }

    /**
     *
     * @param s current node in decision tree
     * @param depth current depth we are at in the decision tree
     * @return boolean we stop going further down the decision tree / based on depth limit
     *         or if the state s is a terminal state.
     */
    private boolean cutOffTest(GameLogic s, int depth) {
        if (terminalTest(s) || depth > depthToGo){
            //Debugger.printer(s);
            return true;
        }
        return false;
    }

    /**
     * Based on the countConnectionOfCoins function provided by the
     * state s we count the position value for both players for each coin played.
     * With the values for both players we compare and normalize the value and return a positive
     * utility if the playerID of the root decision node has a higher value and vice versa.
     * @param s state/GameLogic instance
     * @return double utility depending on the current state s. This utility is an estimation.
     */
    private double evaluation(GameLogic s) { //returns a utility guess
        if (terminalTest(s)) return utility(s);
        // Count number of X_XX, X__X _XX_ etc for each player
        // --> XO_X etc. does not count {X,X,X_} = 1.5 , {X,X,_,_} = 1
        // whoever has more of these is the estimated winner.
        double utilityPlayer = 0.0;
        double utilityOpponent = 0.0;
        for (int i = 0; i < s.playBoard.length; i++) {
            for (int n = 0; n < s.playBoard[i].length; n++) {
                if (s.playBoard[i][n] == 0) continue;
                else if (s.playBoard[i][n] == gl.getPlayerID()) {
                    utilityPlayer = utilityPlayer + s.countConnectionOfCoins(i, n, gl.getPlayerID());
                } else {
                    utilityOpponent = utilityOpponent + s.countConnectionOfCoins(i, n, oppositePlayerID(gl.getPlayerID()));
                }
            }
        }
        return estimateUtility(utilityPlayer, utilityOpponent);
    }

    /**
     *
     * @param positionalValuePlayer sum of positional value for root node player
     * @param positionalValueOpponent sum of positional value for root node opponent
     * @return double normalize utility based on estimated positional value
     */
    private double estimateUtility(double positionalValuePlayer, double positionalValueOpponent) {
        double toRet;
        if (positionalValuePlayer > positionalValueOpponent) {
            toRet = putInRelation(positionalValuePlayer, positionalValueOpponent);
        } else if (positionalValuePlayer < positionalValueOpponent) {
            toRet = -putInRelation(positionalValueOpponent, positionalValuePlayer);
        } else {
            toRet = 0.0;
        }
        return toRet;
    }

    private double putInRelation(double u1, double u2) {
        return (u1 - u2) / u1;
    }

    /**
     * I had to manually clone the state objects as the solution provided from the internet is too slow (DeepCopier) for our purposes.
     * The reason why we need clones is because in each sub node we want to add a different move which could not be done if they all point to the same object in the heap.
     */
    private GameLogic stateCopier(GameLogic s) {
        GameLogic toRet = new GameLogic();
        toRet.playBoard = getPlayBoardClone(s.playBoard);
        toRet.globalXPointer = s.globalXPointer;
        toRet.globalYPointer = s.globalYPointer;
        toRet.setPlayerID(s.getPlayerID());
        toRet.lastMovePlayerID = s.lastMovePlayerID;
        toRet.coinsPerCol = getLevelOfCoinPerColumnClone(s.coinsPerCol);
        toRet.x = s.x;
        toRet.y = s.y;
        return toRet;
    }

    private int[][] getPlayBoardClone(int[][] playBoard) {
        int[][] toRet = new int[playBoard.length][playBoard[0].length];
        for (int i = 0; i < playBoard.length; i++) {
            for (int n = 0; n < playBoard[0].length; n++) {
                toRet[i][n] = playBoard[i][n];
            }
        }
        return toRet;
    }

    public int[] getLevelOfCoinPerColumnClone(int[] levelOfCoinPerColumn) {
        int[] toRet = new int[levelOfCoinPerColumn.length];
        for (int i = 0; i < levelOfCoinPerColumn.length; i++) {
            toRet[i] = levelOfCoinPerColumn[i];
        }
        return toRet;
    }

    private int oppositePlayerID(int playerID) {
        if (playerID == 1) return 2;
        return 1;
    }
}

