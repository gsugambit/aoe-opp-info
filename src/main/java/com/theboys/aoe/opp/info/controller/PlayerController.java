package com.theboys.aoe.opp.info.controller;

import com.theboys.aoe.opp.info.dto.AutocompleteResponseDto;
import com.theboys.aoe.opp.info.dto.internal.GameResult;
import com.theboys.aoe.opp.info.service.Aoe4OppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/aoe4-opp")
@RestController
@RequiredArgsConstructor
public class PlayerController {

    private final Aoe4OppService aoe4OppService;

    @GetMapping("/player/{playerId}/game-info")
    public List<GameResult> test(@PathVariable Long playerId) {
        return aoe4OppService.collect(playerId);
    }

    @GetMapping("/player/search")
    public AutocompleteResponseDto searchPlayer(@RequestParam String gamertag, @RequestParam String leaderboard) {
        return aoe4OppService.autocomplete(gamertag, leaderboard);
    }
}

