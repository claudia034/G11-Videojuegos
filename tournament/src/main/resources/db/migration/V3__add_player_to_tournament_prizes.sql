ALTER TABLE tournament_prizes
    ADD COLUMN player_id BIGINT;

ALTER TABLE tournament_prizes
    ADD CONSTRAINT fk_tournament_prizes_player
        FOREIGN KEY (player_id) REFERENCES players (id);

CREATE INDEX idx_tournament_prizes_player_id
    ON tournament_prizes (player_id);
