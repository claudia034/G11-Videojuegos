package com.tournament.exception;

import com.tournament.infrastructure.response.ApiResponse;
import org.springframework.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Errores de validación
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Valor inválido",
                        (e1, e2) -> e1));

        ex.getBindingResult().getGlobalErrors().forEach(ge ->
                errors.put("_global", ge.getDefaultMessage()));

        log.warn("Error de validación en la petición: {}", errors);

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "Error de validación en los datos enviados", errors));
    }

    // Errorres 400 Bad request
    @ExceptionHandler({
            InvalidRegistrationTypeException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidType(RuntimeException ex) {
        log.warn("Petición incorrecta (400): {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, ex.getMessage()));
    }

    // Errores 403 Forbidden
    @ExceptionHandler({
            ForbiddenOperationException.class,
            UnauthorizedResultException.class,
            org.springframework.security.authorization.AuthorizationDeniedException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleForbidden(RuntimeException ex) {
        log.warn("Petición incorrecta (400): {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, ex.getMessage()));
    }

    // Errores 404 Not Found
    @ExceptionHandler({
            RegistrationNotFoundException.class,
            BracketNotFoundException.class,
            RoundNotFoundException.class,
            TournamentNotFoundException.class,
            ResourceNotFoundException.class,
            TournamentNotFoundException.class,
            MatchNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, ex.getMessage()));
    }

    // Errores 409 Conflict
    @ExceptionHandler({
            TournamentFullException.class,
            AlreadyRegisteredException.class,
            TournamentNotPublishedException.class,
            BracketAlreadyGeneratedException.class,
            RegistrationWithdrawNotAllowedException.class,
            RoundAlreadyCompletedException.class,
            InvalidMatchStateException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, ex.getMessage()));
    }

    // Error 422 Unprocessable entity
    @ExceptionHandler({
            EloRequirementNotMetException.class,
            InsufficientParticipantsException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleUnprocessable(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(422, ex.getMessage()));
    }

    // Error 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Error interno no controlado (500): {}", ex.getMessage(),ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Error interno del servidor"));
    }
}
