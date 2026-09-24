package com.fast.campus.model;

/**
 * Concrete administrator responsible for managing courses, sections,
 * instructor assignments, room assignments, and student requests.
 *
 * <p>Owner: Hasan</p>
 */
public class AcademicOfficeAdmin extends Administrator {

    public AcademicOfficeAdmin(String id, String name, String email,
                               String phoneNumber, String adminId) {
        super(id, name, email, phoneNumber, adminId);
    }

    @Override
    public String getRole() {
        return "AcademicOfficeAdmin";
    }
}
