package com.ai.wumpus_world.model;

public class ActionResult {
    private boolean success;
    private String message;
    private boolean bump = false;
    private boolean scream = false;

    public ActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ActionResult(boolean success, String message, boolean bump) {
        this.success = success;
        this.message = message;
        this.bump = bump;
    }

    public ActionResult(boolean success, String message, boolean bump, boolean scream) {
        this.success = success;
        this.message = message;
        this.bump = bump;
        this.scream = scream;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public boolean hasBump() { return bump; }
    public boolean hasScream() { return scream; }
}