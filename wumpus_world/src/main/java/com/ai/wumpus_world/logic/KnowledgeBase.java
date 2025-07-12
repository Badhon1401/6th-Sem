package com.ai.wumpus_world.logic;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class KnowledgeBase {
    private Set<String> facts;
    private List<Rule> rules;
    private Map<String, Set<String>> implications;
    private Set<String> derivedFacts;
    private Map<String, Integer> factConfidence;

    // Inner class to represent logical rules
    public static class Rule {
        private List<String> premises;
        private String conclusion;
        private String originalRule;

        public Rule(String ruleString) {
            this.originalRule = ruleString;
            parseRule(ruleString);
        }

        private void parseRule(String ruleString) {
            // Parse rules in format: "premise1 & premise2 => conclusion"
            if (ruleString.contains("=>")) {
                String[] parts = ruleString.split("=>");
                if (parts.length == 2) {
                    String premisesPart = parts[0].trim();
                    this.conclusion = parts[1].trim();

                    if (premisesPart.contains("&")) {
                        this.premises = Arrays.asList(premisesPart.split("\\s*&\\s*"));
                    } else {
                        this.premises = Arrays.asList(premisesPart);
                    }
                }
            }
        }

        public List<String> getPremises() { return premises; }
        public String getConclusion() { return conclusion; }
        public String getOriginalRule() { return originalRule; }
    }

    // Clause representation for resolution
    public static class Clause {
        private Set<String> literals;

        public Clause(String... literals) {
            this.literals = new HashSet<>(Arrays.asList(literals));
        }

        public Clause(Set<String> literals) {
            this.literals = new HashSet<>(literals);
        }

        public Set<String> getLiterals() { return literals; }

        public boolean isEmpty() { return literals.isEmpty(); }

        public boolean isUnitClause() { return literals.size() == 1; }

        public String getUnitLiteral() {
            return isUnitClause() ? literals.iterator().next() : null;
        }

        @Override
        public String toString() {
            return literals.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Clause clause = (Clause) obj;
            return Objects.equals(literals, clause.literals);
        }

        @Override
        public int hashCode() {
            return Objects.hash(literals);
        }
    }

    public KnowledgeBase() {
        this.facts = new HashSet<>();
        this.rules = new ArrayList<>();
        this.implications = new HashMap<>();
        this.derivedFacts = new HashSet<>();
        this.factConfidence = new HashMap<>();

        // Initialize with basic Wumpus World rules
        initializeWumpusRules();
    }

    private void initializeWumpusRules() {
        // Wumpus World specific rules
        addRule("Stench(x,y) => PossibleWumpus(x+1,y) | PossibleWumpus(x-1,y) | PossibleWumpus(x,y+1) | PossibleWumpus(x,y-1)");
        addRule("Breeze(x,y) => PossiblePit(x+1,y) | PossiblePit(x-1,y) | PossiblePit(x,y+1) | PossiblePit(x,y-1)");
        addRule("~Stench(x,y) => ~Wumpus(x+1,y) & ~Wumpus(x-1,y) & ~Wumpus(x,y+1) & ~Wumpus(x,y-1)");
        addRule("~Breeze(x,y) => ~Pit(x+1,y) & ~Pit(x-1,y) & ~Pit(x,y+1) & ~Pit(x,y-1)");
        addRule("Visited(x,y) & ~Stench(x,y) => Safe(x+1,y) & Safe(x-1,y) & Safe(x,y+1) & Safe(x,y-1)");
        addRule("Wumpus(x,y) => Stench(x+1,y) | Stench(x-1,y) | Stench(x,y+1) | Stench(x,y-1)");
        addRule("Pit(x,y) => Breeze(x+1,y) | Breeze(x-1,y) | Breeze(x,y+1) | Breeze(x,y-1)");
    }

    public void addFact(String fact) {
        facts.add(fact);
        factConfidence.put(fact, 100); // Full confidence for direct observations

        // Forward chaining with enhanced reasoning
        forwardChain(fact);

        // Perform consistency check
        checkConsistency();
    }

    public void addRule(String rule) {
        rules.add(new Rule(rule));
    }

    public void addRule(Rule rule) {
        rules.add(rule);
    }

    public boolean hasFact(String fact) {
        return facts.contains(fact) || derivedFacts.contains(fact);
    }

    public void removeFact(String fact) {
        facts.remove(fact);
        derivedFacts.remove(fact);
        factConfidence.remove(fact);
    }

    private void forwardChain(String newFact) {
        Queue<String> agenda = new LinkedList<>();
        agenda.add(newFact);
        Set<String> processed = new HashSet<>();

        while (!agenda.isEmpty()) {
            String currentFact = agenda.poll();
            if (processed.contains(currentFact)) continue;
            processed.add(currentFact);

            // Apply Wumpus World specific inference
            applyWumpusWorldInference(currentFact, agenda);

            // Apply general rules
            for (Rule rule : rules) {
                if (rule.getPremises() != null && rule.getConclusion() != null) {
                    boolean allPremisesSatisfied = true;
                    for (String premise : rule.getPremises()) {
                        if (!hasFact(premise) && !satisfiesPremise(premise, currentFact)) {
                            allPremisesSatisfied = false;
                            break;
                        }
                    }

                    if (allPremisesSatisfied && !hasFact(rule.getConclusion())) {
                        derivedFacts.add(rule.getConclusion());
                        factConfidence.put(rule.getConclusion(),
                                Math.max(50, factConfidence.getOrDefault(currentFact, 50) - 10));
                        agenda.add(rule.getConclusion());
                    }
                }
            }
        }
    }

    private boolean satisfiesPremise(String premise, String fact) {
        // Simple pattern matching for premises
        return premise.equals(fact) ||
                (premise.contains("(") && fact.contains("(") &&
                        extractPredicate(premise).equals(extractPredicate(fact)));
    }

    private String extractPredicate(String fact) {
        int parenIndex = fact.indexOf('(');
        return parenIndex > 0 ? fact.substring(0, parenIndex) : fact;
    }

    private void applyWumpusWorldInference(String newFact, Queue<String> agenda) {
        // Enhanced Wumpus World specific reasoning

        if (newFact.startsWith("Stench(")) {
            processStenchFact(newFact, agenda);
        } else if (newFact.startsWith("Breeze(")) {
            processBreezeFact(newFact, agenda);
        } else if (newFact.startsWith("~Stench(")) {
            processNoStenchFact(newFact, agenda);
        } else if (newFact.startsWith("~Breeze(")) {
            processNoBreezeFact(newFact, agenda);
        } else if (newFact.startsWith("Visited(")) {
            processVisitedFact(newFact, agenda);
        }
    }

    private void processStenchFact(String stenchFact, Queue<String> agenda) {
        int[] coords = extractCoordinates(stenchFact);
        if (coords != null) {
            int x = coords[0], y = coords[1];

            // Count how many adjacent cells could contain Wumpus
            int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
            List<String> possibleWumpusPositions = new ArrayList<>();

            for (int[] dir : directions) {
                int nx = x + dir[0], ny = y + dir[1];
                String possibleWumpus = "PossibleWumpus(" + nx + "," + ny + ")";
                String safeCell = "Safe(" + nx + "," + ny + ")";
                String noWumpus = "~Wumpus(" + nx + "," + ny + ")";

                // Only add as possible if not already ruled out
                if (!hasFact(safeCell) && !hasFact(noWumpus)) {
                    possibleWumpusPositions.add(possibleWumpus);
                }
            }

            // Add all possible Wumpus positions
            for (String possibleWumpus : possibleWumpusPositions) {
                if (!hasFact(possibleWumpus)) {
                    derivedFacts.add(possibleWumpus);
                    factConfidence.put(possibleWumpus, 70);
                    agenda.add(possibleWumpus);
                }
            }
        }
    }

    private void processBreezeFact(String breezeFact, Queue<String> agenda) {
        int[] coords = extractCoordinates(breezeFact);
        if (coords != null) {
            int x = coords[0], y = coords[1];

            // Add possible pit locations in adjacent cells
            int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
            for (int[] dir : directions) {
                int nx = x + dir[0], ny = y + dir[1];
                String possiblePit = "PossiblePit(" + nx + "," + ny + ")";
                String safeCell = "Safe(" + nx + "," + ny + ")";

                if (!hasFact(safeCell) && !hasFact(possiblePit)) {
                    derivedFacts.add(possiblePit);
                    factConfidence.put(possiblePit, 60);
                    agenda.add(possiblePit);
                }
            }
        }
    }

    private void processNoStenchFact(String noStenchFact, Queue<String> agenda) {
        int[] coords = extractCoordinates(noStenchFact.substring(1)); // Remove ~
        if (coords != null) {
            int x = coords[0], y = coords[1];

            // Mark adjacent cells as safe from wumpus
            int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
            for (int[] dir : directions) {
                int nx = x + dir[0], ny = y + dir[1];
                String noWumpus = "~Wumpus(" + nx + "," + ny + ")";
                String safeFromWumpus = "SafeFromWumpus(" + nx + "," + ny + ")";

                if (!hasFact(noWumpus)) {
                    derivedFacts.add(noWumpus);
                    derivedFacts.add(safeFromWumpus);
                    factConfidence.put(noWumpus, 90);
                    factConfidence.put(safeFromWumpus, 90);
                    agenda.add(noWumpus);
                    agenda.add(safeFromWumpus);
                }
            }
        }
    }

    private void processNoBreezeFact(String noBreezeFact, Queue<String> agenda) {
        int[] coords = extractCoordinates(noBreezeFact.substring(1)); // Remove ~
        if (coords != null) {
            int x = coords[0], y = coords[1];

            // Mark adjacent cells as safe from pits
            int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
            for (int[] dir : directions) {
                int nx = x + dir[0], ny = y + dir[1];
                String noPit = "~Pit(" + nx + "," + ny + ")";
                String safeFromPit = "SafeFromPit(" + nx + "," + ny + ")";

                if (!hasFact(noPit)) {
                    derivedFacts.add(noPit);
                    derivedFacts.add(safeFromPit);
                    factConfidence.put(noPit, 90);
                    factConfidence.put(safeFromPit, 90);
                    agenda.add(noPit);
                    agenda.add(safeFromPit);
                }
            }
        }
    }

    private void processVisitedFact(String visitedFact, Queue<String> agenda) {
        int[] coords = extractCoordinates(visitedFact);
        if (coords != null) {
            int x = coords[0], y = coords[1];

            // If visited and no stench/breeze, mark adjacent cells as safe
            if (!hasFact("Stench(" + x + "," + y + ")") &&
                    !hasFact("Breeze(" + x + "," + y + ")")) {

                int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
                for (int[] dir : directions) {
                    int nx = x + dir[0], ny = y + dir[1];
                    String safe = "Safe(" + nx + "," + ny + ")";

                    if (!hasFact(safe)) {
                        derivedFacts.add(safe);
                        factConfidence.put(safe, 95);
                        agenda.add(safe);
                    }
                }
            }
        }
    }

    private int[] extractCoordinates(String fact) {
        Pattern pattern = Pattern.compile("\\((\\d+),(\\d+)\\)");
        Matcher matcher = pattern.matcher(fact);
        if (matcher.find()) {
            try {
                return new int[]{Integer.parseInt(matcher.group(1)),
                        Integer.parseInt(matcher.group(2))};
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public boolean query(String query) {
        // First check direct facts
        if (facts.contains(query) || derivedFacts.contains(query)) {
            return true;
        }

        // Use resolution theorem proving
        return performResolution(query);
    }

    public boolean performResolution(String query) {
        // Convert KB to CNF and apply resolution
        Set<Clause> clauses = convertToCNF();

        // Add negation of query
        clauses.add(new Clause("~" + query));

        return resolution(clauses);
    }

    private Set<Clause> convertToCNF() {
        Set<Clause> clauses = new HashSet<>();

        // Convert facts to unit clauses
        for (String fact : facts) {
            clauses.add(new Clause(fact));
        }

        for (String fact : derivedFacts) {
            clauses.add(new Clause(fact));
        }

        // Convert rules to clauses (simplified)
        for (Rule rule : rules) {
            if (rule.getPremises() != null && rule.getConclusion() != null) {
                Set<String> literals = new HashSet<>();

                // Add negated premises
                for (String premise : rule.getPremises()) {
                    literals.add("~" + premise);
                }

                // Add conclusion
                literals.add(rule.getConclusion());

                clauses.add(new Clause(literals));
            }
        }

        return clauses;
    }

    private boolean resolution(Set<Clause> clauses) {
        Set<Clause> newClauses = new HashSet<>();

        List<Clause> clauseList = new ArrayList<>(clauses);

        // Apply resolution rule
        for (int i = 0; i < clauseList.size(); i++) {
            for (int j = i + 1; j < clauseList.size(); j++) {
                Clause c1 = clauseList.get(i);
                Clause c2 = clauseList.get(j);

                Set<Clause> resolvents = resolve(c1, c2);

                // Check if empty clause is derived
                for (Clause resolvent : resolvents) {
                    if (resolvent.isEmpty()) {
                        return true; // Query is entailed
                    }
                    newClauses.add(resolvent);
                }
            }
        }

        // If no new clauses, query is not entailed
        if (newClauses.isEmpty() || clauses.containsAll(newClauses)) {
            return false;
        }

        clauses.addAll(newClauses);
        return resolution(clauses);
    }

    private Set<Clause> resolve(Clause c1, Clause c2) {
        Set<Clause> resolvents = new HashSet<>();

        for (String literal1 : c1.getLiterals()) {
            for (String literal2 : c2.getLiterals()) {
                if (areComplementary(literal1, literal2)) {
                    // Create resolvent
                    Set<String> resolventLiterals = new HashSet<>(c1.getLiterals());
                    resolventLiterals.addAll(c2.getLiterals());
                    resolventLiterals.remove(literal1);
                    resolventLiterals.remove(literal2);

                    resolvents.add(new Clause(resolventLiterals));
                }
            }
        }

        return resolvents;
    }

    private boolean areComplementary(String literal1, String literal2) {
        if (literal1.startsWith("~") && !literal2.startsWith("~")) {
            return literal1.substring(1).equals(literal2);
        } else if (!literal1.startsWith("~") && literal2.startsWith("~")) {
            return literal1.equals(literal2.substring(1));
        }
        return false;
    }

    private void checkConsistency() {
        // Check for contradictions in the knowledge base
        Set<String> allFacts = new HashSet<>(facts);
        allFacts.addAll(derivedFacts);

        for (String fact : allFacts) {
            String negatedFact = fact.startsWith("~") ? fact.substring(1) : "~" + fact;
            if (allFacts.contains(negatedFact)) {
                System.out.println("Warning: Contradiction detected - " + fact + " and " + negatedFact);
            }
        }
    }

    public Set<String> getAllFacts() {
        Set<String> allFacts = new HashSet<>(facts);
        allFacts.addAll(derivedFacts);
        return allFacts;
    }

    public Set<String> getFacts() {
        return new HashSet<>(facts);
    }

    public Set<String> getDerivedFacts() {
        return new HashSet<>(derivedFacts);
    }

    public List<Rule> getRules() {
        return new ArrayList<>(rules);
    }

    public Map<String, Integer> getFactConfidence() {
        return new HashMap<>(factConfidence);
    }

    public int getConfidence(String fact) {
        return factConfidence.getOrDefault(fact, 0);
    }

    // Advanced reasoning methods
    public Set<String> explainFact(String fact) {
        Set<String> explanation = new HashSet<>();

        if (facts.contains(fact)) {
            explanation.add("Direct observation: " + fact);
        } else if (derivedFacts.contains(fact)) {
            // Find which rule derived this fact
            for (Rule rule : rules) {
                if (rule.getConclusion().equals(fact)) {
                    explanation.add("Derived from rule: " + rule.getOriginalRule());
                    for (String premise : rule.getPremises()) {
                        explanation.addAll(explainFact(premise));
                    }
                    break;
                }
            }
        }

        return explanation;
    }

    public List<String> getSafeCells() {
        return getAllFacts().stream()
                .filter(fact -> fact.startsWith("Safe("))
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    public List<String> getDangerousCells() {
        return getAllFacts().stream()
                .filter(fact -> fact.startsWith("PossibleWumpus(") ||
                        fact.startsWith("PossiblePit(") ||
                        fact.startsWith("Wumpus(") ||
                        fact.startsWith("Pit("))
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    public void printKnowledgeBase() {
        System.out.println("=== Knowledge Base ===");
        System.out.println("Facts:");
        facts.forEach(fact -> System.out.println("  " + fact + " (confidence: " +
                getConfidence(fact) + "%)"));

        System.out.println("\nDerived Facts:");
        derivedFacts.forEach(fact -> System.out.println("  " + fact + " (confidence: " +
                getConfidence(fact) + "%)"));

        System.out.println("\nRules:");
        rules.forEach(rule -> System.out.println("  " + rule.getOriginalRule()));

        System.out.println("\nSafe Cells: " + getSafeCells());
        System.out.println("Dangerous Cells: " + getDangerousCells());
    }
}