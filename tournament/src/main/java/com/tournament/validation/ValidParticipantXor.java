package com.tournament.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ParticipantXorValidator.class)
@Documented
public @interface ValidParticipantXor {
    String message() default "Especifique exactamente uno: playerId o teamId";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
