package com.theboys.aoe.opp.info.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.theboys.aoe.opp.info.dto.*;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
public class PlayerSummaryAnalysisService {

    private static final String TOWN_CENTER_KEYWORD = "town_center";
    private static final String VILLAGER_KEYWORD = "villager";
    private static final String TRADE_CART_KEYWORD = "trade_cart";
    private static final String UNIT_TYPE = "Unit";
    private static final List<String> IGNORED_LANDMARK_KEYWORDS = List.of(
            "fortress",
            "capital town center",
            "town center",
            "town_center"
    );

    private static final Set<String> NON_MILITARY_BLACKLIST = Set.of(
            "villager", "scout", "monk", "buddhist_monk", "mounted_villager",
            "mehter", "dervish", "unit_khan_1_mon", "shinto_priest", "general_ship",
            "jeanne_villager", "jeanne_archer", "horseman_grassland", "shaolin_monk",
            "jeanne_ranged_monarch", "imperial_official", "jeanne_manatarms", "transport",
            "trade_ship", "trade_cart", "fishing_boat", "camel_trader", "yatai",
            "cattle", "reindeer_trader", "emissary", "fortress", "treasure_caravan",
            "jeanne_melee_monarch", "jeanne_melee_knight", "pilgrim", "gilded_villager"
    );

    private static final List<String> UNIT_SUFFIXES = List.of(
            "_age_1", "_age_2", "_age_3", "_age_4",
            "_age1", "_age2", "_age3", "_age4",
            "_1", "_2", "_3", "_4"
    );

    public PlayerSummaryDto analyzePlayerSummaries(
            final String profileId,
            final String gamerName,
            final List<JsonNode> gameSummaries
    ) {
        if (gameSummaries == null || gameSummaries.isEmpty()) {
            return PlayerSummaryDto.builder()
                    .withGamesAnalyzed(0)
                    .withMilitaryProduction(Collections.emptyList())
                    .withLandmarks(Collections.emptyList())
                    .build();
        }

        final List<JsonNode> playerNodes = new ArrayList<>();
        for (final JsonNode gameSummary : gameSummaries) {
            final JsonNode playerNode = extractPlayerNode(gameSummary, profileId, gamerName);
            if (playerNode != null) {
                playerNodes.add(playerNode);
            }
        }

        if (playerNodes.isEmpty()) {
            return PlayerSummaryDto.builder()
                    .withGamesAnalyzed(0)
                    .withMilitaryProduction(Collections.emptyList())
                    .withLandmarks(Collections.emptyList())
                    .build();
        }

        final int totalGames = playerNodes.size();

        final AgeUpSummaryDto ageUpSummary = calculateAgeUpSummary(playerNodes);
        final VillagerSummaryDto villagerSummary = calculateVillagerSummary(playerNodes, totalGames);
        final TraderSummaryDto traderSummary = calculateTraderSummary(playerNodes, totalGames);
        final List<MilitaryUnitSummaryDto> militarySummary = calculateMilitarySummary(playerNodes, totalGames);
        final TownCenterSummaryDto townCenterSummary = calculateTownCenterSummary(playerNodes, totalGames);
        final List<LandmarkSummaryDto> landmarkSummaries = calculateLandmarkSummaries(playerNodes, totalGames);
        final Double averageApm = calculateAverageApm(playerNodes, totalGames);

        return PlayerSummaryDto.builder()
                .withGamesAnalyzed(totalGames)
                .withAverageApm(averageApm)
                .withAgeUpTimings(ageUpSummary)
                .withVillagerProduction(villagerSummary)
                .withTraderProduction(traderSummary)
                .withMilitaryProduction(militarySummary)
                .withAdditionalTownCenters(townCenterSummary)
                .withLandmarks(landmarkSummaries)
                .build();
    }

    private JsonNode extractPlayerNode(
            final JsonNode gameSummary,
            final String targetProfileId,
            final String targetGamerName
    ) {
        if (gameSummary == null || !gameSummary.has("players") || !gameSummary.get("players").isArray()) {
            return null;
        }

        for (final JsonNode player : gameSummary.get("players")) {
            final String profileId = player.hasNonNull("profileId")
                    ? player.get("profileId").asText()
                    : (player.hasNonNull("profile_id") ? player.get("profile_id").asText() : null);

            final String name = player.hasNonNull("name") ? player.get("name").asText() : null;

            final boolean profileIdMatches = StringUtils.isNotBlank(targetProfileId)
                    && StringUtils.isNotBlank(profileId)
                    && targetProfileId.equalsIgnoreCase(profileId);

            final boolean nameMatches = StringUtils.isNotBlank(targetGamerName)
                    && StringUtils.isNotBlank(name)
                    && targetGamerName.equalsIgnoreCase(name);

            if (profileIdMatches || nameMatches) {
                return player;
            }
        }

        return null;
    }

