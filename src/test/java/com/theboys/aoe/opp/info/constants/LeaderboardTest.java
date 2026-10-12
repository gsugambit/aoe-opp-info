package com.theboys.aoe.opp.info.constants;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LeaderboardTest {

    @ParameterizedTest
    @CsvSource({
            "1V1, rm_solo",
            "2V2, rm_team",
            "RM_1V1, rm_1v1",
            "RM_2V2, rm_2v2",
            "RM_3V3, rm_3v3",
            "RM_4V4, rm_4v4",
            "QM_1V1, qm_1v1",
            "QM_2V2, qm_2v2",
            "QM_3V3, qm_3v3",
            "QM_4V4, qm_4v4",
            "RM_1V1_CONSOLE, rm_1v1_console",
            "RM_2V2_CONSOLE, rm_2v2_console",
            "RM_3V3_CONSOLE, rm_3v3_console",
            "RM_4V4_CONSOLE, rm_4v4_console",
            "QM_1V1_CONSOLE, qm_1v1_console",
            "QM_2V2_CONSOLE, qm_2v2_console",
            "QM_3V3_CONSOLE, qm_3v3_console",
            "QM_4V4_CONSOLE, qm_4v4_console"
    })
    public void testFindByValue_Success(final String value, final String expectedApiValue) {
        // given / when
        final Leaderboard leaderboard = Leaderboard.findByValue(value);

        // then
        assertNotNull(leaderboard);
        assertEquals(expectedApiValue, leaderboard.getApiValue());
    }

    @ParameterizedTest
    @CsvSource({
            "rm_1v1",
            "qm_1v1",
            "qm_2v2",
            "qm_3v3",
            "qm_4v4",
            "rm_1v1_console",
            "qm_1v1_console",
            "qm_2v2_console",
            "qm_3v3_console",
            "qm_4v4_console"
    })
    public void testFindByApiValue_Success(final String apiValue) {
        // given / when
        final Leaderboard leaderboard = Leaderboard.findByValue(apiValue);

        // then
        assertNotNull(leaderboard);
        assertEquals(apiValue, leaderboard.getApiValue());
    }

    @Test
    public void testFindByValue_NullOrBlankOrInvalid() {
        // given / when / then
        assertNull(Leaderboard.findByValue(null));
        assertNull(Leaderboard.findByValue(""));
        assertNull(Leaderboard.findByValue("   "));
        assertNull(Leaderboard.findByValue("invalid_leaderboard"));
    }
}
