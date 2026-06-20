package com.tournament.exception;

import com.tournament.infrastructure.response.ApiResponse;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "Error de validación", errors));
    }

    @ExceptionHandler(InvalidRegistrationTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidType(InvalidRegistrationTypeException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenOperationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedResultException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedResultException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorizationDenied(
            org.springframework.security.authorization.AuthorizationDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, "Acceso denegado"));
    }

    @ExceptionHandler(InvalidMatchStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidMatchState(InvalidMatchStateException ex) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, ex.getMessage()));
    }

    @ExceptionHandler({
            RegistrationNotFoundException.class,
            BracketNotFoundException.class,
            RoundNotFoundException.class,
            TournamentNotFoundException.class,
            MatchNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, ex.getMessage()));
    }

    @ExceptionHandler({
            TournamentFullException.class,
            AlreadyRegisteredException.class,
            TournamentNotPublishedException.class,
            BracketAlreadyGeneratedException.class,
            RegistrationWithdrawNotAllowedException.class,
            RoundAlreadyCompletedException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, ex.getMessage()));
    }

    @ExceptionHandler({
            EloRequirementNotMetException.class,
            InsufficientParticipantsException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleUnprocessable(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(422, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Error interno del servidor"));
    }
}
