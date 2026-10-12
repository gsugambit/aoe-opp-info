package com.theboys.aoe.opp.info.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    public TimelineMetricsDto getTimelineMetrics(final String gameId) {
        final GameSummaryDao dao = gameSummaryDbRepository.findByGameId(gameId)
            .orElseThrow(() -> new NotFoundException("Game summary not found for gameId: " + gameId));

        return parseTimelineMetrics(dao.getGameId(), dao.getSummaryData(), dao.getLeaderboard());
    }

    public TimelineMetricsDto parseTimelineMetrics(final String gameId, final JsonNode summaryData) {
        return parseTimelineMetrics(gameId, summaryData, null);
    }

    public TimelineMetricsDto parseTimelineMetrics(
        final String gameId,
        final JsonNode summaryData,
        final String fallbackLeaderboard
    ) {
        if (summaryData == null || summaryData.isNull() || summaryData.isMissingNode()) {
            return TimelineMetricsDto.builder()
                .withGameId(gameId)
                .withLeaderboard(fallbackLeaderboard)
                .withMapName(null)
                .withDurationSeconds(0)
                .withPlayers(Collections.emptyList())
                .build();
        }

        final String leaderboard = extractLeaderboard(summaryData, fallbackLeaderboard);
        final String mapName = extractMapName(summaryData);
        final Integer durationSeconds = extractDuration(summaryData);
        final List<PlayerTimelineDto> players = sortPlayersByOutcome(extractPlayersTimeline(summaryData));

        return TimelineMetricsDto.builder()
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

    private List<PlayerTimelineDto> sortPlayersByOutcome(final List<PlayerTimelineDto> players) {
        if (players == null || players.isEmpty()) {
            return Collections.emptyList();
        }
        final List<PlayerTimelineDto> sorted = new ArrayList<>(players);
        sorted.sort((p1, p2) -> {
            final int rank1 = getOutcomeRank(p1 != null ? p1.getResult() : null);
            final int rank2 = getOutcomeRank(p2 != null ? p2.getResult() : null);
            return Integer.compare(rank1, rank2);
        });
        return sorted;
    }

    private int getOutcomeRank(final String result) {
        if (result == null) {
            return 2;
        }
        final String lower = result.trim().toLowerCase();
        if ("win".equals(lower) || "won".equals(lower) || "victory".equals(lower)) {
            return 0;
        }
        if ("loss".equals(lower) || "lost".equals(lower) || "defeat".equals(lower)) {
            return 1;
        }
        return 2;
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

    private List<PlayerTimelineDto> extractPlayersTimeline(final JsonNode summaryData) {
        final List<JsonNode> playerNodes = new ArrayList<>();

        final JsonNode teamsNode = summaryData.path("teams");
        if (teamsNode.isArray()) {
            for (final JsonNode teamElement : teamsNode) {
                if (teamElement.isArray()) {
                    for (final JsonNode playerElement : teamElement) {
                        playerNodes.add(playerElement);
                    }
                } else if (teamElement.isObject()) {
                    final JsonNode nestedPlayers = teamElement.path("players");
                    if (nestedPlayers.isArray()) {
                        for (final JsonNode playerElement : nestedPlayers) {
                            playerNodes.add(playerElement);
                        }
                    } else {
                        playerNodes.add(teamElement);
                    }
                }
            }
        }

        final JsonNode playersNode = summaryData.path("players");
        if (playersNode.isArray() && playerNodes.isEmpty()) {
            for (final JsonNode playerElement : playersNode) {
                playerNodes.add(playerElement);
            }
        }

        final List<PlayerTimelineDto> playerTimelineDtos = new ArrayList<>();
        for (final JsonNode node : playerNodes) {
            playerTimelineDtos.add(mapToPlayerTimelineDto(node));
        }

        return playerTimelineDtos;
    }

    private PlayerTimelineDto mapToPlayerTimelineDto(final JsonNode node) {
        final JsonNode playerObj = node.has("player") ? node.path("player") : node;

        final String profileId = getFirstText(playerObj, "profileId", "profile_id", "id");
        final String name = getFirstText(playerObj, "name", "gamerName", "username");
        final String civilization = getFirstText(playerObj, "civilization", "civ");
        final String result = getFirstText(playerObj, "result", "outcome");
        final Integer apm = getFirstInteger(node, playerObj, "apm", "avgApm", "actionsPerMinute");

        final List<TimelineDataPointDto> dataPoints = extractDataPoints(node, playerObj);

        return PlayerTimelineDto.builder()
            .withProfileId(profileId)
            .withName(name)
            .withCivilization(civilization)
            .withResult(result)
            .withApm(apm)
            .withDataPoints(dataPoints)
            .build();
    }

    private List<TimelineDataPointDto> extractDataPoints(final JsonNode outerNode, final JsonNode innerNode) {
        final JsonNode timestampsNode = findFirstArray(
            innerNode.path("resources").path("timestamps"),
            outerNode.path("resources").path("timestamps")
        );

        if (timestampsNode != null && timestampsNode.isArray() && !timestampsNode.isEmpty()) {
            final JsonNode buildOrderNode = findFirstArray(
                innerNode.path("buildOrder"),
                outerNode.path("buildOrder")
            );
            return calculateDataPointsFromBuildOrder(timestampsNode, buildOrderNode);
        }

        final JsonNode intervalsNode = findFirstArray(
            outerNode.path("intervals"),
            outerNode.path("timeline"),
            outerNode.path("states"),
            outerNode.path("series"),
            innerNode.path("intervals"),
            innerNode.path("timeline"),
            innerNode.path("states"),
            innerNode.path("series")
        );

        if (intervalsNode == null || !intervalsNode.isArray()) {
            return Collections.emptyList();
        }

        return parseLegacyIntervals(intervalsNode);
    }

    private List<TimelineDataPointDto> calculateDataPointsFromBuildOrder(
        final JsonNode timestampsNode,
        final JsonNode buildOrderNode
    ) {
        final List<Integer> villagerFinished = new ArrayList<>();
        final List<Integer> villagerDestroyed = new ArrayList<>();
        final List<Integer> militaryFinished = new ArrayList<>();
        final List<Integer> militaryDestroyed = new ArrayList<>();

        if (buildOrderNode != null && buildOrderNode.isArray()) {
            for (final JsonNode event : buildOrderNode) {
                final String type = event.path("type").asText("");
                final String icon = event.path("icon").asText("").toLowerCase();

                if ("Unit".equalsIgnoreCase(type)) {
                    final boolean isVillager = isVillagerUnit(icon);
                    final List<Integer> finishedTarget = isVillager ? villagerFinished : militaryFinished;
                    final List<Integer> destroyedTarget = isVillager ? villagerDestroyed : militaryDestroyed;

                    appendTimestamps(event.path("finished"), finishedTarget);
                    appendTimestamps(event.path("destroyed"), destroyedTarget);
                }
            }
        }

        Collections.sort(villagerFinished);
        Collections.sort(villagerDestroyed);
        Collections.sort(militaryFinished);
        Collections.sort(militaryDestroyed);

        final List<TimelineDataPointDto> points = new ArrayList<>();
        for (final JsonNode timeNode : timestampsNode) {
            final int timestamp = timeNode.asInt();
            final int villagerCount = Math.max(0,
                countLessOrEqual(villagerFinished, timestamp) - countLessOrEqual(villagerDestroyed, timestamp));
            final int militaryCount = Math.max(0,
                countLessOrEqual(militaryFinished, timestamp) - countLessOrEqual(militaryDestroyed, timestamp));
            final int totalPopulation = villagerCount + militaryCount;

            points.add(TimelineDataPointDto.builder()
                .withTimestamp(timestamp)
                .withVillagerCount(villagerCount)
                .withMilitaryCount(militaryCount)
                .withTotalPopulation(totalPopulation)
                .build());
        }

        return points;
    }

    private boolean isVillagerUnit(final String icon) {
        return icon.contains("villager") || icon.contains("peasant") || icon.contains("worker");
    }

    private void appendTimestamps(final JsonNode arrayNode, final List<Integer> targetList) {
        if (arrayNode != null && arrayNode.isArray()) {
            for (final JsonNode item : arrayNode) {
                if (item.isNumber()) {
                    targetList.add(item.asInt());
                }
            }
        }
    }

    private int countLessOrEqual(final List<Integer> sortedList, final int value) {
        if (sortedList.isEmpty()) {
            return 0;
        }
        int low = 0;
        int high = sortedList.size() - 1;
        int result = 0;
        while (low <= high) {
            final int mid = (low + high) >>> 1;
            if (sortedList.get(mid) <= value) {
                result = mid + 1;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return result;
    }

    private List<TimelineDataPointDto> parseLegacyIntervals(final JsonNode intervalsNode) {
        final List<TimelineDataPointDto> points = new ArrayList<>();
        for (final JsonNode point : intervalsNode) {
            final Integer timestamp = getFirstInt(point, 0, "timestamp", "time", "interval", "t");
            final Integer villagerCount = getFirstInt(
                point,
                0,
                "villagers",
                "villagerCount",
                "vills",
                "workers",
                "workerCount",
                "economy"
            );
            final Integer militaryCount = getFirstInt(
                point,
                0,
                "military",
                "militaryCount",
                "army",
                "soldiers",
                "militaryUnits"
            );
            final Integer totalPopulation = point.hasNonNull("population") ? point.get("population").asInt()
                : point.hasNonNull("totalPopulation") ? point.get("totalPopulation").asInt()
                    : point.hasNonNull("pop") ? point.get("pop").asInt()
                        : (villagerCount + militaryCount);

            points.add(TimelineDataPointDto.builder()
                .withTimestamp(timestamp)
                .withVillagerCount(villagerCount)
                .withMilitaryCount(militaryCount)
                .withTotalPopulation(totalPopulation)
                .build());
        }

        return points;
    }

    private String getFirstText(final JsonNode node, final String... fieldNames) {
        for (final String field : fieldNames) {
            if (node.hasNonNull(field)) {
                return node.get(field).asText();
            }
        }
        return null;
    }

    private Integer getFirstInt(final JsonNode node, final int defaultValue, final String... fieldNames) {
        for (final String field : fieldNames) {
            if (node.hasNonNull(field)) {
                return node.get(field).asInt();
            }
        }
        return defaultValue;
    }

    private Integer getFirstInteger(final JsonNode primary, final JsonNode fallback, final String... fieldNames) {
        for (final String field : fieldNames) {
            if (primary != null && primary.hasNonNull(field)) {
                return primary.get(field).asInt();
            }
            if (fallback != null && fallback.hasNonNull(field)) {
                return fallback.get(field).asInt();
            }
        }
        return null;
    }

    private JsonNode findFirstArray(final JsonNode... candidates) {
        for (final JsonNode candidate : candidates) {
            if (candidate != null && candidate.isArray() && !candidate.isEmpty()) {
                return candidate;
            }
        }
        return null;
    }
}
