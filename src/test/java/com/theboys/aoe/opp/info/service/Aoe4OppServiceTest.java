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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
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
        Long targetPlayerId = 123L;
        Long opponentPlayerId = 456L;

        PlayerDto targetPlayerDto = PlayerDto.builder()
                .profileId(targetPlayerId)
                .name("TargetPlayer")
                .build();

        GamesPage.TeamPlayer targetTeamPlayer = GamesPage.TeamPlayer.builder()
                .profileId(targetPlayerId)
                .name("TargetPlayer")
                .result("null") // Target says null
                .build();

        GamesPage.TeamPlayer opponentTeamPlayer = GamesPage.TeamPlayer.builder()
                .profileId(opponentPlayerId)
                .name("OpponentPlayer")
                .result("null") // Opponent also says null
                .build();

        GamesPage.TeamSlot targetSlot = GamesPage.TeamSlot.builder().player(targetTeamPlayer).build();
        GamesPage.TeamSlot opponentSlot = GamesPage.TeamSlot.builder().player(opponentTeamPlayer).build();

        GamesPage.Game game = GamesPage.Game.builder()
                .gameId(1L)
                .teams(List.of(List.of(targetSlot), List.of(opponentSlot)))
                .build();

        GamesPage gamesPage = GamesPage.builder()
                .games(List.of(game))
                .build();

        when(aoe4WorldRepository.retrievePlayer(targetPlayerId)).thenReturn(targetPlayerDto);
        when(aoe4WorldRepository.retrieveGames(anyLong(), anyList())).thenReturn(gamesPage);

        List<GameResult> results = aoe4OppService.collect(targetPlayerId);

        assertEquals(1, results.size());
        GameResult result = results.get(0);

        System.out.println("[DEBUG_LOG] Result: " + result);

        assertNull(result.getWinningTeam(), "Winning team should be null if no one won");
        // We expect BOTH teams to be in losingTeams if it's a "no winner" situation
        assertEquals(2, result.getLosingTeams().size(), "Both teams should be in losing teams");
    }

    @Test
    public void testCollect_TeammateWins_TargetNull() {
        Long targetPlayerId = 123L;
        Long opponentPlayerId = 456L;

        PlayerDto targetPlayerDto = PlayerDto.builder()
                .profileId(targetPlayerId)
                .name("TargetPlayer")
                .build();

        GamesPage.TeamPlayer targetTeamPlayer = GamesPage.TeamPlayer.builder()
                .profileId(targetPlayerId)
                .name("TargetPlayer")
                .result("null") // Target says null
                .build();

        GamesPage.TeamPlayer targetTeammate = GamesPage.TeamPlayer.builder()
                .profileId(targetPlayerId + 1)
                .name("TargetTeammate")
                .result("win") // Teammate won!
                .build();

        GamesPage.TeamPlayer opponentTeamPlayer = GamesPage.TeamPlayer.builder()
                .profileId(opponentPlayerId)
                .name("OpponentPlayer")
                .result("loss") // Opponent lost
                .build();

        GamesPage.TeamSlot targetSlot = GamesPage.TeamSlot.builder().player(targetTeamPlayer).build();
        GamesPage.TeamSlot teammateSlot = GamesPage.TeamSlot.builder().player(targetTeammate).build();
        GamesPage.TeamSlot opponentSlot = GamesPage.TeamSlot.builder().player(opponentTeamPlayer).build();

        GamesPage.Game game = GamesPage.Game.builder()
                .gameId(1L)
                .teams(List.of(List.of(targetSlot, teammateSlot), List.of(opponentSlot)))
                .build();

        GamesPage gamesPage = GamesPage.builder()
                .games(List.of(game))
                .build();

        when(aoe4WorldRepository.retrievePlayer(targetPlayerId)).thenReturn(targetPlayerDto);
        when(aoe4WorldRepository.retrieveGames(anyLong(), anyList())).thenReturn(gamesPage);

        List<GameResult> results = aoe4OppService.collect(targetPlayerId);

        assertEquals(1, results.size());
        GameResult result = results.get(0);

        System.out.println("[DEBUG_LOG] Result: " + result);

        assertNotNull(result.getWinningTeam(), "Winning team should not be null");
        assertTrue(result.getWinningTeam().getPlayers().stream().anyMatch(p -> p.getProfileId().equals(targetPlayerId)), "Target team should be the winner");
        assertEquals(1, result.getLosingTeams().size(), "Only opponent team should be in losing teams");
    }
}
