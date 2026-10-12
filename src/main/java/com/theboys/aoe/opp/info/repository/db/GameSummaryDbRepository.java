package com.theboys.aoe.opp.info.repository.db;

import com.theboys.aoe.opp.info.dao.GameSummaryDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameSummaryDbRepository extends JpaRepository<GameSummaryDao, String> {

    Optional<GameSummaryDao> findByGameId(final String gameId);

    List<GameSummaryDao> findByProfileId(final String profileId);
}
