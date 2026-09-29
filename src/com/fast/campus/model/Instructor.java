package com.fast.campus.model;

import com.fast.campus.enums.AttendanceStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<Course> viewCourses() {
        return assignedSections.stream()
                .map(Section::getCourse)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Section> viewSections() {
        return assignedSections;
    }

    public List<Student> viewEnrolledStudents(Section section) {
        if (section == null) {
            return new ArrayList<>();
        }
        return section.getEnrolledStudents();
    }

    public void markAttendance(Attendance attendance, AttendanceStatus status) {
    }

    public void updateAttendance(Attendance attendance, AttendanceStatus status) {
    }

    public double calculateAttendancePercentage(Student student, Section section) {
        return 0.0;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public List<Section> getAssignedSections() {
        return assignedSections;
    }

    public void addSection(Section section) {
        if (section != null && !assignedSections.contains(section)) {
            assignedSections.add(section);
        }
    }
}
