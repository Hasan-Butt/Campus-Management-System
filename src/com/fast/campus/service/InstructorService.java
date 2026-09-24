package com.fast.campus.service;

import com.fast.campus.model.*;
import com.fast.campus.util.Logger;

import java.util.List;

/**
 * Service layer for Instructor and FYP use cases.
 *
 * <p>Owner: Saim</p>
 * Responsibilities:
 * <ul>
 *   <li>Attendance: mark, update, calculate percentage</li>
 *   <li>TA assignment (PermanentInstructor only)</li>
 *   <li>FYP: group management, meeting scheduling, evaluation</li>
 *   <li>Persist to data/instructors.txt, data/fypgroups.txt,
 *       data/fypmeetings.txt, data/fyp evaluations.txt</li>
 * </ul>
 */
public class InstructorService {

    private static final String INSTRUCTORS_FILE   = "data/instructors.txt";
    private static final String FYPGROUPS_FILE     = "data/fypgroups.txt";
    private static final String FYPMEETINGS_FILE   = "data/fypmeetings.txt";
    private static final String FYPEVALUATIONS_FILE = "data/fyp_evaluations.txt";

    // ----------------------------------------------------------------- Attendance

    public void markAttendance(Instructor instructor, Student student,
                               Section section, com.fast.campus.enums.AttendanceStatus status) {
        // TODO: Saim — create and persist Attendance record
        Logger.info("Instructor", instructor.getName() + " marked attendance for " + student.getName());
    }

    public void updateAttendance(Attendance attendance, com.fast.campus.enums.AttendanceStatus newStatus) {
        attendance.setStatus(newStatus);
        Logger.info("Instructor", "Attendance updated for " + attendance.getStudent().getName());
    }

    public double calculateAttendancePercentage(Student student, Section section) {
        // TODO: Saim — compute present / total sessions * 100
        return 0.0;
    }

    // ----------------------------------------------------------------- TA Assignment

    public void assignTA(PermanentInstructor instructor, NormalStudent student, Section section) {
        instructor.assignTAAsStudent(student, section);
        Logger.info("PermanentInstructor", "TA assigned: " + student.getName() + " to section " + section.getSectionId());
    }

    // ----------------------------------------------------------------- FYP

    public FYPGroup createFYPGroup(FYPGroup group) {
        // TODO: Saim — validate and persist
        return group;
    }

    public void scheduleFYPMeeting(PermanentInstructor instructor, FYPGroup group, FYPMeeting meeting) {
        group.addMeeting(meeting);
        Logger.info("PermanentInstructor", "FYP meeting scheduled: " + meeting.getMeetingId());
        // TODO: Saim — persist to FYPMEETINGS_FILE
    }

    public void evaluateFYPIdea(PermanentInstructor instructor, FYPGroup group,
                                FYPEvaluation evaluation, double score, String feedback) {
        evaluation.evaluate(score);
        evaluation.addFeedback(feedback);
        group.addEvaluation(evaluation);
        Logger.info("PermanentInstructor", "FYP evaluated for group: " + group.getGroupId());
        // TODO: Saim — persist to FYPEVALUATIONS_FILE
    }
}
