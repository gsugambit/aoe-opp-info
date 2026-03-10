package com.theboys.aoe.opp.info.controller;

import com.theboys.aoe.opp.info.repository.http.GameSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/aoe4-opp/game-summary")
@RestController
@RequiredArgsConstructor
public class GameSummaryController {

    private final GameSummaryRepository gameSummaryRepository;

    @GetMapping("/scrape")
    public void scrape() {
        gameSummaryRepository.scrape();
    }
}

