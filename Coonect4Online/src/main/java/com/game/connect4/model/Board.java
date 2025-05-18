package com.game.connect4.model;

import com.game.connect4.utils.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Board {
    private Player[][] grid;
    private int[] columnHeights;
    private List<Move> moveHistory;

    public Board() {
        grid = new Player[Constants.ROWS][Constants.COLUMNS];
        columnHeights = new int[Constants.COLUMNS];
        moveHistory = new ArrayList<>();

        // Initialize the board with NONE player
        for (Player[] row : grid) {
            Arrays.fill(row, Player.NONE);
        }

        // Initialize column heights to 0
        Arrays.fill(columnHeights, 0);
    }


    public Board(Board board) {
        grid = new Player[Constants.ROWS][Constants.COLUMNS];
        columnHeights = new int[Constants.COLUMNS];
        moveHistory = new ArrayList<>(board.getMoveHistory());

        // Deep copy of the grid
        for (int i = 0; i < Constants.ROWS; i++) {
            grid[i] = Arrays.copyOf(board.grid[i], Constants.COLUMNS);
        }

        // Copy column heights
        System.arraycopy(board.columnHeights, 0, columnHeights, 0, Constants.COLUMNS);
    }


    public int makeMove(int column, Player player) {
        if (!isValidMove(column)) {
            return -1;
        }

        int row = Constants.ROWS - 1 - columnHeights[column];
        grid[row][column] = player;
        columnHeights[column]++;

        Move move = new Move(column, row, player);
        moveHistory.add(move);

        return row;
    }


    public void undoMove(int column) {
        if (columnHeights[column] > 0) {
            columnHeights[column]--;
            int row = Constants.ROWS - 1 - columnHeights[column];
            grid[row][column] = Player.NONE;

            if (!moveHistory.isEmpty()) {
                moveHistory.remove(moveHistory.size() - 1);
            }
        }
    }


    public boolean isValidMove(int column) {
        return column >= 0 && column < Constants.COLUMNS && columnHeights[column] < Constants.ROWS;
    }


    public List<Integer> getValidMoves() {
        List<Integer> validMoves = new ArrayList<>();
        for (int col = 0; col < Constants.COLUMNS; col++) {
            if (isValidMove(col)) {
                validMoves.add(col);
            }
        }
        return validMoves;
    }


    public boolean isFull() {
        for (int col = 0; col < Constants.COLUMNS; col++) {
            if (columnHeights[col] < Constants.ROWS) {
                return false;
            }
        }
        return true;
    }


    public boolean checkWin(int row, int column, Player player) {
        // Check horizontal
        if (checkDirection(row, column, 0, 1, player) + checkDirection(row, column, 0, -1, player) - 1 >= 4) {
            return true;
        }

        // Check vertical
        if (checkDirection(row, column, 1, 0, player) + checkDirection(row, column, -1, 0, player) - 1 >= 4) {
            return true;
        }

        // Check diagonal (top-left to bottom-right)
        if (checkDirection(row, column, -1, -1, player) + checkDirection(row, column, 1, 1, player) - 1 >= 4) {
            return true;
        }

        // Check diagonal (top-right to bottom-left)
        if (checkDirection(row, column, -1, 1, player) + checkDirection(row, column, 1, -1, player) - 1 >= 4) {
            return true;
        }

        return false;
    }


    private int checkDirection(int row, int column, int rowDelta, int colDelta, Player player) {
        int count = 0;
        int r = row;
        int c = column;

        while (r >= 0 && r < Constants.ROWS && c >= 0 && c < Constants.COLUMNS && grid[r][c] == player) {
            count++;
            r += rowDelta;
            c += colDelta;
        }

        return count;
    }

    public Player[][] getGrid() {
        return grid;
    }

    public Player getCell(int row, int col) {
        return grid[row][col];
    }

    public int[] getColumnHeights() {
        return columnHeights;
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }


    public Move getLastMove() {
        if (moveHistory.isEmpty()) {
            return null;
        }
        return moveHistory.get(moveHistory.size() - 1);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Constants.ROWS; i++) {
            for (int j = 0; j < Constants.COLUMNS; j++) {
                switch (grid[i][j]) {
                    case NONE:
                        sb.append("· ");
                        break;
                    case PLAYER:
                        sb.append("X ");
                        break;
                    case AI:
                        sb.append("O ");
                        break;
                }
            }
            sb.append("\n");
        }
        sb.append("---------------\n");
        sb.append("0 1 2 3 4 5 6\n");
        return sb.toString();
    }
}