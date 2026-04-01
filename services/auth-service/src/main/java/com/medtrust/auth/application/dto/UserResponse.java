package com.medtrust.auth.application.dto;

public record UserResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        String role,
        boolean active,
        String lastLoginAt,
        String createdAt) {
}
