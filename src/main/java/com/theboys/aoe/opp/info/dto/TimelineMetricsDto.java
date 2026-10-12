package com.theboys.aoe.opp.info.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(setterPrefix = "with")
public class TimelineMetricsDto {
    private final String gameId;
    private final String leaderboard;
    private final String mapName;
    private final Integer durationSeconds;
    private final List<PlayerTimelineDto> players;
}
