package co.edu.triqui;

import org.junit.Test;
import static org.junit.Assert.*;

public class TicTacToeGameTest {
    @Test public void difficultyDefaultsToExpert() {
        assertEquals(TicTacToeGame.DifficultyLevel.Expert, new TicTacToeGame().getDifficultyLevel());
    }
    @Test public void harderTakesWinAndExpertPrefersWinToBlock() {
        for (TicTacToeGame.DifficultyLevel level : new TicTacToeGame.DifficultyLevel[]{TicTacToeGame.DifficultyLevel.Harder, TicTacToeGame.DifficultyLevel.Expert}) {
            TicTacToeGame game = new TicTacToeGame(new java.util.Random(42)); game.setDifficultyLevel(level);
            game.restore("OO XX X  ");
            assertEquals(2, game.getComputerMove()); assertEquals("OO XX X  ", game.snapshot());
        }
    }
    @Test public void expertBlocksWithoutMutatingBoard() {
        TicTacToeGame game = new TicTacToeGame(); game.restore("XX  O    ");
        assertEquals(2, game.getComputerMove()); assertEquals("XX  O    ", game.snapshot());
    }
    @Test public void easyCanIgnoreWinAndHarderCanIgnoreBlock() {
        for (TicTacToeGame.DifficultyLevel level : new TicTacToeGame.DifficultyLevel[]{TicTacToeGame.DifficultyLevel.Easy, TicTacToeGame.DifficultyLevel.Harder}) {
            TicTacToeGame game = new TicTacToeGame(new java.util.Random(42)); game.setDifficultyLevel(level);
            game.restore(level == TicTacToeGame.DifficultyLevel.Easy ? "OO XX X  " : "XX  O    ");
            String before = game.snapshot(); boolean choseOther = false;
            for (int i = 0; i < 200; i++) {
                int move = game.getComputerMove(); assertEquals(' ', game.at(move));
                choseOther |= move != 2; assertEquals(before, game.snapshot());
            }
            assertTrue(choseOther);
        }
    }
    @Test public void detectsAllWinningLines() {
        int[][] lines = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] line : lines) for (char player : new char[]{'X','O'}) {
            TicTacToeGame game = new TicTacToeGame();
            for (int index : line) assertTrue(game.setMove(player, index));
            assertEquals(player == 'X' ? 2 : 3, game.checkForWinner());
            assertEquals(-1, game.getComputerMove());
        }
    }
    @Test public void handlesDrawInvalidMovesAndReset() {
        TicTacToeGame game = new TicTacToeGame(); game.restore("XOXXOOOXX");
        assertEquals(1, game.checkForWinner()); assertEquals(-1, game.getComputerMove());
        assertFalse(game.setMove('X', 0)); game.clearBoard();
        assertEquals(0, game.checkForWinner()); assertFalse(game.setMove('A', 0));
        assertFalse(game.setMove('X', -1)); assertTrue(game.setMove('X', 4));
        assertFalse(game.setMove('O', 4));
    }
}
