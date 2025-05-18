package com.game.connect4.service;

import com.game.connect4.model.Game;
import com.game.connect4.model.GameStatus;
import com.game.connect4.model.Move;
import com.game.connect4.model.Player;
import com.game.connect4.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class GameService {

    private final AIService aiService;
    private final AnalysisService analysisService;

    // In-memory storage for game sessions
    private final Map<String, Game> games = new HashMap<>();

    @Autowired
    public GameService(AIService aiService, AnalysisService analysisService) {
        this.aiService = aiService;
        this.analysisService = analysisService;
    }


    public Game createGame(int difficultyLevel) {
        Game game = new Game(difficultyLevel);
        games.put(game.getId(), game);
        return game;
    }


    public Game getGame(String gameId) {
        return games.get(gameId);
    }


    public int makePlayerMove(String gameId, int column) {
        Game game = games.get(gameId);
        if (game == null || game.getCurrentPlayer() != Player.PLAYER) {
            return -1;
        }

        int row = game.makeMove(column);

        // If the game is still in progress and it's the AI's turn, analyze the player's move
        if (row != -1 && game.getStatus() == GameStatus.IN_PROGRESS) {
            analyzePlayerMove(game, column);
        }

        return row;
    }


    public Move makeAIMove(String gameId) {
        Game game = games.get(gameId);
        if (game == null || game.getCurrentPlayer() != Player.AI || game.getStatus() != GameStatus.IN_PROGRESS) {
            return null;
        }

        // Get the best move for the AI
        Move bestMove = aiService.findBestMove(game);
        if (bestMove == null) {
            return null;
        }

        // Make the move
        int row = game.makeMove(bestMove.getColumn());
        if (row != -1) {
            bestMove.setRow(row);
            bestMove.setPlayer(Player.AI);

            // Add analysis of the AI's move
            analyzeAIMove(game, bestMove);
        }

        return bestMove;
    }


    public Game resetGame(String gameId) {
        Game game = games.get(gameId);
        if (game != null) {
            game.resetGame();
        }
        return game;
    }


    private void analyzePlayerMove(Game game, int column) {
        int evaluation = aiService.evaluatePlayerMove(game, column);
        String analysis = analysisService.analyzePlayerMove(column, evaluation);
        game.addAnalysis(analysis);
    }


    private void analyzeAIMove(Game game, Move move) {
        String analysis = analysisService.analyzeAIMove(move, game.getDifficultyLevel());
        game.addAnalysis(analysis);
    }


    public void setDifficultyLevel(String gameId, int difficultyLevel) {
        Game game = games.get(gameId);
        if (game != null) {
            game.setDifficultyLevel(difficultyLevel);
        }
    }


    public Map<String, Integer> getDifficultyLevels() {
        Map<String, Integer> levels = new HashMap<>();
        levels.put("Easy", Constants.EASY);
        levels.put("Medium", Constants.MEDIUM);
        levels.put("Hard", Constants.HARD);
        levels.put("Expert", Constants.EXPERT);
        return levels;
    }


    public String getGameSummary(String gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            return "Game not found";
        }

        return analysisService.createGameSummary(game);
    }
}