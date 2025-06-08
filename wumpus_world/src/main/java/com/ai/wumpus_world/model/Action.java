package com.ai.wumpus_world.model;


public class Action {
    public enum Type {
        MOVE_FORWARD, TURN_LEFT, TURN_RIGHT, SHOOT, GRAB, CLIMB
    }

    private Type type;
    private Direction direction;

    public Action(Type type) {
        this.type = type;
    }

    public Action(Type type, Direction direction) {
        this.type = type;
        this.direction = direction;
    }

    public Type getType() { return type; }
    public Direction getDirection() { return direction; }
}
