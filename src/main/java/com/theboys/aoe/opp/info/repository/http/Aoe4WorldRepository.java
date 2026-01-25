package com.theboys.aoe.opp.info.repository.http;

import com.theboys.aoe.opp.info.dto.PlayerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Aoe4WorldRepository {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Value("${aoe4world.baseUrl}")
    private String baseUrl;

    public void test() {

        var userUrl = baseUrl + "/api/v0/players/3861757";
        var gamesUrl = userUrl + "/games?page-0&limit=50";
        LOGGER.info("baseUrl: {}", userUrl);
        ResponseEntity<String> playerEntity = restTemplate.getForEntity(userUrl, String.class);
        LOGGER.info("playerEntity: {}", playerEntity.getBody());
        PlayerDto playerDTO = objectMapper.readValue(playerEntity.getBody(), PlayerDto.class);
        LOGGER.info("playerDTO: {}", playerDTO);
        ResponseEntity<String> gamesEntity = restTemplate.getForEntity(gamesUrl, String.class);
        LOGGER.info("gamesEntity: {}", gamesEntity.getBody());
    }

    public PlayerDto retrievePlayer(Long playerId) {
        var playerUrl = baseUrl + "/api/v0/players/" + playerId;
        return restTemplate.getForObject(playerUrl, PlayerDto.class);
    }

    public String retrieveGames(Long playerId, List<String> leaderboards) {
        String leaderboardParam = leaderboards.isEmpty() ? "" : "&leaderboard=" + String.join(",", leaderboards);
        var gamesUrl = baseUrl + "/api/v0/players/" + playerId + "/games?page-0&limit=50" + leaderboardParam;
        return restTemplate.getForObject(gamesUrl, String.class);
    }


}
