package com.tournament.domain.repository.projection;

public interface PlayerStatsProjection {
    Long getPlayerId();
    String getUsername();
    int getEloRating();
    Integer getWins();
    Integer getLosses();
    Integer getTournamentsPlayed();
    Integer getVirtualPoints();
}
