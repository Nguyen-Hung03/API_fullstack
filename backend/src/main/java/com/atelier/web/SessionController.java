package com.atelier.web;

import com.atelier.api.SessionsApi;
import com.atelier.api.model.LoginRequestDto;
import com.atelier.api.model.SessionDto;
import com.atelier.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController implements SessionsApi {

    private final SessionService sessions;

    public SessionController(SessionService sessions) {
        this.sessions = sessions;
    }

    @Override
    public ResponseEntity<SessionDto> login(LoginRequestDto request) {
        return ResponseEntity.ok(sessions.login(request));
    }
}
