package com.fast.campus.model;

import com.fast.campus.exception.CourseClashException;
import com.fast.campus.exception.CourseFullException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a specific section (class offering) of a course.
 *
 * <p>Owner: Hasan</p>
 */
public class Section {

    private String sectionId;
    private int capacity;
    private Course course;
    private Instructor instructor;      // Stub — will be linked to Saim's Instructor
    private Student teachingAssistant;  // Stub — will be linked to Kabeer's TeachingAssistant
    private Schedule schedule;
    private List<Enrollment> enrollments;

    public Section(String sectionId, int capacity, Course course, Schedule schedule) {
        this.sectionId = sectionId;
        this.capacity = capacity;
        this.course = course;
        this.schedule = schedule;
        this.enrollments = new ArrayList<>();
    }

    // --- Domain operations ---

    /**
     * Enrolls a student in this section.
     *
     * @param enrollment the enrollment record to add
     * @throws CourseFullException  if the section has no available seats
     * @throws CourseClashException if the student has a schedule clash
     */
    public void enroll(Enrollment enrollment) throws CourseFullException, CourseClashException {
        if (isFull()) {
            throw new CourseFullException(sectionId);
        }
        enrollments.add(enrollment);
    }

    /**
     * Drops the enrollment record from this section.
     *
     * @param enrollment the enrollment to remove
     */
    public void drop(Enrollment enrollment) {
        enrollments.remove(enrollment);
    }

    public boolean isFull() {
        return enrollments.size() >= capacity;
    }

    public int getAvailableSeats() {
        return capacity - enrollments.size();
    }

    public void assignInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public void assignTA(Student ta) {
        this.teachingAssistant = ta;
    }

    public List<Student> getEnrolledStudents() {
        return enrollments.stream()
                .map(Enrollment::getStudent)
                .collect(Collectors.toList());
    }

    /**
     * Checks if this section's schedule clashes with another section's schedule.
     */
    public boolean hasClash(Section other) {
        if (this.schedule == null || other.schedule == null) return false;
        return this.schedule.hasClash(other.schedule);
    }

    // --- Getters ---

    public String getSectionId()              { return sectionId; }
    public int getCapacity()                  { return capacity; }
    public Course getCourse()                 { return course; }
    public Instructor getInstructor()         { return instructor; }
    public Student getTeachingAssistant()     { return teachingAssistant; }
    public Schedule getSchedule()             { return schedule; }
    public List<Enrollment> getEnrollments()  { return enrollments; }

    // --- Setters ---

    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }

    @Override
    public String toString() {
        return "Section[" + sectionId + " | " + course.getCourseCode()
                + " | seats=" + getAvailableSeats() + "/" + capacity + "]";
    }
}