    private AgeUpSummaryDto calculateAgeUpSummary(final List<JsonNode> playerNodes) {
        double feudalSum = 0.0;
        int feudalCount = 0;
        double castleSum = 0.0;
        int castleCount = 0;
        double imperialSum = 0.0;
        int imperialCount = 0;

        for (final JsonNode player : playerNodes) {
            final JsonNode actions = player.get("actions");
            if (actions == null || actions.isNull()) {
                continue;
            }

            final Integer feudalAge = getFirstIntFromArray(actions.get("feudalAge"));
            if (feudalAge != null) {
                feudalSum += feudalAge;
                feudalCount++;
            }

            final Integer castleAge = getFirstIntFromArray(actions.get("castleAge"));
            if (castleAge != null) {
                castleSum += castleAge;
                castleCount++;
            }

            final Integer imperialAge = getFirstIntFromArray(actions.get("imperialAge"));
            if (imperialAge != null) {
                imperialSum += imperialAge;
                imperialCount++;
            }
        }

        return AgeUpSummaryDto.builder()
                .withAverageFeudalAgeSeconds(feudalCount > 0 ? round(feudalSum / feudalCount) : null)
                .withAverageCastleAgeSeconds(castleCount > 0 ? round(castleSum / castleCount) : null)
                .withAverageImperialAgeSeconds(imperialCount > 0 ? round(imperialSum / imperialCount) : null)
                .build();
    }

    private VillagerSummaryDto calculateVillagerSummary(final List<JsonNode> playerNodes, final int totalGames) {
        double totalDark = 0.0;
        double totalFeudal = 0.0;
        double totalCastle = 0.0;
        double totalImperial = 0.0;

        for (final JsonNode player : playerNodes) {
            final GameAgeBoundaries boundaries = extractAgeBoundaries(player);
            final JsonNode buildOrder = player.get("buildOrder");
            if (buildOrder == null || !buildOrder.isArray()) {
                continue;
            }

            for (final JsonNode item : buildOrder) {
                final String type = item.hasNonNull("type") ? item.get("type").asText() : "";
                final String icon = item.hasNonNull("icon") ? item.get("icon").asText().toLowerCase() : "";

                if (UNIT_TYPE.equalsIgnoreCase(type) && icon.contains(VILLAGER_KEYWORD)) {
                    final JsonNode finished = item.get("finished");
                    if (finished != null && finished.isArray()) {
                        for (final JsonNode timeNode : finished) {
                            final int time = timeNode.asInt();
                            final Age age = boundaries.determineAge(time);
                            switch (age) {
                                case DARK -> totalDark++;
                                case FEUDAL -> totalFeudal++;
                                case CASTLE -> totalCastle++;
                                case IMPERIAL -> totalImperial++;
                            }
                        }
                    }
                }
            }
        }

        final double avgDark = round(totalDark / totalGames);
        final double avgFeudal = round(totalFeudal / totalGames);
        final double avgCastle = round(totalCastle / totalGames);
        final double avgImperial = round(totalImperial / totalGames);
        final double avgTotal = round(avgDark + avgFeudal + avgCastle + avgImperial);

        return VillagerSummaryDto.builder()
                .withDarkAge(avgDark)
                .withFeudalAge(avgFeudal)
                .withCastleAge(avgCastle)
                .withImperialAge(avgImperial)
                .withTotal(avgTotal)
                .build();
    }

