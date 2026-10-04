package com.fast.campus.model;

import com.fast.campus.exception.CourseClashException;
import com.fast.campus.exception.CourseFullException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a specific section of a course.
 *
 * <p>Owner: Hasan</p>
 */
public class Section {

    private String sectionId;
    private int capacity;
    private Course course;
    private Instructor instructor;    
    private TeachingAssistant teachingAssistant; 
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
     * @param student the student to enroll
     * @throws CourseFullException  if the section has no available seats
     * @throws CourseClashException if the student has a schedule clash
     */
    public void enroll(Student student) throws CourseFullException, CourseClashException {
        if (student == null) {
            return;
        }
        if (isFull()) {
            throw new CourseFullException(sectionId);
        }
        if (this.schedule != null) {
            for (Section enrolled : student.getEnrolledSections()) {
                if (this.hasClash(enrolled)) {
                    throw new CourseClashException(this.sectionId, enrolled.getSectionId());
                }
            }
        }
        String enrollmentId = "ENR-" + sectionId + "-" + student.getStudentId();
        Enrollment enrollment = new Enrollment(enrollmentId, student, this, LocalDate.now());
        
        enrollments.add(enrollment);

        if (student.getEnrollments() != null && !student.getEnrollments().contains(enrollment)) {
            student.getEnrollments().add(enrollment);
        }
        if (!student.getEnrolledSections().contains(this)) {
            student.getEnrolledSections().add(this);
        }
        if (this.course != null && !student.getRegisteredCourses().contains(this.course)) {
            student.getRegisteredCourses().add(this.course);
        }
    }

    /**
     * Overload for enrolling an existing Enrollment record.
     */
    public void enroll(Enrollment enrollment) throws CourseFullException, CourseClashException {
        if (enrollment == null) {
            return;
        }
        if (isFull()) {
            throw new CourseFullException(sectionId);
        }
        enrollments.add(enrollment);
        if (enrollment.getStudent() != null) {
            Student s = enrollment.getStudent();
            if (s.getEnrollments() != null && !s.getEnrollments().contains(enrollment)) {
                s.getEnrollments().add(enrollment);
            }
            if (!s.getEnrolledSections().contains(this)) {
                s.getEnrolledSections().add(this);
            }
        }
    }

    /**
     * Drops a student from this section.
     *
     * @param student the student to drop
     */
    public void drop(Student student) {
        if (student == null) {
            return;
        }
        enrollments.removeIf(e -> {
            boolean match = e.getStudent() != null && e.getStudent().equals(student);
            if (match) {
                e.cancel();
            }
            return match;
        });
        if (student.getEnrollments() != null) {
            student.getEnrollments().removeIf(e -> e.getSection() != null && e.getSection().equals(this));
        }
        student.getEnrolledSections().remove(this);
    }

    /**
     * Drops the enrollment record from this section.
     *
     * @param enrollment the enrollment to remove
     */
    public void drop(Enrollment enrollment) {
        if (enrollment == null) {
            return;
        }
        enrollment.cancel();
        enrollments.remove(enrollment);
        if (enrollment.getStudent() != null) {
            Student s = enrollment.getStudent();
            if (s.getEnrollments() != null) {
                s.getEnrollments().remove(enrollment);
            }
            s.getEnrolledSections().remove(this);
        }
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

    public void assignTA(TeachingAssistant ta) {
        if (this.teachingAssistant != null && this.teachingAssistant != ta) {
            this.teachingAssistant.setAssignedSection(null); // previous TA no longer has this section
        }
        this.teachingAssistant = ta;
        if (ta != null) {
            ta.setAssignedSection(this); // the TA must know its section to create/grade assignments
        }
    }

    public void assignTA(NormalStudent student) {
        if (student != null) {
            assignTA(new TeachingAssistant(student));
        }
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

    public String getSectionId()                        { return sectionId; }
    public int getCapacity()                            { return capacity; }
    public Course getCourse()                           { return course; }
    public Instructor getInstructor()                   { return instructor; }
    public TeachingAssistant getTeachingAssistant()     { return teachingAssistant; }
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
