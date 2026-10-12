package com.theboys.aoe.opp.info.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerGameAnalysisDto {
    private String profileId;
    private String name;
    private String civilization;
    private String result;
    private Integer apm;
    private AgeUpSummaryDto ageUpTimings;
    private VillagerSummaryDto villagerProduction;
    private TraderSummaryDto traderProduction;
    private List<MilitaryUnitSummaryDto> militaryProduction;
    private TownCenterSummaryDto additionalTownCenters;
    private List<LandmarkSummaryDto> landmarks;
}
