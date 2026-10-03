package com.fast.campus.service;

import com.fast.campus.comparator.AssignmentDeadlineComparator;
import com.fast.campus.enums.SubmissionStatus;
import com.fast.campus.exception.AssessmentException;
import com.fast.campus.exception.CampusException;
import com.fast.campus.exception.CourseException;
import com.fast.campus.exception.UnauthorizedActionException;
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
    private static final String ASSIGNMENTS_HEADER =
            "# Format: ASSIGNMENT|id|title|deadline|totalMarks|sectionId|taStudentId|description";
    private static final String SUBMISSIONS_HEADER =
            "# Format: SUBMISSION|submissionId|assignmentId|studentId|date|status|marks"
            + "|feedbackId|evaluator|feedbackDate|comments|content";

    private final AcademicOfficeService academicService; // source of the loaded sections
    private final List<Student> students = new ArrayList<>();
    private final List<Assignment> assignments = new ArrayList<>();

    // ================================================================
    // CONSTRUCTOR — LOAD EXISTING DATA
    // ================================================================

    public StudentService(AcademicOfficeService academicService) {
        this.academicService = academicService;
        loadStudents();
        loadEnrollments();
        loadAssignments();   // needs students (TA) and sections
        loadSubmissions();   // needs assignments and students
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
    // ASSIGNMENTS — STUDENT SIDE
    // ================================================================

    public List<Assignment> viewAssignments(Student student) {
        List<Assignment> result = new ArrayList<>();
        for (Assignment assignment : assignments) {
            if (student.getEnrolledSections().contains(assignment.getSection())) {
                result.add(assignment);
            }
        }
        result.sort(new AssignmentDeadlineComparator()); // earliest deadline first
        Logger.info("Student", student.getStudentId() + " viewed " + result.size() + " assignment(s)");
        return result;
    }

    public Submission submitAssignment(Student student, Assignment assignment, String content)
            throws UnauthorizedActionException, AssessmentException {
        Submission submission;
        try {
            submission = student.submitAssignment(assignment, content);
        } catch (CampusException e) {
            Logger.error("Student", student.getStudentId() + " could not submit: " + e.getMessage());
            throw e;
        }
        saveSubmissions();
        if (submission.getStatus() == SubmissionStatus.LATE) {
            Logger.warn("Student", "LATE submission " + submission.getSubmissionId() + " by "
                    + student.getStudentId() + " for " + assignment.getTitle());
        } else {
            Logger.info("Student", "Submission " + submission.getSubmissionId() + " by "
                    + student.getStudentId() + " for " + assignment.getTitle());
        }
        return submission;
    }

    // ================================================================
    // ASSIGNMENTS — TEACHING ASSISTANT SIDE
    // ================================================================

    public Assignment createAssignment(TeachingAssistant ta, String title, String description,
                                       LocalDate deadline, double totalMarks)
            throws UnauthorizedActionException, AssessmentException {
        Assignment assignment;
        try {
            assignment = ta.createAssignment(title, description, deadline, totalMarks);
        } catch (CampusException e) {
            Logger.error("TeachingAssistant", ta.getStudentId() + " could not create assignment: " + e.getMessage());
            throw e;
        }
        assignments.add(assignment);
        saveAssignments();
        Logger.info("TeachingAssistant", ta.getStudentId() + " created assignment " + assignment.getId()
                + " '" + title + "' for section " + assignment.getSection().getSectionId()
                + " (due " + deadline + ", " + totalMarks + " marks)");
        return assignment;
    }

    public List<Submission> viewSubmissions(TeachingAssistant ta, Assignment assignment)
            throws UnauthorizedActionException {
        try {
            List<Submission> list = ta.viewSubmissions(assignment);
            Logger.info("TeachingAssistant", ta.getStudentId() + " viewed " + list.size()
                    + " submission(s) of " + assignment.getId());
            return list;
        } catch (UnauthorizedActionException e) {
            Logger.error("TeachingAssistant", e.getMessage());
            throw e;
        }
    }

    public List<Submission> checkLateSubmissions(TeachingAssistant ta, Assignment assignment)
            throws UnauthorizedActionException {
        try {
            List<Submission> late = ta.viewLateSubmissions(assignment);
            Logger.info("TeachingAssistant", ta.getStudentId() + " found " + late.size()
                    + " late submission(s) for " + assignment.getId());
            return late;
        } catch (UnauthorizedActionException e) {
            Logger.error("TeachingAssistant", e.getMessage());
            throw e;
        }
    }

    public void evaluateSubmission(TeachingAssistant ta, Submission submission, double marks)
            throws UnauthorizedActionException, AssessmentException {
        try {
            ta.evaluateSubmission(submission, marks);
        } catch (CampusException e) {
            Logger.error("TeachingAssistant", ta.getStudentId() + " could not evaluate: " + e.getMessage());
            throw e;
        }
        saveSubmissions();
        Logger.info("TeachingAssistant", ta.getStudentId() + " evaluated " + submission.getSubmissionId()
                + ": " + marks + "/" + submission.getAssignment().getTotalMarks());
    }

    public void giveFeedback(TeachingAssistant ta, Submission submission, String comments)
            throws UnauthorizedActionException {
        try {
            ta.giveFeedback(submission, comments);
        } catch (UnauthorizedActionException e) {
            Logger.error("TeachingAssistant", e.getMessage());
            throw e;
        }
        saveSubmissions();
        Logger.info("TeachingAssistant", ta.getStudentId() + " gave feedback on " + submission.getSubmissionId());
    }

    public Assignment findAssignment(String assignmentId) {
        for (Assignment assignment : assignments) {
            if (assignment.getId().equals(assignmentId)) {
                return assignment;
            }
        }
        return null;
    }

    public List<Assignment> getAssignments() {
        return assignments;
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
    // PERSISTENCE — ASSIGNMENTS & SUBMISSIONS
    // ================================================================

    private void saveAssignments() {
        List<String> lines = new ArrayList<>();
        lines.add(ASSIGNMENTS_HEADER);
        for (Assignment a : assignments) {
            String taId = a.getCreatedBy() != null ? a.getCreatedBy().getStudentId() : "NONE";
            lines.add(String.join("|", "ASSIGNMENT", a.getId(), clean(a.getTitle()), a.getDeadline().toString(),
                    String.valueOf(a.getTotalMarks()), a.getSection().getSectionId(), taId, clean(a.getDescription())));
        }
        FileManager.writeAllLines(ASSIGNMENTS_FILE, lines);
    }

    private void loadAssignments() {
        for (String line : FileManager.readLines(ASSIGNMENTS_FILE)) {
            String[] p = line.split("\\|", -1);
            if (p.length < 8 || !p[0].equals("ASSIGNMENT")) {
                continue;
            }
            Section section = findSection(p[5]);
            if (section == null) {
                Logger.warn("StudentService", "Skipping assignment with unknown section: " + line);
                continue;
            }
            Student creator = findStudent(p[6]);
            TeachingAssistant ta = creator instanceof TeachingAssistant ? (TeachingAssistant) creator : null;
            try {
                assignments.add(new Assignment(p[1], p[2], p[7], LocalDate.parse(p[3]),
                        Double.parseDouble(p[4]), section, ta));
            } catch (RuntimeException e) { // bad date or number in the file
                Logger.error("StudentService", "Invalid assignment record: " + line);
            }
        }
        Logger.info("StudentService", "Loaded " + assignments.size() + " assignment(s) from file");
    }

    private void saveSubmissions() {
        List<String> lines = new ArrayList<>();
        lines.add(SUBMISSIONS_HEADER);
        for (Assignment a : assignments) {
            for (Submission s : a.getSubmissions()) {
                Feedback f = s.getFeedback();
                lines.add(String.join("|", "SUBMISSION", s.getSubmissionId(), a.getId(),
                        s.getStudent().getStudentId(), s.getSubmissionDate().toString(), s.getStatus().name(),
                        String.valueOf(s.getMarks()),
                        f != null ? f.getFeedbackId() : "NONE",
                        f != null ? clean(f.getEvaluator()) : "",
                        f != null ? f.getDate().toString() : "",
                        f != null ? clean(f.getComments()) : "",
                        clean(s.getContent())));
            }
        }
        FileManager.writeAllLines(SUBMISSIONS_FILE, lines);
    }

    private void loadSubmissions() {
        int loaded = 0;
        for (String line : FileManager.readLines(SUBMISSIONS_FILE)) {
            String[] p = line.split("\\|", -1);
            if (p.length < 12 || !p[0].equals("SUBMISSION")) {
                continue;
            }
            Assignment assignment = findAssignment(p[2]);
            Student student = findStudent(p[3]);
            if (assignment == null || student == null) {
                Logger.warn("StudentService", "Skipping submission with unknown assignment/student: " + line);
                continue;
            }
            try {
                Feedback feedback = p[7].equals("NONE") ? null
                        : new Feedback(p[7], p[8], p[10], LocalDate.parse(p[9]));
                Submission submission = new Submission(p[1], assignment, student, LocalDate.parse(p[4]), p[11],
                        Double.parseDouble(p[6]), feedback, SubmissionStatus.valueOf(p[5]));
                assignment.addSubmission(submission);
                loaded++;
            } catch (RuntimeException e) {
                Logger.error("StudentService", "Invalid submission record: " + line);
            }
        }
        Logger.info("StudentService", "Loaded " + loaded + " submission(s) from file");
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
    // TODO (next parts): requests, attendance
    // ================================================================

    public void viewAttendance(Student student, Section section) {
        // TODO: Kabeer — list attendance records
    }

    public void submitCourseClashRequest(CourseClashRequest request) {
        request.submit();
        Logger.info("Student", "Course clash request submitted: " + request.getRequestId());
        // TODO: Kabeer — persist to REQUESTS_FILE
    }
}
