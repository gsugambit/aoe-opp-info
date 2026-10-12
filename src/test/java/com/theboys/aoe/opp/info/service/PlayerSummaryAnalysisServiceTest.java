package com.theboys.aoe.opp.info.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theboys.aoe.opp.info.dto.AgeUpSummaryDto;
import com.theboys.aoe.opp.info.dto.LandmarkSummaryDto;
import com.theboys.aoe.opp.info.dto.MilitaryUnitSummaryDto;
import com.theboys.aoe.opp.info.dto.PlayerGameAnalysisDto;
import com.theboys.aoe.opp.info.dto.PlayerSummaryDto;
import com.theboys.aoe.opp.info.dto.TownCenterSummaryDto;
import com.theboys.aoe.opp.info.dto.TraderSummaryDto;
import com.theboys.aoe.opp.info.dto.VillagerSummaryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlayerSummaryAnalysisServiceTest {

    private PlayerSummaryAnalysisService analysisService;
    private ObjectMapper objectMapper;
    private JsonNode sampleMatchNode;
    private JsonNode knightsTemplarMatchNode;

    @BeforeEach
    public void setUp() throws Exception {
        analysisService = new PlayerSummaryAnalysisService();
        objectMapper = new ObjectMapper();

        try (final InputStream is = getClass().getResourceAsStream("/samples/match_250598826.json")) {
            assertNotNull(is, "Sample match JSON file should exist");
            sampleMatchNode = objectMapper.readTree(is);
        }

        try (final InputStream is = getClass().getResourceAsStream("/samples/match_223729268.json")) {
            assertNotNull(is, "Knights Templar sample match JSON file should exist");
            knightsTemplarMatchNode = objectMapper.readTree(is);
        }
    }

    @Test
    public void analyzePlayerSummaries_SingleGame_Peter() {
        // given
        final String profileId = "10880067";
        final String gamerName = "peter1057285";

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                profileId,
                gamerName,
                List.of(sampleMatchNode)
        );

        // then
        assertNotNull(summary);
        assertEquals(1, summary.getGamesAnalyzed());
        assertEquals(173.0, summary.getAverageApm());

        // Age up timings
        final AgeUpSummaryDto ageUp = summary.getAgeUpTimings();
        assertNotNull(ageUp);
        assertEquals(341.0, ageUp.getAverageFeudalAgeSeconds());
        assertEquals(706.0, ageUp.getAverageCastleAgeSeconds());
        assertEquals(1889.0, ageUp.getAverageImperialAgeSeconds());

        // Villager production
        final VillagerSummaryDto vills = summary.getVillagerProduction();
        assertNotNull(vills);
        assertEquals(19.0, vills.getDarkAge());
        assertEquals(13.0, vills.getFeudalAge());
        assertEquals(26.0, vills.getCastleAge());
        assertEquals(16.0, vills.getImperialAge());
        assertEquals(74.0, vills.getTotal());

        // Trader production
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(0.0, traders.getDarkAge());
        assertEquals(0.0, traders.getFeudalAge());
        assertEquals(0.0, traders.getCastleAge());
        assertEquals(8.0, traders.getImperialAge());
        assertEquals(8.0, traders.getTotal());

        // Military production
        final List<MilitaryUnitSummaryDto> military = summary.getMilitaryProduction();
        assertNotNull(military);
        assertEquals(11, military.size());
        assertEquals("gilded_archer", military.get(0).getUnitType());
        assertEquals(28.35, military.get(0).getPercentageOfTotal());
        assertEquals("gilded_manatarms", military.get(1).getUnitType());
        assertEquals(20.12, military.get(1).getPercentageOfTotal());
        assertEquals("gilded_spearman", military.get(2).getUnitType());
        assertEquals(15.55, military.get(2).getPercentageOfTotal());
        assertEquals("gilded_crossbowman", military.get(3).getUnitType());
        assertEquals(13.11, military.get(3).getPercentageOfTotal());
        assertEquals("gilded_landsknecht", military.get(4).getUnitType());
        assertEquals(6.4, military.get(4).getPercentageOfTotal());
        assertEquals("gilded_handcannon", military.get(5).getUnitType());
        assertEquals(6.1, military.get(5).getPercentageOfTotal());
        assertEquals("gilded_horseman", military.get(6).getUnitType());
        assertEquals(3.96, military.get(6).getPercentageOfTotal());
        assertEquals("ram", military.get(7).getUnitType());
        assertEquals(2.74, military.get(7).getPercentageOfTotal());
        assertEquals("gilded_knight", military.get(8).getUnitType());
        assertEquals(1.22, military.get(8).getPercentageOfTotal());
        assertEquals("mangonel", military.get(9).getUnitType());
        assertEquals(1.22, military.get(9).getPercentageOfTotal());
        assertEquals("scorpion", military.get(10).getUnitType());
        assertEquals(1.22, military.get(10).getPercentageOfTotal());

        final Map<String, MilitaryUnitSummaryDto> milMap = military.stream()
                .collect(Collectors.toMap(MilitaryUnitSummaryDto::getUnitType, m -> m));

        final MilitaryUnitSummaryDto spearman = milMap.get("gilded_spearman");
        assertNotNull(spearman);
        assertEquals(51.0, spearman.getAverageCount());
        assertEquals(15.55, spearman.getPercentageOfTotal());
        assertEquals(0.0, spearman.getDarkAge());
        assertEquals(0.0, spearman.getFeudalAge());
        assertEquals(15.0, spearman.getCastleAge());
        assertEquals(36.0, spearman.getImperialAge());

        final MilitaryUnitSummaryDto horseman = milMap.get("gilded_horseman");
        assertNotNull(horseman);
        assertEquals(13.0, horseman.getAverageCount());
        assertEquals(3.96, horseman.getPercentageOfTotal());
        assertEquals(0.0, horseman.getDarkAge());
        assertEquals(5.0, horseman.getFeudalAge());
        assertEquals(7.0, horseman.getCastleAge());
        assertEquals(1.0, horseman.getImperialAge());

        final MilitaryUnitSummaryDto archer = milMap.get("gilded_archer");
        assertNotNull(archer);
        assertEquals(93.0, archer.getAverageCount());
        assertEquals(28.35, archer.getPercentageOfTotal());

        final MilitaryUnitSummaryDto knight = milMap.get("gilded_knight");
        assertNotNull(knight);
        assertEquals(4.0, knight.getAverageCount());
        assertEquals(1.22, knight.getPercentageOfTotal());

        final MilitaryUnitSummaryDto manatarms = milMap.get("gilded_manatarms");
        assertNotNull(manatarms);
        assertEquals(66.0, manatarms.getAverageCount());
        assertEquals(20.12, manatarms.getPercentageOfTotal());
        assertEquals(0.0, manatarms.getDarkAge());
        assertEquals(0.0, manatarms.getFeudalAge());
        assertEquals(36.0, manatarms.getCastleAge());
        assertEquals(30.0, manatarms.getImperialAge());

        final MilitaryUnitSummaryDto crossbow = milMap.get("gilded_crossbowman");
        assertNotNull(crossbow);
        assertEquals(43.0, crossbow.getAverageCount());
        assertEquals(13.11, crossbow.getPercentageOfTotal());

        final MilitaryUnitSummaryDto landsknecht = milMap.get("gilded_landsknecht");
        assertNotNull(landsknecht);
        assertEquals(21.0, landsknecht.getAverageCount());
        assertEquals(6.4, landsknecht.getPercentageOfTotal());

        final MilitaryUnitSummaryDto handcannon = milMap.get("gilded_handcannon");
        assertNotNull(handcannon);
        assertEquals(20.0, handcannon.getAverageCount());
        assertEquals(6.1, handcannon.getPercentageOfTotal());

        final MilitaryUnitSummaryDto ram = milMap.get("ram");
        assertNotNull(ram);
        assertEquals(9.0, ram.getAverageCount());
        assertEquals(2.74, ram.getPercentageOfTotal());

        final MilitaryUnitSummaryDto mangonel = milMap.get("mangonel");
        assertNotNull(mangonel);
        assertEquals(4.0, mangonel.getAverageCount());
        assertEquals(1.22, mangonel.getPercentageOfTotal());

        final MilitaryUnitSummaryDto scorpion = milMap.get("scorpion");
        assertNotNull(scorpion);
        assertEquals(4.0, scorpion.getAverageCount());
        assertEquals(1.22, scorpion.getPercentageOfTotal());

        // Additional Town Centers
        final TownCenterSummaryDto tcs = summary.getAdditionalTownCenters();
        assertNotNull(tcs);
        assertEquals(0.0, tcs.getTotal());

        // Landmarks
        final List<LandmarkSummaryDto> landmarks = summary.getLandmarks();
        assertNotNull(landmarks);
        assertEquals(3, landmarks.size());
        assertEquals(2, landmarks.get(0).getTargetAge());
        assertEquals("Meinwerk Palace", landmarks.get(0).getLandmarkName());
        assertEquals(3, landmarks.get(1).getTargetAge());
        assertEquals("Burgrave Palace", landmarks.get(1).getLandmarkName());
        assertEquals(4, landmarks.get(2).getTargetAge());
        assertEquals("Elzbach Palace", landmarks.get(2).getLandmarkName());

        final Map<String, LandmarkSummaryDto> lmMap = landmarks.stream()
                .collect(Collectors.toMap(LandmarkSummaryDto::getLandmarkName, l -> l));

        final LandmarkSummaryDto meinwerk = lmMap.get("Meinwerk Palace");
        assertNotNull(meinwerk);
        assertEquals(100.0, meinwerk.getPickRatePercentage());
        assertEquals(341.0, meinwerk.getAverageGameTimeSeconds());
        assertEquals(2, meinwerk.getTargetAge());
        assertEquals(1, meinwerk.getTimesBuilt());
        assertNotNull(meinwerk.getIcon());

        final LandmarkSummaryDto burgrave = lmMap.get("Burgrave Palace");
        assertNotNull(burgrave);
        assertEquals(100.0, burgrave.getPickRatePercentage());
        assertEquals(706.0, burgrave.getAverageGameTimeSeconds());
        assertEquals(3, burgrave.getTargetAge());

        final LandmarkSummaryDto elzbach = lmMap.get("Elzbach Palace");
        assertNotNull(elzbach);
        assertEquals(100.0, elzbach.getPickRatePercentage());
        assertEquals(1889.0, elzbach.getAverageGameTimeSeconds());
        assertEquals(4, elzbach.getTargetAge());
    }

    @Test
    public void analyzePlayerSummaries_SingleGame_GSUGambit() {
        // given
        final String profileId = "3861757";
        final String gamerName = "GSUGambit";

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                profileId,
                gamerName,
                List.of(sampleMatchNode)
        );

        // then
        assertNotNull(summary);
        assertEquals(1, summary.getGamesAnalyzed());
        assertEquals(122.0, summary.getAverageApm());

        // Age up timings
        final AgeUpSummaryDto ageUp = summary.getAgeUpTimings();
        assertNotNull(ageUp);
        assertEquals(319.0, ageUp.getAverageFeudalAgeSeconds());
        assertEquals(923.0, ageUp.getAverageCastleAgeSeconds());
        assertEquals(1433.0, ageUp.getAverageImperialAgeSeconds());

        // Additional Town Centers
        final TownCenterSummaryDto tcs = summary.getAdditionalTownCenters();
        assertNotNull(tcs);
        assertEquals(0.0, tcs.getDarkAge());
        assertEquals(1.0, tcs.getFeudalAge());
        assertEquals(0.0, tcs.getCastleAge());
        assertEquals(0.0, tcs.getImperialAge());
        assertEquals(1.0, tcs.getTotal());

        // Military production
        final List<MilitaryUnitSummaryDto> military = summary.getMilitaryProduction();
        assertNotNull(military);
        assertEquals(13, military.size());

        final Map<String, MilitaryUnitSummaryDto> milMap = military.stream()
                .collect(Collectors.toMap(MilitaryUnitSummaryDto::getUnitType, m -> m));

        final MilitaryUnitSummaryDto manatarms = milMap.get("manatarms");
        assertNotNull(manatarms);
        assertEquals(35.0, manatarms.getAverageCount());
        assertEquals(4.63, manatarms.getPercentageOfTotal());
        assertEquals(0.0, manatarms.getDarkAge());
        assertEquals(0.0, manatarms.getFeudalAge());
        assertEquals(11.0, manatarms.getCastleAge());
        assertEquals(24.0, manatarms.getImperialAge());

        final MilitaryUnitSummaryDto spearman = milMap.get("spearman");
        assertNotNull(spearman);
        assertEquals(153.0, spearman.getAverageCount());
        assertEquals(20.24, spearman.getPercentageOfTotal());

        final MilitaryUnitSummaryDto horseman = milMap.get("horseman");
        assertNotNull(horseman);
        assertEquals(167.0, horseman.getAverageCount());
        assertEquals(22.09, horseman.getPercentageOfTotal());

        final MilitaryUnitSummaryDto archer = milMap.get("archer");
        assertNotNull(archer);
        assertEquals(283.0, archer.getAverageCount());
        assertEquals(37.43, archer.getPercentageOfTotal());

        final MilitaryUnitSummaryDto knight = milMap.get("knight");
        assertNotNull(knight);
        assertEquals(2.0, knight.getAverageCount());
        assertEquals(0.26, knight.getPercentageOfTotal());

        final MilitaryUnitSummaryDto crossbow = milMap.get("crossbowman");
        assertNotNull(crossbow);
        assertEquals(63.0, crossbow.getAverageCount());
        assertEquals(8.33, crossbow.getPercentageOfTotal());

        final MilitaryUnitSummaryDto wynguardFootman = milMap.get("wynguard_footman");
        assertNotNull(wynguardFootman);
        assertEquals(35.0, wynguardFootman.getAverageCount());
        assertEquals(4.63, wynguardFootman.getPercentageOfTotal());

        final MilitaryUnitSummaryDto scorpion = milMap.get("scorpion");
        assertNotNull(scorpion);
        assertEquals(6.0, scorpion.getAverageCount());
        assertEquals(0.79, scorpion.getPercentageOfTotal());

        final MilitaryUnitSummaryDto bombard = milMap.get("bombard");
        assertNotNull(bombard);
        assertEquals(5.0, bombard.getAverageCount());
        assertEquals(0.66, bombard.getPercentageOfTotal());

        final MilitaryUnitSummaryDto handcannon = milMap.get("handcannon");
        assertNotNull(handcannon);
        assertEquals(3.0, handcannon.getAverageCount());
        assertEquals(0.4, handcannon.getPercentageOfTotal());

        final MilitaryUnitSummaryDto mangonel = milMap.get("mangonel");
        assertNotNull(mangonel);
        assertEquals(2.0, mangonel.getAverageCount());
        assertEquals(0.26, mangonel.getPercentageOfTotal());

        final MilitaryUnitSummaryDto crownKing = milMap.get("crown_king");
        assertNotNull(crownKing);
        assertEquals(1.0, crownKing.getAverageCount());
        assertEquals(0.13, crownKing.getPercentageOfTotal());

        final MilitaryUnitSummaryDto trebuchet = milMap.get("trebuchet_cw");
        assertNotNull(trebuchet);
        assertEquals(1.0, trebuchet.getAverageCount());
        assertEquals(0.13, trebuchet.getPercentageOfTotal());

        // Landmarks
        final List<LandmarkSummaryDto> landmarks = summary.getLandmarks();
        assertNotNull(landmarks);
        assertEquals(3, landmarks.size());

        final Map<String, LandmarkSummaryDto> lmMap = landmarks.stream()
                .collect(Collectors.toMap(LandmarkSummaryDto::getLandmarkName, l -> l));

        assertTrue(lmMap.containsKey("Abbey of Kings"));
        assertTrue(lmMap.containsKey("The White Tower"));
        assertTrue(lmMap.containsKey("Wynguard Palace"));
    }

    @Test
    public void analyzePlayerSummaries_EmptyList_ReturnsZeroSummary() {
        // given / when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "3861757",
                "GSUGambit",
                Collections.emptyList()
        );

        // then
        assertNotNull(summary);
        assertEquals(0, summary.getGamesAnalyzed());
        assertNull(summary.getAverageApm());
    }

    @Test
    public void analyzePlayerSummaries_PlayerNotFound_ReturnsZeroSummary() {
        // given / when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "99999999",
                "NonExistentPlayer",
                List.of(sampleMatchNode)
        );

        // then
        assertNotNull(summary);
        assertEquals(0, summary.getGamesAnalyzed());
    }

    @Test
    public void analyzePlayerSummaries_MultipleGames_CalculatesAveragesCorrectly() throws Exception {
        // given
        final String syntheticGame1 = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "TestPlayer",
                      "apm": 100,
                      "actions": {
                        "feudalAge": [300],
                        "castleAge": [600]
                      },
                      "buildOrder": [
                        { "type": "Unit", "icon": "icons/races/common/units/villager", "finished": [0, 50, 350, 700] },
                        { "type": "Unit", "icon": "icons/races/common/units/spearman_1", "finished": [400, 500] },
                        { "type": "Unit", "icon": "icons/races/hre_historic/units/gilded_manatarms_1", "finished": [750] }
                      ],
                      "analysis": {
                        "landmarks": [
                          { "name": "Landmark A", "icon": "http://lmA.png", "gameTime": 300, "newAge": 2 }
                        ]
                      }
                    }
                  ]
                }
                """;

        final String syntheticGame2 = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "TestPlayer",
                      "apm": 200,
                      "actions": {
                        "feudalAge": [400],
                        "castleAge": [800],
                        "imperialAge": [1200]
                      },
                      "buildOrder": [
                        { "type": "Unit", "icon": "icons/races/common/units/villager", "finished": [0, 100, 500, 900, 1300] },
                        { "type": "Unit", "icon": "icons/races/common/units/spearman_2", "finished": [600] },
                        { "type": "Unit", "icon": "icons/races/common/units/knight_1", "finished": [1000] },
                        { "type": "Unit", "icon": "icons/races/common/units/manatarms_3", "finished": [1400] },
                        { "type": "Building", "icon": "icons/races/common/buildings/town_center", "constructed": [450] }
                      ],
                      "analysis": {
                        "landmarks": [
                          { "name": "Landmark A", "icon": "http://lmA.png", "gameTime": 400, "newAge": 2 },
                          { "name": "Landmark B", "icon": "http://lmB.png", "gameTime": 800, "newAge": 3 }
                        ]
                      }
                    }
                  ]
                }
                """;

        final JsonNode node1 = objectMapper.readTree(syntheticGame1);
        final JsonNode node2 = objectMapper.readTree(syntheticGame2);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "TestPlayer",
                List.of(node1, node2)
        );

        // then
        assertNotNull(summary);
        assertEquals(2, summary.getGamesAnalyzed());
        assertEquals(150.0, summary.getAverageApm());

        final AgeUpSummaryDto ageUp = summary.getAgeUpTimings();
        assertEquals(350.0, ageUp.getAverageFeudalAgeSeconds());
        assertEquals(700.0, ageUp.getAverageCastleAgeSeconds());
        assertEquals(1200.0, ageUp.getAverageImperialAgeSeconds());

        final VillagerSummaryDto vills = summary.getVillagerProduction();
        assertEquals(2.0, vills.getDarkAge());
        assertEquals(1.0, vills.getFeudalAge());
        assertEquals(1.0, vills.getCastleAge());
        assertEquals(0.5, vills.getImperialAge());
        assertEquals(4.5, vills.getTotal());

        final TownCenterSummaryDto tcs = summary.getAdditionalTownCenters();
        assertEquals(0.5, tcs.getFeudalAge());
        assertEquals(0.5, tcs.getTotal());

        final List<LandmarkSummaryDto> lms = summary.getLandmarks();
        assertEquals(2, lms.size());
        final Map<String, LandmarkSummaryDto> lmMap = lms.stream()
                .collect(Collectors.toMap(LandmarkSummaryDto::getLandmarkName, l -> l));

        final LandmarkSummaryDto lmA = lmMap.get("Landmark A");
        assertNotNull(lmA);
        assertEquals(100.0, lmA.getPickRatePercentage());
        assertEquals(350.0, lmA.getAverageGameTimeSeconds());

        final LandmarkSummaryDto lmB = lmMap.get("Landmark B");
        assertNotNull(lmB);
        assertEquals(50.0, lmB.getPickRatePercentage());
        assertEquals(800.0, lmB.getAverageGameTimeSeconds());

        final List<MilitaryUnitSummaryDto> military = summary.getMilitaryProduction();
        assertNotNull(military);
        assertEquals(4, military.size());

        final Map<String, MilitaryUnitSummaryDto> milMap = military.stream()
                .collect(Collectors.toMap(MilitaryUnitSummaryDto::getUnitType, m -> m));

        final MilitaryUnitSummaryDto spearman = milMap.get("spearman");
        assertNotNull(spearman);
        assertEquals(1.5, spearman.getAverageCount());
        assertEquals(50.0, spearman.getPercentageOfTotal());
        assertEquals(0.0, spearman.getDarkAge());
        assertEquals(1.5, spearman.getFeudalAge());
        assertEquals(0.0, spearman.getCastleAge());
        assertEquals(0.0, spearman.getImperialAge());

        final MilitaryUnitSummaryDto knight = milMap.get("knight");
        assertNotNull(knight);
        assertEquals(0.5, knight.getAverageCount());
        assertEquals(16.67, knight.getPercentageOfTotal());
        assertEquals(0.0, knight.getDarkAge());
        assertEquals(0.0, knight.getFeudalAge());
        assertEquals(0.5, knight.getCastleAge());
        assertEquals(0.0, knight.getImperialAge());

        final MilitaryUnitSummaryDto manatarms = milMap.get("manatarms");
        assertNotNull(manatarms);
        assertEquals(0.5, manatarms.getAverageCount());
        assertEquals(16.67, manatarms.getPercentageOfTotal());
        assertEquals(0.0, manatarms.getDarkAge());
        assertEquals(0.0, manatarms.getFeudalAge());
        assertEquals(0.0, manatarms.getCastleAge());
        assertEquals(0.5, manatarms.getImperialAge());

        final MilitaryUnitSummaryDto gildedManatarms = milMap.get("gilded_manatarms");
        assertNotNull(gildedManatarms);
        assertEquals(0.5, gildedManatarms.getAverageCount());
        assertEquals(16.67, gildedManatarms.getPercentageOfTotal());
        assertEquals(0.0, gildedManatarms.getDarkAge());
        assertEquals(0.0, gildedManatarms.getFeudalAge());
        assertEquals(0.5, gildedManatarms.getCastleAge());
        assertEquals(0.0, gildedManatarms.getImperialAge());

        assertNull(milMap.get("horseman"));
        assertNull(milMap.get("archer"));
    }

    @Test
    public void analyzePlayerSummaries_SingleGame_KnightsTemplar_FiltersFortressAndCapitalTownCenter() {
        // given
        final String profileId = "24520615";
        final String gamerName = "Dxggiiezz";

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                profileId,
                gamerName,
                List.of(knightsTemplarMatchNode)
        );

        // then
        assertNotNull(summary);
        assertEquals(1, summary.getGamesAnalyzed());

        final List<LandmarkSummaryDto> landmarks = summary.getLandmarks();
        assertNotNull(landmarks);
        assertEquals(3, landmarks.size());

        final Map<String, LandmarkSummaryDto> lmMap = landmarks.stream()
                .collect(Collectors.toMap(LandmarkSummaryDto::getLandmarkName, l -> l));

        assertTrue(lmMap.containsKey("Kingdom of France"));
        assertTrue(lmMap.containsKey("Republic of Genoa"));
        assertTrue(lmMap.containsKey("Republic of Venice"));
        assertFalse(lmMap.containsKey("Fortress"));
        assertFalse(lmMap.containsKey("Capital Town Center"));

        final LandmarkSummaryDto france = lmMap.get("Kingdom of France");
        assertEquals(100.0, france.getPickRatePercentage());
        assertEquals(222.0, france.getAverageGameTimeSeconds());
        assertEquals(2, france.getTargetAge());
        assertEquals(1, france.getTimesBuilt());

        final LandmarkSummaryDto genoa = lmMap.get("Republic of Genoa");
        assertEquals(100.0, genoa.getPickRatePercentage());
        assertEquals(848.0, genoa.getAverageGameTimeSeconds());
        assertEquals(3, genoa.getTargetAge());
        assertEquals(1, genoa.getTimesBuilt());

        final LandmarkSummaryDto venice = lmMap.get("Republic of Venice");
        assertEquals(100.0, venice.getPickRatePercentage());
        assertEquals(1570.0, venice.getAverageGameTimeSeconds());
        assertEquals(4, venice.getTargetAge());
        assertEquals(1, venice.getTimesBuilt());
    }

    @Test
    public void analyzePlayerSummaries_FiltersIgnoredLandmarks_CaseInsensitive() throws Exception {
        // given
        final String syntheticGame = """
                {
                  "players": [
                    {
                      "profileId": 9999,
                      "name": "IgnoredFilterPlayer",
                      "apm": 100,
                      "actions": {
                        "feudalAge": [300]
                      },
                      "analysis": {
                        "landmarks": [
                          { "name": "Capital Town Center", "icon": "http://tc.png", "gameTime": 0, "newAge": null },
                          { "name": "capital town center", "icon": "http://tc2.png", "gameTime": 0, "newAge": null },
                          { "name": "Town Center", "icon": "http://tc3.png", "gameTime": 0, "newAge": null },
                          { "name": "town_center", "icon": "http://tc4.png", "gameTime": 0, "newAge": null },
                          { "name": "Fortress", "icon": "http://f1.png", "gameTime": 500, "newAge": null },
                          { "name": "fortress", "icon": "http://f2.png", "gameTime": 600, "newAge": null },
                          { "name": "Berkshire Fortress", "icon": "http://f3.png", "gameTime": 700, "newAge": null },
                          { "name": "The White Tower", "icon": "http://wt.png", "gameTime": 650, "newAge": 3 }
                        ]
                      }
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(syntheticGame);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "9999",
                "IgnoredFilterPlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        assertEquals(1, summary.getGamesAnalyzed());

        final List<LandmarkSummaryDto> landmarks = summary.getLandmarks();
        assertNotNull(landmarks);
        assertEquals(1, landmarks.size());
        assertEquals("The White Tower", landmarks.getFirst().getLandmarkName());
        assertEquals(100.0, landmarks.getFirst().getPickRatePercentage());
        assertEquals(650.0, landmarks.getFirst().getAverageGameTimeSeconds());
        assertEquals(3, landmarks.getFirst().getTargetAge());
    }

    @Test
    public void analyzePlayerSummaries_UnequalAgeProgression_CalculatesPickRateConditionedOnTargetAge()
            throws Exception {
        // given
        final String game1Json = """
                {
                  "players": [
                    {
                      "profileId": 1111,
                      "name": "AgeProgressionPlayer",
                      "actions": {
                        "feudalAge": [300]
                      },
                      "analysis": {
                        "landmarks": [
                          { "name": "Feudal LM A", "icon": "http://lm1.png", "gameTime": 300, "newAge": 2 }
                        ]
                      }
                    }
                  ]
                }
                """;

        final String game2Json = """
                {
                  "players": [
                    {
                      "profileId": 1111,
                      "name": "AgeProgressionPlayer",
                      "actions": {
                        "feudalAge": [310],
                        "castleAge": [700]
                      },
                      "analysis": {
                        "landmarks": [
                          { "name": "Feudal LM A", "icon": "http://lm1.png", "gameTime": 310, "newAge": 2 },
                          { "name": "Castle LM B", "icon": "http://lm2.png", "gameTime": 700, "newAge": 3 }
                        ]
                      }
                    }
                  ]
                }
                """;

        final String game3Json = """
                {
                  "players": [
                    {
                      "profileId": 1111,
                      "name": "AgeProgressionPlayer",
                      "actions": {
                        "feudalAge": [305],
                        "castleAge": [690],
                        "imperialAge": [1350]
                      },
                      "analysis": {
                        "landmarks": [
                          { "name": "Feudal LM A", "icon": "http://lm1.png", "gameTime": 305, "newAge": 2 },
                          { "name": "Castle LM C", "icon": "http://lm3.png", "gameTime": 690, "newAge": 3 },
                          { "name": "Imperial LM D", "icon": "http://lm4.png", "gameTime": 1350, "newAge": 4 }
                        ]
                      }
                    }
                  ]
                }
                """;

        final JsonNode g1 = objectMapper.readTree(game1Json);
        final JsonNode g2 = objectMapper.readTree(game2Json);
        final JsonNode g3 = objectMapper.readTree(game3Json);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "1111",
                "AgeProgressionPlayer",
                List.of(g1, g2, g3)
        );

        // then
        assertNotNull(summary);
        assertEquals(3, summary.getGamesAnalyzed());

        final List<LandmarkSummaryDto> landmarks = summary.getLandmarks();
        assertNotNull(landmarks);
        assertEquals(4, landmarks.size());

        final Map<String, LandmarkSummaryDto> lmMap = landmarks.stream()
                .collect(Collectors.toMap(LandmarkSummaryDto::getLandmarkName, l -> l));

        final LandmarkSummaryDto feudalA = lmMap.get("Feudal LM A");
        assertNotNull(feudalA);
        assertEquals(2, feudalA.getTargetAge());
        assertEquals(3, feudalA.getTimesBuilt());
        assertEquals(100.0, feudalA.getPickRatePercentage());
        assertEquals(305.0, feudalA.getAverageGameTimeSeconds());

        final LandmarkSummaryDto castleB = lmMap.get("Castle LM B");
        assertNotNull(castleB);
        assertEquals(3, castleB.getTargetAge());
        assertEquals(1, castleB.getTimesBuilt());
        assertEquals(50.0, castleB.getPickRatePercentage());
        assertEquals(700.0, castleB.getAverageGameTimeSeconds());

        final LandmarkSummaryDto castleC = lmMap.get("Castle LM C");
        assertNotNull(castleC);
        assertEquals(3, castleC.getTargetAge());
        assertEquals(1, castleC.getTimesBuilt());
        assertEquals(50.0, castleC.getPickRatePercentage());
        assertEquals(690.0, castleC.getAverageGameTimeSeconds());

        final LandmarkSummaryDto imperialD = lmMap.get("Imperial LM D");
        assertNotNull(imperialD);
        assertEquals(4, imperialD.getTargetAge());
        assertEquals(1, imperialD.getTimesBuilt());
        assertEquals(100.0, imperialD.getPickRatePercentage());
        assertEquals(1350.0, imperialD.getAverageGameTimeSeconds());
    }

    @Test
    public void analyzePlayerSummaries_RepairedLandmarkWithNullNewAge_ExcludedFromAnalysis() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 2222,
                      "name": "RepairPlayer",
                      "actions": {
                        "feudalAge": [300]
                      },
                      "analysis": {
                        "landmarks": [
                          { "name": "Meinwerk Palace", "icon": "http://lm1.png", "gameTime": 300, "newAge": 2 },
                          { "name": "Meinwerk Palace", "icon": "http://lm1.png", "gameTime": 900, "newAge": null }
                        ]
                      }
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "2222",
                "RepairPlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        assertEquals(1, summary.getGamesAnalyzed());

        final List<LandmarkSummaryDto> landmarks = summary.getLandmarks();
        assertNotNull(landmarks);
        assertEquals(1, landmarks.size());

        final LandmarkSummaryDto lm = landmarks.getFirst();
        assertEquals("Meinwerk Palace", lm.getLandmarkName());
        assertEquals(2, lm.getTargetAge());
        assertEquals(1, lm.getTimesBuilt());
        assertEquals(100.0, lm.getPickRatePercentage());
        assertEquals(300.0, lm.getAverageGameTimeSeconds());
    }

    @Test
    public void analyzePlayerSummaries_StandaloneRebuiltLandmarkWithNullNewAge_ExcludedCompletely() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 3333,
                      "name": "StandaloneRebuildPlayer",
                      "actions": {
                        "feudalAge": [320]
                      },
                      "analysis": {
                        "landmarks": [
                          { "name": "Aachen Chapel", "icon": "http://lm2.png", "gameTime": 320, "newAge": 2 },
                          { "name": "Rebuilt Landmark", "icon": "http://lm3.png", "gameTime": 850, "newAge": null },
                          { "name": "Missing Age Landmark", "icon": "http://lm4.png", "gameTime": 950 }
                        ]
                      }
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "3333",
                "StandaloneRebuildPlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        assertEquals(1, summary.getGamesAnalyzed());

        final List<LandmarkSummaryDto> landmarks = summary.getLandmarks();
        assertNotNull(landmarks);
        assertEquals(1, landmarks.size());

        final LandmarkSummaryDto lm = landmarks.getFirst();
        assertEquals("Aachen Chapel", lm.getLandmarkName());
        assertEquals(2, lm.getTargetAge());
        assertEquals(1, lm.getTimesBuilt());
        assertEquals(100.0, lm.getPickRatePercentage());
        assertEquals(320.0, lm.getAverageGameTimeSeconds());
    }

    @Test
    public void analyzePlayerSummaries_SingleGame_TraderProductionAcrossAllAges() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "TraderPlayer",
                      "actions": {
                        "feudalAge": [300],
                        "castleAge": [600],
                        "imperialAge": [1200]
                      },
                      "buildOrder": [
                        {
                          "type": "Unit",
                          "icon": "icons/races/common/units/trade_cart",
                          "unknown": {
                            "14": [100, 200, 350, 400, 450, 500, 550, 700, 800, 900, 1300]
                          }
                        }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "TraderPlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(2.0, traders.getDarkAge());
        assertEquals(5.0, traders.getFeudalAge());
        assertEquals(3.0, traders.getCastleAge());
        assertEquals(1.0, traders.getImperialAge());
        assertEquals(11.0, traders.getTotal());
    }

    @Test
    public void analyzePlayerSummaries_MultiGame_TraderProductionAveraging() throws Exception {
        // given
        final String game1Json = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "TraderPlayer",
                      "actions": {
                        "feudalAge": [300]
                      },
                      "buildOrder": [
                        {
                          "type": "Unit",
                          "icon": "icons/races/common/units/trade_cart",
                          "unknown": {
                            "14": [310, 320, 330, 340, 350, 360, 370, 380, 390, 400]
                          }
                        }
                      ]
                    }
                  ]
                }
                """;
        final String game2Json = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "TraderPlayer",
                      "actions": {
                        "feudalAge": [300]
                      },
                      "buildOrder": []
                    }
                  ]
                }
                """;
        final JsonNode game1 = objectMapper.readTree(game1Json);
        final JsonNode game2 = objectMapper.readTree(game2Json);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "TraderPlayer",
                List.of(game1, game2)
        );

        // then
        assertNotNull(summary);
        assertEquals(2, summary.getGamesAnalyzed());
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(0.0, traders.getDarkAge());
        assertEquals(5.0, traders.getFeudalAge());
        assertEquals(0.0, traders.getCastleAge());
        assertEquals(0.0, traders.getImperialAge());
        assertEquals(5.0, traders.getTotal());
    }

    @Test
    public void analyzePlayerSummaries_NoTradersProduced() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "NoTradePlayer",
                      "actions": {
                        "feudalAge": [300]
                      },
                      "buildOrder": [
                        {
                          "type": "Unit",
                          "icon": "icons/races/common/units/villager",
                          "finished": [10, 20]
                        }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "NoTradePlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(0.0, traders.getDarkAge());
        assertEquals(0.0, traders.getFeudalAge());
        assertEquals(0.0, traders.getCastleAge());
        assertEquals(0.0, traders.getImperialAge());
        assertEquals(0.0, traders.getTotal());
    }

    @Test
    public void analyzePlayerSummaries_TraderProduction_MissingBuildOrderAndEmptyFinished() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "EdgeCasePlayer",
                      "actions": {},
                      "buildOrder": [
                        {
                          "type": "Unit",
                          "icon": "icons/races/common/units/trade_cart",
                          "unknown": {
                            "14": []
                          }
                        },
                        {
                          "type": "Unit",
                          "icon": "icons/races/common/units/trade_cart",
                          "unknown": {}
                        },
                        {
                          "type": "Unit",
                          "icon": "icons/races/common/units/trade_cart"
                        }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "EdgeCasePlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(0.0, traders.getDarkAge());
        assertEquals(0.0, traders.getFeudalAge());
        assertEquals(0.0, traders.getCastleAge());
        assertEquals(0.0, traders.getImperialAge());
        assertEquals(0.0, traders.getTotal());
    }

    @Test
    public void analyzePlayerSummaries_TraderProduction_IconCaseInsensitivityAndVariants() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "CasePlayer",
                      "actions": {
                        "feudalAge": [300]
                      },
                      "buildOrder": [
                        {
                          "type": "unit",
                          "icon": "ICONS/RACES/COMMON/UNITS/TRADE_CART",
                          "unknown": {
                            "14": [150]
                          }
                        },
                        {
                          "type": "Unit",
                          "icon": "icons/races/mongols/units/trade_cart_special",
                          "unknown": {
                            "14": [350]
                          }
                        }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "CasePlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(1.0, traders.getDarkAge());
        assertEquals(1.0, traders.getFeudalAge());
        assertEquals(2.0, traders.getTotal());
    }

    @Test
    public void analyzePlayerSummaries_TraderProduction_MultipleUnknownKeys() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "MultiKeyPlayer",
                      "actions": {
                        "feudalAge": [300],
                        "castleAge": [600],
                        "imperialAge": [1200]
                      },
                      "buildOrder": [
                        {
                          "type": "Unit",
                          "icon": "icons/races/common/units/trade_cart",
                          "unknown": {
                            "14": [100, 350],
                            "15": [400, 700],
                            "other": [1300]
                          }
                        }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "MultiKeyPlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(1.0, traders.getDarkAge());
        assertEquals(2.0, traders.getFeudalAge());
        assertEquals(1.0, traders.getCastleAge());
        assertEquals(1.0, traders.getImperialAge());
        assertEquals(5.0, traders.getTotal());
    }

    @Test
    public void analyzePlayerSummaries_TraderProduction_MalformedAndNonArrayValues() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "MalformedPlayer",
                      "actions": {
                        "feudalAge": [300]
                      },
                      "buildOrder": [
                        {
                          "type": "Unit",
                          "icon": "icons/races/common/units/trade_cart",
                          "unknown": {
                            "invalid1": 1234,
                            "invalid2": "not an array",
                            "invalid3": null,
                            "valid": [100, 400]
                          }
                        }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "MalformedPlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(1.0, traders.getDarkAge());
        assertEquals(1.0, traders.getFeudalAge());
        assertEquals(0.0, traders.getCastleAge());
        assertEquals(0.0, traders.getImperialAge());
        assertEquals(2.0, traders.getTotal());
    }

    @Test
    public void analyzePlayerSummaries_TraderProduction_DirectArrayFallback() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "ArrayFallbackPlayer",
                      "actions": {
                        "feudalAge": [300]
                      },
                      "buildOrder": [
                        {
                          "type": "Unit",
                          "icon": "icons/races/common/units/trade_cart",
                          "unknown": [100, 400]
                        }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "ArrayFallbackPlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(1.0, traders.getDarkAge());
        assertEquals(1.0, traders.getFeudalAge());
        assertEquals(0.0, traders.getCastleAge());
        assertEquals(0.0, traders.getImperialAge());
        assertEquals(2.0, traders.getTotal());
    }

    @Test
    public void analyzePlayerSummaries_CamelTraderProduction() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "AyyubidPlayer",
                      "actions": {
                        "feudalAge": [240],
                        "castleAge": [540],
                        "imperialAge": [1100]
                      },
                      "buildOrder": [
                        {
                          "type": "Unit",
                          "icon": "icons/races/ayyubids/units/camel_trader",
                          "unknown": {
                            "14": [260, 280, 300, 600, 1200]
                          }
                        }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "AyyubidPlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(0.0, traders.getDarkAge());
        assertEquals(3.0, traders.getFeudalAge());
        assertEquals(1.0, traders.getCastleAge());
        assertEquals(1.0, traders.getImperialAge());
        assertEquals(5.0, traders.getTotal());

        // Ensure camel_trader is blacklisted from military
        assertTrue(summary.getMilitaryProduction().isEmpty());
    }

    @Test
    public void analyzePlayerSummaries_ReindeerTraderProduction() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "MongolPlayer",
                      "actions": {
                        "feudalAge": [300],
                        "castleAge": [600]
                      },
                      "buildOrder": [
                        {
                          "type": "Unit",
                          "icon": "icons/races/mongols/units/reindeer_trader",
                          "unknown": [150, 350, 700]
                        }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "MongolPlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(1.0, traders.getDarkAge());
        assertEquals(1.0, traders.getFeudalAge());
        assertEquals(1.0, traders.getCastleAge());
        assertEquals(0.0, traders.getImperialAge());
        assertEquals(3.0, traders.getTotal());
    }

    @Test
    public void analyzePlayerSummaries_TreasureCaravanProduction() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "CaravanPlayer",
                      "actions": {
                        "feudalAge": [300]
                      },
                      "buildOrder": [
                        {
                          "type": "Unit",
                          "icon": "icons/races/common/units/treasure_caravan",
                          "unknown": {
                            "1": [350, 400]
                          }
                        }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "CaravanPlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(0.0, traders.getDarkAge());
        assertEquals(2.0, traders.getFeudalAge());
        assertEquals(2.0, traders.getTotal());
    }

    @Test
    public void analyzePlayerSummaries_PilgrimProduction() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "ByzantinePlayer",
                      "actions": {
                        "feudalAge": [300],
                        "castleAge": [600]
                      },
                      "buildOrder": [
                        {
                          "type": "Unit",
                          "icon": "icons/races/byzantines/units/pilgrim",
                          "unknown": {
                            "1": [200, 400, 800]
                          }
                        }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "ByzantinePlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(1.0, traders.getDarkAge());
        assertEquals(1.0, traders.getFeudalAge());
        assertEquals(1.0, traders.getCastleAge());
        assertEquals(3.0, traders.getTotal());
    }

    @Test
    public void analyzePlayerSummaries_TradeShipProduction_FinishedArray() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "NavalPlayer",
                      "actions": {
                        "feudalAge": [300],
                        "castleAge": [600],
                        "imperialAge": [1200]
                      },
                      "buildOrder": [
                        {
                          "type": "Unit",
                          "icon": "icons/races/common/units/trade_ship",
                          "finished": [180, 320, 340, 750, 1300]
                        }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "NavalPlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(1.0, traders.getDarkAge());
        assertEquals(2.0, traders.getFeudalAge());
        assertEquals(1.0, traders.getCastleAge());
        assertEquals(1.0, traders.getImperialAge());
        assertEquals(5.0, traders.getTotal());

        // Trade ship should not be considered a military ship
        assertTrue(summary.getMilitaryProduction().isEmpty());
    }

    @Test
    public void analyzePlayerSummaries_TraderCartVariant() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "TraderCartPlayer",
                      "actions": {
                        "feudalAge": [300]
                      },
                      "buildOrder": [
                        {
                          "type": "Unit",
                          "icon": "icons/races/common/units/trader_cart",
                          "finished": [350, 450]
                        }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "TraderCartPlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(0.0, traders.getDarkAge());
        assertEquals(2.0, traders.getFeudalAge());
        assertEquals(2.0, traders.getTotal());
    }

    @Test
    public void analyzePlayerSummaries_MixedTraderUnitsAndStructures() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 12345,
                      "name": "MixedTradePlayer",
                      "actions": {
                        "feudalAge": [300],
                        "castleAge": [600]
                      },
                      "buildOrder": [
                        {
                          "type": "Unit",
                          "icon": "icons/races/ayyubids/units/camel_trader",
                          "unknown": {
                            "14": [310, 320]
                          }
                        },
                        {
                          "type": "Unit",
                          "icon": "icons/races/common/units/trade_ship",
                          "finished": [150, 650]
                        },
                        {
                          "type": "Unit",
                          "icon": "icons/races/common/units/trade_cart",
                          "unknown": [400]
                        }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "12345",
                "MixedTradePlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final TraderSummaryDto traders = summary.getTraderProduction();
        assertNotNull(traders);
        assertEquals(1.0, traders.getDarkAge());   // trade_ship @ 150
        assertEquals(3.0, traders.getFeudalAge()); // camel_trader @ 310, 320, trade_cart @ 400
        assertEquals(1.0, traders.getCastleAge()); // trade_ship @ 650
        assertEquals(0.0, traders.getImperialAge());
        assertEquals(5.0, traders.getTotal());
    }

    @Test
    public void testIsTraderUnit() {
        // positive matches
        assertTrue(analysisService.isTraderUnit("icons/races/common/units/trade_cart"));
        assertTrue(analysisService.isTraderUnit("icons/races/common/units/trader_cart"));
        assertTrue(analysisService.isTraderUnit("icons/races/ayyubids/units/camel_trader"));
        assertTrue(analysisService.isTraderUnit("icons/races/mongols/units/reindeer_trader"));
        assertTrue(analysisService.isTraderUnit("icons/races/common/units/treasure_caravan"));
        assertTrue(analysisService.isTraderUnit("icons/races/byzantines/units/pilgrim"));
        assertTrue(analysisService.isTraderUnit("icons/races/common/units/trade_ship"));
        assertTrue(analysisService.isTraderUnit("TRADE_CART"));
        assertTrue(analysisService.isTraderUnit("CAMEL_TRADER_AGE_2"));

        // negative matches
        assertFalse(analysisService.isTraderUnit(null));
        assertFalse(analysisService.isTraderUnit(""));
        assertFalse(analysisService.isTraderUnit("   "));
        assertFalse(analysisService.isTraderUnit("icons/races/common/units/villager"));
        assertFalse(analysisService.isTraderUnit("icons/races/common/units/knight"));
        assertFalse(analysisService.isTraderUnit("icons/races/common/units/scout"));
    }

    @Test
    public void analyzeSinglePlayerGame_WithTraderUnits() throws Exception {
        // given
        final String playerJson = """
                {
                  "profileId": 9999,
                  "name": "SingleTraderPlayer",
                  "civilization": "ayyubids",
                  "result": "win",
                  "actions": {
                    "feudalAge": [300],
                    "castleAge": [600]
                  },
                  "buildOrder": [
                    {
                      "type": "Unit",
                      "icon": "icons/races/ayyubids/units/camel_trader",
                      "unknown": {
                        "14": [310, 320, 330]
                      }
                    },
                    {
                      "type": "Unit",
                      "icon": "icons/races/common/units/trade_ship",
                      "finished": [120, 650]
                    }
                  ]
                }
                """;
        final JsonNode playerNode = objectMapper.readTree(playerJson);

        // when
        final PlayerGameAnalysisDto analysis = analysisService.analyzeSinglePlayerGame(playerNode);

        // then
        assertNotNull(analysis);
        assertEquals("9999", analysis.getProfileId());
        assertEquals("SingleTraderPlayer", analysis.getName());
        assertEquals("ayyubids", analysis.getCivilization());
        assertEquals("win", analysis.getResult());

        final TraderSummaryDto traders = analysis.getTraderProduction();
        assertNotNull(traders);
        assertEquals(1.0, traders.getDarkAge());
        assertEquals(3.0, traders.getFeudalAge());
        assertEquals(1.0, traders.getCastleAge());
        assertEquals(0.0, traders.getImperialAge());
        assertEquals(5.0, traders.getTotal());

        assertTrue(analysis.getMilitaryProduction().isEmpty());
    }

    @Test
    public void analyzePlayerSummaries_ExpandedMilitaryUnits_AllCategoriesAndPrecedence() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 8888,
                      "name": "MilitaryTester",
                      "actions": {
                        "feudalAge": [300],
                        "castleAge": [600],
                        "imperialAge": [1200]
                      },
                      "buildOrder": [
                        { "type": "Unit", "icon": "icons/races/byzantines/units/palatine_cataphracts", "finished": [1300, 1400] },
                        { "type": "Unit", "icon": "icons/races/byzantines/units/cataphract", "finished": [700] },
                        { "type": "Unit", "icon": "icons/races/common/units/handcannoneer", "finished": [1350] },
                        { "type": "Unit", "icon": "icons/races/common/units/cannon", "finished": [1450] },
                        { "type": "Unit", "icon": "icons/races/chinese/units/nest_of_bees", "finished": [750, 800] },
                        { "type": "Unit", "icon": "icons/races/common/units/trebuchet", "finished": [850] },
                        { "type": "Unit", "icon": "icons/races/common/units/ram", "finished": [350] },
                        { "type": "Unit", "icon": "icons/races/common/units/crossbowman", "finished": [650] },
                        { "type": "Unit", "icon": "icons/races/japanese/units/samurai", "finished": [400] },
                        { "type": "Unit", "icon": "icons/races/japanese/units/onna_bugeisha", "finished": [450] },
                        { "type": "Unit", "icon": "icons/races/abbasid/units/ghulam", "finished": [680] },
                        { "type": "Unit", "icon": "icons/races/mongols/units/mangudai", "finished": [320] },
                        { "type": "Unit", "icon": "icons/races/delhi/units/war_elephant", "finished": [720] },
                        { "type": "Unit", "icon": "icons/races/rus/units/streltsy", "finished": [1250] },
                        { "type": "Unit", "icon": "icons/races/ottomans/units/jannisary", "finished": [710] },
                        { "type": "Unit", "icon": "icons/races/ottomans/units/mehter", "finished": [310] },
                        { "type": "Unit", "icon": "icons/races/rus/units/warrior_monk", "finished": [620] },
                        { "type": "Unit", "icon": "icons/races/mongols/units/khan", "finished": [50] }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "8888",
                "MilitaryTester",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final List<MilitaryUnitSummaryDto> military = summary.getMilitaryProduction();
        assertNotNull(military);
        assertEquals(17, military.size());

        final Map<String, MilitaryUnitSummaryDto> milMap = military.stream()
                .collect(Collectors.toMap(MilitaryUnitSummaryDto::getUnitType, m -> m));

        // Precedence verification: palatine_cataphracts vs cataphract
        final MilitaryUnitSummaryDto palatine = milMap.get("palatine_cataphracts");
        assertNotNull(palatine);
        assertEquals(2.0, palatine.getAverageCount());
        assertEquals(2.0, palatine.getImperialAge());

        final MilitaryUnitSummaryDto cataphract = milMap.get("cataphract");
        assertNotNull(cataphract);
        assertEquals(1.0, cataphract.getAverageCount());
        assertEquals(1.0, cataphract.getCastleAge());

        // Precedence verification: handcannoneer vs cannon
        final MilitaryUnitSummaryDto handcannon = milMap.get("handcannoneer");
        assertNotNull(handcannon);
        assertEquals(1.0, handcannon.getAverageCount());
        assertEquals(1.0, handcannon.getImperialAge());

        final MilitaryUnitSummaryDto cannon = milMap.get("cannon");
        assertNotNull(cannon);
        assertEquals(1.0, cannon.getAverageCount());
        assertEquals(1.0, cannon.getImperialAge());

        // Siege verification
        final MilitaryUnitSummaryDto nestOfBees = milMap.get("nest_of_bees");
        assertNotNull(nestOfBees);
        assertEquals(2.0, nestOfBees.getAverageCount());
        assertEquals(2.0, nestOfBees.getCastleAge());

        final MilitaryUnitSummaryDto trebuchet = milMap.get("trebuchet");
        assertNotNull(trebuchet);
        assertEquals(1.0, trebuchet.getAverageCount());
        assertEquals(1.0, trebuchet.getCastleAge());

        final MilitaryUnitSummaryDto ram = milMap.get("ram");
        assertNotNull(ram);
        assertEquals(1.0, ram.getAverageCount());
        assertEquals(1.0, ram.getFeudalAge());

        // Regional & unique units
        final MilitaryUnitSummaryDto crossbow = milMap.get("crossbowman");
        assertNotNull(crossbow);
        assertEquals(1.0, crossbow.getAverageCount());
        assertEquals(1.0, crossbow.getCastleAge());

        final MilitaryUnitSummaryDto samurai = milMap.get("samurai");
        assertNotNull(samurai);
        assertEquals(1.0, samurai.getAverageCount());
        assertEquals(1.0, samurai.getFeudalAge());

        final MilitaryUnitSummaryDto onnaBugeisha = milMap.get("onna_bugeisha");
        assertNotNull(onnaBugeisha);
        assertEquals(1.0, onnaBugeisha.getAverageCount());
        assertEquals(1.0, onnaBugeisha.getFeudalAge());

        final MilitaryUnitSummaryDto ghulam = milMap.get("ghulam");
        assertNotNull(ghulam);
        assertEquals(1.0, ghulam.getAverageCount());
        assertEquals(1.0, ghulam.getCastleAge());

        final MilitaryUnitSummaryDto mangudai = milMap.get("mangudai");
        assertNotNull(mangudai);
        assertEquals(1.0, mangudai.getAverageCount());
        assertEquals(1.0, mangudai.getFeudalAge());

        final MilitaryUnitSummaryDto warElephant = milMap.get("war_elephant");
        assertNotNull(warElephant);
        assertEquals(1.0, warElephant.getAverageCount());
        assertEquals(1.0, warElephant.getCastleAge());

        final MilitaryUnitSummaryDto streltsy = milMap.get("streltsy");
        assertNotNull(streltsy);
        assertEquals(1.0, streltsy.getAverageCount());
        assertEquals(1.0, streltsy.getImperialAge());

        final MilitaryUnitSummaryDto jannisary = milMap.get("jannisary");
        assertNotNull(jannisary);
        assertEquals(1.0, jannisary.getAverageCount());
        assertEquals(1.0, jannisary.getCastleAge());

        assertNull(milMap.get("mehter"));

        final MilitaryUnitSummaryDto warriorMonk = milMap.get("warrior_monk");
        assertNotNull(warriorMonk);
        assertEquals(1.0, warriorMonk.getAverageCount());
        assertEquals(1.0, warriorMonk.getCastleAge());

        final MilitaryUnitSummaryDto khan = milMap.get("khan");
        assertNotNull(khan);
        assertEquals(1.0, khan.getAverageCount());
        assertEquals(1.0, khan.getDarkAge());

        // Unproduced unit baseline
        assertNull(milMap.get("spearman"));
    }

    @Test
    public void analyzePlayerSummaries_ZeroMilitaryProduction_ReturnsEmptyList() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 9999,
                      "name": "EcoOnlyPlayer",
                      "actions": {
                        "feudalAge": [300],
                        "castleAge": [600],
                        "imperialAge": [1200]
                      },
                      "buildOrder": [
                        { "type": "Unit", "icon": "icons/races/common/units/villager", "finished": [10, 30, 50] },
                        { "type": "Building", "icon": "icons/races/common/buildings/house", "finished": [100] }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "9999",
                "EcoOnlyPlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final List<MilitaryUnitSummaryDto> military = summary.getMilitaryProduction();
        assertNotNull(military);
        assertTrue(military.isEmpty());
    }

    @Test
    public void analyzePlayerSummaries_KnightsTemplar_HospitallerKnightDynamicTracking() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 7777,
                      "name": "TemplarPlayer",
                      "actions": {
                        "feudalAge": [300],
                        "castleAge": [600]
                      },
                      "buildOrder": [
                        { "type": "Unit", "icon": "icons/races/templar/units/hospitaller_knight_age_2", "finished": [400, 500] },
                        { "type": "Unit", "icon": "icons/races/common/units/knight", "finished": [700] }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "7777",
                "TemplarPlayer",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final List<MilitaryUnitSummaryDto> military = summary.getMilitaryProduction();
        assertNotNull(military);
        assertEquals(2, military.size());

        final Map<String, MilitaryUnitSummaryDto> milMap = military.stream()
                .collect(Collectors.toMap(MilitaryUnitSummaryDto::getUnitType, m -> m));

        final MilitaryUnitSummaryDto hospitaller = milMap.get("hospitaller_knight");
        assertNotNull(hospitaller);
        assertEquals(2.0, hospitaller.getAverageCount());
        assertEquals(2.0, hospitaller.getFeudalAge());
        assertEquals(66.67, hospitaller.getPercentageOfTotal());

        final MilitaryUnitSummaryDto knight = milMap.get("knight");
        assertNotNull(knight);
        assertEquals(1.0, knight.getAverageCount());
        assertEquals(1.0, knight.getCastleAge());
        assertEquals(33.33, knight.getPercentageOfTotal());
    }

    @Test
    public void analyzePlayerSummaries_SuffixStripping_AllSupportedVariants() throws Exception {
        // given
        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 5555,
                      "name": "SuffixTester",
                      "actions": {
                        "feudalAge": [300]
                      },
                      "buildOrder": [
                        { "type": "Unit", "icon": "icons/races/hre/units/spearman_age_1", "finished": [350] },
                        { "type": "Unit", "icon": "icons/races/hre/units/spearman_age_2", "finished": [360] },
                        { "type": "Unit", "icon": "icons/races/hre/units/spearman_age_3", "finished": [370] },
                        { "type": "Unit", "icon": "icons/races/hre/units/spearman_age_4", "finished": [380] },
                        { "type": "Unit", "icon": "icons/races/english/units/longbowman_age1", "finished": [390] },
                        { "type": "Unit", "icon": "icons/races/english/units/longbowman_age2", "finished": [400] },
                        { "type": "Unit", "icon": "icons/races/english/units/longbowman_age3", "finished": [410] },
                        { "type": "Unit", "icon": "icons/races/english/units/longbowman_age4", "finished": [420] },
                        { "type": "Unit", "icon": "icons/races/mongols/units/mangudai_1", "finished": [430] },
                        { "type": "Unit", "icon": "icons/races/mongols/units/mangudai_2", "finished": [440] },
                        { "type": "Unit", "icon": "icons/races/mongols/units/mangudai_3", "finished": [450] },
                        { "type": "Unit", "icon": "icons/races/mongols/units/mangudai_4", "finished": [460] }
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "5555",
                "SuffixTester",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final List<MilitaryUnitSummaryDto> military = summary.getMilitaryProduction();
        assertNotNull(military);
        assertEquals(3, military.size());

        final Map<String, MilitaryUnitSummaryDto> milMap = military.stream()
                .collect(Collectors.toMap(MilitaryUnitSummaryDto::getUnitType, m -> m));

        final MilitaryUnitSummaryDto spearman = milMap.get("spearman");
        assertNotNull(spearman);
        assertEquals(4.0, spearman.getAverageCount());

        final MilitaryUnitSummaryDto longbowman = milMap.get("longbowman");
        assertNotNull(longbowman);
        assertEquals(4.0, longbowman.getAverageCount());

        final MilitaryUnitSummaryDto mangudai = milMap.get("mangudai");
        assertNotNull(mangudai);
        assertEquals(4.0, mangudai.getAverageCount());
    }

    @Test
    public void analyzePlayerSummaries_BlacklistFiltering_AllExcludedUnits() throws Exception {
        // given
        final List<String> blacklistedIcons = List.of(
                "icons/races/common/units/villager",
                "icons/races/common/units/scout",
                "icons/races/common/units/monk",
                "icons/races/japanese/units/buddhist_monk",
                "icons/races/mongols/units/mounted_villager",
                "icons/races/ottomans/units/mehter",
                "icons/races/ayyyubids/units/dervish",
                "icons/races/mongols/units/unit_khan_1_mon",
                "icons/races/japanese/units/shinto_priest",
                "icons/races/common/units/general_ship",
                "icons/races/french/units/jeanne_villager",
                "icons/races/french/units/jeanne_archer",
                "icons/races/mongols/units/horseman_grassland",
                "icons/races/chinese/units/shaolin_monk",
                "icons/races/french/units/jeanne_ranged_monarch",
                "icons/races/chinese/units/imperial_official",
                "icons/races/french/units/jeanne_manatarms",
                "icons/races/common/units/transport",
                "icons/races/common/units/trade_ship",
                "icons/races/common/units/trade_cart",
                "icons/races/common/units/fishing_boat",
                "icons/races/abbasid/units/camel_trader",
                "icons/races/japanese/units/yatai",
                "icons/races/malians/units/cattle",
                "icons/races/mongols/units/reindeer_trader",
                "icons/races/byzantines/units/emissary",
                "icons/races/common/units/fortress",
                "icons/races/malians/units/treasure_caravan",
                "icons/races/french/units/jeanne_melee_monarch",
                "icons/races/french/units/jeanne_melee_knight",
                "icons/races/byzantines/units/pilgrim",
                "icons/races/hre/units/gilded_villager"
        );

        final StringBuilder buildOrderJson = new StringBuilder();
        for (int i = 0; i < blacklistedIcons.size(); i++) {
            if (i > 0) buildOrderJson.append(",\n");
            buildOrderJson.append("{ \"type\": \"Unit\", \"icon\": \"").append(blacklistedIcons.get(i)).append("\", \"finished\": [100] }");
        }

        final String gameJson = """
                {
                  "players": [
                    {
                      "profileId": 4444,
                      "name": "BlacklistTester",
                      "actions": {},
                      "buildOrder": [
                """ + buildOrderJson + """
                      ]
                    }
                  ]
                }
                """;
        final JsonNode gameNode = objectMapper.readTree(gameJson);

        // when
        final PlayerSummaryDto summary = analysisService.analyzePlayerSummaries(
                "4444",
                "BlacklistTester",
                List.of(gameNode)
        );

        // then
        assertNotNull(summary);
        final List<MilitaryUnitSummaryDto> military = summary.getMilitaryProduction();
        assertNotNull(military);
        assertTrue(military.isEmpty(), "All blacklisted non-military units should be filtered out");
    }

    @Test
    public void extractMilitaryUnitName_DirectHelperTests() {
        // null / empty / blank
        assertNull(analysisService.extractMilitaryUnitName(null));
        assertNull(analysisService.extractMilitaryUnitName(""));
        assertNull(analysisService.extractMilitaryUnitName("   "));

        // path parsing and normalization
        assertEquals("hospitaller_knight", analysisService.extractMilitaryUnitName("icons/races/templar/units/hospitaller_knight_age_2"));
        assertEquals("knight", analysisService.extractMilitaryUnitName("icons/races/common/units/knight"));
        assertEquals("archer", analysisService.extractMilitaryUnitName("ARCHER_AGE_2"));
        assertEquals("archer", analysisService.extractMilitaryUnitName("archer"));

        // suffix stripping
        assertEquals("spearman", analysisService.extractMilitaryUnitName("spearman_age_1"));
        assertEquals("spearman", analysisService.extractMilitaryUnitName("spearman_age_2"));
        assertEquals("spearman", analysisService.extractMilitaryUnitName("spearman_age_3"));
        assertEquals("spearman", analysisService.extractMilitaryUnitName("spearman_age_4"));
        assertEquals("spearman", analysisService.extractMilitaryUnitName("spearman_age1"));
        assertEquals("spearman", analysisService.extractMilitaryUnitName("spearman_age2"));
        assertEquals("spearman", analysisService.extractMilitaryUnitName("spearman_age3"));
        assertEquals("spearman", analysisService.extractMilitaryUnitName("spearman_age4"));
        assertEquals("spearman", analysisService.extractMilitaryUnitName("spearman_1"));
        assertEquals("spearman", analysisService.extractMilitaryUnitName("spearman_2"));
        assertEquals("spearman", analysisService.extractMilitaryUnitName("spearman_3"));
        assertEquals("spearman", analysisService.extractMilitaryUnitName("spearman_4"));

        // blacklist exclusions
        assertNull(analysisService.extractMilitaryUnitName("icons/races/common/units/villager"));
        assertNull(analysisService.extractMilitaryUnitName("villager_age_2"));
        assertNull(analysisService.extractMilitaryUnitName("scout_1"));
        assertNull(analysisService.extractMilitaryUnitName("unit_khan_1_mon"));
        assertNull(analysisService.extractMilitaryUnitName("mehter"));
        assertNull(analysisService.extractMilitaryUnitName("jeanne_villager"));
        assertNull(analysisService.extractMilitaryUnitName("gilded_villager"));
    }

    @Test
    public void analyzeGamePlayers_SampleMatch_250598826() {
        // when
        final List<PlayerGameAnalysisDto> players = analysisService.analyzeGamePlayers(sampleMatchNode);

        // then
        assertNotNull(players);
        assertEquals(2, players.size());

        // Winner should be first due to sortPlayersByOutcome (GSUGambit won)
        final PlayerGameAnalysisDto winner = players.get(0);
        assertEquals("3861757", winner.getProfileId());
        assertEquals("GSUGambit", winner.getName());
        assertEquals("win", winner.getResult());

        // Loser is peter1057285
        final PlayerGameAnalysisDto loser = players.get(1);
        assertEquals("10880067", loser.getProfileId());
        assertEquals("peter1057285", loser.getName());
        assertEquals("loss", loser.getResult());
        assertEquals(173, loser.getApm());

        // Timings for peter1057285
        assertNotNull(loser.getAgeUpTimings());
        assertEquals(341.0, loser.getAgeUpTimings().getAverageFeudalAgeSeconds());
        assertEquals(706.0, loser.getAgeUpTimings().getAverageCastleAgeSeconds());
        assertEquals(1889.0, loser.getAgeUpTimings().getAverageImperialAgeSeconds());

        // Villagers for peter1057285
        assertNotNull(loser.getVillagerProduction());
        assertEquals(19.0, loser.getVillagerProduction().getDarkAge());
        assertEquals(13.0, loser.getVillagerProduction().getFeudalAge());
        assertEquals(26.0, loser.getVillagerProduction().getCastleAge());
        assertEquals(16.0, loser.getVillagerProduction().getImperialAge());
        assertEquals(74.0, loser.getVillagerProduction().getTotal());

        // Town Centers for peter1057285
        final TownCenterSummaryDto tcs = loser.getAdditionalTownCenters();
        assertNotNull(tcs);
        assertEquals(0.0, tcs.getDarkAge());
        assertEquals(0.0, tcs.getFeudalAge());
        assertEquals(0.0, tcs.getCastleAge());
        assertEquals(0.0, tcs.getImperialAge());
        assertEquals(0.0, tcs.getTotal());

        // Military
        assertNotNull(loser.getMilitaryProduction());
        assertFalse(loser.getMilitaryProduction().isEmpty());

        // Landmarks
        assertNotNull(loser.getLandmarks());
        assertFalse(loser.getLandmarks().isEmpty());
    }

    @Test
    public void analyzeGamePlayers_KnightsTemplarMatch_223729268() {
        // when
        final List<PlayerGameAnalysisDto> players = analysisService.analyzeGamePlayers(knightsTemplarMatchNode);

        // then
        assertNotNull(players);
        assertEquals(4, players.size());

        final PlayerGameAnalysisDto p1 = players.stream()
                .filter(p -> "3861757".equals(p.getProfileId()))
                .findFirst()
                .orElse(null);
        assertNotNull(p1);
        assertEquals("GSUGambit", p1.getName());
        assertEquals("english", p1.getCivilization());
        assertEquals("win", p1.getResult());
        assertNotNull(p1.getAgeUpTimings());
        assertNotNull(p1.getVillagerProduction());
        assertNotNull(p1.getMilitaryProduction());
        assertNotNull(p1.getLandmarks());
    }

    @Test
    public void analyzeGamePlayers_NullOrEmptyNode() {
        // when & then
        assertTrue(analysisService.analyzeGamePlayers(null).isEmpty());
        assertTrue(analysisService.analyzeGamePlayers(objectMapper.createObjectNode()).isEmpty());
    }

    @Test
    public void analyzeSinglePlayerGame_NullNode() {
        assertNull(analysisService.analyzeSinglePlayerGame(null));
    }
}
