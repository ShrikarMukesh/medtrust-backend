package com.medtrust.clinical.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Diagnosis {

    private static final Pattern ICD10_PATTERN = Pattern.compile("^[A-Z]\\d{2}(\\.\\d{1,4})?$");

    private final String code;
    private final String description;

    private Diagnosis(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Diagnosis create(String code, String description) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Diagnosis code cannot be empty");
        }
        if (!ICD10_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException(
                    "Diagnosis code must be a valid ICD-10 format, e.g. J06.9");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Diagnosis description cannot be empty");
        }
        return new Diagnosis(code, description);
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Diagnosis diagnosis = (Diagnosis) o;
        return Objects.equals(code, diagnosis.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code + " - " + description;
    }
}
