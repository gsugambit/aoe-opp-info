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

    public List<GameResult> collect(Long targetPlayerId) {
        PlayerDto targetPlayer = aoe4WorldRepository.retrievePlayer(targetPlayerId);
        LOGGER.info("Retrieved target player: {}", targetPlayer);
        GamesPage gamesPage = aoe4WorldRepository.retrieveGames(targetPlayerId, List.of("rm_2v2"));
        LOGGER.info("Retrieved games page: {}", gamesPage);

        List<GameResult> gameResults = new LinkedList<>();

        gamesPage.getGames()
                .forEach(game -> {
                    List<GamesPage.TeamPlayer> teamPlayers = game.playerSlot(targetPlayerId);
                    Optional<GamesPage.TeamPlayer> targetTeamPlayerOp = teamPlayers.stream().filter(player -> targetPlayerId.equals(player.getProfileId())).findAny();

                    if (targetTeamPlayerOp.isPresent()) {
                        GamesPage.TeamPlayer targetTeamPlayer = targetTeamPlayerOp.get();
                        List<List<GamesPage.TeamSlot>> opponentSlots = game.opponentSlot(targetPlayerId);


                        GameResult.GameResultBuilder builder = GameResult.builder()
                                .gameId(game.getGameId())
                                .map(game.getMap())
                                .avgMmr(game.getAverageMmr())
                                .duration(game.getDuration())
                                .startedAt(game.getStartedAt())
                                .server(game.getServer())
                                .avgRating(game.getAverageRating())
                                .season(game.getSeason())
                                .matchType(game.getKind());
                        List<GameResult.ResultPlayer> targetResultPlayers = teamPlayers.stream().map(GameResult.ResultPlayer::from).toList();
                        GameResult.ResultTeam targetTeam = GameResult.ResultTeam.builder().players(targetResultPlayers).build();

                        boolean targetTeamWon = teamPlayers.stream().anyMatch(player -> "win".equals(player.getResult()));
                        List<GameResult.ResultTeam> losingTeams = new LinkedList<>();

                        if (targetTeamWon) {
                            builder.winningTeam(targetTeam);
                        } else {
                            losingTeams.add(targetTeam);
                        }

                        opponentSlots.forEach(slots -> {
                            boolean opponentTeamWon = slots.stream().anyMatch(slot -> "win".equals(slot.getPlayer().getResult()));
                            List<GameResult.ResultPlayer> players = slots.stream().map(slot -> GameResult.ResultPlayer.from(slot.getPlayer())).toList();
                            GameResult.ResultTeam opponentTeam = GameResult.ResultTeam.builder().players(players).build();

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

    public AutocompleteResponseDto autocomplete(String gamertag, String leaderboard) {

        Leaderboard leader = Leaderboard.findByValue(leaderboard);

        if (leader == null) {
            throw new InvalidRequestException("leaderboard not valid: " + leaderboard);
        }

        AutocompleteResponse response = aoe4WorldRepository.autocomplete(gamertag, leader);
        return response.toDto();
    }

}
