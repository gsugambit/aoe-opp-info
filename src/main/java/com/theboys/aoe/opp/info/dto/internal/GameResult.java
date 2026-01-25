package com.theboys.aoe.opp.info.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.theboys.aoe.opp.info.dto.GamesPage;
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
public class GameResult {

    private Instant startedAt;
    private Long duration;
    private String map;
    private String matchType;
    private Long gameId;
    private Integer season;
    private String server;
    private Integer avgRating;
    private Integer avgMmr;

    private ResultTeam winningTeam;
    @Builder.Default
    private List<ResultTeam> losingTeams = new ArrayList<>();

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
    public static class ResultTeam {
        @Builder.Default
        private List<ResultPlayer> players = new ArrayList<>();

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
    public static class ResultPlayer {
        private Long profileId;
        private String name;
        private String civilization;
        private Integer rating;
        private Integer mmr;

        public static ResultPlayer from(GamesPage.TeamPlayer teamPlayer) {
            return ResultPlayer.builder()
                    .profileId(teamPlayer.getProfileId())
                    .name(teamPlayer.getName())
                    .civilization(teamPlayer.getCivilization())
                    .rating(teamPlayer.getRating())
                    .mmr(teamPlayer.getMmr())
                    .build();
        }

        @Override
        public String toString() {
            return ObjectUtils.toString(this);
        }
    }
}
