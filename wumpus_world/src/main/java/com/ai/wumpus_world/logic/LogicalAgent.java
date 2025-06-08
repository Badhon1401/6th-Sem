package com.ai.wumpus_world.logic;

import com.ai.wumpus_world.model.*;

import java.util.*;

public class LogicalAgent {
    private WumpusWorld world;
    private Position currentPos;
    private Direction currentDirection;
    private KnowledgeBase kb;
    private Set<Position> visitedPositions;
    private Stack<Position> pathStack;
    private boolean hasGold = false;
    private boolean hasArrow = true;
    private List<String> thoughtProcess;

    public LogicalAgent(WumpusWorld world) {
        this.world = world;
        this.currentPos = new Position(0, 0);
        this.currentDirection = Direction.EAST;
        this.kb = new KnowledgeBase();
        this.visitedPositions = new HashSet<>();
        this.pathStack = new Stack<>();
        this.thoughtProcess = new ArrayList<>();

        // Initialize knowledge base with basic facts
        initializeKnowledgeBase();
    }

    private void initializeKnowledgeBase() {
        // Add basic rules to KB
        kb.addRule("If stench in adjacent cell, then wumpus nearby");
        kb.addRule("If breeze in adjacent cell, then pit nearby");
        kb.addRule("If glitter, then gold is here");
        kb.addRule("Safe cells have no pit and no live wumpus");

        // Mark starting position as safe
        kb.addFact("Safe(0,0)");
        visitedPositions.add(currentPos);

        addThought("Agent initialized at (0,0). Starting exploration...");
    }

    public void makeMove() {
        if (world.isGameOver()) return;

        // Get current percepts
        Percept percept = world.getPercept(currentPos);
        updateKnowledgeBase(percept);

        // Decide next action based on logical reasoning
        Action nextAction = decideAction(percept);

        if (nextAction != null) {
            executeAction(nextAction);
        }
    }

    private void updateKnowledgeBase(Percept percept) {
        String pos = currentPos.x + "," + currentPos.y;

        // Add percept facts
        if (percept.hasStench()) {
            kb.addFact("Stench(" + pos + ")");
            addThought("Detected stench at " + currentPos + " - Wumpus nearby!");
            inferWumpusLocations();
        } else {
            kb.addFact("NoStench(" + pos + ")");
        }

        if (percept.hasBreeze()) {
            kb.addFact("Breeze(" + pos + ")");
            addThought("Detected breeze at " + currentPos + " - Pit nearby!");
            inferPitLocations();
        } else {
            kb.addFact("NoBreeze(" + pos + ")");
        }

        if (percept.hasGlitter()) {
            kb.addFact("Glitter(" + pos + ")");
            addThought("Gold detected at " + currentPos + "!");
        }

        // Mark current position as safe and visited
        kb.addFact("Safe(" + pos + ")");
        kb.addFact("Visited(" + pos + ")");
        visitedPositions.add(new Position(currentPos.x, currentPos.y));
    }

    private void inferWumpusLocations() {
        // Use logical inference to deduce possible wumpus locations
        for (Position neighbor : getAdjacentPositions(currentPos)) {
            if (world.isValidPosition(neighbor.x, neighbor.y)) {
                String neighborPos = neighbor.x + "," + neighbor.y;
                if (!kb.hasFact("Safe(" + neighborPos + ")")) {
                    kb.addFact("PossibleWumpus(" + neighborPos + ")");
                    addThought("Inferred: Wumpus might be at " + neighbor);
                }
            }
        }
    }

    private void inferPitLocations() {
        // Use logical inference to deduce possible pit locations
        for (Position neighbor : getAdjacentPositions(currentPos)) {
            if (world.isValidPosition(neighbor.x, neighbor.y)) {
                String neighborPos = neighbor.x + "," + neighbor.y;
                if (!kb.hasFact("Safe(" + neighborPos + ")")) {
                    kb.addFact("PossiblePit(" + neighborPos + ")");
                    addThought("Inferred: Pit might be at " + neighbor);
                }
            }
        }
    }

