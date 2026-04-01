package com.medtrust.patient.infrastructure.persistence.entity;

import java.io.Serializable;

public class EmergencyContactData implements Serializable {

    private String name;
    private String relationship;
    private String phone;

    public EmergencyContactData() {
    }

    public EmergencyContactData(String name, String relationship, String phone) {
        this.name = name;
        this.relationship = relationship;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
