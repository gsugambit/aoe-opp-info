package com.theboys.aoe.opp.info.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theboys.aoe.opp.info.dto.MatchupAnalysisRequest;
import com.theboys.aoe.opp.info.dto.MatchupAnalysisResponse;
import com.theboys.aoe.opp.info.dto.MatchupPlayerDto;
import com.theboys.aoe.opp.info.dto.PlayerMatchupResultDto;
import com.theboys.aoe.opp.info.dto.PlayerSummaryDto;
import com.theboys.aoe.opp.info.dto.TimelineMetricsDto;
import com.theboys.aoe.opp.info.service.GameSummaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameSummaryControllerTest {

    @Mock
    private GameSummaryService gameSummaryService;

    @InjectMocks
    private GameSummaryController gameSummaryController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testScrape() {
        // given
        final String profileId = "3861757";
        final String gamerName = "GSUGambit";
        final String gameId = "223729268";
        final String leaderboard = "rm_1v1";
        final Integer page = 1;
        final String expectedResponse = "{\"summary\": true}";

        when(gameSummaryService.scrape(profileId, gamerName, gameId, leaderboard, page)).thenReturn(expectedResponse);

        // when
        final String actualResponse = gameSummaryController.scrape(profileId, gamerName, gameId, leaderboard, page);

        // then
        assertEquals(expectedResponse, actualResponse);
        verify(gameSummaryService).scrape(profileId, gamerName, gameId, leaderboard, page);
    }

    @Test
    public void testScrapeMatchup() {
        // given
        final MatchupPlayerDto player = MatchupPlayerDto.builder()
                .withProfileId("3861757")
                .withGamerName("GSUGambit")
                .withCivilization("english")
                .withLeaderboard("rm_solo")
                .build();
        final MatchupAnalysisRequest request = MatchupAnalysisRequest.builder()
                .withPlayers(Collections.singletonList(player))
                .build();
        final PlayerSummaryDto summaryDto = PlayerSummaryDto.builder()
                .withAverageApm(145.5)
                .withGamesAnalyzed(1)
                .build();
        final PlayerMatchupResultDto resultDto = PlayerMatchupResultDto.builder()
                .withProfileId("3861757")
                .withGamerName("GSUGambit")
                .withCivilization("english")
                .withGameIds(Collections.singletonList("223729268"))
                .withSummary(summaryDto)
                .build();
        final MatchupAnalysisResponse expectedResponse = MatchupAnalysisResponse.builder()
                .withResults(Collections.singletonList(resultDto))
                .build();

        when(gameSummaryService.scrapeMatchup(request)).thenReturn(expectedResponse);

        // when
        final MatchupAnalysisResponse actualResponse = gameSummaryController.scrapeMatchup(request);

        // then
        assertEquals(expectedResponse, actualResponse);
        verify(gameSummaryService).scrapeMatchup(request);
    }

    @Test
    public void testGetGameSummary() {
        // given
        final String gameId = "223729268";
        final String expectedJson = "{\"gameId\": \"223729268\"}";

        when(gameSummaryService.getGameSummary(gameId)).thenReturn(expectedJson);

        // when
        final String actualJson = gameSummaryController.getGameSummary(gameId);

        // then
        assertEquals(expectedJson, actualJson);
        verify(gameSummaryService).getGameSummary(gameId);
    }

    @Test
    public void testGetTimelineMetrics() {
        // given
        final String gameId = "223729268";
        final TimelineMetricsDto expectedDto = TimelineMetricsDto.builder()
                .withGameId(gameId)
                .withLeaderboard("rm_1v1")
                .withDurationSeconds(1500)
                .withPlayers(Collections.emptyList())
                .build();

        when(gameSummaryService.getTimelineMetrics(gameId)).thenReturn(expectedDto);

        // when
        final TimelineMetricsDto actualDto = gameSummaryController.getTimelineMetrics(gameId);

        // then
        assertEquals(expectedDto, actualDto);
        assertEquals("rm_1v1", actualDto.getLeaderboard());
        verify(gameSummaryService).getTimelineMetrics(gameId);
    }
}
