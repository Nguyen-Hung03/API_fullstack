package com.atelier.repo;

import com.atelier.domain.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {

    @Query("SELECT s FROM AuthSession s JOIN FETCH s.customer WHERE s.token = :token")
    Optional<AuthSession> findByToken(@Param("token") String token);
}
