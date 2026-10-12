package com.theboys.aoe.opp.info.controller;

import com.theboys.aoe.opp.info.dto.AutocompleteResponseDto;
import com.theboys.aoe.opp.info.dto.GamesPage;
import com.theboys.aoe.opp.info.dto.internal.GameResult;
import com.theboys.aoe.opp.info.service.Aoe4OppService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlayerControllerTest {

    @Mock
    private Aoe4OppService aoe4OppService;

    @InjectMocks
    private PlayerController playerController;

    @Test
    public void testGetGameInfo() {
        // given
        final Long playerId = 12345L;
        final Integer page = 1;
        final Integer perPage = 20;
        final List<String> leaderboards = List.of("rm_1v1");
        final List<GameResult> gameResults = List.of(GameResult.builder().gameId(1L).build());

        when(aoe4OppService.collect(playerId, page, perPage, leaderboards)).thenReturn(gameResults);

        // when
        final List<GameResult> result = playerController.test(playerId, page, perPage, leaderboards, null);

        // then
        assertSame(gameResults, result);
        verify(aoe4OppService).collect(playerId, page, perPage, leaderboards);
    }

    @Test
    public void testRetrieveGames() {
        // given
        final Long playerId = 12345L;
        final Integer page = 2;
        final Integer perPage = 50;
        final List<String> leaderboards = List.of("rm_1v1", "qm_1v1");
        final GamesPage gamesPage = GamesPage.builder().page(page).perPage(perPage).build();

        when(aoe4OppService.retrieveGames(playerId, page, perPage, leaderboards)).thenReturn(gamesPage);

        // when
        final GamesPage result = playerController.retrieveGames(playerId, page, perPage, null, leaderboards);

        // then
        assertSame(gamesPage, result);
        verify(aoe4OppService).retrieveGames(playerId, page, perPage, leaderboards);
    }

    @Test
    public void testSearchPlayer() {
        // given
        final String gamertag = "Lucifron";
        final String leaderboard = "1V1";
        final AutocompleteResponseDto responseDto = AutocompleteResponseDto.builder().query(gamertag).build();

        when(aoe4OppService.autocomplete(gamertag, leaderboard)).thenReturn(responseDto);

        // when
        final AutocompleteResponseDto result = playerController.searchPlayer(gamertag, leaderboard);

        // then
        assertSame(responseDto, result);
        verify(aoe4OppService).autocomplete(gamertag, leaderboard);
    }
}
