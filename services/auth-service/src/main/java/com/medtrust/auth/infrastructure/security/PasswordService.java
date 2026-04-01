package com.medtrust.auth.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean verify(String rawPassword, String hash) {
        return encoder.matches(rawPassword, hash);
    }
}
