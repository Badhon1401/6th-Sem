package com.ai.wumpus_world.service;

import com.ai.wumpus_world.model.*;
import com.ai.wumpus_world.logic.LogicalAgent;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class WumpusWorldService {
    private WumpusWorld world;
    private LogicalAgent agent;
    private boolean autoPlay = false;

    public WumpusWorldService() {
        generateRandomWorld();
    }

    public void generateRandomWorld() {
        world = new WumpusWorld(10, 10);
        world.generateRandom();
        agent = new LogicalAgent(world);
    }

    public void loadPredefinedWorld() {
        world = new WumpusWorld(10, 10);
        world.loadFromString(getSampleWorld());
        agent = new LogicalAgent(world);
    }

    private String getSampleWorld() {
        return "---P--P--- --W------- ------P--- -----P---- ---G------ W-----P--- ---------- P------W-- ---P--P--- ----------";
    }

    public void agentStep() {
        if (!world.isGameOver()) {
            agent.makeMove();
        }
    }

    public Map<String, Object> getGameState() {
        Map<String, Object> state = new HashMap<>();
        state.put("world", world.toMap());
        state.put("agent", agent.toMap());
        state.put("gameOver", world.isGameOver());
        state.put("score", world.getScore());
        state.put("message", world.getGameMessage());
        return state;
    }

    public Map<String, Object> getKnowledgeBaseState() {
        return agent.getKnowledgeBaseMap();
    }

    // Getters
    public WumpusWorld getCurrentWorld() { return world; }
    public LogicalAgent getAgent() { return agent; }
    public void setAutoPlay(boolean autoPlay) { this.autoPlay = autoPlay; }
}