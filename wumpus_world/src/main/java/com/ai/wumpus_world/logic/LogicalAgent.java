package com.ai.wumpus_world.logic;

import com.ai.wumpus_world.model.*;
import java.util.*;

public class LogicalAgent {

    private WumpusWorld world;
    private Position currentPos;
    private Direction currentDirection;
    private KnowledgeBase kb;
    private Set<Position> visitedPositions;
    private Set<Position> safePositions;
    private Set<Position> dangerousPositions;
    private boolean hasGold = false;
    private boolean hasArrow = true;
    private boolean wumpusKilled = false;
    private List<String> thoughtProcess;
    private Map<Position, Double> riskAssessment;
    private Position goldPosition = null;
    private Position wumpusPosition = null;

    // Strategic constants
    private static final double SAFE_THRESHOLD = 0.8;
    private static final double RISK_THRESHOLD = 0.3;
    private static final int MAX_EXPLORATION_MOVES = 50;
    private int moveCount = 0;

    public LogicalAgent(WumpusWorld world) {
        this.world = world;
        this.currentPos = new Position(0, 0);
        this.currentDirection = Direction.EAST;
        this.kb = new KnowledgeBase();
        this.visitedPositions = new HashSet<>();
        this.safePositions = new HashSet<>();
        this.dangerousPositions = new HashSet<>();
        this.thoughtProcess = new ArrayList<>();
        this.riskAssessment = new HashMap<>();

        initializeKnowledgeBase();
    }

    private void initializeKnowledgeBase() {
        // Add sophisticated Wumpus World rules
        kb.addRule("Stench(x,y) => Adjacent(x,y,wx,wy) & Wumpus(wx,wy)");
        kb.addRule("Breeze(x,y) => Adjacent(x,y,px,py) & Pit(px,py)");
        kb.addRule("~Stench(x,y) & Visited(x,y) => ~Wumpus(x+1,y) & ~Wumpus(x-1,y) & ~Wumpus(x,y+1) & ~Wumpus(x,y-1)");
        kb.addRule("~Breeze(x,y) & Visited(x,y) => ~Pit(x+1,y) & ~Pit(x-1,y) & ~Pit(x,y+1) & ~Pit(x,y-1)");
        kb.addRule("Glitter(x,y) => Gold(x,y)");
        kb.addRule("WumpusDead => ~Danger(x,y) for all Wumpus(x,y)");

        String startPos = formatPosition(currentPos);
        kb.addFact("Safe(" + startPos + ")");
        kb.addFact("Visited(" + startPos + ")");
        kb.addFact("~Stench(" + startPos + ")");
        kb.addFact("~Breeze(" + startPos + ")");

        safePositions.add(new Position(currentPos.x, currentPos.y));
        visitedPositions.add(new Position(currentPos.x, currentPos.y));

        addThought("Advanced LogicalAgent initialized with sophisticated reasoning");
        addThought("Starting position (0,0) marked as safe");
    }

    public void makeMove() {
        if (world.isGameOver()) return;

        moveCount++;
        addThought("=== MOVE " + moveCount + " ===");

        // Get and process current percepts
        Percept percept = world.getPercept(currentPos);
        updateKnowledgeBase(percept);

        // Perform advanced logical reasoning
        performAdvancedReasoning();

        // Update risk assessment
        updateRiskAssessment();

        // Decide next action using strategic planning
        Action nextAction = strategicDecisionMaking(percept);

        if (nextAction != null) {
            executeAction(nextAction);
        } else {
            addThought("No valid action determined - staying put");
        }

        // Print reasoning for debugging
        if (moveCount % 5 == 0) {
            printCurrentKnowledge();
        }
    }

    private void updateKnowledgeBase(Percept percept) {
        String posStr = formatPosition(currentPos);
        addThought("Processing percepts at " + currentPos);

        // Add percept facts with proper negation
        if (percept.hasStench()) {
            kb.addFact("Stench(" + posStr + ")");
            addThought("STENCH detected! Wumpus is adjacent");
        } else {
            kb.addFact("~Stench(" + posStr + ")");
            addThought("No stench - adjacent cells safe from Wumpus");
        }

        if (percept.hasBreeze()) {
            kb.addFact("Breeze(" + posStr + ")");
            addThought("BREEZE detected! Pit is adjacent");
        } else {
            kb.addFact("~Breeze(" + posStr + ")");
            addThought("No breeze - adjacent cells safe from pits");
        }

        if (percept.hasGlitter()) {
            kb.addFact("Glitter(" + posStr + ")");
            goldPosition = new Position(currentPos.x, currentPos.y);
            addThought("GOLD found at " + currentPos + "!");
        }

        if (percept.hasScream()) {
            kb.addFact("Scream");
            kb.addFact("WumpusDead");
            wumpusKilled = true;
            addThought("WUMPUS KILLED! All Wumpus threats eliminated");
        }

        // Mark current position as visited and safe
        kb.addFact("Visited(" + posStr + ")");
        kb.addFact("Safe(" + posStr + ")");

        visitedPositions.add(new Position(currentPos.x, currentPos.y));
        safePositions.add(new Position(currentPos.x, currentPos.y));
    }

