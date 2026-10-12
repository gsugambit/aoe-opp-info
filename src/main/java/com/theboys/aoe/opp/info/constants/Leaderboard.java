package com.theboys.aoe.opp.info.constants;

import lombok.Getter;

@Getter
public enum Leaderboard {

    ONE_V_ONE("1V1", "rm_solo"),
    TWO_V_TWO("2V2", "rm_team"),
    RM_1V1("RM_1V1", "rm_1v1"),
    RM_2V2("RM_2V2", "rm_2v2"),
    RM_3V3("RM_3V3", "rm_3v3"),
    RM_4V4("RM_4V4", "rm_4v4"),
    QM_1V1("QM_1V1", "qm_1v1"),
    QM_2V2("QM_2V2", "qm_2v2"),
    QM_3V3("QM_3V3", "qm_3v3"),
    QM_4V4("QM_4V4", "qm_4v4"),
    RM_1V1_CONSOLE("RM_1V1_CONSOLE", "rm_1v1_console"),
    RM_2V2_CONSOLE("RM_2V2_CONSOLE", "rm_2v2_console"),
    RM_3V3_CONSOLE("RM_3V3_CONSOLE", "rm_3v3_console"),
    RM_4V4_CONSOLE("RM_4V4_CONSOLE", "rm_4v4_console"),
    QM_1V1_CONSOLE("QM_1V1_CONSOLE", "qm_1v1_console"),
    QM_2V2_CONSOLE("QM_2V2_CONSOLE", "qm_2v2_console"),
    QM_3V3_CONSOLE("QM_3V3_CONSOLE", "qm_3v3_console"),
    QM_4V4_CONSOLE("QM_4V4_CONSOLE", "qm_4v4_console");

    private final String value;
    private final String apiValue;

    Leaderboard(final String value, final String apiValue) {
        this.apiValue = apiValue;
        this.value = value;
    }

    public static Leaderboard findByValue(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (final Leaderboard leaderboard : values()) {
            final boolean matchesValue = leaderboard.value.equalsIgnoreCase(value);
            final boolean matchesApiValue = leaderboard.apiValue.equalsIgnoreCase(value);
            final boolean matchesName = leaderboard.name().equalsIgnoreCase(value);

            if (matchesValue || matchesApiValue || matchesName) {
                return leaderboard;
            }
        }

        return null;
    }

}
