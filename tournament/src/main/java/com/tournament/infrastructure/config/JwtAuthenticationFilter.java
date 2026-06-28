package com.tournament.infrastructure.config;

import com.tournament.application.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService         jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String accessTokenParam = request.getParameter("access_token");

        if ((authHeader == null || !authHeader.startsWith("Bearer "))
                && (accessTokenParam == null || accessTokenParam.isBlank())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : accessTokenParam;

        try {
            final String userEmail = jwtService.extractEmail(jwt);

            if (userEmail != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isAccessTokenValid(jwt)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Usuario autenticado via JWT: {} | endpoint: {} {}",
                            userEmail, request.getMethod(), request.getRequestURI());
                }
            }

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.debug("Token JWT rechazado en {}: {}", request.getRequestURI(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/api-docs/");
    }
}
