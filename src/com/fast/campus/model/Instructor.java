package com.fast.campus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Instructor model.
 *
 * <p>Owner: Saim</p>
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

    protected String teacherId;
    protected List<Section> assignedSections;

    public List<Section> viewCourses() {
        return assignedSections;
    }

    public List<Section> viewSection() {
        return assignedSections;
    }

    public void markAttendance(Student student, Section section) {
    }

    public void updateAttendance(Student student, Section section) {
    }

    public double calculateAttendancePercentage(Student student, Section section) {
        return 0.0;
    }

    public List<Student> viewStudents() {
        List<Student> allStudents = new ArrayList<>();
        for (Section section : assignedSections) {
            allStudents.addAll(section.getEnrolledStudents());
        }
        return allStudents;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public List<Section> getAssignedSections() {
        return assignedSections;
    }

    public void addSection(Section section) {
        assignedSections.add(section);
    }
}
