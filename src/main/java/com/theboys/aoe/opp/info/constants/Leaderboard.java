package com.theboys.aoe.opp.info.constants;

import lombok.Getter;

@Getter
public enum Leaderboard {

    ONE_V_ONE("1V1", "rm_solo"),
    TWO_V_TWO("2V2", "rm_team");

    private final String value;
    private final String apiValue;

    Leaderboard(String value, String apiValue) {
        this.apiValue = apiValue;
        this.value = value;
    }

    public static Leaderboard findByValue(String value) {
        for (Leaderboard l : values()) {
            if (l.value.equals(value)) {
                return l;
            }
        }

        return null;
    }

}
