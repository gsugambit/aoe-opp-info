package com.theboys.aoe.opp.info.controller;

import com.theboys.aoe.opp.info.dto.MatchupAnalysisRequest;
import com.theboys.aoe.opp.info.dto.MatchupAnalysisResponse;
import com.theboys.aoe.opp.info.dto.TimelineMetricsDto;
import com.theboys.aoe.opp.info.service.GameSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/aoe4-opp/game-summary")
@RestController
@RequiredArgsConstructor
public class GameSummaryController {

    private final GameSummaryService gameSummaryService;

    @GetMapping(value = {"/scrape", "/scape"}, produces = "application/json")
    public String scrape(
            @RequestParam final String profileId,
            @RequestParam final String gamerName,
            @RequestParam final String gameId,
            @RequestParam final String leaderboard,
            @RequestParam final Integer page
    ) {
        return gameSummaryService.scrape(profileId, gamerName, gameId, leaderboard, page);
    }

    @PostMapping(value = "/matchup-analysis", produces = "application/json")
    public MatchupAnalysisResponse scrapeMatchup(@RequestBody final MatchupAnalysisRequest request) {
        return gameSummaryService.scrapeMatchup(request);
    }

    @GetMapping(value = "/{gameId}", produces = "application/json")
    public String getGameSummary(@PathVariable final String gameId) {
        return gameSummaryService.getGameSummary(gameId);
    }

    @GetMapping(value = "/{gameId}/timeline", produces = "application/json")
    public TimelineMetricsDto getTimelineMetrics(@PathVariable final String gameId) {
        return gameSummaryService.getTimelineMetrics(gameId);
    }
}

