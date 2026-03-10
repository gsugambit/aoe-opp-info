package com.theboys.aoe.opp.info.controller;

import com.theboys.aoe.opp.info.repository.http.GameSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/aoe4-opp/game-summary")
@RestController
@RequiredArgsConstructor
public class GameSummaryController {

    private final GameSummaryRepository gameSummaryRepository;

    /**
     * Protecting the userAgent I am identifying the app with
     *
     * @param gamerProfile
     * @param userAgent
     */
    // TODO: this will be made into a config once scraping is truly supported by the app
    @GetMapping("/scrape")
    public void scrape(@RequestParam String gamerProfile, @RequestParam String userAgent) {
        gameSummaryRepository.scrape(gamerProfile, userAgent);
    }
}

