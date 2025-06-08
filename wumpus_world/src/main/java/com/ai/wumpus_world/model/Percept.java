package com.ai.wumpus_world.model;

public class Percept {
    private boolean stench, breeze, glitter, bump, scream;

    public Percept() {}

    public Percept(boolean stench, boolean breeze, boolean glitter, boolean bump, boolean scream) {
        this.stench = stench;
        this.breeze = breeze;
        this.glitter = glitter;
        this.bump = bump;
        this.scream = scream;
    }

    // Getters
    public boolean hasStench() { return stench; }
    public boolean hasBreeze() { return breeze; }
    public boolean hasGlitter() { return glitter; }
    public boolean hasBump() { return bump; }
    public boolean hasScream() { return scream; }
}
