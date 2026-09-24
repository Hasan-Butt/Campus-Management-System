package com.fast.campus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Instructor model stub.
 *
 * <p>Owner: Saim — This is a compilable stub so Hasan's Section can reference it.
 * Saim will expand this class with full business logic.</p>
 */
public abstract class Instructor extends Person {

    protected String teacherId;
    protected List<Section> assignedSections;

    public Instructor(String id, String name, String email,
                      String phoneNumber, String teacherId) {
        super(id, name, email, phoneNumber);
        this.teacherId = teacherId;
        this.assignedSections = new ArrayList<>();
    }

    // --- Domain operations (to be implemented by Saim) ---

    public List<Section> viewCourses()  { return assignedSections; }
    public List<Section> viewSection()  { return assignedSections; }

    public void markAttendance(Student student, Section section) {
        // TODO: Saim — implement attendance marking
    }

    public void updateAttendance(Student student, Section section) {
        // TODO: Saim — implement attendance update
    }

    public double calculateAttendancePercentage(Student student, Section section) {
        // TODO: Saim — implement attendance percentage
        return 0.0;
    }

    public void viewStudents() {
        // TODO: Saim — implement
    }

    // --- Getters ---

    public String getTeacherId()                   { return teacherId; }
    public List<Section> getAssignedSections()     { return assignedSections; }

    public void addSection(Section section) {
        assignedSections.add(section);
    }
}
