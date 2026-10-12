package com.theboys.aoe.opp.info.service;

import com.theboys.aoe.opp.info.constants.Leaderboard;
import com.theboys.aoe.opp.info.dto.AutocompleteResponse;
import com.theboys.aoe.opp.info.dto.AutocompleteResponseDto;
import com.theboys.aoe.opp.info.dto.GamesPage;
import com.theboys.aoe.opp.info.dto.PlayerDto;
import com.theboys.aoe.opp.info.dto.internal.GameResult;
import com.theboys.aoe.opp.info.exception.InvalidRequestException;
import com.theboys.aoe.opp.info.repository.http.Aoe4WorldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class Aoe4OppService {

    private final Aoe4WorldRepository aoe4WorldRepository;
    private final ObjectMapper objectMapper;

    public List<GameResult> collect(final Long targetPlayerId) {
        return collect(targetPlayerId, null, null, List.of());
    }

    public List<GameResult> collect(
            final Long targetPlayerId,
            final Integer page,
            final Integer perPage,
            final List<String> leaderboards
    ) {
        LOGGER.debug("Collecting games for player {} for leaderboards {}", targetPlayerId, leaderboards);
        final PlayerDto targetPlayer = aoe4WorldRepository.retrievePlayer(targetPlayerId);
        //LOGGER.info("Retrieved target player: {}", targetPlayer);
        final List<String> effectiveLeaderboards = (leaderboards == null || leaderboards.isEmpty())
                ? List.of()
                : leaderboards;
        final GamesPage gamesPage = aoe4WorldRepository.retrieveGames(
                targetPlayerId, page, perPage, effectiveLeaderboards
        );
        //LOGGER.info("Retrieved games page: {}", gamesPage);

        final List<GameResult> gameResults = new LinkedList<>();

        gamesPage.getGames().forEach(game -> {
            final List<GamesPage.TeamPlayer> teamPlayers = game.playerSlot(targetPlayerId);
            final Optional<GamesPage.TeamPlayer> targetTeamPlayerOp = teamPlayers.stream()
                    .filter(player -> targetPlayerId.equals(player.getProfileId()))
                    .findAny();

            if (targetTeamPlayerOp.isPresent()) {
                final List<List<GamesPage.TeamSlot>> opponentSlots = game.opponentSlot(targetPlayerId);

                final GameResult.GameResultBuilder builder = GameResult.builder()
                        .gameId(game.getGameId())
                        .map(game.getMap())
                        .avgMmr(game.getAverageMmr())
                        .duration(game.getDuration())
                        .startedAt(game.getStartedAt())
                        .server(game.getServer())
                        .avgRating(game.getAverageRating())
                        .season(game.getSeason())
                        .matchType(game.getKind());
                final List<GameResult.ResultPlayer> targetResultPlayers = teamPlayers.stream()
                        .map(GameResult.ResultPlayer::from)
                        .toList();
                final GameResult.ResultTeam targetTeam = GameResult.ResultTeam.builder()
                        .players(targetResultPlayers)
                        .build();

                final boolean targetTeamWon = teamPlayers.stream()
                        .anyMatch(player -> "win".equals(player.getResult()));
                final List<GameResult.ResultTeam> losingTeams = new LinkedList<>();

                if (targetTeamWon) {
                    builder.winningTeam(targetTeam);
                } else {
                    losingTeams.add(targetTeam);
                }

                opponentSlots.forEach(slots -> {
                    final boolean opponentTeamWon = slots.stream()
                            .anyMatch(slot -> "win".equals(slot.getPlayer().getResult()));
                    final List<GameResult.ResultPlayer> players = slots.stream()
                            .map(slot -> GameResult.ResultPlayer.from(slot.getPlayer()))
                            .toList();
                    final GameResult.ResultTeam opponentTeam = GameResult.ResultTeam.builder()
                            .players(players)
                            .build();

                    if (opponentTeamWon) {
                        builder.winningTeam(opponentTeam);
                    } else {
                        losingTeams.add(opponentTeam);
                    }
                });

                builder.losingTeams(losingTeams);
                gameResults.add(builder.build());
            }
        });

        return gameResults;
    }

    public GamesPage retrieveGames(
            final Long targetPlayerId,
            final Integer page,
            final Integer perPage,
            final List<String> leaderboards
    ) {
        return aoe4WorldRepository.retrieveGames(targetPlayerId, page, perPage, leaderboards);
    }

    public AutocompleteResponseDto autocomplete(final String gamertag, final String leaderboard) {
        final Leaderboard leader = Leaderboard.findByValue(leaderboard);

        if (leader == null) {
            throw new InvalidRequestException("leaderboard not valid: " + leaderboard);
        }

        final AutocompleteResponse response = aoe4WorldRepository.autocomplete(gamertag, leader);
        return response.toDto();
    }

}
