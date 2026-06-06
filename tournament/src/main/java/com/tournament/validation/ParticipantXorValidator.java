package com.tournament.validation;

import com.tournament.application.dto.request.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ParticipantXorValidator
        implements ConstraintValidator<ValidParticipantXor, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest req, ConstraintValidatorContext ctx) {
        if (req == null) return true;
        boolean hasPlayer = req.getPlayerId() != null;
        boolean hasTeam   = req.getTeamId()   != null;
        return hasPlayer ^ hasTeam;
    }
}
