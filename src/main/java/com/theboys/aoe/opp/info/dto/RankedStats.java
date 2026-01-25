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
public class RankedStats {

    @JsonProperty("civilizations")
    @Builder.Default
    public List<Civilization> civilizations = new ArrayList<>();
    @JsonProperty("previous_seasons")
    @Builder.Default
    public List<SeasonDto> previousSeasons = new ArrayList<>();
    @JsonProperty("rating")
    private Integer rating;
    @JsonProperty("max_rating")
    private Integer maxRating;
    @JsonProperty("max_rating_7d")
    private Integer maxRating7d;
    @JsonProperty("max_rating_1m")
    private Integer maxRating1m;
    @JsonProperty("rank")
    private Integer rank;
    @JsonProperty("rank_level")
    private String rankLevel;
    @JsonProperty("streak")
    private Integer streak;
    @JsonProperty("games_count")
    private Integer gamesCount;
    @JsonProperty("wins_count")
    private Integer winsCount;
    @JsonProperty("losses_count")
    private Integer lossesCount;
    @JsonProperty("disputes_count")
    private Integer disputesCount;
    @JsonProperty("drops_count")
    private Integer dropsCount;
    @JsonProperty("last_game_at")
    private Instant lastGameAt;
    @JsonProperty("win_rate")
    private Double winRate;
    @JsonProperty("season")
    private Integer season;

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
    public static class Civilization {
        @JsonProperty("civilization")
        private String civilization;
        @JsonProperty("win_rate")
        private Double winRate;
        @JsonProperty("pick_rate")
        private Double pickRate;
        @JsonProperty("games_count")
        private Integer gamesCount;

        @Override
        public String toString() {
            return ObjectUtils.toString(this);
        }
    }
}
