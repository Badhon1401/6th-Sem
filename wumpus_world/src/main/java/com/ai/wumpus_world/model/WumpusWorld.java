package com.ai.wumpus_world.model;

import java.util.*;

public class WumpusWorld {
    private int width, height;
    private Cell[][] grid;
    private Position agentPos;
    private boolean agentAlive = true;
    private boolean wumpusAlive = true;
    private boolean hasArrow = true;
    private boolean hasGold = false;
    private int score = 0;
    private String gameMessage = "Game started";

    public WumpusWorld(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Cell[height][width];
        initializeGrid();
        this.agentPos = new Position(0, 0);
    }

    private void initializeGrid() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j] = new Cell();
            }
        }
    }

    public void generateRandom() {
        Random rand = new Random();

        // Place Wumpus (1-2 wumpuses)
        int wumpusCount = 1 + rand.nextInt(2);
        for (int i = 0; i < wumpusCount; i++) {
            Position pos = getRandomEmptyPosition(rand);
            if (pos != null) {
                grid[pos.y][pos.x].setWumpus(true);
                addStench(pos);
            }
        }

        // Place Pits (10-15% of cells)
        int pitCount = (width * height) / 8 + rand.nextInt(5);
        for (int i = 0; i < pitCount; i++) {
            Position pos = getRandomEmptyPosition(rand);
            if (pos != null) {
                grid[pos.y][pos.x].setPit(true);
                addBreeze(pos);
            }
        }

        // Place Gold (1-3 pieces)
        int goldCount = 1 + rand.nextInt(3);
        for (int i = 0; i < goldCount; i++) {
            Position pos = getRandomEmptyPosition(rand);
            if (pos != null) {
                grid[pos.y][pos.x].setGold(true);
                grid[pos.y][pos.x].setGlitter(true);
            }
        }
    }

    public void loadFromString(String worldStr) {
        String[] rows = worldStr.split(" ");
        for (int i = 0; i < Math.min(rows.length, height); i++) {
            String row = rows[i];
            for (int j = 0; j < Math.min(row.length(), width); j++) {
                char c = row.charAt(j);
                switch (c) {
                    case 'W':
                        grid[i][j].setWumpus(true);
                        addStench(new Position(j, i));
                        break;
                    case 'P':
                        grid[i][j].setPit(true);
                        addBreeze(new Position(j, i));
                        break;
                    case 'G':
                        grid[i][j].setGold(true);
                        grid[i][j].setGlitter(true);
                        break;
                }
            }
        }
    }

    private Position getRandomEmptyPosition(Random rand) {
        List<Position> emptyPositions = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (isEmpty(j, i) && !(i == 0 && j == 0)) {
                    emptyPositions.add(new Position(j, i));
                }
            }
        }
        return emptyPositions.isEmpty() ? null : emptyPositions.get(rand.nextInt(emptyPositions.size()));
    }

    private boolean isEmpty(int x, int y) {
        Cell cell = grid[y][x];
        return !cell.hasWumpus() && !cell.hasPit() && !cell.hasGold();
    }

    private void addStench(Position pos) {
        for (Position neighbor : getNeighbors(pos)) {
            grid[neighbor.y][neighbor.x].setStench(true);
        }
    }

    private void addBreeze(Position pos) {
        for (Position neighbor : getNeighbors(pos)) {
            grid[neighbor.y][neighbor.x].setBreeze(true);
        }
    }

    private List<Position> getNeighbors(Position pos) {
        List<Position> neighbors = new ArrayList<>();
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        for (int[] dir : directions) {
            int newX = pos.x + dir[0];
            int newY = pos.y + dir[1];
            if (isValidPosition(newX, newY)) {
                neighbors.add(new Position(newX, newY));
            }
        }
        return neighbors;
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public List<Position> getValidMoves(Position pos) {
        return getNeighbors(pos);
    }

    public Percept getPercept(Position pos) {
        if (!isValidPosition(pos.x, pos.y)) return new Percept();

        Cell cell = grid[pos.y][pos.x];
        return new Percept(
                cell.hasStench(),
                cell.hasBreeze(),
                cell.hasGlitter(),
                false, // bump
                false  // scream
        );
    }

    public ActionResult performAction(Action action, Position currentPos) {
        switch (action.getType()) {
            case MOVE_FORWARD:
                return moveAgent(action.getDirection(), currentPos);
            case SHOOT:
                return shootArrow(action.getDirection(), currentPos);
            case GRAB:
                return grabGold(currentPos);
            default:
                return new ActionResult(false, "Invalid action");
        }
    }

    private ActionResult moveAgent(Direction direction, Position currentPos) {
        Position newPos = getNewPosition(currentPos, direction);

        if (!isValidPosition(newPos.x, newPos.y)) {
            return new ActionResult(false, "Bump! Hit the wall", true);
        }

        this.agentPos = newPos;
        Cell cell = grid[newPos.y][newPos.x];

        if (cell.hasPit()) {
            agentAlive = false;
            score -= 1000;
            gameMessage = "Agent fell into a pit! Game Over!";
            return new ActionResult(false, "Fell into pit");
        }

        if (cell.hasWumpus() && wumpusAlive) {
            agentAlive = false;
            score -= 1000;
            gameMessage = "Agent eaten by Wumpus! Game Over!";
            return new ActionResult(false, "Eaten by Wumpus");
        }

        score -= 1; // Cost of moving
        return new ActionResult(true, "Moved successfully");
    }

    private ActionResult shootArrow(Direction direction, Position currentPos) {
        if (!hasArrow) {
            return new ActionResult(false, "No arrow left");
        }

        hasArrow = false;
        score -= 10; // Cost of shooting

        // Check if arrow hits Wumpus
        Position arrowPos = currentPos;
        while (true) {
            arrowPos = getNewPosition(arrowPos, direction);
            if (!isValidPosition(arrowPos.x, arrowPos.y)) break;

            if (grid[arrowPos.y][arrowPos.x].hasWumpus()) {
                wumpusAlive = false;
                grid[arrowPos.y][arrowPos.x].setWumpus(false);
                removeStench(arrowPos);
                gameMessage = "Wumpus killed! Scream heard!";
                return new ActionResult(true, "Wumpus killed", false, true);
            }
        }

        return new ActionResult(true, "Arrow missed");
    }

    private ActionResult grabGold(Position currentPos) {
        Cell cell = grid[currentPos.y][currentPos.x];
        if (cell.hasGold()) {
            cell.setGold(false);
            cell.setGlitter(false);
            hasGold = true;
            score += 1000;
            gameMessage = "Gold grabbed! +1000 points!";
            return new ActionResult(true, "Gold grabbed");
        }
        return new ActionResult(false, "No gold here");
    }

    private void removeStench(Position wumpusPos) {
        for (Position neighbor : getNeighbors(wumpusPos)) {
            // Check if there are other wumpuses causing stench
            boolean hasOtherWumpusStench = false;
            for (Position otherNeighbor : getNeighbors(neighbor)) {
                if (!otherNeighbor.equals(wumpusPos) &&
                        grid[otherNeighbor.y][otherNeighbor.x].hasWumpus()) {
                    hasOtherWumpusStench = true;
                    break;
                }
            }
            if (!hasOtherWumpusStench) {
                grid[neighbor.y][neighbor.x].setStench(false);
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

    public boolean isGameOver() {
        return !agentAlive || (hasGold && agentPos.equals(new Position(0, 0)));
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("width", width);
        map.put("height", height);
        map.put("grid", gridToMap());
        map.put("agentPos", agentPos);
        map.put("agentAlive", agentAlive);
        map.put("hasArrow", hasArrow);
        map.put("hasGold", hasGold);
        map.put("score", score);
        return map;
    }

    private Object[][][] gridToMap() {
        Object[][][] gridMap = new Object[height][width][];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                gridMap[i][j] = grid[i][j].toArray();
            }
        }
        return gridMap;
    }

    // Getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Position getAgentPos() { return agentPos; }
    public boolean isAgentAlive() { return agentAlive; }
    public boolean hasArrow() { return hasArrow; }
    public boolean hasGold() { return hasGold; }
    public int getScore() { return score; }
    public String getGameMessage() { return gameMessage; }
    public Cell getCell(int x, int y) { return grid[y][x]; }
}