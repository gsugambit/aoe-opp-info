package com.theboys.aoe.opp.info.constants;

import lombok.Getter;

@Getter
public enum Civilization {

    ABBASID_DYNASTY("abbasid_dynasty", "Abbasid Dynasty"),
    AYYUBIDS("ayyubids", "Ayyubids"),
    BYZANTINES("byzantines", "Byzantines"),
    CHINESE("chinese", "Chinese"),
    DELHI_SULTANATE("delhi_sultanate", "Delhi Sultanate"),
    ENGLISH("english", "English"),
    FRENCH("french", "French"),
    GOLDEN_HORDE("golden_horde", "Golden Horde"),
    HOLY_ROMAN_EMPIRE("holy_roman_empire", "Holy Roman Empire"),
    HOUSE_OF_LANCASTER("house_of_lancaster", "House of Lancaster"),
    JAPANESE("japanese", "Japanese"),
    JEANNE_DARC("jeanne_darc", "Jeanne d'Arc"),
    JIN_DYNASTY("jin_dynasty", "Jin Dynasty"),
    KNIGHTS_TEMPLAR("knights_templar", "Knights Templar"),
    MACEDONIAN_DYNASTY("macedonian_dynasty", "Macedonian Dynasty"),
    MALIANS("malians", "Malians"),
    MONGOLS("mongols", "Mongols"),
    ORDER_OF_THE_DRAGON("order_of_the_dragon", "Order of the Dragon"),
    OTTOMANS("ottomans", "Ottomans"),
    RUS("rus", "Rus"),
    SENGOKU_DAIMYO("sengoku_daimyo", "Sengoku Daimyo"),
    TUGHLAQ_DYNASTY("tughlaq_dynasty", "Tughlaq Dynasty"),
    ZHU_XIS_LEGACY("zhu_xis_legacy", "Zhu Xi's Legacy");

    private final String value;
    private final String displayName;

    Civilization(final String value, final String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public static Civilization findByValue(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (final Civilization civilization : values()) {
            final boolean matchesValue = civilization.value.equalsIgnoreCase(value);
            final boolean matchesDisplayName = civilization.displayName.equalsIgnoreCase(value);
            final boolean matchesName = civilization.name().equalsIgnoreCase(value);

            if (matchesValue || matchesDisplayName || matchesName) {
                return civilization;
            }
        }

        return null;
    }

}