    private TraderSummaryDto calculateTraderSummary(final List<JsonNode> playerNodes, final int totalGames) {
        double totalDark = 0.0;
        double totalFeudal = 0.0;
        double totalCastle = 0.0;
        double totalImperial = 0.0;

        for (final JsonNode player : playerNodes) {
            final GameAgeBoundaries boundaries = extractAgeBoundaries(player);
            final JsonNode buildOrder = player.get("buildOrder");
            if (buildOrder == null || !buildOrder.isArray()) {
                continue;
            }

            for (final JsonNode item : buildOrder) {
                final String type = item.hasNonNull("type") ? item.get("type").asText() : "";
                final String icon = item.hasNonNull("icon") ? item.get("icon").asText().toLowerCase() : "";

                if (UNIT_TYPE.equalsIgnoreCase(type) && icon.contains(TRADE_CART_KEYWORD)) {
                    final JsonNode unknown = item.get("unknown");
                    if (unknown != null) {
                        if (unknown.isObject()) {
                            for (final JsonNode timeArrayNode : unknown) {
                                if (timeArrayNode != null && timeArrayNode.isArray()) {
                                    for (final JsonNode timeNode : timeArrayNode) {
                                        final int time = timeNode.asInt();
                                        final Age age = boundaries.determineAge(time);
                                        switch (age) {
                                            case DARK -> totalDark++;
                                            case FEUDAL -> totalFeudal++;
                                            case CASTLE -> totalCastle++;
                                            case IMPERIAL -> totalImperial++;
                                        }
                                    }
                                }
                            }
                        } else if (unknown.isArray()) {
                            for (final JsonNode timeNode : unknown) {
                                final int time = timeNode.asInt();
                                final Age age = boundaries.determineAge(time);
                                switch (age) {
                                    case DARK -> totalDark++;
                                    case FEUDAL -> totalFeudal++;
                                    case CASTLE -> totalCastle++;
                                    case IMPERIAL -> totalImperial++;
                                }
                            }
                        }
                    }
                }
            }
        }

        final double avgDark = round(totalDark / totalGames);
        final double avgFeudal = round(totalFeudal / totalGames);
        final double avgCastle = round(totalCastle / totalGames);
        final double avgImperial = round(totalImperial / totalGames);
        final double avgTotal = round(avgDark + avgFeudal + avgCastle + avgImperial);

        return TraderSummaryDto.builder()
                .withDarkAge(avgDark)
                .withFeudalAge(avgFeudal)
                .withCastleAge(avgCastle)
                .withImperialAge(avgImperial)
                .withTotal(avgTotal)
                .build();
    }

    private List<MilitaryUnitSummaryDto> calculateMilitarySummary(
            final List<JsonNode> playerNodes,
            final int totalGames
    ) {
        final Map<String, double[]> unitCounts = new LinkedHashMap<>();

        for (final JsonNode player : playerNodes) {
            final GameAgeBoundaries boundaries = extractAgeBoundaries(player);
            final JsonNode buildOrder = player.get("buildOrder");
            if (buildOrder == null || !buildOrder.isArray()) {
                continue;
            }

            for (final JsonNode item : buildOrder) {
                final String type = item.hasNonNull("type") ? item.get("type").asText() : "";
                final String icon = item.hasNonNull("icon") ? item.get("icon").asText() : "";

                if (!UNIT_TYPE.equalsIgnoreCase(type)) {
                    continue;
                }

                final String matchedUnitType = extractMilitaryUnitName(icon);
                if (matchedUnitType == null) {
                    continue;
                }

                final JsonNode finished = item.get("finished");
                if (finished != null && finished.isArray()) {
                    final double[] ageCounts = unitCounts.computeIfAbsent(matchedUnitType, k -> new double[4]);
                    for (final JsonNode timeNode : finished) {
                        final int time = timeNode.asInt();
                        final Age age = boundaries.determineAge(time);
                        switch (age) {
                            case DARK -> ageCounts[0]++;
                            case FEUDAL -> ageCounts[1]++;
                            case CASTLE -> ageCounts[2]++;
                            case IMPERIAL -> ageCounts[3]++;
                        }
                    }
                }
            }
        }

        double totalAllMilitary = 0.0;
        for (final double[] ageCounts : unitCounts.values()) {
            for (final double count : ageCounts) {
                totalAllMilitary += count;
            }
        }

        final List<MilitaryUnitSummaryDto> results = new ArrayList<>();
        for (final Map.Entry<String, double[]> entry : unitCounts.entrySet()) {
            final String unitType = entry.getKey();
            final double[] counts = entry.getValue();
            final double totalUnitCount = counts[0] + counts[1] + counts[2] + counts[3];
            if (totalUnitCount <= 0.0) {
                continue;
            }

            final double darkAvg = round(counts[0] / totalGames);
            final double feudalAvg = round(counts[1] / totalGames);
            final double castleAvg = round(counts[2] / totalGames);
            final double imperialAvg = round(counts[3] / totalGames);
            final double totalAvg = round(totalUnitCount / totalGames);
            final double percentage = totalAllMilitary > 0.0
                    ? round((totalUnitCount / totalAllMilitary) * 100.0)
                    : 0.0;

            results.add(MilitaryUnitSummaryDto.builder()
                    .withUnitType(unitType)
                    .withAverageCount(totalAvg)
                    .withPercentageOfTotal(percentage)
                    .withDarkAge(darkAvg)
                    .withFeudalAge(feudalAvg)
                    .withCastleAge(castleAvg)
                    .withImperialAge(imperialAvg)
                    .build());
        }

        results.sort(Comparator
                .comparing((MilitaryUnitSummaryDto m) -> m.getPercentageOfTotal() == null ? 0.0 : -m.getPercentageOfTotal())
                .thenComparing((MilitaryUnitSummaryDto m) -> m.getAverageCount() == null ? 0.0 : -m.getAverageCount())
                .thenComparing((MilitaryUnitSummaryDto m) -> m.getUnitType() == null ? "" : m.getUnitType()));

        return results;
    }

