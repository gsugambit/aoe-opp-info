package com.theboys.aoe.opp.info.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theboys.aoe.opp.info.dao.GameSummaryDao;
import com.theboys.aoe.opp.info.dto.GameAnalysisResponse;
import com.theboys.aoe.opp.info.dto.MatchupAnalysisRequest;
import com.theboys.aoe.opp.info.dto.MatchupAnalysisResponse;
import com.theboys.aoe.opp.info.dto.MatchupPlayerDto;
import com.theboys.aoe.opp.info.dto.PlayerGameAnalysisDto;
import com.theboys.aoe.opp.info.dto.PlayerMatchupResultDto;
import com.theboys.aoe.opp.info.dto.PlayerSummaryDto;
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

    @Test
    public void testGetGameAnalysis_Success() throws Exception {
        // given
        final String gameId = "223729268";
        final String json = "{\"map\":\"Gorge\",\"duration\":2188,\"leaderboard\":\"rm_1v1\",\"players\":[]}";
        final JsonNode summaryData = objectMapper.readTree(json);

        final GameSummaryDao dao = new GameSummaryDao();
        dao.setGameId(gameId);
        dao.setLeaderboard("rm_1v1");
        dao.setSummaryData(summaryData);

        final PlayerGameAnalysisDto playerDto = PlayerGameAnalysisDto.builder()
                .withProfileId("3861757")
                .withName("GSUGambit")
                .withCivilization("english")
                .withResult("win")
                .withApm(145)
                .build();

        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.of(dao));
        when(playerSummaryAnalysisService.analyzeGamePlayers(summaryData)).thenReturn(List.of(playerDto));

        // when
        final GameAnalysisResponse response = gameSummaryService.getGameAnalysis(gameId);

        // then
        assertNotNull(response);
        assertEquals(gameId, response.getGameId());
        assertEquals("rm_1v1", response.getLeaderboard());
        assertEquals("Gorge", response.getMapName());
        assertEquals(2188, response.getDurationSeconds());
        assertEquals(1, response.getPlayers().size());
        assertEquals("GSUGambit", response.getPlayers().get(0).getName());
        verify(gameSummaryDbRepository).findByGameId(gameId);
        verify(playerSummaryAnalysisService).analyzeGamePlayers(summaryData);
    }

    @Test
    public void testGetGameAnalysis_NotFound() {
        // given
        final String gameId = "999999";
        when(gameSummaryDbRepository.findByGameId(gameId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> gameSummaryService.getGameAnalysis(gameId));
        verify(gameSummaryDbRepository).findByGameId(gameId);
    }

    @Test
    public void testParseGameAnalysis_NullOrEmptySummaryData() {
        // when
        final GameAnalysisResponse response = gameSummaryService.parseGameAnalysis("123", null, "rm_solo");

        // then
        assertNotNull(response);
        assertEquals("123", response.getGameId());
        assertEquals("rm_solo", response.getLeaderboard());
        assertNotNull(response.getPlayers());
        assertTrue(response.getPlayers().isEmpty());
    }

    @Test
    public void testParseGameAnalysis_FallbackLeaderboard() throws Exception {
        // given
        final String json = "{\"map\":\"Dry Arabia\",\"duration\":1200}";
        final JsonNode summaryData = objectMapper.readTree(json);
        when(playerSummaryAnalysisService.analyzeGamePlayers(summaryData)).thenReturn(Collections.emptyList());

        // when
        final GameAnalysisResponse response = gameSummaryService.parseGameAnalysis("123", summaryData, "rm_fallback");

        // then
        assertNotNull(response);
        assertEquals("123", response.getGameId());
        assertEquals("rm_fallback", response.getLeaderboard());
        assertEquals("Dry Arabia", response.getMapName());
        assertEquals(1200, response.getDurationSeconds());
        assertTrue(response.getPlayers().isEmpty());
    }
}
