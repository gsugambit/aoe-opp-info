package com.theboys.aoe.opp.info.repository.http;

import com.theboys.aoe.opp.info.constants.Leaderboard;
import com.theboys.aoe.opp.info.dto.AutocompleteResponse;
import com.theboys.aoe.opp.info.dto.GamesPage;
import com.theboys.aoe.opp.info.dto.PlayerDto;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
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
    @Value("${aoe4world.apiKey:}")
    private String apiKey;

    public PlayerDto retrievePlayer(final Long playerId) {
        final String playerUrl = baseUrl + "/api/v0/players/" + playerId;
        return restTemplate.getForObject(playerUrl, PlayerDto.class);
    }

    public GamesPage retrieveGames(
            final Long playerId,
            final Integer page,
            final Integer perPage,
            final List<String> leaderboards
    ) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(baseUrl + "/api/v0/players/" + playerId + "/games");

        if (page != null) {
            uriBuilder.queryParam("page", page);
        }

        if (perPage != null) {
            uriBuilder.queryParam("per_page", perPage);
        }

        if (leaderboards != null && !leaderboards.isEmpty()) {
            uriBuilder.queryParam("leaderboard", String.join(",", leaderboards));
        }
        if (StringUtils.isNotBlank(apiKey)) {
            uriBuilder.queryParam("api_key", apiKey);
        }

        final String gamesUrl = uriBuilder.toUriString();
        LOGGER.debug("Retrieving games from {}", gamesUrl);
        return restTemplate.getForObject(gamesUrl, GamesPage.class);
    }

    public GamesPage retrieveGames(final Long playerId, final List<String> leaderboards) {
        return retrieveGames(playerId, null, null, leaderboards);
    }

    public AutocompleteResponse autocomplete(final String gamertag, final Leaderboard leaderboard) {
        final String autocompleteUrl = baseUrl + "/api/v0/players/autocomplete?leaderboard={leaderboard}&query={query}";
        return restTemplate.getForObject(
                autocompleteUrl,
                AutocompleteResponse.class,
                Map.of("leaderboard", leaderboard.getApiValue(), "query", gamertag)
        );
    }
}