    private TownCenterSummaryDto calculateTownCenterSummary(final List<JsonNode> playerNodes, final int totalGames) {
        double totalDark = 0.0;
        double totalFeudal = 0.0;
        double totalCastle = 0.0;
        double totalImperial = 0.0;

        for (final JsonNode player : playerNodes) {
            final GameAgeBoundaries boundaries = extractAgeBoundaries(player);
            final JsonNode buildOrder = player.get("buildOrder");
            if (buildOrder == null || !buildOrder.isArray()) {
                continue;
            }

            for (final JsonNode item : buildOrder) {
                final String icon = item.hasNonNull("icon") ? item.get("icon").asText().toLowerCase() : "";

                if (icon.contains(TOWN_CENTER_KEYWORD)) {
                    final List<Integer> timestamps = new ArrayList<>();

                    final JsonNode constructed = item.get("constructed");
                    if (constructed != null && constructed.isArray()) {
                        for (final JsonNode timeNode : constructed) {
                            final int time = timeNode.asInt();
                            if (time > 0) {
                                timestamps.add(time);
                            }
                        }
                    }

                    final JsonNode finished = item.get("finished");
                    if (finished != null && finished.isArray()) {
                        for (final JsonNode timeNode : finished) {
                            final int time = timeNode.asInt();
                            if (time > 0) {
                                timestamps.add(time);
                            }
                        }
                    }

                    for (final int time : timestamps) {
                        final Age age = boundaries.determineAge(time);
                        switch (age) {
                            case DARK -> totalDark++;
                            case FEUDAL -> totalFeudal++;
                            case CASTLE -> totalCastle++;
                            case IMPERIAL -> totalImperial++;
                        }
                    }
                }
            }
        }

        final double avgDark = round(totalDark / totalGames);
        final double avgFeudal = round(totalFeudal / totalGames);
        final double avgCastle = round(totalCastle / totalGames);
        final double avgImperial = round(totalImperial / totalGames);
        final double avgTotal = round(avgDark + avgFeudal + avgCastle + avgImperial);

        return TownCenterSummaryDto.builder()
                .withDarkAge(avgDark)
                .withFeudalAge(avgFeudal)
                .withCastleAge(avgCastle)
                .withImperialAge(avgImperial)
                .withTotal(avgTotal)
                .build();
    }

