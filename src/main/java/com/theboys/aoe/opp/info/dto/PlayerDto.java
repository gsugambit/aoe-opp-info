package com.theboys.aoe.opp.info.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.theboys.aoe.opp.info.utils.ObjectUtils;
import lombok.*;

@Data
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
@Builder
public class PlayerDto {

    @JsonProperty("name")
    private String name;
    @JsonProperty("profile_id")
    private Long profileId;
    @JsonProperty("steam_id")
    private String steamId;
    @JsonProperty("site_url")
    private String siteUrl;
    @JsonProperty("country")
    private String country;
    @JsonProperty("modes")
    private Mode modes;

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
    public static class Mode {

        @JsonProperty("rm_solo")
        private RankedStats rmSolo;
        @JsonProperty("rm_team")
        private RankedStats rmTeam;
        @JsonProperty("rm_1v1_elo")
        private RankedStats rm1v1Elo;
        @JsonProperty("rm_2v2_elo")
        private RankedStats rm2v2Elo;
        @JsonProperty("qm_3v3")
        private RankedStats qm3v3;
        @JsonProperty("qm_4v4")
        private RankedStats qm4v4;

        @Override
        public String toString() {
            return ObjectUtils.toString(this);
        }
    }
}
