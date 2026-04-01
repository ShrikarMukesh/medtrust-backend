package com.medtrust.patient.infrastructure.persistence.entity;

import java.io.Serializable;

public class InsuranceInfoData implements Serializable {

    private String provider;
    private String policyNumber;
    private String groupNumber;
    private String expirationDate;

    public InsuranceInfoData() {
    }

    public InsuranceInfoData(String provider, String policyNumber,
                             String groupNumber, String expirationDate) {
        this.provider = provider;
        this.policyNumber = policyNumber;
        this.groupNumber = groupNumber;
        this.expirationDate = expirationDate;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(String groupNumber) {
        this.groupNumber = groupNumber;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
}
