package com.medtrust.auth.domain.repository;

import com.medtrust.auth.domain.model.RefreshToken;
import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByToken(String token);
    void revokeAllByUserId(String userId);
}
