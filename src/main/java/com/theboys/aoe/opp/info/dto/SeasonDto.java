package com.theboys.aoe.opp.info.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.theboys.aoe.opp.info.utils.ObjectUtils;
import lombok.*;

import java.time.Instant;

@Data
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
@Builder
public class SeasonDto {
    @JsonProperty("rating")
    private Integer rating;
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
}
