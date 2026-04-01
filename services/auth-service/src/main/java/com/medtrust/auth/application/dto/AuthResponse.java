package com.medtrust.auth.application.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserInfo user) {

    public record UserInfo(
            String id,
            String email,
            String firstName,
            String lastName,
            String role) {
    }
}
