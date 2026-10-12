CREATE TABLE IF NOT EXISTS game_summary (
    game_id VARCHAR(64) PRIMARY KEY,
    profile_id VARCHAR(64),
    gamer_name VARCHAR(128),
    leaderboard VARCHAR(64),
    summary_data JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_game_summary_profile_id ON game_summary (profile_id);
CREATE INDEX IF NOT EXISTS idx_game_summary_leaderboard ON game_summary (leaderboard);
CREATE INDEX IF NOT EXISTS idx_game_summary_data_gin ON game_summary USING gin (summary_data jsonb_path_ops);
