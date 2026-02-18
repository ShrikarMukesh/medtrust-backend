package com.medtrust.clinical.infrastructure.persistence.entity;

import java.io.Serializable;

public class DiagnosisData implements Serializable {

    private String code;
    private String description;

    public DiagnosisData() {
    }

    public DiagnosisData(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
