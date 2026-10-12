package com.theboys.aoe.opp.info.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(setterPrefix = "with")
public class PlayerTimelineDto {
    private final String profileId;
    private final String name;
    private final String civilization;
    private final String result;
    private final Integer apm;
    private final List<TimelineDataPointDto> dataPoints;
}
