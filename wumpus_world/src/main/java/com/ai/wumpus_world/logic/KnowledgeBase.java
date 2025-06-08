package com.ai.wumpus_world.logic;


import java.util.*;

public class KnowledgeBase {
    private Set<String> facts;
    private List<String> rules;
    private Map<String, Set<String>> implications;

    public KnowledgeBase() {
        this.facts = new HashSet<>();
        this.rules = new ArrayList<>();
        this.implications = new HashMap<>();
    }

    public void addFact(String fact) {
        facts.add(fact);
        // Forward chaining - derive new facts based on rules
        forwardChain(fact);
    }

    public void addRule(String rule) {
        rules.add(rule);
    }

    public boolean hasFact(String fact) {
        return facts.contains(fact);
    }

    public void removeFact(String fact) {
        facts.remove(fact);
    }

    private void forwardChain(String newFact) {
        // Simple forward chaining implementation
        // In a real system, this would be more sophisticated

        // Example: If we know "Stench(1,1)" and we have a rule about stenches
        // we can derive "PossibleWumpus" in adjacent cells

        if (newFact.startsWith("Stench(")) {
            String coords = newFact.substring(7, newFact.length() - 1);
            String[] parts = coords.split(",");
            if (parts.length == 2) {
                try {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);

                    // Add implications for adjacent cells
                    int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
                    for (int[] dir : directions) {
                        String adjacentFact = "PossibleWumpus(" + (x + dir[0]) + "," + (y + dir[1]) + ")";
                        if (!hasFact("Safe(" + (x + dir[0]) + "," + (y + dir[1]) + ")")) {
                            facts.add(adjacentFact);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Invalid format, ignore
                }
            }
        }

        // Similar logic for breezes and pits
        if (newFact.startsWith("Breeze(")) {
            String coords = newFact.substring(7, newFact.length() - 1);
            String[] parts = coords.split(",");
            if (parts.length == 2) {
                try {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);

                    int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
                    for (int[] dir : directions) {
                        String adjacentFact = "PossiblePit(" + (x + dir[0]) + "," + (y + dir[1]) + ")";
                        if (!hasFact("Safe(" + (x + dir[0]) + "," + (y + dir[1]) + ")")) {
                            facts.add(adjacentFact);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Invalid format, ignore
                }
            }
        }
    }

    public boolean query(String query) {
        // Simple query processing - in a real system would use resolution
        return facts.contains(query);
    }

    public Set<String> getFacts() {
        return new HashSet<>(facts);
    }

    public List<String> getRules() {
        return new ArrayList<>(rules);
    }

    public void performResolution(String query) {
        // Placeholder for resolution theorem proving
        // Would implement full resolution in a complete system
    }
}