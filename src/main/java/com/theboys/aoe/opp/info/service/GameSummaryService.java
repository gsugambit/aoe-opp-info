package com.theboys.aoe.opp.info.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GameSummaryService {

    private static final int DEFAULT_LIMIT = 10;
    private static final String FINISHED_AT = "finishedAt";

    private final GameSummaryRepository gameSummaryRepository;
    private final GameSummaryDbRepository gameSummaryDbRepository;
    private final PlayerSummaryAnalysisService playerSummaryAnalysisService;
    private final ObjectMapper objectMapper;

    public String scrape(
        final String profileId,
        final String gamerName,
        final String gameId,
        final String leaderboard,
        final Integer page
    ) {
        final String rawJson = gameSummaryRepository.scrape(profileId, gamerName, gameId, leaderboard, page);

        try {
            final JsonNode summaryData = objectMapper.readTree(rawJson);
            persistGameSummary(gameId, profileId, gamerName, leaderboard, summaryData);
        } catch (final JsonProcessingException e) {
            LOGGER.error("Failed to parse game summary JSON for gameId: {}", gameId, e);
            throw new BusinessException("Failed to parse game summary JSON", e);
        }

        return rawJson;
    }

    public MatchupAnalysisResponse scrapeMatchup(final MatchupAnalysisRequest request) {
        if (request == null || request.getPlayers() == null || request.getPlayers().isEmpty()) {
            return MatchupAnalysisResponse.builder()
                .withResults(Collections.emptyList())
                .build();
        }

        final List<PlayerMatchupResultDto> results = new ArrayList<>();

        for (final MatchupPlayerDto player : request.getPlayers()) {
            final String profileId = player.getProfileId();
            final String gamerName = player.getGamerName();
            final String civilization = player.getCivilization();
            final String leaderboard = player.getLeaderboard();

            final List<String> gameIds = new ArrayList<>();
            final List<JsonNode> summaryNodes = new ArrayList<>();
            try {
                final Map<String, String> gameUrls = gameSummaryRepository.scrapeRecentGameUrls(
                    profileId, gamerName, civilization, leaderboard, DEFAULT_LIMIT
                );

                if (gameUrls != null) {
                    for (final Map.Entry<String, String> entry : gameUrls.entrySet()) {
                        final String gameId = entry.getKey();
                        final String signedUrl = entry.getValue();

                        try {
                            final Optional<GameSummaryDao> cachedDao = gameSummaryDbRepository.findByGameId(gameId);
                            final boolean isCachedAndFinished = cachedDao.isPresent()
                                && cachedDao.get().getSummaryData() != null
                                && cachedDao.get().getSummaryData().hasNonNull(FINISHED_AT);

                            if (isCachedAndFinished) {
                                LOGGER.debug("DB cache hit for finished gameId: {}", gameId);
                                gameIds.add(gameId);
                                summaryNodes.add(cachedDao.get().getSummaryData());
                            } else {
                                final Optional<String> summaryJsonOpt = gameSummaryRepository.fetchGameSummaryJson(signedUrl);
                                if (summaryJsonOpt.isPresent()) {
                                    final JsonNode summaryData = objectMapper.readTree(summaryJsonOpt.get());
                                    persistGameSummary(gameId, profileId, gamerName, leaderboard, summaryData);
                                    gameIds.add(gameId);
                                    summaryNodes.add(summaryData);
                                } else {
                                    LOGGER.warn("Skipping gameId: {} due to unsuccessful fetch", gameId);
                                }
                            }
                        } catch (final JsonProcessingException e) {
                            LOGGER.warn("Failed to parse JSON for gameId {}: {}", gameId, e.getMessage());
                        } catch (final Exception e) {
                            LOGGER.warn("Failed to process game summary for gameId {}: {}", gameId, e.getMessage());
                        }
                    }
                }
            } catch (final Exception e) {
                LOGGER.warn("Failed to scrape games for player {}: {}", profileId, e.getMessage());
            }

            final PlayerSummaryDto playerSummary = playerSummaryAnalysisService.analyzePlayerSummaries(
                profileId,
                gamerName,
                summaryNodes
            );

            results.add(PlayerMatchupResultDto.builder()
                .withProfileId(profileId)
                .withGamerName(gamerName)
                .withCivilization(civilization)
                .withGameIds(gameIds)
                .withSummary(playerSummary)
                .build());
        }

        return MatchupAnalysisResponse.builder()
            .withResults(results)
            .build();
    }

    private GameSummaryDao persistGameSummary(
        final String gameId,
        final String profileId,
        final String gamerName,
        final String leaderboard,
        final JsonNode summaryData
    ) {
        final Optional<GameSummaryDao> existingDao = gameSummaryDbRepository.findByGameId(gameId);
        final GameSummaryDao dao = existingDao.orElseGet(GameSummaryDao::new);

        if (dao.getCreatedAt() == null) {
            dao.setGameId(gameId);
            dao.setCreatedAt(OffsetDateTime.now());
        }

        dao.setProfileId(profileId);
        dao.setGamerName(gamerName);
        dao.setLeaderboard(leaderboard);
        dao.setSummaryData(summaryData);
        dao.setUpdatedAt(OffsetDateTime.now());

        final GameSummaryDao saved = gameSummaryDbRepository.save(dao);
        LOGGER.info("Persisted game summary for gameId: {}", gameId);
        return saved;
    }

    public String getGameSummary(final String gameId) {
        return gameSummaryDbRepository.findByGameId(gameId)
            .map(dao -> dao.getSummaryData() != null ? dao.getSummaryData().toString() : "{}")
            .orElseThrow(() -> new NotFoundException("Game summary not found for gameId: " + gameId));
    }

    public GameAnalysisResponse getGameAnalysis(final String gameId) {
        final GameSummaryDao dao = gameSummaryDbRepository.findByGameId(gameId)
            .orElseThrow(() -> new NotFoundException("Game summary not found for gameId: " + gameId));

        return parseGameAnalysis(dao.getGameId(), dao.getSummaryData(), dao.getLeaderboard());
    }

    public GameAnalysisResponse parseGameAnalysis(final String gameId, final JsonNode summaryData) {
        return parseGameAnalysis(gameId, summaryData, null);
    }

    public GameAnalysisResponse parseGameAnalysis(
        final String gameId,
        final JsonNode summaryData,
        final String fallbackLeaderboard
    ) {
        if (summaryData == null || summaryData.isNull() || summaryData.isMissingNode()) {
            return GameAnalysisResponse.builder()
                .withGameId(gameId)
                .withLeaderboard(fallbackLeaderboard)
                .withPlayers(Collections.emptyList())
                .build();
        }

        final String leaderboard = extractLeaderboard(summaryData, fallbackLeaderboard);
        final String mapName = extractMapName(summaryData);
        final Integer durationSeconds = extractDuration(summaryData);
        final List<PlayerGameAnalysisDto> players = playerSummaryAnalysisService.analyzeGamePlayers(summaryData);

        return GameAnalysisResponse.builder()
            .withGameId(gameId)
            .withLeaderboard(leaderboard)
            .withMapName(mapName)
            .withDurationSeconds(durationSeconds)
            .withPlayers(players)
            .build();
    }

    private String extractLeaderboard(final JsonNode summaryData, final String fallbackLeaderboard) {
        if (summaryData.hasNonNull("leaderboard") && !summaryData.get("leaderboard").asText().trim().isEmpty()) {
            return summaryData.get("leaderboard").asText().trim();
        }
        if (summaryData.path("game").hasNonNull("leaderboard")
            && !summaryData.path("game").get("leaderboard").asText().trim().isEmpty()) {
            return summaryData.path("game").get("leaderboard").asText().trim();
        }
        if (summaryData.hasNonNull("leaderboardId") && !summaryData.get("leaderboardId").asText().trim().isEmpty()) {
            return summaryData.get("leaderboardId").asText().trim();
        }
        if (summaryData.path("game").hasNonNull("leaderboardId")
            && !summaryData.path("game").get("leaderboardId").asText().trim().isEmpty()) {
            return summaryData.path("game").get("leaderboardId").asText().trim();
        }
        if (fallbackLeaderboard != null && !fallbackLeaderboard.trim().isEmpty()) {
            return fallbackLeaderboard.trim();
        }
        return null;
    }

    private String extractMapName(final JsonNode summaryData) {
        if (summaryData.hasNonNull("mapName") && !summaryData.get("mapName").asText().trim().isEmpty()) {
            return summaryData.get("mapName").asText().trim();
        }
        if (summaryData.hasNonNull("map") && summaryData.get("map").isTextual() && !summaryData.get("map").asText().trim().isEmpty()) {
            return summaryData.get("map").asText().trim();
        }
        if (summaryData.path("map").hasNonNull("name") && !summaryData.path("map").get("name").asText().trim().isEmpty()) {
            return summaryData.path("map").get("name").asText().trim();
        }
        if (summaryData.path("game").hasNonNull("mapName") && !summaryData.path("game").get("mapName").asText().trim().isEmpty()) {
            return summaryData.path("game").get("mapName").asText().trim();
        }
        if (summaryData.path("game").hasNonNull("map") && summaryData.path("game").get("map").isTextual() && !summaryData.path("game")
            .get("map").asText().trim().isEmpty()) {
            return summaryData.path("game").get("map").asText().trim();
        }
        if (summaryData.path("game").path("map").hasNonNull("name") && !summaryData.path("game").path("map").get("name").asText().trim()
            .isEmpty()) {
            return summaryData.path("game").path("map").get("name").asText().trim();
        }
        return null;
    }

    private Integer extractDuration(final JsonNode summaryData) {
        if (summaryData.hasNonNull("duration")) {
            return summaryData.get("duration").asInt();
        }
        if (summaryData.path("game").hasNonNull("duration")) {
            return summaryData.path("game").get("duration").asInt();
        }
        return 0;
    }
}
