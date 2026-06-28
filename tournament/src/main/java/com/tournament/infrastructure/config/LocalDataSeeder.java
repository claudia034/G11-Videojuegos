package com.tournament.infrastructure.config;

import com.tournament.domain.entity.Player;
import com.tournament.domain.entity.PlayerStats;
import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentPrize;
import com.tournament.domain.entity.User;
import com.tournament.domain.enums.PrizeType;
import com.tournament.domain.enums.TournamentFormat;
import com.tournament.domain.enums.TournamentStatus;
import com.tournament.domain.enums.UserRole;
import com.tournament.domain.repository.PlayerRepository;
import com.tournament.domain.repository.PlayerStatsRepository;
import com.tournament.domain.repository.TournamentRepository;
import com.tournament.domain.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("local")
@RequiredArgsConstructor
public class LocalDataSeeder {

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final PlayerStatsRepository playerStatsRepository;
    private final TournamentRepository tournamentRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedLocalData() {
        return args -> {
            if (userRepository.count() > 0 || tournamentRepository.count() > 0) {
                return;
            }

            User admin = userRepository.save(User.builder()
                    .email("admin@nexus.gg")
                    .passwordHash(passwordEncoder.encode("demo12345"))
                    .role(UserRole.ADMIN)
                    .build());

            User playerOneUser = userRepository.save(User.builder()
                    .email("xshadow99@nexus.gg")
                    .passwordHash(passwordEncoder.encode("demo12345"))
                    .role(UserRole.PLAYER)
                    .build());

            User organizer = userRepository.save(User.builder()
                    .email("organizer@nexus.gg")
                    .passwordHash(passwordEncoder.encode("demo12345"))
                    .role(UserRole.ORGANIZER)
                    .build());

            User playerTwoUser = userRepository.save(User.builder()
                    .email("novax@nexus.gg")
                    .passwordHash(passwordEncoder.encode("demo12345"))
                    .role(UserRole.PLAYER)
                    .build());

            User playerThreeUser = userRepository.save(User.builder()
                    .email("valkyr@nexus.gg")
                    .passwordHash(passwordEncoder.encode("demo12345"))
                    .role(UserRole.PLAYER)
                    .build());

            User playerFourUser = userRepository.save(User.builder()
                    .email("riftking@nexus.gg")
                    .passwordHash(passwordEncoder.encode("demo12345"))
                    .role(UserRole.PLAYER)
                    .build());

            Player shadow = playerRepository.save(Player.builder()
                    .username("xShadow99")
                    .userId(playerOneUser.getId())
                    .eloRating(1847)
                    .build());

            Player nova = playerRepository.save(Player.builder()
                    .username("NovaX")
                    .userId(playerTwoUser.getId())
                    .eloRating(1765)
                    .build());

            Player valkyr = playerRepository.save(Player.builder()
                    .username("Valkyr")
                    .userId(playerThreeUser.getId())
                    .eloRating(1692)
                    .build());

            Player rift = playerRepository.save(Player.builder()
                    .username("RiftKing")
                    .userId(playerFourUser.getId())
                    .eloRating(1610)
                    .build());

            playerStatsRepository.save(PlayerStats.builder()
                    .player(shadow)
                    .wins(18)
                    .losses(7)
                    .tournamentsPlayed(12)
                    .virtualPoints(2450)
                    .build());

            playerStatsRepository.save(PlayerStats.builder()
                    .player(nova)
                    .wins(14)
                    .losses(8)
                    .tournamentsPlayed(11)
                    .virtualPoints(1900)
                    .build());

            playerStatsRepository.save(PlayerStats.builder()
                    .player(valkyr)
                    .wins(11)
                    .losses(9)
                    .tournamentsPlayed(10)
                    .virtualPoints(1500)
                    .build());

            playerStatsRepository.save(PlayerStats.builder()
                    .player(rift)
                    .wins(9)
                    .losses(10)
                    .tournamentsPlayed(9)
                    .virtualPoints(1200)
                    .build());

            tournamentRepository.save(buildTournament(
                    admin,
                    "Night Cup #12",
                    "Bracket semanal para la comunidad competitiva.",
                    "Valorant",
                    TournamentFormat.SINGLE_ELIMINATION,
                    TournamentStatus.REGISTRATION_OPEN,
                    16,
                    120,
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(3),
                    LocalDateTime.now().plusDays(5)
            ));

            tournamentRepository.save(buildTournament(
                    organizer,
                    "Nexus Masters",
                    "Serie principal con jugadores de alto ELO.",
                    "League of Legends",
                    TournamentFormat.DOUBLE_ELIMINATION,
                    TournamentStatus.IN_PROGRESS,
                    8,
                    300,
                    LocalDateTime.now().minusDays(10),
                    LocalDateTime.now().minusDays(2),
                    LocalDateTime.now().minusHours(6)
            ));

            tournamentRepository.save(buildTournament(
                    organizer,
                    "Indie Clash",
                    "Torneo experimental para la proxima temporada.",
                    "Rocket League",
                    TournamentFormat.ROUND_ROBIN,
                    TournamentStatus.DRAFT,
                    12,
                    80,
                    LocalDateTime.now().plusDays(5),
                    LocalDateTime.now().plusDays(10),
                    LocalDateTime.now().plusDays(12)
            ));
        };
    }

    private Tournament buildTournament(
            User organizer,
            String name,
            String description,
            String gameName,
            TournamentFormat format,
            TournamentStatus status,
            int maxParticipants,
            int prizeAmount,
            LocalDateTime registrationStartAt,
            LocalDateTime registrationEndAt,
            LocalDateTime startAt
    ) {
        Tournament tournament = Tournament.builder()
                .name(name)
                .description(description)
                .gameName(gameName)
                .format(format)
                .status(status)
                .maxParticipants(maxParticipants)
                .teamBased(false)
                .minElo(1000)
                .maxElo(2500)
                .organizer(organizer)
                .registrationStartAt(registrationStartAt)
                .registrationEndAt(registrationEndAt)
                .startAt(startAt)
                .endAt(startAt.plusDays(1))
                .build();

        tournament.addPrize(TournamentPrize.builder()
                .position(1)
                .name("Bolsa principal")
                .description("Premio del primer lugar")
                .prizeType(PrizeType.CASH)
                .amount(BigDecimal.valueOf(prizeAmount))
                .currency("USD")
                .build());

        return tournament;
    }
}