    private List<LandmarkSummaryDto> calculateLandmarkSummaries(
            final List<JsonNode> playerNodes,
            final int totalGames
    ) {
        final Map<Integer, Integer> gamesPerAge = new HashMap<>();
        for (int age = 2; age <= 4; age++) {
            int count = 0;
            for (final JsonNode player : playerNodes) {
                if (playerReachedAge(player, age)) {
                    count++;
                }
            }
            gamesPerAge.put(age, count);
        }

        final Map<String, LandmarkAggregator> aggregators = new LinkedHashMap<>();

        for (final JsonNode player : playerNodes) {
            final JsonNode analysis = player.get("analysis");
            if (analysis == null || !analysis.has("landmarks") || !analysis.get("landmarks").isArray()) {
                continue;
            }

            final Map<String, JsonNode> earliestLandmarksInGame = new HashMap<>();
            for (final JsonNode lmNode : analysis.get("landmarks")) {
                if (!lmNode.hasNonNull("newAge")) {
                    continue;
                }

                final String name = lmNode.hasNonNull("name") ? lmNode.get("name").asText() : "";
                if (isIgnoredLandmark(name)) {
                    continue;
                }

                final int gameTime = lmNode.hasNonNull("gameTime") ? lmNode.get("gameTime").asInt() : 0;
                if (!earliestLandmarksInGame.containsKey(name)
                        || gameTime < earliestLandmarksInGame.get(name).get("gameTime").asInt()) {
                    earliestLandmarksInGame.put(name, lmNode);
                }
            }

            for (final Map.Entry<String, JsonNode> entry : earliestLandmarksInGame.entrySet()) {
                final String name = entry.getKey();
                final JsonNode lmNode = entry.getValue();
                final String icon = lmNode.hasNonNull("icon") ? lmNode.get("icon").asText() : null;
                final int gameTime = lmNode.hasNonNull("gameTime") ? lmNode.get("gameTime").asInt() : 0;
                final Integer newAge = extractLandmarkTargetAge(lmNode);

                final LandmarkAggregator agg = aggregators.computeIfAbsent(
                        name,
                        k -> new LandmarkAggregator(name, icon, newAge)
                );
                agg.recordBuild(gameTime);
                if (agg.getIcon() == null && icon != null) {
                    agg.setIcon(icon);
                }
                if (agg.getTargetAge() == null && newAge != null) {
                    agg.setTargetAge(newAge);
                }
            }
        }

        final List<LandmarkSummaryDto> results = new ArrayList<>();
        for (final LandmarkAggregator agg : aggregators.values()) {
            final Integer targetAge = agg.getTargetAge();
            final boolean hasAgeGames = targetAge != null
                    && gamesPerAge.containsKey(targetAge)
                    && gamesPerAge.get(targetAge) > 0;
            final int denominator = hasAgeGames ? gamesPerAge.get(targetAge) : totalGames;

            final double pickRate = round(((double) agg.getTimesBuilt() / denominator) * 100.0);
            final double avgGameTime = round((double) agg.getTotalGameTime() / agg.getTimesBuilt());

            results.add(LandmarkSummaryDto.builder()
                    .withLandmarkName(agg.getName())
                    .withIcon(agg.getIcon())
                    .withPickRatePercentage(pickRate)
                    .withAverageGameTimeSeconds(avgGameTime)
                    .withTargetAge(agg.getTargetAge())
                    .withTimesBuilt(agg.getTimesBuilt())
                    .build());
        }

        results.sort(Comparator
                .comparing((LandmarkSummaryDto lm) -> lm.getTargetAge() == null ? 99 : lm.getTargetAge())
                .thenComparing((LandmarkSummaryDto lm) -> lm.getPickRatePercentage() == null ? 0.0 : -lm.getPickRatePercentage())
                .thenComparing((LandmarkSummaryDto lm) -> lm.getLandmarkName() == null ? "" : lm.getLandmarkName()));

        return results;
    }

    private Double calculateAverageApm(final List<JsonNode> playerNodes, final int totalGames) {
        double totalApm = 0.0;
        int apmCount = 0;

        for (final JsonNode player : playerNodes) {
            if (player.hasNonNull("apm")) {
                totalApm += player.get("apm").asDouble();
                apmCount++;
            }
        }

        if (apmCount == 0) {
            return null;
        }

        return round(totalApm / totalGames);
    }

    String extractMilitaryUnitName(final String icon) {
        if (StringUtils.isBlank(icon)) {
            return null;
        }

        final int lastSlash = icon.lastIndexOf('/');
        String unitName = (lastSlash >= 0) ? icon.substring(lastSlash + 1) : icon;
        unitName = unitName.trim().toLowerCase();

        for (final String suffix : UNIT_SUFFIXES) {
            if (unitName.endsWith(suffix)) {
                unitName = unitName.substring(0, unitName.length() - suffix.length());
                break;
            }
        }

        if (unitName.isEmpty() || NON_MILITARY_BLACKLIST.contains(unitName)) {
            return null;
        }

        return unitName;
    }

