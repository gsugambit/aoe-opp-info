package com.theboys.aoe.opp.info.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LandmarkSummaryDto {
    private String landmarkName;
    private String icon;
    private Double pickRatePercentage;
    private Double averageGameTimeSeconds;
    private Integer targetAge;
    private Integer timesBuilt;
}
