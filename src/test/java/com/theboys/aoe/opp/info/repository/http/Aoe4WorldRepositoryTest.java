package com.theboys.aoe.opp.info.repository.http;

import com.theboys.aoe.opp.info.constants.Leaderboard;
import com.theboys.aoe.opp.info.dto.AutocompleteResponse;
import com.theboys.aoe.opp.info.dto.GamesPage;
import com.theboys.aoe.opp.info.dto.PlayerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class Aoe4WorldRepositoryTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private Aoe4WorldRepository aoe4WorldRepository;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(aoe4WorldRepository, "baseUrl", "https://aoe4world.com");
    }

    @Test
    public void testRetrievePlayer() {
        // given
        final Long playerId = 12345L;
        final String expectedUrl = "https://aoe4world.com/api/v0/players/12345";
        final PlayerDto playerDto = PlayerDto.builder().profileId(playerId).name("Test").build();

        when(restTemplate.getForObject(expectedUrl, PlayerDto.class)).thenReturn(playerDto);

        // when
        final PlayerDto result = aoe4WorldRepository.retrievePlayer(playerId);

        // then
        assertSame(playerDto, result);
        verify(restTemplate).getForObject(expectedUrl, PlayerDto.class);
    }

    @Test
    public void testRetrieveGames_AllParameters() {
        // given
        final Long playerId = 12345L;
        final Integer page = 2;
        final Integer perPage = 25;
        final List<String> leaderboards = List.of("rm_1v1", "qm_1v1");
        final String expectedUrl = "https://aoe4world.com/api/v0/players/12345/games?page=2&per_page=25&leaderboard=rm_1v1,qm_1v1";
        final GamesPage gamesPage = GamesPage.builder().build();

        when(restTemplate.getForObject(expectedUrl, GamesPage.class)).thenReturn(gamesPage);

        // when
        final GamesPage result = aoe4WorldRepository.retrieveGames(playerId, page, perPage, leaderboards);

        // then
        assertSame(gamesPage, result);
        verify(restTemplate).getForObject(expectedUrl, GamesPage.class);
    }

    @Test
    public void testRetrieveGames_NoOptionalParameters() {
        // given
        final Long playerId = 12345L;
        final String expectedUrl = "https://aoe4world.com/api/v0/players/12345/games";
        final GamesPage gamesPage = GamesPage.builder().build();

        when(restTemplate.getForObject(expectedUrl, GamesPage.class)).thenReturn(gamesPage);

        // when
        final GamesPage result = aoe4WorldRepository.retrieveGames(playerId, null, null, null);

        // then
        assertSame(gamesPage, result);
        verify(restTemplate).getForObject(expectedUrl, GamesPage.class);
    }

    @Test
    public void testRetrieveGames_OverloadedTwoArgs() {
        // given
        final Long playerId = 12345L;
        final List<String> leaderboards = List.of("rm_solo");
        final String expectedUrl = "https://aoe4world.com/api/v0/players/12345/games?leaderboard=rm_solo";
        final GamesPage gamesPage = GamesPage.builder().build();

        when(restTemplate.getForObject(expectedUrl, GamesPage.class)).thenReturn(gamesPage);

        // when
        final GamesPage result = aoe4WorldRepository.retrieveGames(playerId, leaderboards);

        // then
        assertSame(gamesPage, result);
        verify(restTemplate).getForObject(expectedUrl, GamesPage.class);
    }

    @Test
    public void testAutocomplete() {
        // given
        final String gamertag = "Lucifron";
        final Leaderboard leaderboard = Leaderboard.RM_1V1;
        final String expectedUrl = "https://aoe4world.com/api/v0/players/autocomplete?leaderboard={leaderboard}&query={query}";
        final AutocompleteResponse response = AutocompleteResponse.builder().query(gamertag).build();

        when(restTemplate.getForObject(
                eq(expectedUrl),
                eq(AutocompleteResponse.class),
                eq(Map.of("leaderboard", "rm_1v1", "query", gamertag))
        )).thenReturn(response);

        // when
        final AutocompleteResponse result = aoe4WorldRepository.autocomplete(gamertag, leaderboard);

        // then
        assertSame(response, result);
    }
}