    private Action decideAction(Percept percept) {
        addThought("Analyzing situation at " + currentPos + "...");

        // If gold is here, grab it
        if (percept.hasGlitter() && !hasGold) {
            addThought("Decision: Grabbing gold!");
            return new Action(Action.Type.GRAB);
        }

        // If we have gold, head back to start
        if (hasGold) {
            addThought("Have gold, returning to start...");
            return planReturnPath();
        }

        // Find safe unexplored position
        Position nextPos = findSafeUnexploredPosition();
        if (nextPos != null) {
            addThought("Decision: Moving to safe unexplored position " + nextPos);
            return new Action(Action.Type.MOVE_FORWARD, getDirectionTo(nextPos));
        }

        // If no safe moves, try probabilistic reasoning
        nextPos = findBestProbabilisticMove();
        if (nextPos != null) {
            addThought("Decision: Taking calculated risk, moving to " + nextPos);
            return new Action(Action.Type.MOVE_FORWARD, getDirectionTo(nextPos));
        }

        // Consider shooting if we can deduce wumpus location
        Position wumpusPos = deduceWumpusLocation();
        if (wumpusPos != null && hasArrow) {
            addThought("Decision: Shooting arrow at suspected Wumpus location " + wumpusPos);
            return new Action(Action.Type.SHOOT, getDirectionTo(wumpusPos));
        }

        addThought("No good moves available, staying put");
        return null;
    }

    private Position findSafeUnexploredPosition() {
        for (Position neighbor : getAdjacentPositions(currentPos)) {
            if (world.isValidPosition(neighbor.x, neighbor.y) &&
                    !visitedPositions.contains(neighbor) &&
                    isSafePosition(neighbor)) {
                return neighbor;
            }
        }
        return null;
    }

    private Position findBestProbabilisticMove() {
        double bestProbability = 0.0;
        Position bestMove = null;

        for (Position neighbor : getAdjacentPositions(currentPos)) {
            if (world.isValidPosition(neighbor.x, neighbor.y) &&
                    !visitedPositions.contains(neighbor)) {

                double safeProbability = calculateSafeProbability(neighbor);
                if (safeProbability > bestProbability) {
                    bestProbability = safeProbability;
                    bestMove = neighbor;
                }
            }
        }

        return bestProbability > 0.3 ? bestMove : null; // Only if >30% safe
    }

    private double calculateSafeProbability(Position pos) {
        // Simple probabilistic reasoning based on gathered evidence
        String posStr = pos.x + "," + pos.y;

        if (kb.hasFact("PossibleWumpus(" + posStr + ")") ||
                kb.hasFact("PossiblePit(" + posStr + ")")) {
            return 0.2; // Low probability if danger suspected
        }

        // Check surrounding evidence
        int dangerIndicators = 0;
        int totalIndicators = 0;

        for (Position adjacent : getAdjacentPositions(pos)) {
            if (visitedPositions.contains(adjacent)) {
                totalIndicators++;
                String adjStr = adjacent.x + "," + adjacent.y;
                if (kb.hasFact("Stench(" + adjStr + ")") || kb.hasFact("Breeze(" + adjStr + ")")) {
                    dangerIndicators++;
                }
            }
        }

        if (totalIndicators == 0) return 0.5; // No information
        return 1.0 - ((double) dangerIndicators / totalIndicators);
    }

    private boolean isSafePosition(Position pos) {
        String posStr = pos.x + "," + pos.y;
        return kb.hasFact("Safe(" + posStr + ")") ||
                (!kb.hasFact("PossibleWumpus(" + posStr + ")") &&
                        !kb.hasFact("PossiblePit(" + posStr + ")"));
    }

