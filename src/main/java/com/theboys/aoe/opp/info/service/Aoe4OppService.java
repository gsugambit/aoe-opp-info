package com.theboys.aoe.opp.info.service;

import com.theboys.aoe.opp.info.dto.GamesPage;
import com.theboys.aoe.opp.info.dto.PlayerDto;
import com.theboys.aoe.opp.info.dto.internal.GameResult;
import com.theboys.aoe.opp.info.repository.http.Aoe4WorldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class Aoe4OppService {

    private final Aoe4WorldRepository aoe4WorldRepository;
    private final ObjectMapper objectMapper;
    @Value("${aoe4world.targetPlayerId}")
    private Long targetPlayerId;

    public void collect() {
        PlayerDto targetPlayer = aoe4WorldRepository.retrievePlayer(targetPlayerId);
        LOGGER.info("Retrieved target player: {}", targetPlayer);
        String gamesJson = aoe4WorldRepository.retrieveGames(targetPlayerId, List.of("rm_2v2"));
        LOGGER.info("Retrieved games JSON: {}", gamesJson);
        GamesPage gamesPage = objectMapper.readValue(gamesJson, GamesPage.class);
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

                        List<GameResult.ResultTeam> loserOpponents = opponentSlots.stream()
                                .filter(slots -> slots.stream().anyMatch(slot -> "loss".equals(slot.getPlayer().getResult())))
                                .map(slots -> {
                                    List<GameResult.ResultPlayer> losers = slots.stream().map(slot -> GameResult.ResultPlayer.from(slot.getPlayer())).toList();

                                    return GameResult.ResultTeam.builder().players(losers).build();
                                })
                                .collect(Collectors.toList());

                        if ("win".equals(targetTeamPlayer.getResult())) {
                            builder.winningTeam(targetTeam);
                        } else {
                            loserOpponents.add(targetTeam);

                            GameResult.ResultTeam winnerOpponent = opponentSlots.stream()
                                    .filter(slots -> slots.stream().anyMatch(slot -> "win".equals(slot.getPlayer().getResult())))
                                    .map(slots -> {
                                        List<GameResult.ResultPlayer> losers = slots.stream().map(slot -> GameResult.ResultPlayer.from(slot.getPlayer())).toList();

                                        return GameResult.ResultTeam.builder().players(losers).build();
                                    })
                                    .findAny().orElse(null);
                            builder.winningTeam(winnerOpponent);
                        }
                        builder.losingTeams(loserOpponents);
                        gameResults.add(builder.build());
                    }

                });

        //LOGGER.info("gameResults: {}", gameResults);

        // create output folder if does not exist
        String outputFolderName = "build/output";
        File outputFolder = new File(outputFolderName);
        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }

        //write file to root directory called game-results-DATE.json
        String fileName = outputFolderName + "/game-results-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss")) + ".json";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(gameResults));
        } catch (IOException e) {
            LOGGER.error("Error writing game results to file: {}", e.getMessage());
        }

    }

}
