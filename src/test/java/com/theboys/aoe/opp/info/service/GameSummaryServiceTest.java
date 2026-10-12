package com.theboys.aoe.opp.info.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theboys.aoe.opp.info.dao.GameSummaryDao;
import com.theboys.aoe.opp.info.dto.MatchupAnalysisRequest;
import com.theboys.aoe.opp.info.dto.MatchupAnalysisResponse;
import com.theboys.aoe.opp.info.dto.MatchupPlayerDto;
import com.theboys.aoe.opp.info.dto.PlayerMatchupResultDto;
import com.theboys.aoe.opp.info.dto.PlayerSummaryDto;
import com.theboys.aoe.opp.info.dto.PlayerTimelineDto;
import com.theboys.aoe.opp.info.dto.TimelineDataPointDto;
import com.theboys.aoe.opp.info.dto.TimelineMetricsDto;
import com.theboys.aoe.opp.info.dto.internal.ScrapedGameSummary;
import com.theboys.aoe.opp.info.exception.BusinessException;
import com.theboys.aoe.opp.info.exception.NotFoundException;
import com.theboys.aoe.opp.info.repository.db.GameSummaryDbRepository;
import com.theboys.aoe.opp.info.repository.http.GameSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameSummaryServiceTest {

    @Mock
    private GameSummaryRepository gameSummaryRepository;

    @Mock
    private GameSummaryDbRepository gameSummaryDbRepository;

    @Mock
    private PlayerSummaryAnalysisService playerSummaryAnalysisService;

    private ObjectMapper objectMapper;

    private GameSummaryService gameSummaryService;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        gameSummaryService = new GameSummaryService(
                gameSummaryRepository,
                gameSummaryDbRepository,
                playerSummaryAnalysisService,
                objectMapper
        );
    }

    @Test
    public void testScrape_NewEntity() {
        // given
        final String profileId = "3861757";
        final String gamerName = "GSUGambit";
        final String gameId = "223729268";
        final String leaderboard = "rm_1v1";
        final Integer page = 1;
        final String rawJson = "{\"summary\": true, \"duration\": 1200}";

        when(gameSummaryRepository.scrape(profileId, gamerName, gameId, leaderboard, page)).thenReturn(rawJson);
        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.empty());

        // when
        final String actualResponse = gameSummaryService.scrape(profileId, gamerName, gameId, leaderboard, page);

        // then
        assertEquals(rawJson, actualResponse);
        final ArgumentCaptor<GameSummaryDao> captor = ArgumentCaptor.forClass(GameSummaryDao.class);
        verify(gameSummaryDbRepository).save(captor.capture());
        final GameSummaryDao savedDao = captor.getValue();
        assertEquals(gameId, savedDao.getGameId());
        assertEquals(profileId, savedDao.getProfileId());
        assertEquals(gamerName, savedDao.getGamerName());
        assertEquals(leaderboard, savedDao.getLeaderboard());
        assertNotNull(savedDao.getSummaryData());
        assertNotNull(savedDao.getCreatedAt());
        assertNotNull(savedDao.getUpdatedAt());
    }

    @Test
    public void testScrape_ExistingEntity_UpdatesIdempotently() {
        // given
        final String profileId = "3861757";
        final String gamerName = "GSUGambit";
        final String gameId = "223729268";
        final String leaderboard = "rm_1v1";
        final Integer page = 1;
        final String rawJson = "{\"summary\": true, \"updated\": true}";

        final GameSummaryDao existingDao = new GameSummaryDao();
        final OffsetDateTime initialCreated = OffsetDateTime.now().minusDays(1);
        existingDao.setGameId(gameId);
        existingDao.setCreatedAt(initialCreated);

        when(gameSummaryRepository.scrape(profileId, gamerName, gameId, leaderboard, page)).thenReturn(rawJson);
        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.of(existingDao));

        // when
        final String actualResponse = gameSummaryService.scrape(profileId, gamerName, gameId, leaderboard, page);

        // then
        assertEquals(rawJson, actualResponse);
        final ArgumentCaptor<GameSummaryDao> captor = ArgumentCaptor.forClass(GameSummaryDao.class);
        verify(gameSummaryDbRepository).save(captor.capture());
        final GameSummaryDao savedDao = captor.getValue();
        assertEquals(initialCreated, savedDao.getCreatedAt());
        assertEquals("rm_1v1", savedDao.getLeaderboard());
        assertNotNull(savedDao.getUpdatedAt());
    }

    @Test
    public void testScrape_InvalidJson_ThrowsBusinessException() {
        // given
        final String profileId = "3861757";
        final String gamerName = "GSUGambit";
        final String gameId = "223729268";
        final String leaderboard = "rm_1v1";
        final Integer page = 1;

        when(gameSummaryRepository.scrape(profileId, gamerName, gameId, leaderboard, page)).thenReturn("invalid json");

        // when & then
        assertThrows(BusinessException.class, () ->
                gameSummaryService.scrape(profileId, gamerName, gameId, leaderboard, page)
        );
    }

    @Test
    public void testGetGameSummary_Found() throws Exception {
        // given
        final String gameId = "223729268";
        final JsonNode node = objectMapper.readTree("{\"id\": \"223729268\"}");
        final GameSummaryDao dao = new GameSummaryDao();
        dao.setGameId(gameId);
        dao.setSummaryData(node);

        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.of(dao));

        // when
        final String result = gameSummaryService.getGameSummary(gameId);

        // then
        assertTrue(result.contains("223729268"));
    }

    @Test
    public void testGetGameSummary_NotFound() {
        // given
        final String gameId = "999999";
        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> gameSummaryService.getGameSummary(gameId));
    }

    @Test
    public void testGetTimelineMetrics_Found_StructuredPayload() throws Exception {
        // given
        final String gameId = "223729268";
        final String jsonString = """
                {
                  "duration": 1800,
                  "teams": [
                    [
                      {
                        "player": {
                          "profile_id": 3861757,
                          "name": "GSUGambit",
                          "civilization": "english",
                          "result": "win"
                        },
                        "intervals": [
                          {
                            "timestamp": 60,
                            "villagerCount": 8,
                            "militaryCount": 0,
                            "totalPopulation": 8
                          },
                          {
                            "timestamp": 120,
                            "villagerCount": 14,
                            "militaryCount": 2,
                            "totalPopulation": 16
                          }
                        ]
                      }
                    ],
                    [
                      {
                        "player": {
                          "profile_id": 9999999,
                          "name": "Opponent",
                          "civilization": "french",
                          "result": "loss"
                        },
                        "intervals": [
                          {
                            "timestamp": 60,
                            "villagerCount": 7,
                            "militaryCount": 1,
                            "totalPopulation": 8
                          }
                        ]
                      }
                    ]
                  ]
                }
                """;

        final JsonNode node = objectMapper.readTree(jsonString);
        final GameSummaryDao dao = new GameSummaryDao();
        dao.setGameId(gameId);
        dao.setSummaryData(node);

        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.of(dao));

        // when
        final TimelineMetricsDto timelineMetrics = gameSummaryService.getTimelineMetrics(gameId);

        // then
        assertEquals("223729268", timelineMetrics.getGameId());
        assertEquals(1800, timelineMetrics.getDurationSeconds());
        assertEquals(2, timelineMetrics.getPlayers().size());

        final PlayerTimelineDto player1 = timelineMetrics.getPlayers().get(0);
        assertEquals("3861757", player1.getProfileId());
        assertEquals("GSUGambit", player1.getName());
        assertEquals("english", player1.getCivilization());
        assertEquals("win", player1.getResult());
        assertEquals(2, player1.getDataPoints().size());
        assertEquals(60, player1.getDataPoints().get(0).getTimestamp());
        assertEquals(8, player1.getDataPoints().get(0).getVillagerCount());
        assertEquals(0, player1.getDataPoints().get(0).getMilitaryCount());
        assertEquals(8, player1.getDataPoints().get(0).getTotalPopulation());

        final PlayerTimelineDto player2 = timelineMetrics.getPlayers().get(1);
        assertEquals("9999999", player2.getProfileId());
        assertEquals("Opponent", player2.getName());
        assertEquals("french", player2.getCivilization());
        assertEquals("loss", player2.getResult());
        assertEquals(1, player2.getDataPoints().size());
        assertEquals(7, player2.getDataPoints().get(0).getVillagerCount());
        assertEquals(1, player2.getDataPoints().get(0).getMilitaryCount());
    }

    @Test
    public void testGetTimelineMetrics_AlternativeFieldNamesAndFallbacks() throws Exception {
        // given
        final String gameId = "111222";
        final String jsonString = """
                {
                  "game": {
                    "duration": 900
                  },
                  "players": [
                    {
                      "profileId": "123",
                      "gamerName": "PlayerA",
                      "civ": "abbasid",
                      "outcome": "win",
                      "timeline": [
                        {
                          "time": 30,
                          "vills": 10,
                          "army": 5
                        }
                      ]
                    }
                  ]
                }
                """;

        final JsonNode node = objectMapper.readTree(jsonString);
        final GameSummaryDao dao = new GameSummaryDao();
        dao.setGameId(gameId);
        dao.setSummaryData(node);

        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.of(dao));

        // when
        final TimelineMetricsDto timelineMetrics = gameSummaryService.getTimelineMetrics(gameId);

        // then
        assertEquals("111222", timelineMetrics.getGameId());
        assertEquals(900, timelineMetrics.getDurationSeconds());
        assertEquals(1, timelineMetrics.getPlayers().size());

        final PlayerTimelineDto player = timelineMetrics.getPlayers().get(0);
        assertEquals("123", player.getProfileId());
        assertEquals("PlayerA", player.getName());
        assertEquals("abbasid", player.getCivilization());
        assertEquals("win", player.getResult());
        assertEquals(1, player.getDataPoints().size());
        assertEquals(30, player.getDataPoints().get(0).getTimestamp());
        assertEquals(10, player.getDataPoints().get(0).getVillagerCount());
        assertEquals(5, player.getDataPoints().get(0).getMilitaryCount());
        assertEquals(15, player.getDataPoints().get(0).getTotalPopulation());
    }

    @Test
    public void testGetTimelineMetrics_NullOrMissingSummaryData() {
        // given
        final String gameId = "empty-game";
        final GameSummaryDao dao = new GameSummaryDao();
        dao.setGameId(gameId);
        dao.setLeaderboard("rm_solo");
        dao.setSummaryData(null);

        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.of(dao));

        // when
        final TimelineMetricsDto result = gameSummaryService.getTimelineMetrics(gameId);

        // then
        assertEquals("empty-game", result.getGameId());
        assertEquals("rm_solo", result.getLeaderboard());
        assertEquals(0, result.getDurationSeconds());
        assertEquals(0, result.getPlayers().size());
    }

    @Test
    public void testGetTimelineMetrics_LeaderboardFallbackToDao() throws Exception {
        // given
        final String gameId = "dao-fallback-game";
        final String jsonString = """
                {
                  "duration": 500,
                  "players": []
                }
                """;

        final JsonNode node = objectMapper.readTree(jsonString);
        final GameSummaryDao dao = new GameSummaryDao();
        dao.setGameId(gameId);
        dao.setLeaderboard("rm_1v1");
        dao.setSummaryData(node);

        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.of(dao));

        // when
        final TimelineMetricsDto result = gameSummaryService.getTimelineMetrics(gameId);

        // then
        assertEquals("dao-fallback-game", result.getGameId());
        assertEquals("rm_1v1", result.getLeaderboard());
        assertEquals(500, result.getDurationSeconds());
    }

    @Test
    public void testGetTimelineMetrics_LeaderboardNestedInGameNode() throws Exception {
        // given
        final String gameId = "nested-game";
        final String jsonString = """
                {
                  "game": {
                    "duration": 600,
                    "leaderboard": "rm_team"
                  },
                  "players": []
                }
                """;

        final JsonNode node = objectMapper.readTree(jsonString);
        final GameSummaryDao dao = new GameSummaryDao();
        dao.setGameId(gameId);
        dao.setLeaderboard("fallback");
        dao.setSummaryData(node);

        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.of(dao));

        // when
        final TimelineMetricsDto result = gameSummaryService.getTimelineMetrics(gameId);

        // then
        assertEquals("nested-game", result.getGameId());
        assertEquals("rm_team", result.getLeaderboard());
        assertEquals(600, result.getDurationSeconds());
    }

    @Test
    public void testGetTimelineMetrics_RealMatch250598826() throws Exception {
        // given
        final String gameId = "250598826";
        final JsonNode node;
        try (final InputStream inputStream = getClass().getResourceAsStream("/samples/match_250598826.json")) {
            assertNotNull(inputStream);
            node = objectMapper.readTree(inputStream);
        }

        final GameSummaryDao dao = new GameSummaryDao();
        dao.setGameId(gameId);
        dao.setSummaryData(node);

        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.of(dao));

        // when
        final TimelineMetricsDto timelineMetrics = gameSummaryService.getTimelineMetrics(gameId);

        // then
        assertEquals("250598826", timelineMetrics.getGameId());
        assertEquals("rm_1v1", timelineMetrics.getLeaderboard());
        assertEquals("Gorge", timelineMetrics.getMapName());
        assertEquals(3129, timelineMetrics.getDurationSeconds());
        assertEquals(2, timelineMetrics.getPlayers().size());

        // Winner is sorted first
        final PlayerTimelineDto player1 = timelineMetrics.getPlayers().get(0);
        assertEquals("3861757", player1.getProfileId());
        assertEquals("GSUGambit", player1.getName());
        assertEquals("english", player1.getCivilization());
        assertEquals("win", player1.getResult());
        assertEquals(122, player1.getApm());
        assertEquals(153, player1.getDataPoints().size());

        // t = 0s
        assertEquals(0, player1.getDataPoints().get(0).getTimestamp());
        assertEquals(6, player1.getDataPoints().get(0).getVillagerCount());
        assertEquals(1, player1.getDataPoints().get(0).getMilitaryCount());
        assertEquals(7, player1.getDataPoints().get(0).getTotalPopulation());

        // t = 60s
        assertEquals(60, player1.getDataPoints().get(3).getTimestamp());
        assertEquals(8, player1.getDataPoints().get(3).getVillagerCount());
        assertEquals(1, player1.getDataPoints().get(3).getMilitaryCount());
        assertEquals(9, player1.getDataPoints().get(3).getTotalPopulation());

        // last data point at t = 3040s
        final TimelineDataPointDto player1LastPoint = player1.getDataPoints().get(152);
        assertEquals(3040, player1LastPoint.getTimestamp());
        assertEquals(119, player1LastPoint.getVillagerCount());
        assertEquals(65, player1LastPoint.getMilitaryCount());
        assertEquals(184, player1LastPoint.getTotalPopulation());

        // Loser is sorted second
        final PlayerTimelineDto player2 = timelineMetrics.getPlayers().get(1);
        assertEquals("10880067", player2.getProfileId());
        assertEquals("peter1057285", player2.getName());
        assertEquals("order_of_the_dragon", player2.getCivilization());
        assertEquals("loss", player2.getResult());
        assertEquals(173, player2.getApm());
        assertEquals(153, player2.getDataPoints().size());

        // t = 0s
        assertEquals(0, player2.getDataPoints().get(0).getTimestamp());
        assertEquals(5, player2.getDataPoints().get(0).getVillagerCount());
        assertEquals(1, player2.getDataPoints().get(0).getMilitaryCount());
        assertEquals(6, player2.getDataPoints().get(0).getTotalPopulation());

        // t = 60s (index 3: 0, 20, 40, 60)
        assertEquals(60, player2.getDataPoints().get(3).getTimestamp());
        assertEquals(7, player2.getDataPoints().get(3).getVillagerCount());
        assertEquals(1, player2.getDataPoints().get(3).getMilitaryCount());
        assertEquals(8, player2.getDataPoints().get(3).getTotalPopulation());

        // last data point at t = 3040s (index 152)
        final TimelineDataPointDto player2LastPoint = player2.getDataPoints().get(152);
        assertEquals(3040, player2LastPoint.getTimestamp());
        assertEquals(52, player2LastPoint.getVillagerCount());
        assertEquals(8, player2LastPoint.getMilitaryCount());
        assertEquals(60, player2LastPoint.getTotalPopulation());
    }

    @Test
    public void testGetTimelineMetrics_MissingBuildOrder() throws Exception {
        // given
        final String gameId = "no-build-order";
        final String jsonString = """
                {
                  "duration": 600,
                  "players": [
                    {
                      "profileId": "123",
                      "name": "PlayerOne",
                      "civilization": "french",
                      "result": "win",
                      "resources": {
                        "timestamps": [0, 60, 120]
                      }
                    }
                  ]
                }
                """;
        final JsonNode node = objectMapper.readTree(jsonString);
        final GameSummaryDao dao = new GameSummaryDao();
        dao.setGameId(gameId);
        dao.setSummaryData(node);

        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.of(dao));

        // when
        final TimelineMetricsDto result = gameSummaryService.getTimelineMetrics(gameId);

        // then
        assertEquals(1, result.getPlayers().size());
        final PlayerTimelineDto player = result.getPlayers().get(0);
        assertEquals(3, player.getDataPoints().size());
        assertEquals(0, player.getDataPoints().get(0).getVillagerCount());
        assertEquals(0, player.getDataPoints().get(0).getMilitaryCount());
        assertEquals(0, player.getDataPoints().get(0).getTotalPopulation());
    }

    @Test
    public void testGetTimelineMetrics_MissingTimestamps() throws Exception {
        // given
        final String gameId = "no-timestamps";
        final String jsonString = """
                {
                  "duration": 600,
                  "players": [
                    {
                      "profileId": "123",
                      "name": "PlayerOne",
                      "civilization": "french",
                      "result": "win"
                    }
                  ]
                }
                """;
        final JsonNode node = objectMapper.readTree(jsonString);
        final GameSummaryDao dao = new GameSummaryDao();
        dao.setGameId(gameId);
        dao.setSummaryData(node);

        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.of(dao));

        // when
        final TimelineMetricsDto result = gameSummaryService.getTimelineMetrics(gameId);

        // then
        assertEquals(1, result.getPlayers().size());
        assertTrue(result.getPlayers().get(0).getDataPoints().isEmpty());
    }

    @Test
    public void testGetTimelineMetrics_NotFound() {
        // given
        final String gameId = "nonexistent";
        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> gameSummaryService.getTimelineMetrics(gameId));
    }

    @Test
    public void testScrapeMatchup_FullCacheHit_BypassesHttpFetch() throws Exception {
        // given
        final MatchupPlayerDto player = MatchupPlayerDto.builder()
                .withProfileId("111")
                .withGamerName("PlayerOne")
                .withCivilization("english")
                .withLeaderboard("rm_solo")
                .build();
        final MatchupAnalysisRequest request = MatchupAnalysisRequest.builder()
                .withPlayers(List.of(player))
                .build();

        final Map<String, String> urls = new LinkedHashMap<>();
        urls.put("g1", "https://aoe4world.com/players/111-PlayerOne/games/g1?sig=1");
        urls.put("g2", "https://aoe4world.com/players/111-PlayerOne/games/g2?sig=2");

        when(gameSummaryRepository.scrapeRecentGameUrls("111", "PlayerOne", "english", "rm_solo", 10))
                .thenReturn(urls);

        final GameSummaryDao dao1 = new GameSummaryDao();
        dao1.setGameId("g1");
        dao1.setSummaryData(objectMapper.readTree("{\"duration\": 100, \"finishedAt\": 1234567}"));

        final GameSummaryDao dao2 = new GameSummaryDao();
        dao2.setGameId("g2");
        dao2.setSummaryData(objectMapper.readTree("{\"duration\": 200, \"finishedAt\": 2345678}"));

        when(gameSummaryDbRepository.findByGameId("g1")).thenReturn(Optional.of(dao1));
        when(gameSummaryDbRepository.findByGameId("g2")).thenReturn(Optional.of(dao2));

        final PlayerSummaryDto dummySummary = PlayerSummaryDto.builder().withAverageApm(150.0).build();
        when(playerSummaryAnalysisService.analyzePlayerSummaries(
                org.mockito.ArgumentMatchers.eq("111"),
                org.mockito.ArgumentMatchers.eq("PlayerOne"),
                org.mockito.ArgumentMatchers.anyList()
        )).thenReturn(dummySummary);

        // when
        final MatchupAnalysisResponse response = gameSummaryService.scrapeMatchup(request);

        // then
        assertNotNull(response);
        assertEquals(1, response.getResults().size());
        assertEquals(List.of("g1", "g2"), response.getResults().get(0).getGameIds());
        assertEquals(dummySummary, response.getResults().get(0).getSummary());

        verify(gameSummaryRepository, never()).fetchGameSummaryJson(anyString());
        verify(gameSummaryDbRepository, never()).save(any(GameSummaryDao.class));
    }

    @Test
    public void testScrapeMatchup_FullCacheMiss() {
        // given
        final MatchupPlayerDto player1 = MatchupPlayerDto.builder()
                .withProfileId("111")
                .withGamerName("PlayerOne")
                .withCivilization("english")
                .withLeaderboard("rm_solo")
                .build();
        final MatchupPlayerDto player2 = MatchupPlayerDto.builder()
                .withProfileId("222")
                .withGamerName("PlayerTwo")
                .withCivilization("french")
                .withLeaderboard("rm_solo")
                .build();

        final MatchupAnalysisRequest request = MatchupAnalysisRequest.builder()
                .withPlayers(List.of(player1, player2))
                .build();

        final Map<String, String> player1Urls = new LinkedHashMap<>();
        player1Urls.put("g1", "https://aoe4world.com/players/111-PlayerOne/games/g1?sig=1");
        player1Urls.put("g2", "https://aoe4world.com/players/111-PlayerOne/games/g2?sig=2");

        final Map<String, String> player2Urls = new LinkedHashMap<>();
        player2Urls.put("g3", "https://aoe4world.com/players/222-PlayerTwo/games/g3?sig=3");

        when(gameSummaryRepository.scrapeRecentGameUrls("111", "PlayerOne", "english", "rm_solo", 10))
                .thenReturn(player1Urls);
        when(gameSummaryRepository.scrapeRecentGameUrls("222", "PlayerTwo", "french", "rm_solo", 10))
                .thenReturn(player2Urls);

        when(gameSummaryDbRepository.findByGameId("g1")).thenReturn(Optional.empty());
        when(gameSummaryDbRepository.findByGameId("g2")).thenReturn(Optional.empty());
        when(gameSummaryDbRepository.findByGameId("g3")).thenReturn(Optional.empty());

        when(gameSummaryRepository.fetchGameSummaryJson("https://aoe4world.com/players/111-PlayerOne/games/g1?sig=1"))
                .thenReturn(Optional.of("{\"duration\": 100, \"finishedAt\": 1000}"));
        when(gameSummaryRepository.fetchGameSummaryJson("https://aoe4world.com/players/111-PlayerOne/games/g2?sig=2"))
                .thenReturn(Optional.of("{\"duration\": 200, \"finishedAt\": 2000}"));
        when(gameSummaryRepository.fetchGameSummaryJson("https://aoe4world.com/players/222-PlayerTwo/games/g3?sig=3"))
                .thenReturn(Optional.of("{\"duration\": 300, \"finishedAt\": 3000}"));

        final PlayerSummaryDto dummySummary1 = PlayerSummaryDto.builder().withAverageApm(150.0).build();
        final PlayerSummaryDto dummySummary2 = PlayerSummaryDto.builder().withAverageApm(120.0).build();
        when(playerSummaryAnalysisService.analyzePlayerSummaries(
                org.mockito.ArgumentMatchers.eq("111"),
                org.mockito.ArgumentMatchers.eq("PlayerOne"),
                org.mockito.ArgumentMatchers.anyList()
        )).thenReturn(dummySummary1);
        when(playerSummaryAnalysisService.analyzePlayerSummaries(
                org.mockito.ArgumentMatchers.eq("222"),
                org.mockito.ArgumentMatchers.eq("PlayerTwo"),
                org.mockito.ArgumentMatchers.anyList()
        )).thenReturn(dummySummary2);

        // when
        final MatchupAnalysisResponse response = gameSummaryService.scrapeMatchup(request);

        // then
        assertNotNull(response);
        assertEquals(2, response.getResults().size());

        final PlayerMatchupResultDto res1 = response.getResults().get(0);
        assertEquals("111", res1.getProfileId());
        assertEquals("PlayerOne", res1.getGamerName());
        assertEquals("english", res1.getCivilization());
        assertEquals(List.of("g1", "g2"), res1.getGameIds());
        assertEquals(dummySummary1, res1.getSummary());

        final PlayerMatchupResultDto res2 = response.getResults().get(1);
        assertEquals("222", res2.getProfileId());
        assertEquals("PlayerTwo", res2.getGamerName());
        assertEquals("french", res2.getCivilization());
        assertEquals(List.of("g3"), res2.getGameIds());
        assertEquals(dummySummary2, res2.getSummary());

        verify(gameSummaryDbRepository).save(org.mockito.ArgumentMatchers.argThat(dao -> "g1".equals(dao.getGameId())));
        verify(gameSummaryDbRepository).save(org.mockito.ArgumentMatchers.argThat(dao -> "g2".equals(dao.getGameId())));
        verify(gameSummaryDbRepository).save(org.mockito.ArgumentMatchers.argThat(dao -> "g3".equals(dao.getGameId())));
    }

    @Test
    public void testScrapeMatchup_PartialCacheHit_FinishedAndUnfinished() throws Exception {
        // given
        final MatchupPlayerDto player = MatchupPlayerDto.builder()
                .withProfileId("111")
                .withGamerName("PlayerOne")
                .withCivilization("english")
                .withLeaderboard("rm_solo")
                .build();
        final MatchupAnalysisRequest request = MatchupAnalysisRequest.builder()
                .withPlayers(List.of(player))
                .build();

        final Map<String, String> urls = new LinkedHashMap<>();
        urls.put("g1", "https://aoe4world.com/players/111-PlayerOne/games/g1?sig=1");
        urls.put("g2", "https://aoe4world.com/players/111-PlayerOne/games/g2?sig=2");
        urls.put("g3", "https://aoe4world.com/players/111-PlayerOne/games/g3?sig=3");

        when(gameSummaryRepository.scrapeRecentGameUrls("111", "PlayerOne", "english", "rm_solo", 10))
                .thenReturn(urls);

        // g1 is cached and finished
        final GameSummaryDao dao1 = new GameSummaryDao();
        dao1.setGameId("g1");
        dao1.setSummaryData(objectMapper.readTree("{\"duration\": 100, \"finishedAt\": 1234567}"));
        when(gameSummaryDbRepository.findByGameId("g1")).thenReturn(Optional.of(dao1));

        // g2 is in DB but unfinished (finishedAt is null)
        final GameSummaryDao dao2 = new GameSummaryDao();
        dao2.setGameId("g2");
        dao2.setSummaryData(objectMapper.readTree("{\"duration\": 200, \"finishedAt\": null}"));
        when(gameSummaryDbRepository.findByGameId("g2")).thenReturn(Optional.of(dao2));

        // g3 is not in DB
        when(gameSummaryDbRepository.findByGameId("g3")).thenReturn(Optional.empty());

        when(gameSummaryRepository.fetchGameSummaryJson("https://aoe4world.com/players/111-PlayerOne/games/g2?sig=2"))
                .thenReturn(Optional.of("{\"duration\": 250, \"finishedAt\": 2222222}"));
        when(gameSummaryRepository.fetchGameSummaryJson("https://aoe4world.com/players/111-PlayerOne/games/g3?sig=3"))
                .thenReturn(Optional.of("{\"duration\": 300, \"finishedAt\": 3333333}"));

        final PlayerSummaryDto dummySummary = PlayerSummaryDto.builder().withAverageApm(150.0).build();
        when(playerSummaryAnalysisService.analyzePlayerSummaries(
                org.mockito.ArgumentMatchers.eq("111"),
                org.mockito.ArgumentMatchers.eq("PlayerOne"),
                org.mockito.ArgumentMatchers.anyList()
        )).thenReturn(dummySummary);

        // when
        final MatchupAnalysisResponse response = gameSummaryService.scrapeMatchup(request);

        // then
        assertNotNull(response);
        assertEquals(1, response.getResults().size());
        assertEquals(List.of("g1", "g2", "g3"), response.getResults().get(0).getGameIds());

        verify(gameSummaryRepository, never()).fetchGameSummaryJson("https://aoe4world.com/players/111-PlayerOne/games/g1?sig=1");
        verify(gameSummaryRepository).fetchGameSummaryJson("https://aoe4world.com/players/111-PlayerOne/games/g2?sig=2");
        verify(gameSummaryRepository).fetchGameSummaryJson("https://aoe4world.com/players/111-PlayerOne/games/g3?sig=3");

        verify(gameSummaryDbRepository).save(org.mockito.ArgumentMatchers.argThat(dao -> "g2".equals(dao.getGameId())));
        verify(gameSummaryDbRepository).save(org.mockito.ArgumentMatchers.argThat(dao -> "g3".equals(dao.getGameId())));
    }

    @Test
    public void testScrapeMatchup_EmptyOrNullRequest() {
        // when
        final MatchupAnalysisResponse resNull = gameSummaryService.scrapeMatchup(null);
        final MatchupAnalysisResponse resEmpty = gameSummaryService.scrapeMatchup(
                MatchupAnalysisRequest.builder().withPlayers(Collections.emptyList()).build()
        );

        // then
        assertNotNull(resNull);
        assertTrue(resNull.getResults().isEmpty());
        assertNotNull(resEmpty);
        assertTrue(resEmpty.getResults().isEmpty());
    }

    @Test
    public void testScrapeMatchup_ExistingEntityUpdatePreservesCreatedAt() {
        // given
        final MatchupPlayerDto player = MatchupPlayerDto.builder()
                .withProfileId("111")
                .withGamerName("PlayerOne")
                .withCivilization("english")
                .withLeaderboard("rm_solo")
                .build();
        final MatchupAnalysisRequest request = MatchupAnalysisRequest.builder()
                .withPlayers(List.of(player))
                .build();

        final OffsetDateTime originalCreatedAt = OffsetDateTime.now().minusDays(5);
        final GameSummaryDao existingDao = new GameSummaryDao();
        existingDao.setGameId("g1");
        existingDao.setCreatedAt(originalCreatedAt);

        final Map<String, String> urls = new LinkedHashMap<>();
        urls.put("g1", "https://aoe4world.com/players/111-PlayerOne/games/g1?sig=1");

        when(gameSummaryRepository.scrapeRecentGameUrls("111", "PlayerOne", "english", "rm_solo", 10))
                .thenReturn(urls);
        when(gameSummaryDbRepository.findByGameId("g1")).thenReturn(Optional.of(existingDao));
        when(gameSummaryRepository.fetchGameSummaryJson("https://aoe4world.com/players/111-PlayerOne/games/g1?sig=1"))
                .thenReturn(Optional.of("{\"duration\": 500, \"finishedAt\": 12345}"));

        // when
        final MatchupAnalysisResponse response = gameSummaryService.scrapeMatchup(request);

        // then
        assertNotNull(response);
        assertEquals(1, response.getResults().size());
        assertEquals(List.of("g1"), response.getResults().get(0).getGameIds());

        final ArgumentCaptor<GameSummaryDao> captor = ArgumentCaptor.forClass(GameSummaryDao.class);
        verify(gameSummaryDbRepository).save(captor.capture());
        final GameSummaryDao savedDao = captor.getValue();
        assertEquals(originalCreatedAt, savedDao.getCreatedAt());
        assertNotNull(savedDao.getUpdatedAt());
        assertEquals("PlayerOne", savedDao.getGamerName());
    }

    @Test
    public void testScrapeMatchup_PartialFailureGraceful() {
        // given
        final MatchupPlayerDto player1 = MatchupPlayerDto.builder()
                .withProfileId("111")
                .withGamerName("PlayerOne")
                .withCivilization("english")
                .withLeaderboard("rm_solo")
                .build();
        final MatchupPlayerDto player2 = MatchupPlayerDto.builder()
                .withProfileId("222")
                .withGamerName("PlayerTwo")
                .withCivilization("french")
                .withLeaderboard("rm_solo")
                .build();

        final MatchupAnalysisRequest request = MatchupAnalysisRequest.builder()
                .withPlayers(List.of(player1, player2))
                .build();

        final Map<String, String> player1Urls = new LinkedHashMap<>();
        player1Urls.put("g1", "https://aoe4world.com/players/111-PlayerOne/games/g1?sig=1");
        player1Urls.put("g2", "https://aoe4world.com/players/111-PlayerOne/games/g2?sig=2");

        when(gameSummaryRepository.scrapeRecentGameUrls("111", "PlayerOne", "english", "rm_solo", 10))
                .thenReturn(player1Urls);
        when(gameSummaryRepository.scrapeRecentGameUrls("222", "PlayerTwo", "french", "rm_solo", 10))
                .thenThrow(new RuntimeException("Network timeout"));

        when(gameSummaryDbRepository.findByGameId("g1")).thenReturn(Optional.empty());
        when(gameSummaryDbRepository.findByGameId("g2")).thenReturn(Optional.empty());

        when(gameSummaryRepository.fetchGameSummaryJson("https://aoe4world.com/players/111-PlayerOne/games/g1?sig=1"))
                .thenReturn(Optional.of("invalid json"));
        when(gameSummaryRepository.fetchGameSummaryJson("https://aoe4world.com/players/111-PlayerOne/games/g2?sig=2"))
                .thenReturn(Optional.of("{\"duration\": 200, \"finishedAt\": 2222}"));

        // when
        final MatchupAnalysisResponse response = gameSummaryService.scrapeMatchup(request);

        // then
        assertNotNull(response);
        assertEquals(2, response.getResults().size());
        assertEquals(List.of("g2"), response.getResults().get(0).getGameIds());
        assertTrue(response.getResults().get(1).getGameIds().isEmpty());
    }
}
