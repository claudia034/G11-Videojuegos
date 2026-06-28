package com.tournament.application.service;

import com.tournament.application.dto.response.MatchHistoryItemDto;
import com.tournament.application.dto.response.PlayerHistoryDto;
import com.tournament.application.dto.response.PlayerRankingDto;
import com.tournament.application.dto.response.PlayerStatsDto;
import com.tournament.application.event.MatchCompletedEvent;
import com.tournament.domain.entity.*;
import com.tournament.domain.enums.PrizeType;
import com.tournament.domain.repository.MatchRepository;
import com.tournament.domain.repository.MatchResultRepository;
import com.tournament.domain.repository.PlayerRepository;
import com.tournament.domain.repository.PlayerStatsRepository;
import com.tournament.domain.repository.RegistrationRepository;
import com.tournament.domain.enums.RegistrationStatus;
import com.tournament.application.event.TournamentStartedEvent;
import com.tournament.exception.PlayerNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlayerStatisticsService {

    private final PlayerRepository playerRepository;
    private final PlayerStatsRepository playerStatsRepository;
    private final MatchRepository matchRepository;
    private final MatchResultRepository matchResultRepository;
    private final EloCalculatorService eloCalculatorService;
    private final RegistrationRepository registrationRepository;

    @EventListener
    public void handleMatchCompleted(MatchCompletedEvent event) {
        log.info("Procesando estadísticas para la partida completada: {}", event.getMatchId());
        
        Match match = matchRepository.findById(event.getMatchId()).orElse(null);
        if (match == null || match.getWinner() == null) {
            return;
        }

        // Entregar premios si es la Gran Final
        if (match.getNextMatch() == null) {
            Tournament tournament = match.getRound().getBracket().getTournament();
            BigDecimal totalCashPrize = tournament.getPrizes().stream()
                    .filter(p -> p.getPrizeType() == PrizeType.CASH && p.getAmount() != null)
                    .map(TournamentPrize::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalCashPrize.compareTo(BigDecimal.ZERO) > 0) {
                List<Player> winners = extractPlayers(match.getWinner());
                int pointsPerPlayer = totalCashPrize.multiply(new BigDecimal(20)).intValue() / Math.max(1, winners.size());
                
                for (Player w : winners) {
                    PlayerStats winnerStats = playerStatsRepository.findByPlayerId(w.getId())
                            .orElseGet(() -> PlayerStats.builder().player(w).build());
                    winnerStats.addVirtualPoints(pointsPerPlayer);
                    playerStatsRepository.save(winnerStats);
                    log.info("Jugador {} ganó la final! Recibe {} puntos virtuales.", w.getUsername(), pointsPerPlayer);
                }
            }
        }

        Registration registration1 = match.getRegistration1();
        Registration registration2 = match.getRegistration2();
        if (registration1 == null || registration2 == null) return;

        boolean reg1Won = match.getWinner().getId().equals(registration1.getId());
        
        int r1Elo = registration1.getParticipantElo();
        int r2Elo = registration2.getParticipantElo();

        List<Player> team1Players = extractPlayers(registration1);
        List<Player> team2Players = extractPlayers(registration2);

        for (Player player : team1Players) {
            updatePlayerStats(player, r2Elo, reg1Won);
        }

        for (Player player : team2Players) {
            updatePlayerStats(player, r1Elo, !reg1Won);
        }
    }

    private void updatePlayerStats(Player player, int opponentElo, boolean won) {
        PlayerStats stats = playerStatsRepository.findByPlayerId(player.getId())
                .orElseGet(() -> PlayerStats.builder().player(player).build());

        int newElo = eloCalculatorService.calculateNewElo(player.getEloRating(), opponentElo, won);
        player.setEloRating(newElo);
        playerRepository.save(player);

        if (won) stats.incrementWins();
        else stats.incrementLosses();

        playerStatsRepository.save(stats);
        log.info("Actualizando stats para jugador {}: nuevo ELO {}, ganó: {}", player.getUsername(), newElo, won);
    }

    private List<Player> extractPlayers(Registration registration) {
        if (registration.isPlayerRegistration()) {
            return List.of(registration.getPlayer());
        }
        return registration.getTeam().getMembers().stream()
                .map(TeamMember::getPlayer)
                .collect(Collectors.toList());
    }

    @EventListener
    public void handleTournamentStarted(TournamentStartedEvent event) {
        log.info("Procesando inicio del torneo: {}", event.getTournamentId());
        
        List<Registration> registrations = registrationRepository
                .findByTournamentIdAndStatus(event.getTournamentId(), RegistrationStatus.CONFIRMED);
        
        for (Registration reg : registrations) {
            List<Player> players = extractPlayers(reg);

            for (Player player : players) {
                PlayerStats stats = playerStatsRepository.findByPlayerId(player.getId())
                        .orElseGet(() -> PlayerStats.builder().player(player).build());
                
                stats.incrementTournamentsPlayed();
                playerStatsRepository.save(stats);
                log.info("Se incrementaron los torneos jugados para el jugador: {}", player.getUsername());
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<PlayerRankingDto> getGlobalRanking(Pageable pageable) {
        return playerRepository.findAllByOrderByEloRatingDesc(pageable)
                .map(player -> {
                    PlayerStats stats = playerStatsRepository.findByPlayerId(player.getId()).orElse(null);

                    int wins = stats != null ? stats.getWins() : 0;
                    int losses = stats != null ? stats.getLosses() : 0;
                    int tp = stats != null ? stats.getTournamentsPlayed() : 0;

                    return PlayerRankingDto.builder()
                            .playerId(player.getId())
                            .username(player.getUsername())
                            .eloRating(player.getEloRating())
                            .wins(wins)
                            .losses(losses)
                            .tournamentsPlayed(tp)
                            .virtualPoints(stats != null ? stats.getVirtualPoints() : 0)
                            .build();
                });
    }

    @Transactional(readOnly = true)
    public PlayerStatsDto getPlayerStats(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
        PlayerStats stats = playerStatsRepository.findByPlayerId(playerId).orElse(null);
        
        int wins = stats != null ? stats.getWins() : 0;
        int losses = stats != null ? stats.getLosses() : 0;
        int tp = stats != null ? stats.getTournamentsPlayed() : 0;

        double winRate = (wins + losses) > 0 ? (double) wins / (wins + losses) * 100.0 : 0.0;

        return PlayerStatsDto.builder()
                .playerId(player.getId())
                .username(player.getUsername())
                .eloRating(player.getEloRating())
                .wins(wins)
                .losses(losses)
                .tournamentsPlayed(tp)
                .virtualPoints(stats != null ? stats.getVirtualPoints() : 0)
                .winRate(winRate)
                .build();
    }

    @Transactional(readOnly = true)
    public PlayerStatsDto getCurrentPlayerStats(Long userId) {
        Player player = playerRepository.findByUserId(userId)
                .orElseThrow(() -> new PlayerNotFoundException(userId));
        return getPlayerStats(player.getId());
    }

    @Transactional(readOnly = true)
    public PlayerHistoryDto getPlayerHistory(Long playerId, String gameName, String tournamentName) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));

        List<Match> allCompleted = matchRepository.findCompletedMatchesByPlayerAndFilters(playerId, gameName, tournamentName);

        List<MatchHistoryItemDto> items = new ArrayList<>();
        for (Match m : allCompleted) {
            MatchResult result = matchResultRepository.findByMatchId(m.getId()).orElse(null);
            if (result == null) continue;

            Registration myReg = getMyRegistration(m, playerId);
            Registration opponentReg = myReg.getId().equals(m.getRegistration1().getId()) ? m.getRegistration2() : m.getRegistration1();

            boolean won = m.getWinner().getId().equals(myReg.getId());

            int myScore = myReg.getId().equals(m.getRegistration1().getId()) ?
                    (result.getScore1() != null ? result.getScore1() : 0) :
                    (result.getScore2() != null ? result.getScore2() : 0);
            int opponentScore = opponentReg.getId().equals(m.getRegistration2().getId()) ?
                    (result.getScore2() != null ? result.getScore2() : 0) :
                    (result.getScore1() != null ? result.getScore1() : 0);

            items.add(MatchHistoryItemDto.builder()
                    .matchId(m.getId())
                    .tournamentName(m.getRound().getBracket().getTournament().getName())
                    .opponentName(opponentReg.getParticipantName())
                    .myScore(myScore)
                    .opponentScore(opponentScore)
                    .won(won)
                    .completedAt(result.getResolvedAt() != null ? result.getResolvedAt() : result.getSubmittedAt())
                    .build());
        }

        // Sort descendiente
        items.sort((a, b) -> b.getCompletedAt().compareTo(a.getCompletedAt()));

        return PlayerHistoryDto.builder()
                .playerId(player.getId())
                .username(player.getUsername())
                .matches(items)
                .build();
    }

    private boolean isPlayerInMatch(Match m, Long playerId) {
        return (m.getRegistration1() != null && extractPlayers(m.getRegistration1())
                        .stream().anyMatch(p -> p.getId().equals(playerId))) ||
               (m.getRegistration2() != null && extractPlayers(m.getRegistration2())
                        .stream().anyMatch(p -> p.getId().equals(playerId)));
    }

    private Registration getMyRegistration(Match m, Long playerId) {
        if (m.getRegistration1() != null && extractPlayers(m.getRegistration1())
                .stream().anyMatch(p -> p.getId().equals(playerId))) {
            return m.getRegistration1();
        }
        return m.getRegistration2();
    }
}
