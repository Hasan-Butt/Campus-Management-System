package com.fast.campus.model;

import com.fast.campus.enums.EnrollmentStatus;

import java.time.LocalDate;

/**
 * Records a student's enrollment in a specific section.
 *
 * <p>Owner: Hasan</p>
 */
public class Enrollment {

    private String enrollmentId;
    private Student student;
    private Section section;
    private LocalDate enrollmentDate;
    private EnrollmentStatus status;

    public Enrollment(String enrollmentId, Student student, Section section, LocalDate enrollmentDate) {
        this.enrollmentId = enrollmentId;
        this.student = student;
        this.section = section;
        this.enrollmentDate = enrollmentDate;
        this.status = EnrollmentStatus.ACTIVE;
    }

    // --- Domain operations ---

    public void cancel() {
        this.status = EnrollmentStatus.DROPPED;
    }

    // --- Getters ---

    public String getEnrollmentId()        { return enrollmentId; }
    public Student getStudent()            { return student; }
    public Section getSection()            { return section; }
    public LocalDate getEnrollmentDate()   { return enrollmentDate; }
    public EnrollmentStatus getStatus()    { return status; }

    // --- Setters ---

    public void setStatus(EnrollmentStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Enrollment[" + enrollmentId + " | " + student.getName()
                + " -> " + section.getSectionId() + " | " + status + "]";
    }
}
