package com.ai.wumpus_world.controller;

import com.ai.wumpus_world.model.*;
import com.ai.wumpus_world.service.WumpusWorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class WumpusWorldController {

    @Autowired
    private WumpusWorldService wumpusService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("world", wumpusService.getCurrentWorld());
        model.addAttribute("agent", wumpusService.getAgent());
        return "index";
    }

    @PostMapping("/new-game")
    @ResponseBody
    public Map<String, Object> newGame(@RequestParam(defaultValue = "random") String type) {
        if ("random".equals(type)) {
            wumpusService.generateRandomWorld();
        } else {
            wumpusService.loadPredefinedWorld();
        }
        return wumpusService.getGameState();
    }

    @PostMapping("/step")
    @ResponseBody
    public Map<String, Object> step() {
        wumpusService.agentStep();
        return wumpusService.getGameState();
    }

    @PostMapping("/auto-play")
    @ResponseBody
    public Map<String, Object> autoPlay() {
        wumpusService.setAutoPlay(true);
        return wumpusService.getGameState();
    }

    @GetMapping("/knowledge-base")
    @ResponseBody
    public Map<String, Object> getKnowledgeBase() {
        return wumpusService.getKnowledgeBaseState();
    }
}