package com.theboys.aoe.opp.info.constants;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CivilizationTest {

    @Test
    public void testCivilizationCount() {
        assertEquals(23, Civilization.values().length);
    }

    @ParameterizedTest
    @CsvSource({
            "ABBASID_DYNASTY, abbasid_dynasty, Abbasid Dynasty",
            "AYYUBIDS, ayyubids, Ayyubids",
            "BYZANTINES, byzantines, Byzantines",
            "CHINESE, chinese, Chinese",
            "DELHI_SULTANATE, delhi_sultanate, Delhi Sultanate",
            "ENGLISH, english, English",
            "FRENCH, french, French",
            "GOLDEN_HORDE, golden_horde, Golden Horde",
            "HOLY_ROMAN_EMPIRE, holy_roman_empire, Holy Roman Empire",
            "HOUSE_OF_LANCASTER, house_of_lancaster, House of Lancaster",
            "JAPANESE, japanese, Japanese",
            "JEANNE_DARC, jeanne_darc, Jeanne d'Arc",
            "JIN_DYNASTY, jin_dynasty, Jin Dynasty",
            "KNIGHTS_TEMPLAR, knights_templar, Knights Templar",
            "MACEDONIAN_DYNASTY, macedonian_dynasty, Macedonian Dynasty",
            "MALIANS, malians, Malians",
            "MONGOLS, mongols, Mongols",
            "ORDER_OF_THE_DRAGON, order_of_the_dragon, Order of the Dragon",
            "OTTOMANS, ottomans, Ottomans",
            "RUS, rus, Rus",
            "SENGOKU_DAIMYO, sengoku_daimyo, Sengoku Daimyo",
            "TUGHLAQ_DYNASTY, tughlaq_dynasty, Tughlaq Dynasty",
            "ZHU_XIS_LEGACY, zhu_xis_legacy, Zhu Xi's Legacy"
    })
    public void testCivilizationEnumValues(final String enumName, final String expectedValue, final String expectedDisplayName) {
        // given / when
        final Civilization civ = Civilization.valueOf(enumName);

        // then
        assertNotNull(civ);
        assertEquals(expectedValue, civ.getValue());
        assertEquals(expectedDisplayName, civ.getDisplayName());
    }

    @ParameterizedTest
    @CsvSource({
            "abbasid_dynasty, abbasid_dynasty",
            "Abbasid Dynasty, abbasid_dynasty",
            "ABBASID_DYNASTY, abbasid_dynasty",
            "mongols, mongols",
            "Mongols, mongols",
            "MONGOLS, mongols",
            "zhu_xis_legacy, zhu_xis_legacy",
            "Zhu Xi's Legacy, zhu_xis_legacy",
            "jeanne_darc, jeanne_darc",
            "Jeanne d'Arc, jeanne_darc"
    })
    public void testFindByValue_Success(final String input, final String expectedValue) {
        // given / when
        final Civilization civ = Civilization.findByValue(input);

        // then
        assertNotNull(civ);
        assertEquals(expectedValue, civ.getValue());
    }

    @Test
    public void testFindByValue_NullOrBlankOrInvalid() {
        // given / when / then
        assertNull(Civilization.findByValue(null));
        assertNull(Civilization.findByValue(""));
        assertNull(Civilization.findByValue("   "));
        assertNull(Civilization.findByValue("unknown_civilization"));
    }
}
