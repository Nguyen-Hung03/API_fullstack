package com.atelier.config;

import com.atelier.domain.AuthSession;
import com.atelier.repo.AuthSessionRepository;
import com.atelier.security.AuthContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final AuthSessionRepository sessions;

    public AuthFilter(AuthSessionRepository sessions) {
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            if (!token.isEmpty()) {
                sessions.findByToken(token).ifPresent(session -> attachIfActive(request, session));
            }
        }
        chain.doFilter(request, response);
    }

    private void attachIfActive(HttpServletRequest request, AuthSession session) {
        if (session.getExpiresAt().isAfter(Instant.now())) {
            request.setAttribute(AuthContext.ATTR, session.getCustomer());
        }
    }
}
