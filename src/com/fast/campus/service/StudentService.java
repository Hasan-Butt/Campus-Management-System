package com.fast.campus.service;

import com.fast.campus.model.*;
import com.fast.campus.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for Student and Teaching Assistant use cases.
 *
 * <p>Owner: Kabeer</p>
 * Responsibilities:
 * <ul>
 *   <li>Course registration and drop</li>
 *   <li>Timetable and attendance viewing</li>
 *   <li>Assignment submission</li>
 *   <li>TA: create assignments, evaluate submissions</li>
 *   <li>Persist to data/students.txt, data/assignments.txt,
 *       data/submissions.txt, data/requests.txt, data/attendance.txt</li>
 * </ul>
 */
public class StudentService {

    private static final String STUDENTS_FILE    = "data/students.txt";
    private static final String ASSIGNMENTS_FILE = "data/assignments.txt";
    private static final String SUBMISSIONS_FILE = "data/submissions.txt";
    private static final String REQUESTS_FILE    = "data/requests.txt";
    private static final String ATTENDANCE_FILE  = "data/attendance.txt";

    // ------------------------------------------------------------------ Student

    public void registerCourse(Student student, Section section) {
        // TODO: Kabeer — check clash, credit hour limit, then enroll
        Logger.info("Student", student.getName() + " registered for section " + section.getSectionId());
    }

    public void dropCourse(Student student, Section section) {
        // TODO: Kabeer — drop enrollment, update persistence
        Logger.info("Student", student.getName() + " dropped section " + section.getSectionId());
    }

    public void viewTimetable(Student student) {
        // TODO: Kabeer — print enrolled sections schedule
    }

    public void viewAssignments(Student student) {
        // TODO: Kabeer — list assignments for enrolled sections
    }

    public void submitAssignment(Submission submission) {
        // TODO: Kabeer — validate deadline, mark late if needed, persist
        submission.submit();
        Logger.info("Student", "Assignment submitted: " + submission.getSubmissionId());
    }

    public void viewAttendance(Student student, Section section) {
        // TODO: Kabeer — list attendance records
    }

    public void submitCourseClashRequest(CourseClashRequest request) {
        request.submit();
        Logger.info("Student", "Course clash request submitted: " + request.getRequestId());
        // TODO: Kabeer — persist to REQUESTS_FILE
    }

    // ----------------------------------------------------------------- TA

    public Assignment createAssignment(TeachingAssistant ta, Assignment assignment) {
        // TODO: Kabeer — validate and persist assignment
        Logger.info("TeachingAssistant", "Assignment created: " + assignment.getTitle());
        return assignment;
    }

    public List<Submission> viewSubmissions(Assignment assignment) {
        return assignment.getSubmissions();
    }

    public void evaluateSubmission(Submission submission, double marks, Feedback feedback) {
        submission.assignMarks(marks);
        submission.addFeedback(feedback);
        Logger.info("TeachingAssistant", "Submission evaluated: " + submission.getSubmissionId());
    }
}
