package com.tournament.application.service;

import com.tournament.application.dto.request.AdminMatchDecisionRequest;
import com.tournament.domain.entity.Match;
import com.tournament.domain.entity.MatchResult;
import com.tournament.domain.entity.Registration;
import com.tournament.domain.entity.ResultEvidence;

import java.time.LocalDateTime;

final class MatchAdminSupport {

    private MatchAdminSupport() {
    }

    static MatchResult buildOrUpdateAdministrativeResult(
            Match match,
            MatchResult existingResult,
            Registration winner,
            AdminMatchDecisionRequest request,
            Long adminUserId
    ) {
        MatchResult result = existingResult != null ? existingResult : MatchResult.builder()
                .match(match)
                .reportedByUserId(adminUserId)
                .submittedAt(LocalDateTime.now())
                .build();

        result.setWinner(winner);
        result.setScore1(request.getScore1());
        result.setScore2(request.getScore2());
        result.setReportedByUserId(adminUserId);
        result.setSubmittedAt(result.getSubmittedAt() != null ? result.getSubmittedAt() : LocalDateTime.now());
        result.setAdminNotes(request.getAdminNotes());
        result.setResolvedByUserId(adminUserId);
        result.setResolvedAt(LocalDateTime.now());
        result.setDisputeReason(null);
        result.setDisputeRaisedByUserId(null);

        if (request.getEvidenceUrl() != null && !request.getEvidenceUrl().isBlank()) {
            ResultEvidence evidence = ResultEvidence.builder()
                    .matchResult(result)
                    .evidenceUrl(request.getEvidenceUrl())
                    .uploadedByUserId(adminUserId)
                    .build();
            result.getEvidences().add(evidence);
        }

        return result;
    }
}
