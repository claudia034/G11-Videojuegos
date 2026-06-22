package com.tournament.application.service;

import com.tournament.application.dto.request.LoginRequest;
import com.tournament.application.dto.request.SignUpRequest;
import com.tournament.application.dto.response.AuthResponse;
import com.tournament.domain.entity.RefreshToken;
import com.tournament.domain.entity.User;
import com.tournament.domain.entity.Player;
import com.tournament.domain.enums.UserRole;
import com.tournament.domain.repository.PlayerRepository;
import com.tournament.domain.repository.RefreshTokenRepository;
import com.tournament.domain.repository.UserRepository;
import com.tournament.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository         userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService             jwtService;
    private final PasswordEncoder        passwordEncoder;
    private final AuthenticationManager  authenticationManager;
    private final PlayerRepository playerRepository;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpMs;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_#])[A-Za-z\\d@$!%*?&_#]{8,}$"
    );

    public AuthResponse register(SignUpRequest request) {

        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new WeakPasswordException(
                    "La contrasena debe contener al menos: " +
                            "1 mayuscula, 1 minuscula, 1 numero y 1 caracter especial (@$!%*?&_#)");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.PLAYER)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == UserRole.PLAYER) {
            Player newPlayer = Player.builder()
                    .userId(savedUser.getId())
                    .username(request.getUsername())
                    .eloRating(1000)
                    .build();
            playerRepository.save(newPlayer);
        }

        return buildTokenPair(savedUser, UUID.randomUUID().toString());
    }

    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail().toLowerCase().trim();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (DisabledException e) {
            throw new AccountDisabledException();
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(InvalidCredentialsException::new);

        return buildTokenPair(user, UUID.randomUUID().toString());
    }

    public AuthResponse refresh(String refreshTokenValue) {

        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException(
                        "Refresh token no reconocido. Por favor inicie sesion de nuevo."));

        if (stored.isRevoked()) {
            revokeEntireFamily(stored.getFamilyId());
            throw new InvalidTokenException(
                    "Token de seguridad comprometido. " +
                            "Por seguridad se cerraron todas sus sesiones activas. " +
                            "Por favor inicie sesion de nuevo.");
        }

        if (stored.isExpired()) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new TokenExpiredException(
                    "La sesion ha expirado. Por favor inicie sesion de nuevo.");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildTokenPair(stored.getUser(), stored.getFamilyId());
    }

    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    public void logoutAllSessions(Long userId) {
        List<RefreshToken> activeSessions =
                refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
        activeSessions.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(activeSessions);
    }

    private AuthResponse buildTokenPair(User user, String familyId) {
        String accessToken    = jwtService.generateAccessToken(user);
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .familyId(familyId)
                .expiresAt(LocalDateTime.now()
                        .plusSeconds(jwtService.getRefreshTokenExpMs() / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpMs() / 1000)
                .user(AuthResponse.UserInfo.from(user))
                .build();
    }

    private void revokeEntireFamily(String familyId) {
        List<RefreshToken> family =
                refreshTokenRepository.findByFamilyIdAndRevokedFalse(familyId);
        family.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(family);
    }
}