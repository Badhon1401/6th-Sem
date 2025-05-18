package com.game.connect4.service;

import com.game.connect4.model.Game;
import com.game.connect4.model.GameStatus;
import com.game.connect4.model.Move;
import com.game.connect4.utils.Constants;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class AnalysisService {


    public String analyzePlayerMove(int column, int evaluation) {
        String moveQuality;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        if (evaluation > 500) {
            moveQuality = "Excellent move!";
        } else if (evaluation > 100) {
            moveQuality = "Good move!";
        } else if (evaluation > -100) {
            moveQuality = "Fair move";
        } else if (evaluation > -500) {
            moveQuality = "Questionable move";
        } else {
            moveQuality = "Poor move";
        }

        return String.format("[%s] Player dropped in column %d. %s", timestamp, column, moveQuality);
    }


    public String analyzeAIMove(Move move, int difficultyLevel) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String difficultyName = getDifficultyName(difficultyLevel);

        String analysis;
        if (move.getScore() >= Constants.WIN_SCORE - 100) {
            analysis = "AI found a winning move!";
        } else if (move.getScore() <= Constants.LOSE_SCORE + 100) {
            analysis = "AI is trying to prevent a loss";
        } else if (move.getScore() > 500) {
            analysis = "AI made a strong move";
        } else if (move.getScore() > 100) {
            analysis = "AI made a solid move";
        } else {
            analysis = "AI made a standard move";
        }

        return String.format("[%s] AI (%s) dropped in column %d. %s",
                timestamp, difficultyName, move.getColumn(), analysis);
    }


    public String createGameSummary(Game game) {
        StringBuilder summary = new StringBuilder();
        summary.append("Game Summary\n");
        summary.append("============\n\n");

        // Game status
        summary.append("Result: ");
        switch (game.getStatus()) {
            case PLAYER_WIN:
                summary.append("Player won!");
                break;
            case AI_WIN:
                summary.append("AI won!");
                break;
            case DRAW:
                summary.append("The game ended in a draw.");
                break;
            case IN_PROGRESS:
                summary.append("Game is still in progress.");
                break;
        }
        summary.append("\n\n");

        // Difficulty level
        summary.append("Difficulty: ").append(getDifficultyName(game.getDifficultyLevel())).append("\n\n");

        // Number of moves
        List<Move> moves = game.getBoard().getMoveHistory();
        summary.append("Total moves: ").append(moves.size()).append("\n");
        summary.append("Player moves: ").append(moves.size() / 2 + moves.size() % 2).append("\n");
        summary.append("AI moves: ").append(moves.size() / 2).append("\n\n");

        // Move analysis
        summary.append("Move Analysis\n");
        summary.append("------------\n");
        List<String> analyses = game.getGameAnalysis();
        for (String analysis : analyses) {
            summary.append(analysis).append("\n");
        }

        // Overall assessment
        summary.append("\nOverall Assessment\n");
        summary.append("-----------------\n");

        if (game.getStatus() == GameStatus.PLAYER_WIN) {
            summary.append("Congratulations! You demonstrated excellent strategy to defeat the AI.");
        } else if (game.getStatus() == GameStatus.AI_WIN) {
            summary.append("The AI claimed victory this time. Try to identify where you could improve your strategy.");
        } else if (game.getStatus() == GameStatus.DRAW) {
            summary.append("A well-matched game that ended in a draw. Both sides played competitively.");
        } else {
            summary.append("Game is still ongoing.");
        }

        return summary.toString();
    }


    private String getDifficultyName(int difficultyLevel) {
        if (difficultyLevel <= Constants.EASY) {
            return "Easy";
        } else if (difficultyLevel <= Constants.MEDIUM) {
            return "Medium";
        } else if (difficultyLevel <= Constants.HARD) {
            return "Hard";
        } else {
            return "Expert";
        }
    }
}