    private void performAdvancedReasoning() {
        addThought("Performing advanced logical reasoning...");

        // Use resolution theorem proving for complex inferences
        performSafetyInference();
        performThreatLocalization();
        performStrategicAnalysis();

        addThought("Advanced reasoning complete");
    }

    private void performSafetyInference() {
        List<Position> candidates = getAdjacentUnvisitedPositions();

        for (Position pos : candidates) {
            String posStr = formatPosition(pos);
            boolean isSafe = true;
            boolean isDangerous = false;

            // Check all adjacent visited positions
            List<Position> adjacentVisited = getAdjacentVisitedPositions(pos);

            for (Position adj : adjacentVisited) {
                String adjStr = formatPosition(adj);

                // If adjacent cell has stench/breeze, this position could be dangerous
                if (kb.hasFact("Stench(" + adjStr + ")")) {
                    isDangerous = true;
                    break;
                }
                if (kb.hasFact("Breeze(" + adjStr + ")")) {
                    isDangerous = true;
                    break;
                }
            }

            // Check if all adjacent visited positions are safe
            if (!isDangerous) {
                boolean allAdjacentSafe = true;
                for (Position adj : adjacentVisited) {
                    String adjStr = formatPosition(adj);
                    if (!kb.hasFact("~Stench(" + adjStr + ")") || !kb.hasFact("~Breeze(" + adjStr + ")")) {
                        allAdjacentSafe = false;
                        break;
                    }
                }

                if (allAdjacentSafe && !adjacentVisited.isEmpty()) {
                    safePositions.add(pos);
                    kb.addFact("Safe(" + posStr + ")");
                    addThought("Logically deduced: " + pos + " is SAFE (no danger indicators)");
                }
            } else {
                dangerousPositions.add(pos);
                addThought("Logically deduced: " + pos + " is DANGEROUS (danger indicators present)");
            }
        }
    }

