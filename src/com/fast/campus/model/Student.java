package com.fast.campus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Student model stub.
 *
 * <p>Owner: Kabeer — This is a compilable stub so Hasan's Enrollment and
 * Saim's FYPGroup can reference it. Kabeer will expand this class.</p>
 */
public abstract class Student extends Person {

    protected String studentId;
    protected int totalCreditHours;
    protected List<Section> enrolledSections;
    protected List<Course> registeredCourses;

    public Student(String id, String name, String email,
                   String phoneNumber, String studentId) {
        super(id, name, email, phoneNumber);
        this.studentId = studentId;
        this.totalCreditHours = 0;
        this.enrolledSections = new ArrayList<>();
        this.registeredCourses = new ArrayList<>();
    }

    // --- Domain operations (to be implemented by Kabeer) ---

    public List<Course> viewCourses()     { return registeredCourses; }
    public List<Section> viewSection()    { return enrolledSections; }

    public void dropSection(Section section) {
        // TODO: Kabeer — implement drop logic
    }

    public void viewTimetable() {
        // TODO: Kabeer — implement timetable view
    }

    // --- Getters ---

    public String getStudentId()                    { return studentId; }
    public int getTotalCreditHours()                { return totalCreditHours; }
    public List<Section> getEnrolledSections()      { return enrolledSections; }
    public List<Course> getRegisteredCourses()      { return registeredCourses; }

    // --- Setters ---

    public void setTotalCreditHours(int totalCreditHours) {
        this.totalCreditHours = totalCreditHours;
    }
}
