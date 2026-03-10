package com.theboys.aoe.opp.info.service;

import com.theboys.aoe.opp.info.repository.http.GameSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class GameSummaryService {

    private final GameSummaryRepository gameSummaryRepository;

    public void scrape(String gamerProfile, String userAgent) {
        gameSummaryRepository.scrape(gamerProfile, userAgent);
    }
}