    private Position deduceWumpusLocation() {
        // Try to deduce exact wumpus location using logical reasoning
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                Position pos = new Position(x, y);
                if (canDeduceWumpusAt(pos)) {
                    return pos;
                }
            }
        }
        return null;
    }

    private boolean canDeduceWumpusAt(Position pos) {
        String posStr = pos.x + "," + pos.y;
        if (kb.hasFact("Safe(" + posStr + ")")) return false;

        // Check if all adjacent visited cells have stench
        List<Position> adjacentVisited = new ArrayList<>();
        for (Position adj : getAdjacentPositions(pos)) {
            if (visitedPositions.contains(adj)) {
                adjacentVisited.add(adj);
            }
        }

        if (adjacentVisited.size() >= 2) {
            boolean allHaveStench = true;
            for (Position adj : adjacentVisited) {
                String adjStr = adj.x + "," + adj.y;
                if (!kb.hasFact("Stench(" + adjStr + ")")) {
                    allHaveStench = false;
                    break;
                }
            }
            return allHaveStench;
        }

        return false;
    }

    private Action planReturnPath() {
        // A* pathfinding to return to (0,0)
        Position target = new Position(0, 0);
        List<Position> path = findPath(currentPos, target);

        if (!path.isEmpty()) {
            Position nextStep = path.get(0);
            return new Action(Action.Type.MOVE_FORWARD, getDirectionTo(nextStep));
        }

        return null;
    }

    private List<Position> findPath(Position start, Position goal) {
        // Simple A* implementation
        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Set<Position> closedSet = new HashSet<>();
        Map<Position, Position> cameFrom = new HashMap<>();
        Map<Position, Integer> gScore = new HashMap<>();

        openSet.add(new PathNode(start, 0, manhattanDistance(start, goal)));
        gScore.put(start, 0);

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();

            if (current.pos.equals(goal)) {
                return reconstructPath(cameFrom, current.pos);
            }

            closedSet.add(current.pos);

            for (Position neighbor : getAdjacentPositions(current.pos)) {
                if (!world.isValidPosition(neighbor.x, neighbor.y) ||
                        closedSet.contains(neighbor) ||
                        !isSafePosition(neighbor)) {
                    continue;
                }

                int tentativeGScore = gScore.get(current.pos) + 1;

                if (!gScore.containsKey(neighbor) || tentativeGScore < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current.pos);
                    gScore.put(neighbor, tentativeGScore);
                    int fScore = tentativeGScore + manhattanDistance(neighbor, goal);
                    openSet.add(new PathNode(neighbor, tentativeGScore, fScore));
                }
            }
        }

        return new ArrayList<>();
    }

    private List<Position> reconstructPath(Map<Position, Position> cameFrom, Position current) {
        List<Position> path = new ArrayList<>();
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            if (!current.equals(this.currentPos)) {
                path.add(0, current);
            }
        }
        return path;
    }

    private int manhattanDistance(Position a, Position b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private List<Position> getAdjacentPositions(Position pos) {
        List<Position> adjacent = new ArrayList<>();
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        for (int[] dir : directions) {
            adjacent.add(new Position(pos.x + dir[0], pos.y + dir[1]));
        }

        return adjacent;
    }

    private Direction getDirectionTo(Position target) {
        int dx = target.x - currentPos.x;
        int dy = target.y - currentPos.y;

        if (dx > 0) return Direction.EAST;
        if (dx < 0) return Direction.WEST;
        if (dy > 0) return Direction.SOUTH;
        if (dy < 0) return Direction.NORTH;

        return currentDirection; // No movement needed
    }

    private void executeAction(Action action) {
        ActionResult result = world.performAction(action, currentPos);

        if (result.isSuccess()) {
            switch (action.getType()) {
                case MOVE_FORWARD:
                    Position newPos = getNewPosition(currentPos, action.getDirection());
                    if (world.isValidPosition(newPos.x, newPos.y)) {
                        pathStack.push(currentPos);
                        currentPos = newPos;
                        currentDirection = action.getDirection();
                        addThought("Moved to " + currentPos);
                    }
                    break;
                case GRAB:
                    hasGold = true;
                    addThought("Successfully grabbed gold!");
                    break;
                case SHOOT:
                    hasArrow = false;
                    if (result.hasScream()) {
                        addThought("Arrow hit! Wumpus is dead!");
                        kb.addFact("WumpusDead");
                        updateSafetyAfterWumpusDeath();
                    } else {
                        addThought("Arrow missed the target");
                    }
                    break;
            }
        } else {
            addThought("Action failed: " + result.getMessage());
        }
    }

    private void updateSafetyAfterWumpusDeath() {
        // Mark previously dangerous positions as potentially safe
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                String pos = x + "," + y;
                if (kb.hasFact("PossibleWumpus(" + pos + ")")) {
                    kb.removeFact("PossibleWumpus(" + pos + ")");
                    kb.addFact("Safe(" + pos + ")");
                }
            }
        }
    }

    private Position getNewPosition(Position pos, Direction direction) {
        switch (direction) {
            case NORTH: return new Position(pos.x, pos.y - 1);
            case SOUTH: return new Position(pos.x, pos.y + 1);
            case EAST: return new Position(pos.x + 1, pos.y);
            case WEST: return new Position(pos.x - 1, pos.y);
            default: return pos;
        }
    }

    private void addThought(String thought) {
        thoughtProcess.add("[" + System.currentTimeMillis() + "] " + thought);
        if (thoughtProcess.size() > 20) {
            thoughtProcess.remove(0); // Keep only recent thoughts
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("position", currentPos);
        map.put("direction", currentDirection);
        map.put("hasGold", hasGold);
        map.put("hasArrow", hasArrow);
        map.put("visitedPositions", visitedPositions);
        map.put("thoughtProcess", thoughtProcess);
        return map;
    }

    public Map<String, Object> getKnowledgeBaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("facts", kb.getFacts());
        map.put("rules", kb.getRules());
        map.put("thoughtProcess", thoughtProcess);
        map.put("visitedPositions", visitedPositions);
        return map;
    }
}



