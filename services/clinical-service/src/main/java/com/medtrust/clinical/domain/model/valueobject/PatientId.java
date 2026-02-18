package com.medtrust.clinical.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class PatientId {

    private final String value;

    private PatientId(String value) {
        this.value = value;
    }

    public static PatientId create() {
        return new PatientId(UUID.randomUUID().toString());
    }

    public static PatientId from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PatientId cannot be empty");
        }
        return new PatientId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PatientId patientId = (PatientId) o;
        return Objects.equals(value, patientId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
