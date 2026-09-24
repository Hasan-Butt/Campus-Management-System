package com.fast.campus.model;

/**
 * Abstract class representing a campus administrator.
 * Extended by AcademicOfficeAdmin and any future admin roles.
 */
public abstract class Administrator extends Person {

    protected String adminId;

    public Administrator(String id, String name, String email, String phoneNumber, String adminId) {
        super(id, name, email, phoneNumber);
        this.adminId = adminId;
    }

    public String getAdminId() { return adminId; }
}
