package com.theboys.aoe.opp.info.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
public class TimelineDataPointDto {
    private final Integer timestamp;
    private final Integer villagerCount;
    private final Integer militaryCount;
    private final Integer totalPopulation;
}
