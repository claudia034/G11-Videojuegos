package com.tournament.application.service;

import com.tournament.application.dto.response.MatchDetailResponse;
import com.tournament.application.dto.response.TournamentManagementSummaryDto;
import com.tournament.domain.entity.Match;
import com.tournament.domain.entity.MatchResult;
import com.tournament.domain.entity.Tournament;
import com.tournament.domain.enums.MatchStatus;
import com.tournament.domain.enums.RegistrationStatus;
import com.tournament.domain.repository.MatchRepository;
import com.tournament.domain.repository.MatchResultRepository;
import com.tournament.domain.repository.RegistrationRepository;
import com.tournament.domain.repository.TournamentRepository;
import com.tournament.exception.TournamentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentManagementService {

    private final TournamentRepository tournamentRepository;
    private final RegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;
    private final MatchResultRepository matchResultRepository;

    @Transactional(readOnly = true)
    public TournamentManagementSummaryDto getSummary(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        int confirmedParticipants = registrationRepository
                .findByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED)
                .size();
        int pendingParticipants = registrationRepository
                .findByTournamentIdAndStatus(tournamentId, RegistrationStatus.PENDING)
                .size();

        List<Match> allMatches = matchRepository.findAllByTournamentId(tournamentId);
        List<Match> disputedMatches = matchRepository.findByTournamentIdAndStatus(tournamentId, MatchStatus.DISPUTED);

        List<MatchDetailResponse> disputedDetails = disputedMatches.stream()
                .map(match -> {
                    MatchResult result = matchResultRepository.findByMatchId(match.getId()).orElse(null);
                    return MatchDetailResponse.from(match, result);
                })
                .toList();

        return TournamentManagementSummaryDto.builder()
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .confirmedParticipants(confirmedParticipants)
                .pendingParticipants(pendingParticipants)
                .totalParticipants(confirmedParticipants + pendingParticipants)
                .scheduledMatches((int) allMatches.stream().filter(match -> match.getStatus() == MatchStatus.SCHEDULED).count())
                .completedMatches((int) allMatches.stream().filter(match -> match.getStatus() == MatchStatus.COMPLETED).count())
                .disputedMatches(disputedMatches.size())
                .disputedMatchDetails(disputedDetails)
                .build();
    }
}
