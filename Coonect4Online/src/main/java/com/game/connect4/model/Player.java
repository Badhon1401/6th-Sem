package com.game.connect4.model;


public enum Player {
    NONE(0),
    PLAYER(1),
    AI(2);

    private final int value;

    Player(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public Player getOpponent() {
        if (this == PLAYER) {
            return AI;
        } else if (this == AI) {
            return PLAYER;
        }
        return NONE;
    }
}