package com.theboys.aoe.opp.info.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.theboys.aoe.opp.info.utils.ObjectUtils;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
@Builder
public class GamesPage {

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("page")
    private Integer page;

    @JsonProperty("per_page")
    private Integer perPage;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("filters")
    private Filters filters;

    @Builder.Default
    @JsonProperty("games")
    private List<Game> games = new ArrayList<>();

    @Override
    public String toString() {
        return ObjectUtils.toString(this);
    }

    @Data
    @EqualsAndHashCode
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
    @Builder
    public static class Filters {

        @JsonProperty("leaderboard")
        private String leaderboard;

        @JsonProperty("since")
        private Instant since;

        @Builder.Default
        @JsonProperty("profile_ids")
        private List<Long> profileIds = new ArrayList<>();

        @JsonProperty("opponent_profile_id")
        private Long opponentProfileId;

        @Builder.Default
        @JsonProperty("opponent_profile_ids")
        private List<Long> opponentProfileIds = new ArrayList<>();

        @Override
        public String toString() {
            return ObjectUtils.toString(this);
        }
    }

    @Data
    @EqualsAndHashCode
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
    @Builder
    public static class Game {

        @JsonProperty("game_id")
        private Long gameId;

        @JsonProperty("started_at")
        private Instant startedAt;

        @JsonProperty("updated_at")
        private Instant updatedAt;

        @JsonProperty("duration")
        private Long duration;

        @JsonProperty("map")
        private String map;

        @JsonProperty("kind")
        private String kind;

        @JsonProperty("leaderboard")
        private String leaderboard;

        @JsonProperty("mmr_leaderboard")
        private String mmrLeaderboard;

        @JsonProperty("season")
        private Integer season;

        @JsonProperty("server")
        private String server;

        @JsonProperty("patch")
        private Integer patch;

        @JsonProperty("average_rating")
        private Integer averageRating;

        @JsonProperty("average_rating_deviation")
        private Integer averageRatingDeviation;

        @JsonProperty("average_mmr")
        private Integer averageMmr;

        @JsonProperty("average_mmr_deviation")
        private Integer averageMmrDeviation;

        @JsonProperty("ongoing")
        private Boolean ongoing;

        @JsonProperty("just_finished")
        private Boolean justFinished;

        /**
         * JSON shape:
         * "teams": [
         * [ { "player": { ... } }, { "player": { ... } } ],
         * [ { "player": { ... } } ]
         * ]
         */
        @Builder.Default
        @JsonProperty("teams")
        private List<List<TeamSlot>> teams = new ArrayList<>();

        public List<TeamPlayer> playerSlot(Long playerId) {
            List<TeamSlot> teamSlot = teams.stream()
                    .filter(slots -> slots.stream().anyMatch(slot -> playerId.equals(slot.player.profileId)))
                    .findAny().orElse(new ArrayList<>());

            return teamSlot.stream().map(slot -> slot.player).toList();
        }

        public List<List<TeamSlot>> opponentSlot(Long playerId) {
            return teams.stream()
                    .filter(teamSlots -> teamSlots.stream().noneMatch(slot -> playerId.equals(slot.player.profileId)))
                    .toList();
        }

        @Override
        public String toString() {
            return ObjectUtils.toString(this);
        }
    }

    @Data
    @EqualsAndHashCode
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
    @Builder
    public static class TeamSlot {

        @JsonProperty("player")
        private TeamPlayer player;

        @Override
        public String toString() {
            return ObjectUtils.toString(this);
        }
    }

    @Data
    @EqualsAndHashCode
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
    @Builder
    public static class TeamPlayer {

        @JsonProperty("profile_id")
        private Long profileId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("country")
        private String country;

        @JsonProperty("result")
        private String result;

        @JsonProperty("civilization")
        private String civilization;

        @JsonProperty("civilization_randomized")
        private Boolean civilizationRandomized;

        @JsonProperty("rating")
        private Integer rating;

        @JsonProperty("rating_diff")
        private Integer ratingDiff;

        @JsonProperty("mmr")
        private Integer mmr;

        @JsonProperty("mmr_diff")
        private Integer mmrDiff;

        @JsonProperty("input_type")
        private String inputType;

        @Override
        public String toString() {
            return ObjectUtils.toString(this);
        }
    }
}
