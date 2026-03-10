package com.theboys.aoe.opp.info.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
@Builder
public class AutocompleteResponseDto {

    private String query;
    private String leaderboard;
    private Integer count;
    @Builder.Default
    private List<AutocompletePlayerDto> players = new ArrayList<>();

    @Data
    @EqualsAndHashCode
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
    @Builder
    public static class AutocompletePlayerDto {
        private String name;
        private Long profileId;
        private Integer rating;
        private Integer rank;
        private String country;
        private Double winRate;
    }
}