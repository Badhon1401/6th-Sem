package com.ai.wumpus_world.logic;

import com.ai.wumpus_world.model.Position;


public class PathNode implements Comparable<PathNode> {
    Position pos;
    int gScore;
    int fScore;

    PathNode(Position pos, int gScore, int fScore) {
        this.pos = pos;
        this.gScore = gScore;
        this.fScore = fScore;
    }

    @Override
    public int compareTo(PathNode other) {
        return Integer.compare(this.fScore, other.fScore);
    }
}

