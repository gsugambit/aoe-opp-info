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
public class TownCenterSummaryDto {
    private Double darkAge;
    private Double feudalAge;
    private Double castleAge;
    private Double imperialAge;
    private Double total;
}
