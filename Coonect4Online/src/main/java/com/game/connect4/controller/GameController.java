package com.game.connect4.controller;

import com.game.connect4.model.Game;
import com.game.connect4.model.GameStatus;
import com.game.connect4.model.Move;
import com.game.connect4.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }


    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("difficultyLevels", gameService.getDifficultyLevels());
        return "index";
    }


    @PostMapping("/game/new")
    public String newGame(@RequestParam("difficulty") int difficulty) {
        Game game = gameService.createGame(difficulty);
        return "redirect:/game/" + game.getId();
    }


    @GetMapping("/game/{gameId}")
    public String showGame(@PathVariable String gameId, Model model) {
        Game game = gameService.getGame(gameId);
        if (game == null) {
            return "redirect:/";
        }

        model.addAttribute("game", game);
        model.addAttribute("board", game.getBoard());
        model.addAttribute("gameId", gameId);
        model.addAttribute("difficulty", game.getDifficultyLevel()); // <-- ADD THIS LINE

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            model.addAttribute("gameSummary", gameService.getGameSummary(gameId));
        }

        return "game";
    }



    @PostMapping("/game/{gameId}/move")
    @ResponseBody
    public MoveResponse makeMove(@PathVariable String gameId, @RequestParam("column") int column) {
        Game game = gameService.getGame(gameId);
        if (game == null) {
            return new MoveResponse(false, "Game not found", null, GameStatus.IN_PROGRESS);
        }

        int playerRow = gameService.makePlayerMove(gameId, column);
        if (playerRow == -1) {
            return new MoveResponse(false, "Invalid move", null, game.getStatus());
        }

        MoveResponse response = new MoveResponse(true, "Move successful",
                new MoveInfo(column, playerRow), game.getStatus());

        if (game.getStatus() == GameStatus.IN_PROGRESS) {
            Move aiMove = gameService.makeAIMove(gameId);
            if (aiMove != null) {
                response.setAiMove(new MoveInfo(aiMove.getColumn(), aiMove.getRow()));
                response.setGameStatus(game.getStatus());


                if (game.getStatus() != GameStatus.IN_PROGRESS) {
                    response.setGameSummary(gameService.getGameSummary(gameId));
                }
            }
        } else {

            response.setGameSummary(gameService.getGameSummary(gameId));
        }

        return response;
    }


    @PostMapping("/game/{gameId}/reset")
    public String resetGame(@PathVariable String gameId) {
        gameService.resetGame(gameId);
        return "redirect:/game/" + gameId;
    }


    @PostMapping("/game/{gameId}/difficulty")
    public String changeDifficulty(@PathVariable String gameId, @RequestParam("difficulty") int difficulty) {
        gameService.setDifficultyLevel(gameId, difficulty);
        return "redirect:/game/" + gameId;
    }

    @GetMapping("/game/{gameId}/analysis")
    public String showGameAnalysis(@PathVariable String gameId, Model model) {
        Game game = gameService.getGame(gameId);
        if (game == null) {
            return "redirect:/";
        }

        model.addAttribute("game", game);
        model.addAttribute("difficulty", game.getDifficultyLevel());
        return "analysis";
    }

    public static class MoveResponse {
        private boolean success;
        private String message;
        private MoveInfo playerMove;
        private MoveInfo aiMove;
        private GameStatus gameStatus;
        private String gameSummary;

        public MoveResponse(boolean success, String message, MoveInfo playerMove, GameStatus gameStatus) {
            this.success = success;
            this.message = message;
            this.playerMove = playerMove;
            this.gameStatus = gameStatus;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public MoveInfo getPlayerMove() {
            return playerMove;
        }

        public void setPlayerMove(MoveInfo playerMove) {
            this.playerMove = playerMove;
        }

        public MoveInfo getAiMove() {
            return aiMove;
        }

        public void setAiMove(MoveInfo aiMove) {
            this.aiMove = aiMove;
        }

        public GameStatus getGameStatus() {
            return gameStatus;
        }

        public void setGameStatus(GameStatus gameStatus) {
            this.gameStatus = gameStatus;
        }

        public String getGameSummary() {
            return gameSummary;
        }

        public void setGameSummary(String gameSummary) {
            this.gameSummary = gameSummary;
        }
    }


    public static class MoveInfo {
        private int column;
        private int row;

        public MoveInfo(int column, int row) {
            this.column = column;
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }
    }
}