    private void performThreatLocalization() {
        if (wumpusKilled) return;

        // Try to precisely locate the Wumpus using logical deduction
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                Position pos = new Position(x, y);
                if (visitedPositions.contains(pos)) continue;

                String posStr = formatPosition(pos);

                // Use resolution to determine if Wumpus is definitely at this position
                if (kb.query("Wumpus(" + posStr + ")")) {
                    wumpusPosition = pos;
                    addThought("WUMPUS LOCATED at " + pos + " through logical deduction!");
                    break;
                }
            }
        }
    }

    private void performStrategicAnalysis() {

        int safeCells = safePositions.size();
        int dangerousCells = dangerousPositions.size();
        int unknownCells = (world.getWidth() * world.getHeight()) - visitedPositions.size();

        addThought("Strategic Analysis: " + safeCells + " safe, " +
                dangerousCells + " dangerous, " + unknownCells + " unknown");

        if (hasGold) {
            addThought("Strategy: RETURN TO START with gold");
        } else if (goldPosition != null) {
            addThought("Strategy: NAVIGATE TO GOLD at " + goldPosition);
        } else if (wumpusPosition != null && hasArrow) {
            addThought("Strategy: ELIMINATE WUMPUS threat at " + wumpusPosition);
        } else {
            addThought("Strategy: INTELLIGENT EXPLORATION");
        }
    }

    private void updateRiskAssessment() {
        riskAssessment.clear();

        for (Position pos : getAdjacentUnvisitedPositions()) {
            double risk = calculatePositionRisk(pos);
            riskAssessment.put(pos, risk);
        }
    }

    private double calculatePositionRisk(Position pos) {
        if (safePositions.contains(pos)) return 0.0;
        if (dangerousPositions.contains(pos)) return 1.0;

        // Check KB for explicit safety/danger facts
        String posStr = formatPosition(pos);
        if (kb.hasFact("Safe(" + posStr + ")")) return 0.0;
        if (kb.hasFact("PossibleWumpus(" + posStr + ")") ||
                kb.hasFact("PossiblePit(" + posStr + ")")) return 0.9;

        // Analyze adjacent visited positions for danger indicators
        List<Position> adjacentVisited = getAdjacentVisitedPositions(pos);
        if (adjacentVisited.isEmpty()) return 0.7; // Unknown area

        int dangerIndicators = 0;
        int safeIndicators = 0;

        for (Position adj : adjacentVisited) {
            String adjStr = formatPosition(adj);
            if (kb.hasFact("Stench(" + adjStr + ")") || kb.hasFact("Breeze(" + adjStr + ")")) {
                dangerIndicators++;
            } else if (kb.hasFact("~Stench(" + adjStr + ")") && kb.hasFact("~Breeze(" + adjStr + ")")) {
                safeIndicators++;
            }
        }

        // More sophisticated risk calculation
        if (safeIndicators > 0 && dangerIndicators == 0) {
            return 0.1; // Very safe
        } else if (dangerIndicators > 0 && safeIndicators == 0) {
            return 0.95; // Very dangerous
        } else if (dangerIndicators > safeIndicators) {
            return 0.8; // Likely dangerous
        } else {
            return 0.3; // Moderately safe
        }
    }

    private Action strategicDecisionMaking(Percept percept) {
        addThought("Strategic decision making...");

        if (percept.hasGlitter() && !hasGold) {
            addThought("DECISION: Grab gold!");
            return new Action(Action.Type.GRAB);
        }

        if (hasGold) {
            addThought("DECISION: Return to start with gold");
            return planOptimalReturn();
        }

        if (goldPosition != null && !hasGold) {
            addThought("DECISION: Navigate to gold at " + goldPosition);
            return planPathToPosition(goldPosition);
        }

        // Priority 4: Eliminate Wumpus if located and have arrow
        if (wumpusPosition != null && hasArrow && !wumpusKilled) {
            if (isAdjacentTo(currentPos, wumpusPosition)) {
                Direction shootDir = getDirectionTo(wumpusPosition);
                addThought("DECISION: Shoot Wumpus at " + wumpusPosition);
                return new Action(Action.Type.SHOOT, shootDir);
            } else {
                addThought("DECISION: Move to shooting position for Wumpus");
                return planPathToAdjacentPosition(wumpusPosition);
            }
        }

        // Priority 5: Intelligent exploration
        return planIntelligentExploration();
    }

    private Action planIntelligentExploration() {
        addThought("Planning intelligent exploration...");

        // First, try to find definitely safe adjacent positions
        List<Position> safeAdjacent = new ArrayList<>();
        for (Position pos : getAdjacentUnvisitedPositions()) {
            if (safePositions.contains(pos) || riskAssessment.getOrDefault(pos, 1.0) < 0.2) {
                safeAdjacent.add(pos);
            }
        }

        if (!safeAdjacent.isEmpty()) {
            Position target = safeAdjacent.get(0);
            addThought("DECISION: Move to confirmed safe position " + target);
            return moveToPosition(target);
        }

        // If no safe adjacent positions, find path to nearest safe unexplored area
        Position explorationTarget = findNearestSafeUnexplored();
        if (explorationTarget != null) {
            addThought("DECISION: Navigate to safe unexplored area " + explorationTarget);
            return planPathToPosition(explorationTarget);
        }

        // Last resort: take calculated risk only if very low
        Position bestPosition = null;
        double lowestRisk = Double.MAX_VALUE;

        for (Position pos : getAdjacentUnvisitedPositions()) {
            double risk = riskAssessment.getOrDefault(pos, 1.0);
            if (risk < lowestRisk && risk < 0.3) { // Only very low risk
                lowestRisk = risk;
                bestPosition = pos;
            }
        }

        if (bestPosition != null) {
            addThought("DECISION: Taking minimal risk, move to " + bestPosition + " (risk: " +
                    String.format("%.2f", lowestRisk) + ")");
            return moveToPosition(bestPosition);
        }

        addThought("DECISION: No safe moves available - staying put");
        return null;
    }
    private Position findNearestSafeUnexplored() {
        Queue<Position> queue = new LinkedList<>();
        Set<Position> visited = new HashSet<>();

        queue.add(currentPos);
        visited.add(currentPos);

        while (!queue.isEmpty()) {
            Position current = queue.poll();

            for (Position adj : getAdjacentPositions(current)) {
                if (!visited.contains(adj) && world.isValidPosition(adj.x, adj.y)) {
                    visited.add(adj);

                    // Check if this is an unexplored safe position
                    if (!visitedPositions.contains(adj) &&
                            (safePositions.contains(adj) || riskAssessment.getOrDefault(adj, 1.0) < 0.2)) {
                        return adj;
                    }

                    // Only continue search through safe positions
                    if (safePositions.contains(adj)) {
                        queue.add(adj);
                    }
                }
            }
        }

        return null;
    }

    private Position findExplorationTarget() {
        // Find unexplored positions that are likely safe
        for (Position safe : safePositions) {
            List<Position> adjacent = getAdjacentPositions(safe);
            for (Position adj : adjacent) {
                if (world.isValidPosition(adj.x, adj.y) &&
                        !visitedPositions.contains(adj) &&
                        !dangerousPositions.contains(adj)) {
                    return adj;
                }
            }
        }
        return null;
    }

    private Action planOptimalReturn() {
        Position start = new Position(0, 0);
        List<Position> path = findSafePath(currentPos, start);

        if (!path.isEmpty()) {
            Position nextStep = path.get(0);
            addThought("Returning to start via safe path: next step " + nextStep);
            return moveToPosition(nextStep);
        }

        addThought("No safe path to start found!");
        return null;
    }

    private Action planPathToPosition(Position target) {
        List<Position> path = findSafePath(currentPos, target);

        if (!path.isEmpty()) {
            Position nextStep = path.get(0);
            addThought("Moving toward " + target + " via safe path: next step " + nextStep);
            return moveToPosition(nextStep);
        }

        addThought("No safe path to " + target + " found!");
        return null;
    }

    private Action planPathToAdjacentPosition(Position target) {
        List<Position> adjacentToTarget = getAdjacentPositions(target);

        for (Position adj : adjacentToTarget) {
            if (world.isValidPosition(adj.x, adj.y) &&
                    (safePositions.contains(adj) || riskAssessment.getOrDefault(adj, 1.0) < RISK_THRESHOLD)) {

                List<Position> path = findSafePath(currentPos, adj);
                if (!path.isEmpty()) {
                    Position nextStep = path.get(0);
                    addThought("Moving to shooting position " + adj + " adjacent to " + target);
                    return moveToPosition(nextStep);
                }
            }
        }

        return null;
    }

    private Action moveToPosition(Position target) {
        // Direct movement to adjacent position
        if (isAdjacentTo(currentPos, target)) {
            Direction moveDir = getDirectionTo(target);
            return new Action(Action.Type.MOVE_FORWARD, moveDir);
        }

        // Should not happen if path planning is correct
        addThought("ERROR: Attempted to move to non-adjacent position " + target);
        return null;
    }

    private List<Position> findSafePath(Position start, Position goal) {
        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Set<Position> closedSet = new HashSet<>();
        Map<Position, Position> cameFrom = new HashMap<>();
        Map<Position, Double> gScore = new HashMap<>();

        openSet.add(new PathNode(start, 0, manhattanDistance(start, goal)));
        gScore.put(start, 0.0);

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();

            if (current.pos.equals(goal)) {
                return reconstructPath(cameFrom, current.pos);
            }

            closedSet.add(current.pos);

            for (Position neighbor : getAdjacentPositions(current.pos)) {
                if (!world.isValidPosition(neighbor.x, neighbor.y) || closedSet.contains(neighbor)) {
                    continue;
                }

                // Skip definitely dangerous positions unless it's the goal
                if (!neighbor.equals(goal) && dangerousPositions.contains(neighbor)) {
                    continue;
                }

                // Skip positions with high risk unless it's the goal
                double risk = riskAssessment.getOrDefault(neighbor, 0.5);
                if (!neighbor.equals(goal) && risk > 0.7) {
                    continue;
                }

                double moveCost = 1.0 + (risk * 5.0); // Add risk penalty
                double tentativeGScore = gScore.get(current.pos) + moveCost;

                if (!gScore.containsKey(neighbor) || tentativeGScore < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current.pos);
                    gScore.put(neighbor, tentativeGScore);
                    double fScore = tentativeGScore + manhattanDistance(neighbor, goal);
                    openSet.add(new PathNode(neighbor, tentativeGScore, fScore));
                }
            }
        }

        return new ArrayList<>(); // No safe path found
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

    private void executeAction(Action action) {
        ActionResult result = world.performAction(action, currentPos);

        if (result.isSuccess()) {
            switch (action.getType()) {
                case MOVE_FORWARD:
                    Position newPos = getPositionInDirection(currentPos, action.getDirection());
                    if (world.isValidPosition(newPos.x, newPos.y)) {
                        currentPos = newPos;
                        currentDirection = action.getDirection();
                        addThought("Successfully moved to " + currentPos + " facing " + currentDirection);
                    }
                    break;
                case GRAB:
                    hasGold = true;
                    addThought("Successfully grabbed gold!");
                    break;
                case SHOOT:
                    hasArrow = false;
                    addThought("Arrow fired " + action.getDirection());
                    break;
            }
        } else {
            addThought("Action failed: " + result.getMessage());
        }
    }

    // Utility methods
    private String formatPosition(Position pos) {
        return pos.x + "," + pos.y;
    }

    private List<Position> getAdjacentPositions(Position pos) {
        List<Position> adjacent = new ArrayList<>();
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        for (int[] dir : directions) {
            Position adj = new Position(pos.x + dir[0], pos.y + dir[1]);
            if (world.isValidPosition(adj.x, adj.y)) {
                adjacent.add(adj);
            }
        }
        return adjacent;
    }

    private List<Position> getAdjacentUnvisitedPositions() {
        List<Position> unvisited = new ArrayList<>();
        for (Position adj : getAdjacentPositions(currentPos)) {
            if (!visitedPositions.contains(adj)) {
                unvisited.add(adj);
            }
        }
        return unvisited;
    }

    private List<Position> getAdjacentVisitedPositions(Position pos) {
        List<Position> visited = new ArrayList<>();
        for (Position adj : getAdjacentPositions(pos)) {
            if (visitedPositions.contains(adj)) {
                visited.add(adj);
            }
        }
        return visited;
    }

    private boolean isAdjacentTo(Position pos1, Position pos2) {
        return manhattanDistance(pos1, pos2) == 1;
    }

    private Direction getDirectionTo(Position target) {
        int dx = target.x - currentPos.x;
        int dy = target.y - currentPos.y;

        if (dx > 0) return Direction.EAST;
        if (dx < 0) return Direction.WEST;
        if (dy > 0) return Direction.SOUTH;
        if (dy < 0) return Direction.NORTH;

        return currentDirection;
    }

    private Position getPositionInDirection(Position pos, Direction dir) {
        switch (dir) {
            case NORTH: return new Position(pos.x, pos.y - 1);
            case SOUTH: return new Position(pos.x, pos.y + 1);
            case EAST: return new Position(pos.x + 1, pos.y);
            case WEST: return new Position(pos.x - 1, pos.y);
            default: return pos;
        }
    }

    private int manhattanDistance(Position a, Position b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private void addThought(String thought) {
        String timestampedThought = "[Move " + moveCount + "] " + thought;
        thoughtProcess.add(timestampedThought);

        // Keep only recent thoughts
        if (thoughtProcess.size() > 50) {
            thoughtProcess.remove(0);
        }

        // Debug output
        System.out.println(timestampedThought);
    }

    private void printCurrentKnowledge() {
        System.out.println("\n=== KNOWLEDGE BASE STATE ===");
        System.out.println("Position: " + currentPos);
        System.out.println("Safe positions: " + safePositions.size());
        System.out.println("Dangerous positions: " + dangerousPositions.size());
        System.out.println("Has gold: " + hasGold);
        System.out.println("Has arrow: " + hasArrow);
        System.out.println("Wumpus killed: " + wumpusKilled);

        if (goldPosition != null) {
            System.out.println("Gold location: " + goldPosition);
        }
        if (wumpusPosition != null) {
            System.out.println("Wumpus location: " + wumpusPosition);
        }

        kb.printKnowledgeBase();
        System.out.println("============================\n");
    }

    // PathNode class for A* pathfinding
    private static class PathNode implements Comparable<PathNode> {
        Position pos;
        double gScore;
        double fScore;

        public PathNode(Position pos, double gScore, double fScore) {
            this.pos = pos;
            this.gScore = gScore;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(PathNode other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }

    // Public interface methods
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("position", currentPos);
        map.put("direction", currentDirection);
        map.put("hasGold", hasGold);
        map.put("hasArrow", hasArrow);
        map.put("wumpusKilled", wumpusKilled);
        map.put("visitedPositions", visitedPositions);
        map.put("safePositions", safePositions);
        map.put("dangerousPositions", dangerousPositions);
        map.put("thoughtProcess", thoughtProcess);
        map.put("moveCount", moveCount);
        map.put("riskAssessment", riskAssessment);
        return map;
    }

    public Map<String, Object> getKnowledgeBaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("facts", kb.getAllFacts());
        map.put("derivedFacts", kb.getDerivedFacts());
        map.put("rules", kb.getRules());
        map.put("confidence", kb.getFactConfidence());
        map.put("safeCells", kb.getSafeCells());
        map.put("dangerousCells", kb.getDangerousCells());
        map.put("thoughtProcess", thoughtProcess);
        map.put("visitedPositions", visitedPositions);
        map.put("riskAssessment", riskAssessment);
        return map;
    }
}