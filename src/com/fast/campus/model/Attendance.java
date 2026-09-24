package com.fast.campus.model;

import com.fast.campus.enums.AttendanceStatus;
import java.time.LocalDate;

/**
 * Records a student's attendance for a single session of a section.
 *
 * <p>Owner: Kabeer</p>
 */
public class Attendance {

    private Student student;
    private Section section;
    private LocalDate date;
    private AttendanceStatus status;

    public Attendance(Student student, Section section, LocalDate date, AttendanceStatus status) {
        this.student = student;
        this.section = section;
        this.date = date;
        this.status = status;
    }

    // --- Getters / Setters ---

    public Student getStudent()              { return student; }
    public Section getSection()              { return section; }
    public LocalDate getDate()               { return date; }
    public AttendanceStatus getStatus()      { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Attendance[" + student.getName() + " | " + section.getSectionId()
                + " | " + date + " | " + status + "]";
    }
}
