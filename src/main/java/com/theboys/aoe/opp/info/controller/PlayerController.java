package com.theboys.aoe.opp.info.controller;

import com.theboys.aoe.opp.info.dto.AutocompleteResponseDto;
import com.theboys.aoe.opp.info.dto.GamesPage;
import com.theboys.aoe.opp.info.dto.internal.GameResult;
import com.theboys.aoe.opp.info.service.Aoe4OppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1/aoe4-opp")
@RestController
@RequiredArgsConstructor
public class PlayerController {

    private final Aoe4OppService aoe4OppService;

    @GetMapping("/player/{playerId}/game-info")
    public List<GameResult> test(
            @PathVariable final Long playerId,
            @RequestParam(required = false) final Integer page,
            @RequestParam(required = false, name = "per_page") final Integer perPage,
            @RequestParam(required = false, name = "leaderboard") final List<String> leaderboard,
            @RequestParam(required = false, name = "leaderboards") final List<String> leaderboards
    ) {
        final List<String> resolvedLeaderboards = leaderboard != null ? leaderboard : leaderboards;
        return aoe4OppService.collect(playerId, page, perPage, resolvedLeaderboards);
    }

    @GetMapping("/player/{playerId}/games")
    public GamesPage retrieveGames(
            @PathVariable final Long playerId,
            @RequestParam(required = false) final Integer page,
            @RequestParam(required = false, name = "per_page") final Integer perPage,
            @RequestParam(required = false, name = "leaderboard") final List<String> leaderboard,
            @RequestParam(required = false, name = "leaderboards") final List<String> leaderboards
    ) {
        final List<String> resolvedLeaderboards = leaderboard != null ? leaderboard : leaderboards;
        return aoe4OppService.retrieveGames(playerId, page, perPage, resolvedLeaderboards);
    }

    @GetMapping("/player/search")
    public AutocompleteResponseDto searchPlayer(
            @RequestParam final String gamertag,
            @RequestParam final String leaderboard
    ) {
        return aoe4OppService.autocomplete(gamertag, leaderboard);
    }
}