    private boolean isIgnoredLandmark(final String landmarkName) {
        if (StringUtils.isBlank(landmarkName)) {
            return true;
        }
        final String lowerName = landmarkName.toLowerCase();
        for (final String keyword : IGNORED_LANDMARK_KEYWORDS) {
            if (lowerName.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean playerReachedAge(final JsonNode player, final int targetAge) {
        if (targetAge <= 1) {
            return true;
        }

        final GameAgeBoundaries boundaries = extractAgeBoundaries(player);
        if (targetAge == 2) {
            final boolean reachedAge2 = boundaries.feudalAge() != null
                    || boundaries.castleAge() != null
                    || boundaries.imperialAge() != null;
            if (reachedAge2) {
                return true;
            }
        } else if (targetAge == 3) {
            final boolean reachedAge3 = boundaries.castleAge() != null
                    || boundaries.imperialAge() != null;
            if (reachedAge3) {
                return true;
            }
        } else if (targetAge == 4) {
            final boolean reachedAge4 = boundaries.imperialAge() != null;
            if (reachedAge4) {
                return true;
            }
        }

        final JsonNode analysis = player.get("analysis");
        if (analysis != null && analysis.has("landmarks") && analysis.get("landmarks").isArray()) {
            for (final JsonNode lmNode : analysis.get("landmarks")) {
                if (!lmNode.hasNonNull("newAge")) {
                    continue;
                }
                final String name = lmNode.hasNonNull("name") ? lmNode.get("name").asText() : "";
                if (isIgnoredLandmark(name)) {
                    continue;
                }
                final Integer landmarkAge = extractLandmarkTargetAge(lmNode);
                if (landmarkAge != null && landmarkAge >= targetAge) {
                    return true;
                }
            }
        }

        return false;
    }

    private Integer extractLandmarkTargetAge(final JsonNode lmNode) {
        if (lmNode == null || !lmNode.hasNonNull("newAge")) {
            return null;
        }
        return lmNode.get("newAge").asInt();
    }

    private GameAgeBoundaries extractAgeBoundaries(final JsonNode player) {
        final JsonNode actions = player.get("actions");
        if (actions == null || actions.isNull()) {
            return new GameAgeBoundaries(null, null, null);
        }

        final Integer feudalAge = getFirstIntFromArray(actions.get("feudalAge"));
        final Integer castleAge = getFirstIntFromArray(actions.get("castleAge"));
        final Integer imperialAge = getFirstIntFromArray(actions.get("imperialAge"));

        return new GameAgeBoundaries(feudalAge, castleAge, imperialAge);
    }

    private Integer getFirstIntFromArray(final JsonNode node) {
        if (node != null && node.isArray() && !node.isEmpty()) {
            return node.get(0).asInt();
        }
        return null;
    }

    private Double round(final double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private enum Age {
        DARK,
        FEUDAL,
        CASTLE,
        IMPERIAL
    }

    private record GameAgeBoundaries(Integer feudalAge, Integer castleAge, Integer imperialAge) {

        Age determineAge(final int timestamp) {
            if (feudalAge != null && timestamp < feudalAge) {
                return Age.DARK;
            }
            if (feudalAge != null && (castleAge == null || timestamp < castleAge)) {
                return Age.FEUDAL;
            }
            if (castleAge != null && (imperialAge == null || timestamp < imperialAge)) {
                return Age.CASTLE;
            }
            if (imperialAge != null && timestamp >= imperialAge) {
                return Age.IMPERIAL;
            }
            return Age.DARK;
        }
    }

    private static class LandmarkAggregator {
        private final String name;
        private String icon;
        private Integer targetAge;
        private int timesBuilt;
        private long totalGameTime;

        LandmarkAggregator(final String name, final String icon, final Integer targetAge) {
            this.name = name;
            this.icon = icon;
            this.targetAge = targetAge;
            this.timesBuilt = 0;
            this.totalGameTime = 0;
        }

        void recordBuild(final int gameTime) {
            this.timesBuilt++;
            this.totalGameTime += gameTime;
        }

        String getName() {
            return name;
        }

        String getIcon() {
            return icon;
        }

        void setIcon(final String icon) {
            this.icon = icon;
        }

        Integer getTargetAge() {
            return targetAge;
        }

        void setTargetAge(final Integer targetAge) {
            this.targetAge = targetAge;
        }

        int getTimesBuilt() {
            return timesBuilt;
        }

        long getTotalGameTime() {
            return totalGameTime;
        }
    }
}
