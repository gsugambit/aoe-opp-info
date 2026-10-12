package com.theboys.aoe.opp.info.dao;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "game_summary")
public class GameSummaryDao {

    @Id
    @Column(name = "game_id", nullable = false, length = 64)
    private String gameId;

    @Column(name = "profile_id", length = 64)
    private String profileId;

    @Column(name = "gamer_name", length = 128)
    private String gamerName;

    @Column(name = "leaderboard", length = 64)
    private String leaderboard;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "summary_data", nullable = false, columnDefinition = "jsonb")
    private JsonNode summaryData;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
