package com.medtrust.clinical.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public final class MRN {

    private static final Pattern MRN_PATTERN = Pattern.compile("^MRN-\\d{6,10}$");

    private final String value;

    private MRN(String value) {
        this.value = value;
    }

    public static MRN create(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("MRN cannot be empty");
        }
        if (!MRN_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "MRN must follow the format MRN-XXXXXX (6-10 digits), e.g. MRN-000001");
        }
        return new MRN(value);
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
        MRN mrn = (MRN) o;
        return Objects.equals(value, mrn.value);
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
