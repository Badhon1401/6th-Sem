package com.game.connect4.model;


public class Move {
    private int column;
    private int row;
    private Player player;
    private int score;

    public Move(int column) {
        this.column = column;
    }

    public Move(int column, int row, Player player) {
        this.column = column;
        this.row = row;
        this.player = player;
    }

    public Move(int column, int score) {
        this.column = column;
        this.score = score;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Move{" +
                "column=" + column +
                ", row=" + row +
                ", player=" + player +
                ", score=" + score +
                '}';
    }
}