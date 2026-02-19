package com.medtrust.clinical.application.dto;

public record PatientResponse(
                String id,
                String mrn,
                String firstName,
                String lastName,
                String dateOfBirth,
                String gender,
                ContactInfoResponse contactInfo,
                String createdAt) {
        public record ContactInfoResponse(
                        String phone,
                        String email,
                        String address) {
        }
}
