package com.ai.wumpus_world.model;

public class Cell {
    private boolean wumpus = false;
    private boolean pit = false;
    private boolean gold = false;
    private boolean stench = false;
    private boolean breeze = false;
    private boolean glitter = false;
    private boolean visited = false;

    // Getters and Setters
    public boolean hasWumpus() { return wumpus; }
    public void setWumpus(boolean wumpus) { this.wumpus = wumpus; }

    public boolean hasPit() { return pit; }
    public void setPit(boolean pit) { this.pit = pit; }

    public boolean hasGold() { return gold; }
    public void setGold(boolean gold) { this.gold = gold; }

    public boolean hasStench() { return stench; }
    public void setStench(boolean stench) { this.stench = stench; }

    public boolean hasBreeze() { return breeze; }
    public void setBreeze(boolean breeze) { this.breeze = breeze; }

    public boolean hasGlitter() { return glitter; }
    public void setGlitter(boolean glitter) { this.glitter = glitter; }

    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }

    public Object[] toArray() {
        return new Object[]{wumpus, pit, gold, stench, breeze, glitter, visited};
    }
}
