package co.edu.triqui;

import java.util.Arrays;
import java.util.Random;

/** Pure Java rules; no dependency on the Android UI. */
public final class TicTacToeGame {
    public enum DifficultyLevel { Easy, Harder, Expert }
    private DifficultyLevel difficultyLevel = DifficultyLevel.Expert;
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel level) {
        if (level == null) throw new IllegalArgumentException("Missing difficulty");
        difficultyLevel = level;
    }
    public static final char HUMAN_PLAYER = 'X', COMPUTER_PLAYER = 'O', OPEN_SPOT = ' ';
    public static final int BOARD_SIZE = 9;
    private static final int[][] LINES = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
    private final char[] board = new char[9];
    private final Random random;
    public TicTacToeGame() { this(new Random()); }
    TicTacToeGame(Random random) { this.random = random; clearBoard(); }
    public void clearBoard() { Arrays.fill(board, OPEN_SPOT); }
    public char at(int i) { return board[i]; }
    public boolean setMove(char player, int location) {
        if ((player != 'X' && player != 'O') || location < 0 || location > 8 || board[location] != OPEN_SPOT || checkForWinner() != 0) return false;
        board[location] = player;
        return true;
    }
    public int[] winningLine() {
        for (int[] line : LINES) if (board[line[0]] != OPEN_SPOT && board[line[0]] == board[line[1]] && board[line[1]] == board[line[2]]) return line.clone();
        return new int[0];
    }
    public int checkForWinner() {
        int[] line = winningLine();
        if (line.length > 0) return board[line[0]] == 'X' ? 2 : 3;
        for (char c : board) if (c == OPEN_SPOT) return 0;
        return 1;
    }
    /** Suggest a legal move for the selected difficulty without changing the board. */
    public int getComputerMove() {
        if (checkForWinner() != 0) return -1;
        if (difficultyLevel == DifficultyLevel.Easy) return getRandomMove();
        int move = getWinningMove();
        if (move == -1 && difficultyLevel == DifficultyLevel.Expert) move = getBlockingMove();
        return move == -1 ? getRandomMove() : move;
    }
    public int getWinningMove() { return findCompletion(COMPUTER_PLAYER); }
    public int getBlockingMove() { return findCompletion(HUMAN_PLAYER); }
    private int findCompletion(char player) {
        if (checkForWinner() != 0) return -1;
        for (int i = 0; i < 9; i++) if (board[i] == OPEN_SPOT) {
            board[i] = player;
            boolean wins = winningLine().length > 0;
            board[i] = OPEN_SPOT;
            if (wins) return i;
        }
        return -1;
    }
    public int getRandomMove() {
        if (checkForWinner() != 0) return -1;
        int[] free = new int[9]; int count = 0;
        for (int i = 0; i < 9; i++) if (board[i] == OPEN_SPOT) free[count++] = i;
        return free[random.nextInt(count)];
    }
    public String snapshot() { return new String(board); }
    public void restore(String state) {
        if (state == null || state.length() != 9) throw new IllegalArgumentException("Invalid board");
        for (char c : state.toCharArray()) if (c != 'X' && c != 'O' && c != OPEN_SPOT) throw new IllegalArgumentException("Invalid mark");
        state.getChars(0, 9, board, 0);
    }
}
