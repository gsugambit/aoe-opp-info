package com.theboys.aoe.opp.info.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
@Builder
public class AutocompleteResponse {

    private String query;
    private String leaderboard;
    private Integer count;
    @Builder.Default
    private List<AutocompletePlayer> players = new ArrayList<>();

    public static class AutocompletePlayer {
        @JsonProperty("name")
        private String name;
        @JsonProperty("profile_id")
        private Long profileId;
        @JsonProperty("site_url")
        private String siteUrl;
        @JsonProperty("rating")
        private Integer rating;
        @JsonProperty("rank")
        private Integer rank;
        @JsonProperty("country")
        private String country;
        @JsonProperty("streak")
        private Integer streak;
        @JsonProperty("games_count")
        private Integer gamesCount;
        @JsonProperty("wins_count")
        private Integer winsCount;
        @JsonProperty("losses_count")
        private Integer lossesCount;
        @JsonProperty("win_rate")
        private Double winRate;

        public AutocompleteResponseDto.AutocompletePlayerDto toDto() {
            return AutocompleteResponseDto.AutocompletePlayerDto.builder()
                    .rank(rank)
                    .winRate(winRate)
                    .name(name)
                    .country(country)
                    .profileId(profileId)
                    .rating(rating)
                    .build();
        }
    }

    public AutocompleteResponseDto toDto() {
        return AutocompleteResponseDto.builder()
                .query(query)
                .count(count)
                .leaderboard(leaderboard)
                .players(players.stream()
                        .map(AutocompletePlayer::toDto)
                        .collect(Collectors.toList()))
                .build();
    }
}
