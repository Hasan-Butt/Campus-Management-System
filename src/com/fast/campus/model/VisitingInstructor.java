package com.fast.campus.model;

/**
 * Visiting (part-time / contract) instructor.
 *
 * <p>Owner: Saim</p>
 */
public class VisitingInstructor extends Instructor {

    public VisitingInstructor(String id, String name, String email,
                              String phoneNumber, String teacherId) {
        super(id, name, email, phoneNumber, teacherId);
    }

    @Override
    public String getRole() { return "VisitingInstructor"; }
}
