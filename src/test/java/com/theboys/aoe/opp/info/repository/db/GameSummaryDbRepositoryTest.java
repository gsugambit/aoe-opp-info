package com.theboys.aoe.opp.info.repository.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theboys.aoe.opp.info.dao.GameSummaryDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class GameSummaryDbRepositoryTest {

    @Autowired
    private GameSummaryDbRepository gameSummaryDbRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSaveAndFindByGameId() {
        // given
        final ObjectNode summaryJson = objectMapper.createObjectNode();
        summaryJson.put("gameId", "12345");
        summaryJson.put("duration", 1800);

        final GameSummaryDao dao = new GameSummaryDao();
        dao.setGameId("12345");
        dao.setProfileId("9999");
        dao.setGamerName("PlayerOne");
        dao.setLeaderboard("rm_1v1");
        dao.setSummaryData(summaryJson);
        dao.setCreatedAt(OffsetDateTime.now());
        dao.setUpdatedAt(OffsetDateTime.now());

        // when
        gameSummaryDbRepository.save(dao);
        final Optional<GameSummaryDao> result = gameSummaryDbRepository.findByGameId("12345");

        // then
        assertTrue(result.isPresent());
        assertEquals("12345", result.get().getGameId());
        assertEquals("9999", result.get().getProfileId());
        assertEquals("PlayerOne", result.get().getGamerName());
        assertEquals("rm_1v1", result.get().getLeaderboard());
        assertEquals(1800, result.get().getSummaryData().get("duration").asInt());
    }

    @Test
    public void testFindByProfileId() {
        // given
        final ObjectNode json1 = objectMapper.createObjectNode();
        json1.put("gameId", "game-1");

        final GameSummaryDao dao1 = new GameSummaryDao();
        dao1.setGameId("game-1");
        dao1.setProfileId("profile-abc");
        dao1.setGamerName("GamerA");
        dao1.setLeaderboard("rm_1v1");
        dao1.setSummaryData(json1);
        dao1.setCreatedAt(OffsetDateTime.now());
        dao1.setUpdatedAt(OffsetDateTime.now());

        final ObjectNode json2 = objectMapper.createObjectNode();
        json2.put("gameId", "game-2");

        final GameSummaryDao dao2 = new GameSummaryDao();
        dao2.setGameId("game-2");
        dao2.setProfileId("profile-abc");
        dao2.setGamerName("GamerA");
        dao2.setLeaderboard("rm_2v2");
        dao2.setSummaryData(json2);
        dao2.setCreatedAt(OffsetDateTime.now());
        dao2.setUpdatedAt(OffsetDateTime.now());

        gameSummaryDbRepository.save(dao1);
        gameSummaryDbRepository.save(dao2);

        // when
        final List<GameSummaryDao> results = gameSummaryDbRepository.findByProfileId("profile-abc");

        // then
        assertEquals(2, results.size());
    }
}
