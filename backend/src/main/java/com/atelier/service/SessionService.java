package com.atelier.service;

import com.atelier.api.model.LoginRequestDto;
import com.atelier.api.model.SessionDto;
import com.atelier.domain.AuthSession;
import com.atelier.domain.Customer;
import com.atelier.error.ApiException;
import com.atelier.repo.AuthSessionRepository;
import com.atelier.repo.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class SessionService {

    private final CustomerRepository customers;
    private final AuthSessionRepository sessions;
    private final PasswordEncoder passwordEncoder;
    private final ApiMapper mapper;

    public SessionService(CustomerRepository customers, AuthSessionRepository sessions,
                          PasswordEncoder passwordEncoder, ApiMapper mapper) {
        this.customers = customers;
        this.sessions = sessions;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    @Transactional
    public SessionDto login(LoginRequestDto request) {
        Customer customer = customers.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> ApiException.badRequest("ERR_INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng"));
        if (!passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            throw ApiException.badRequest("ERR_INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng");
        }
        AuthSession session = new AuthSession();
        session.setToken(UUID.randomUUID().toString());
        session.setCustomer(customer);
        session.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        sessions.save(session);

        SessionDto dto = new SessionDto();
        dto.setToken(session.getToken());
        dto.setCustomer(mapper.toCustomer(customer));
        return dto;
    }
}
