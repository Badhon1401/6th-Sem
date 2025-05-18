package com.game.connect4.service;

import com.game.connect4.model.Board;
import com.game.connect4.model.Game;
import com.game.connect4.model.GameStatus;
import com.game.connect4.model.Move;
import com.game.connect4.model.Player;
import com.game.connect4.utils.Constants;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIService {


    public Move findBestMove(Game game) {
        Board board = game.getBoard();
        int depth = game.getDifficultyLevel();


        List<Integer> validMoves = board.getValidMoves();
        if (validMoves.isEmpty()) {
            return null;
        }


        if (validMoves.size() == 1) {
            return new Move(validMoves.get(0));
        }

        int bestScore = Integer.MIN_VALUE;
        int bestColumn = validMoves.get(0);


        for (int column : validMoves) {

            int row = board.makeMove(column, Player.AI);


            int score;


            if (board.checkWin(row, column, Player.AI)) {
                score = Constants.WIN_SCORE;
            } else {

                score = minimax(board, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            }


            board.undoMove(column);


            if (score > bestScore) {
                bestScore = score;
                bestColumn = column;
            }
        }

        return new Move(bestColumn, bestScore);
    }


    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizing) {

        if (depth == 0 || board.isFull()) {
            return evaluateBoard(board, depth);
        }

        List<Integer> validMoves = board.getValidMoves();

        if (isMaximizing) {
            int maxScore = Integer.MIN_VALUE;

            for (int column : validMoves) {
                int row = board.makeMove(column, Player.AI);

                int score;
                if (board.checkWin(row, column, Player.AI)) {
                    score = Constants.WIN_SCORE;
                } else {
                    score = minimax(board, depth - 1, alpha, beta, false);
                }

                board.undoMove(column);

                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score);

                if (beta <= alpha) {
                    break;
                }
            }

            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;

            for (int column : validMoves) {
                int row = board.makeMove(column, Player.PLAYER);

                int score;
                if (board.checkWin(row, column, Player.PLAYER)) {
                    score = Constants.LOSE_SCORE;
                } else {
                    score = minimax(board, depth - 1, alpha, beta, true);
                }

                board.undoMove(column);

                minScore = Math.min(minScore, score);
                beta = Math.min(beta, score);

                if (beta <= alpha) {
                    break; // Alpha cutoff
                }
            }

            return minScore;
        }
    }


    private int evaluateBoard(Board board, int depth) {

        Move lastMove = board.getLastMove();
        if (lastMove == null) {
            return 0;
        }

        int score = 0;
        Player[][] grid = board.getGrid();


        if (board.checkWin(lastMove.getRow(), lastMove.getColumn(), lastMove.getPlayer())) {
            if (lastMove.getPlayer() == Player.AI) {
                return Constants.WIN_SCORE + depth;
            } else {
                return Constants.LOSE_SCORE - depth;
            }
        }

        else if (board.isFull()) {
            return Constants.DRAW_SCORE;
        }


        for (int col = 0; col < Constants.COLUMNS; col++) {
            for (int row = 0; row < Constants.ROWS; row++) {
                if (grid[row][col] == Player.AI) {
                    score += Constants.COLUMN_WEIGHTS[col];
                } else if (grid[row][col] == Player.PLAYER) {
                    score -= Constants.COLUMN_WEIGHTS[col];
                }
            }
        }


        score += evaluateLines(board, Player.AI) - evaluateLines(board, Player.PLAYER);

        return score;
    }


    private int evaluateLines(Board board, Player player) {
        int score = 0;
        Player[][] grid = board.getGrid();


        for (int row = 0; row < Constants.ROWS; row++) {
            for (int col = 0; col <= Constants.COLUMNS - 4; col++) {
                score += evaluateLine(grid, row, col, 0, 1, player);
            }
        }


        for (int row = 0; row <= Constants.ROWS - 4; row++) {
            for (int col = 0; col < Constants.COLUMNS; col++) {
                score += evaluateLine(grid, row, col, 1, 0, player);
            }
        }


        for (int row = 0; row <= Constants.ROWS - 4; row++) {
            for (int col = 0; col <= Constants.COLUMNS - 4; col++) {
                score += evaluateLine(grid, row, col, 1, 1, player);
            }
        }


        for (int row = 0; row <= Constants.ROWS - 4; row++) {
            for (int col = 3; col < Constants.COLUMNS; col++) {
                score += evaluateLine(grid, row, col, 1, -1, player);
            }
        }

        return score;
    }

    private int evaluateLine(Player[][] grid, int row, int col, int rowDelta, int colDelta, Player player) {
        Player opponent = player.getOpponent();
        int playerCount = 0;
        int emptyCount = 0;

        for (int i = 0; i < 4; i++) {
            int r = row + i * rowDelta;
            int c = col + i * colDelta;

            if (grid[r][c] == player) {
                playerCount++;
            } else if (grid[r][c] == Player.NONE) {
                emptyCount++;
            } else {

                return 0;
            }
        }

        // Score based on number of player pieces
        if (playerCount == 3 && emptyCount == 1) {
            return 100; // Three in a row with an empty space
        } else if (playerCount == 2 && emptyCount == 2) {
            return 10; // Two in a row with two empty spaces
        } else if (playerCount == 1 && emptyCount == 3) {
            return 1; // One piece with three empty spaces
        }

        return 0;
    }

    public int evaluatePlayerMove(Game game, int column) {

        Board boardCopy = new Board(game.getBoard());

        int score = minimax(boardCopy, game.getDifficultyLevel(),
                Integer.MIN_VALUE, Integer.MAX_VALUE, true);

        return -score;
    }
}