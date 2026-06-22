ALTER TABLE tournaments
    ADD COLUMN is_team_based BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE tournaments
    ADD COLUMN min_elo INTEGER;

ALTER TABLE tournaments
    ADD COLUMN max_elo INTEGER;

ALTER TABLE tournaments
    ADD COLUMN organizer_id BIGINT;

ALTER TABLE tournaments
    ADD CONSTRAINT chk_tournaments_min_elo
        CHECK (min_elo IS NULL OR min_elo >= 0);

ALTER TABLE tournaments
    ADD CONSTRAINT chk_tournaments_max_elo
        CHECK (max_elo IS NULL OR max_elo >= 0);

ALTER TABLE tournaments
    ADD CONSTRAINT chk_tournaments_elo_range
        CHECK (min_elo IS NULL OR max_elo IS NULL OR min_elo <= max_elo);

CREATE INDEX idx_tournaments_organizer_id
    ON tournaments (organizer_id);
