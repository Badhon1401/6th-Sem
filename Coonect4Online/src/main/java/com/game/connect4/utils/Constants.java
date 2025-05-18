package com.game.connect4.utils;


public class Constants {
    // Board dimensions
    public static final int ROWS = 6;
    public static final int COLUMNS = 7;

    // Difficulty levels
    public static final int EASY = 1;
    public static final int MEDIUM = 3;
    public static final int HARD = 5;
    public static final int EXPERT = 7;

    // Evaluation scores
    public static final int WIN_SCORE = 1000000;
    public static final int LOSE_SCORE = -1000000;
    public static final int DRAW_SCORE = 0;

    // Column weights for heuristic evaluation (center columns are more valuable)
    public static final int[] COLUMN_WEIGHTS = {1, 2, 3, 4, 3, 2, 1};
}