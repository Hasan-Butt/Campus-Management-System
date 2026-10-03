package com.fast.campus.service;

import com.fast.campus.exception.CourseException;
import com.fast.campus.exception.UserException;
import com.fast.campus.model.*;
import com.fast.campus.util.FileManager;
import com.fast.campus.util.Logger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for Student and Teaching Assistant use cases.
 *
 * <p>Owner: Kabeer</p>
 * Responsibilities:
 * <ul>
 *   <li>Student records, course registration and drop (+ enrollment records)</li>
 *   <li>Timetable and attendance viewing</li>
 *   <li>Assignment submission</li>
 *   <li>TA: create assignments, evaluate submissions</li>
 *   <li>Persist to data/students.txt, data/enrollments.txt, data/assignments.txt,
 *       data/submissions.txt, data/requests.txt</li>
 * </ul>
 */
public class StudentService {

    private static final String STUDENTS_FILE    = "data/students.txt";
    private static final String ENROLLMENTS_FILE = "data/enrollments.txt";
    private static final String ASSIGNMENTS_FILE = "data/assignments.txt";
    private static final String SUBMISSIONS_FILE = "data/submissions.txt";
    private static final String REQUESTS_FILE    = "data/requests.txt";

    private static final String STUDENTS_HEADER =
            "# Format: STUDENT|id|name|email|phone|studentId|role|assignedSectionId";
    private static final String ENROLLMENTS_HEADER =
            "# Format: ENROLLMENT|enrollmentId|studentId|sectionId|date|status";

    private final AcademicOfficeService academicService; // source of the loaded sections
    private final List<Student> students = new ArrayList<>();

    // ================================================================
    // CONSTRUCTOR — LOAD EXISTING DATA
    // ================================================================

    public StudentService(AcademicOfficeService academicService) {
        this.academicService = academicService;
        loadStudents();
        loadEnrollments();
    }

    // ================================================================
    // STUDENT RECORDS
    // ================================================================

    public void addStudent(Student student) throws UserException {
        if (student == null || student.getStudentId() == null || student.getStudentId().isBlank()) {
            throw new UserException("Student must have a student ID");
        }
        if (findStudent(student.getStudentId()) != null) {
            throw new UserException("Student " + student.getStudentId() + " already exists");
        }
        students.add(student);
        saveStudents();
        Logger.info("StudentService", "Student added: " + student.getStudentId() + " (" + student.getRole() + ")");
    }

    public Student findStudent(String studentId) {
        for (Student student : students) {
            if (student.getStudentId().equals(studentId)) {
                return student;
            }
        }
        return null;
    }

    public List<Student> getStudents() {
        return students;
    }

    // ================================================================
    // REGISTRATION
    // ================================================================

    public void registerCourse(Student student, Section section) throws CourseException {
        try {
            student.register(section);
        } catch (CourseException e) {
            Logger.error("Student", student.getStudentId() + " could not register: " + e.getMessage());
            throw e;
        }
        saveEnrollments();
        Logger.info("Student", student.getStudentId() + " registered for section " + section.getSectionId()
                + " (total credit hours: " + student.getTotalCreditHours() + ")");
    }

    public void dropCourse(Student student, Section section) throws CourseException {
        try {
            student.drop(section);
        } catch (CourseException e) {
            Logger.error("Student", student.getStudentId() + " could not drop: " + e.getMessage());
            throw e;
        }
        saveEnrollments();
        Logger.info("Student", student.getStudentId() + " dropped section " + section.getSectionId()
                + " (total credit hours: " + student.getTotalCreditHours() + ")");
    }

    public List<Schedule> viewTimetable(Student student) {
        Logger.info("Student", student.getStudentId() + " viewed timetable");
        return student.viewTimetable();
    }

    // ================================================================
    // PERSISTENCE — STUDENTS & ENROLLMENTS
    // ================================================================

    private void saveStudents() {
        List<String> lines = new ArrayList<>();
        lines.add(STUDENTS_HEADER);
        for (Student s : students) {
            String sectionId = "NONE";
            if (s instanceof TeachingAssistant) {
                Section assigned = ((TeachingAssistant) s).getAssignedSection();
                if (assigned != null) {
                    sectionId = assigned.getSectionId();
                }
            }
            lines.add(String.join("|", "STUDENT", s.getId(), clean(s.getName()), clean(s.getEmail()),
                    clean(s.getPhoneNumber()), s.getStudentId(), s.getRole(), sectionId));
        }
        FileManager.writeAllLines(STUDENTS_FILE, lines);
    }

    private void loadStudents() {
        for (String line : FileManager.readLines(STUDENTS_FILE)) {
            String[] p = line.split("\\|", -1);
            if (p.length < 7 || !p[0].equals("STUDENT")) {
                continue;
            }
            Student student;
            if (p[6].equals("TeachingAssistant")) {
                TeachingAssistant ta = new TeachingAssistant(p[1], p[2], p[3], p[4], p[5]);
                Section section = p.length > 7 ? findSection(p[7]) : null;
                if (section != null) {
                    ta.setAssignedSection(section);
                    section.assignTA(ta);
                }
                student = ta;
            } else {
                student = new NormalStudent(p[1], p[2], p[3], p[4], p[5]);
            }
            students.add(student);
        }
        Logger.info("StudentService", "Loaded " + students.size() + " student(s) from file");
    }

    private void saveEnrollments() {
        List<String> lines = new ArrayList<>();
        lines.add(ENROLLMENTS_HEADER);
        for (Student s : students) {
            for (Enrollment e : s.getEnrollments()) {
                lines.add(String.join("|", "ENROLLMENT", e.getEnrollmentId(), s.getStudentId(),
                        e.getSection().getSectionId(), e.getEnrollmentDate().toString(), e.getStatus().name()));
            }
        }
        FileManager.writeAllLines(ENROLLMENTS_FILE, lines);
    }

    private void loadEnrollments() {
        int loaded = 0;
        for (String line : FileManager.readLines(ENROLLMENTS_FILE)) {
            String[] p = line.split("\\|", -1);
            if (p.length < 6 || !p[0].equals("ENROLLMENT") || !p[5].equals("ACTIVE")) {
                continue;
            }
            Student student = findStudent(p[2]);
            Section section = findSection(p[3]);
            if (student == null || section == null) {
                Logger.warn("StudentService", "Skipping enrollment with unknown student/section: " + line);
                continue;
            }
            try {
                section.enroll(new Enrollment(p[1], student, section, LocalDate.parse(p[4])));
                // Section.enroll(Enrollment) doesn't record the course, so add it here
                if (section.getCourse() != null && !student.getRegisteredCourses().contains(section.getCourse())) {
                    student.getRegisteredCourses().add(section.getCourse());
                }
                student.calculateTotalCreditHours();
                loaded++;
            } catch (CourseException e) {
                Logger.error("StudentService", "Could not restore enrollment " + p[1] + ": " + e.getMessage());
            }
        }
        Logger.info("StudentService", "Loaded " + loaded + " enrollment(s) from file");
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private Section findSection(String sectionId) {
        for (Section section : academicService.getSections()) {
            if (section.getSectionId().equals(sectionId)) {
                return section;
            }
        }
        return null;
    }

    /** Free text can't contain the field separator or line breaks. */
    private static String clean(String text) {
        return text == null ? "" : text.replace("|", "/").replace("\n", " ").replace("\r", " ");
    }

    // ================================================================
    // TODO (next parts): assignments, submissions, requests, attendance
    // ================================================================

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
