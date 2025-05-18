package com.game.connect4.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Game {
    private String id;
    private Board board;
    private GameStatus status;
    private Player currentPlayer;
    private int difficultyLevel;
    private List<String> gameAnalysis;

    public Game(int difficultyLevel) {
        this.id = UUID.randomUUID().toString();
        this.board = new Board();
        this.status = GameStatus.IN_PROGRESS;
        this.currentPlayer = Player.PLAYER; // Player starts first
        this.difficultyLevel = difficultyLevel;
        this.gameAnalysis = new ArrayList<>();
    }


    public void resetGame() {
        this.board = new Board();
        this.status = GameStatus.IN_PROGRESS;
        this.currentPlayer = Player.PLAYER;
        this.gameAnalysis.clear();
    }


    public int makeMove(int column) {
        if (status != GameStatus.IN_PROGRESS) {
            return -1;
        }

        int row = board.makeMove(column, currentPlayer);
        if (row == -1) {
            return -1;
        }

        // Check for win
        if (board.checkWin(row, column, currentPlayer)) {
            status = (currentPlayer == Player.PLAYER) ? GameStatus.PLAYER_WIN : GameStatus.AI_WIN;
        }
        // Check for draw
        else if (board.isFull()) {
            status = GameStatus.DRAW;
        }

        // Switch players if game is still in progress
        if (status == GameStatus.IN_PROGRESS) {
            currentPlayer = currentPlayer.getOpponent();
        }

        return row;
    }

    public String getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public GameStatus getStatus() {
        return status;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public List<String> getGameAnalysis() {
        return gameAnalysis;
    }

    public void addAnalysis(String analysis) {
        this.gameAnalysis.add(analysis);
    }


    public Game copy() {
        Game gameCopy = new Game(this.difficultyLevel);
        gameCopy.board = new Board(this.board);
        gameCopy.status = this.status;
        gameCopy.currentPlayer = this.currentPlayer;
        return gameCopy;
    }
}