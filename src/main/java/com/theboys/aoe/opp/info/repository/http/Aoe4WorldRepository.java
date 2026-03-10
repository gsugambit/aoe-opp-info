package com.theboys.aoe.opp.info.repository.http;

import com.theboys.aoe.opp.info.constants.Leaderboard;
import com.theboys.aoe.opp.info.dto.AutocompleteResponse;
import com.theboys.aoe.opp.info.dto.GamesPage;
import com.theboys.aoe.opp.info.dto.PlayerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class Aoe4WorldRepository {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Value("${aoe4world.baseUrl}")
    private String baseUrl;

    public PlayerDto retrievePlayer(Long playerId) {
        var playerUrl = baseUrl + "/api/v0/players/" + playerId;
        return restTemplate.getForObject(playerUrl, PlayerDto.class);
    }

    public GamesPage retrieveGames(Long playerId, List<String> leaderboards) {
        String leaderboardParam = leaderboards.isEmpty() ? "" : "&leaderboard=" + String.join(",", leaderboards);
        var gamesUrl = baseUrl + "/api/v0/players/" + playerId + "/games?page-0&limit=50" + leaderboardParam;
        return restTemplate.getForObject(gamesUrl, GamesPage.class);
    }

    public AutocompleteResponse autocomplete(String gamertag, Leaderboard leaderboard) {
        var autocompleteUrl = baseUrl + "/api/v0/players/autocomplete?leaderboard={leaderboard}&query={query}";
        return restTemplate.getForObject(autocompleteUrl, AutocompleteResponse.class, Map.of("leaderboard", leaderboard.getApiValue(), "query", gamertag));
    }
}
