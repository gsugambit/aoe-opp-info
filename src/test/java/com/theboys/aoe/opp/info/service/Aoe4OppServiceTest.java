package com.theboys.aoe.opp.info.service;

import com.theboys.aoe.opp.info.dto.GamesPage;
import com.theboys.aoe.opp.info.dto.PlayerDto;
import com.theboys.aoe.opp.info.dto.internal.GameResult;
import com.theboys.aoe.opp.info.repository.http.Aoe4WorldRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class Aoe4OppServiceTest {

    @Mock
    private Aoe4WorldRepository aoe4WorldRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private Aoe4OppService aoe4OppService;

    @Test
    public void testCollect_NoWinner_BothNull() {
        // given
        final Long targetPlayerId = 123L;
        final Long opponentPlayerId = 456L;

        final PlayerDto targetPlayerDto = PlayerDto.builder()
                .profileId(targetPlayerId)
                .name("TargetPlayer")
                .build();

        final GamesPage.TeamPlayer targetTeamPlayer = GamesPage.TeamPlayer.builder()
                .profileId(targetPlayerId)
                .name("TargetPlayer")
                .result("null")
                .build();

        final GamesPage.TeamPlayer opponentTeamPlayer = GamesPage.TeamPlayer.builder()
                .profileId(opponentPlayerId)
                .name("OpponentPlayer")
                .result("null")
                .build();

        final GamesPage.TeamSlot targetSlot = GamesPage.TeamSlot.builder().player(targetTeamPlayer).build();
        final GamesPage.TeamSlot opponentSlot = GamesPage.TeamSlot.builder().player(opponentTeamPlayer).build();

        final GamesPage.Game game = GamesPage.Game.builder()
                .gameId(1L)
                .teams(List.of(List.of(targetSlot), List.of(opponentSlot)))
                .build();

        final GamesPage gamesPage = GamesPage.builder()
                .games(List.of(game))
                .build();

        when(aoe4WorldRepository.retrievePlayer(targetPlayerId)).thenReturn(targetPlayerDto);
        when(aoe4WorldRepository.retrieveGames(anyLong(), any(), any(), anyList())).thenReturn(gamesPage);

        // when
        final List<GameResult> results = aoe4OppService.collect(targetPlayerId);

        // then
        assertEquals(1, results.size());
        final GameResult result = results.get(0);

        assertNull(result.getWinningTeam(), "Winning team should be null if no one won");
        assertEquals(2, result.getLosingTeams().size(), "Both teams should be in losing teams");
    }

    @Test
    public void testCollect_TeammateWins_TargetNull() {
        // given
        final Long targetPlayerId = 123L;
        final Long opponentPlayerId = 456L;

        final PlayerDto targetPlayerDto = PlayerDto.builder()
                .profileId(targetPlayerId)
                .name("TargetPlayer")
                .build();

        final GamesPage.TeamPlayer targetTeamPlayer = GamesPage.TeamPlayer.builder()
                .profileId(targetPlayerId)
                .name("TargetPlayer")
                .result("null")
                .build();

        final GamesPage.TeamPlayer targetTeammate = GamesPage.TeamPlayer.builder()
                .profileId(targetPlayerId + 1)
                .name("TargetTeammate")
                .result("win")
                .build();

        final GamesPage.TeamPlayer opponentTeamPlayer = GamesPage.TeamPlayer.builder()
                .profileId(opponentPlayerId)
                .name("OpponentPlayer")
                .result("loss")
                .build();

        final GamesPage.TeamSlot targetSlot = GamesPage.TeamSlot.builder().player(targetTeamPlayer).build();
        final GamesPage.TeamSlot teammateSlot = GamesPage.TeamSlot.builder().player(targetTeammate).build();
        final GamesPage.TeamSlot opponentSlot = GamesPage.TeamSlot.builder().player(opponentTeamPlayer).build();

        final GamesPage.Game game = GamesPage.Game.builder()
                .gameId(1L)
                .teams(List.of(List.of(targetSlot, teammateSlot), List.of(opponentSlot)))
                .build();

        final GamesPage gamesPage = GamesPage.builder()
                .games(List.of(game))
                .build();

        when(aoe4WorldRepository.retrievePlayer(targetPlayerId)).thenReturn(targetPlayerDto);
        when(aoe4WorldRepository.retrieveGames(anyLong(), any(), any(), anyList())).thenReturn(gamesPage);

        // when
        final List<GameResult> results = aoe4OppService.collect(targetPlayerId);

        // then
        assertEquals(1, results.size());
        final GameResult result = results.get(0);

        assertNotNull(result.getWinningTeam(), "Winning team should not be null");
        assertTrue(
                result.getWinningTeam().getPlayers().stream().anyMatch(p -> p.getProfileId().equals(targetPlayerId)),
                "Target team should be the winner"
        );
        assertEquals(1, result.getLosingTeams().size(), "Only opponent team should be in losing teams");
    }

    @Test
    public void testCollect_WithPaginationAndLeaderboard() {
        // given
        final Long targetPlayerId = 123L;
        final Integer page = 1;
        final Integer perPage = 25;
        final List<String> leaderboards = List.of("rm_1v1", "qm_1v1");

        final PlayerDto targetPlayerDto = PlayerDto.builder()
                .profileId(targetPlayerId)
                .name("TargetPlayer")
                .build();

        final GamesPage gamesPage = GamesPage.builder()
                .games(List.of())
                .build();

        when(aoe4WorldRepository.retrievePlayer(targetPlayerId)).thenReturn(targetPlayerDto);
        when(aoe4WorldRepository.retrieveGames(targetPlayerId, page, perPage, leaderboards)).thenReturn(gamesPage);

        // when
        final List<GameResult> results = aoe4OppService.collect(targetPlayerId, page, perPage, leaderboards);

        // then
        assertTrue(results.isEmpty());
        verify(aoe4WorldRepository).retrieveGames(targetPlayerId, page, perPage, leaderboards);
    }

    @Test
    public void testRetrieveGames() {
        // given
        final Long targetPlayerId = 123L;
        final Integer page = 2;
        final Integer perPage = 10;
        final List<String> leaderboards = List.of("qm_2v2", "qm_3v3");
        final GamesPage gamesPage = GamesPage.builder().page(page).perPage(perPage).build();

        when(aoe4WorldRepository.retrieveGames(targetPlayerId, page, perPage, leaderboards)).thenReturn(gamesPage);

        // when
        final GamesPage result = aoe4OppService.retrieveGames(targetPlayerId, page, perPage, leaderboards);

        // then
        assertSame(gamesPage, result);
        verify(aoe4WorldRepository).retrieveGames(targetPlayerId, page, perPage, leaderboards);
    }
}
