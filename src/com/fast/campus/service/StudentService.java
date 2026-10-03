package com.fast.campus.service;

import com.fast.campus.comparator.AssignmentDeadlineComparator;
import com.fast.campus.comparator.RequestDateComparator;
import com.fast.campus.comparator.RequestPriorityComparator;
import com.fast.campus.enums.AttendanceStatus;
import com.fast.campus.enums.RequestCategory;
import com.fast.campus.enums.RequestStatus;
import com.fast.campus.enums.SubmissionStatus;
import com.fast.campus.exception.AssessmentException;
import com.fast.campus.exception.CampusException;
import com.fast.campus.exception.CourseException;
import com.fast.campus.exception.InvalidRequestException;
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
    private static final String ATTENDANCE_FILE  = "data/attendance.txt"; // written by InstructorService; read-only here

    private static final String STUDENTS_HEADER =
            "# Format: STUDENT|id|name|email|phone|studentId|role|assignedSectionId";
    private static final String ENROLLMENTS_HEADER =
            "# Format: ENROLLMENT|enrollmentId|studentId|sectionId|date|status";
    private static final String ASSIGNMENTS_HEADER =
            "# Format: ASSIGNMENT|id|title|deadline|totalMarks|sectionId|taStudentId|description";
    private static final String SUBMISSIONS_HEADER =
            "# Format: SUBMISSION|submissionId|assignmentId|studentId|date|status|marks"
            + "|feedbackId|evaluator|feedbackDate|comments|content";
    private static final String REQUESTS_HEADER =
            "# Format: REQUEST|requestId|type|studentId|date|status|priority|detail1|detail2|description"
            + "  (CLASH: detail1=conflictingSectionId, detail2=requestedSectionId; GENERIC: detail1=category)";

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
        loadRequests();      // needs students and sections
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
                Assignment assignment = new Assignment(p[1], p[2], p[7], LocalDate.parse(p[3]),
                        Double.parseDouble(p[4]), section, ta);
                assignments.add(assignment);
                if (ta != null) {
                    ta.getCreatedAssignments().add(assignment);
                }
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
                        f != null && f.getEvaluator() != null ? f.getEvaluator().getEvaluatorId() : "",
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
                Student evaluatorStudent = findStudent(p[8]); // feedback here is given by TAs
                Evaluator evaluator = evaluatorStudent instanceof TeachingAssistant
                        ? (TeachingAssistant) evaluatorStudent : null;
                Feedback feedback = p[7].equals("NONE") ? null
                        : new Feedback(p[7], evaluator, p[10], LocalDate.parse(p[9]));
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
    // REQUESTS
    // ================================================================

    public CourseClashRequest submitCourseClashRequest(Student student, Section currentSection,
                                                       Section wantedSection, String description, int priority)
            throws InvalidRequestException {
        CourseClashRequest request = new CourseClashRequest("R-" + System.currentTimeMillis(),
                description, priority, currentSection, wantedSection);
        try {
            student.submitCourseClashRequest(request);
        } catch (InvalidRequestException e) {
            Logger.error("Student", student.getStudentId() + " clash request rejected: " + e.getMessage());
            throw e;
        }
        saveRequests();
        Logger.info("Student", student.getStudentId() + " submitted course clash request "
                + request.getRequestId() + " (" + request.getConflictDetails() + ")");
        return request;
    }

    public GenericRequest submitGenericRequest(Student student, RequestCategory category,
                                               String description, int priority)
            throws InvalidRequestException {
        GenericRequest request = new GenericRequest("R-" + System.currentTimeMillis(),
                description, priority, category);
        try {
            student.submitGenericRequest(request);
        } catch (InvalidRequestException e) {
            Logger.error("Student", student.getStudentId() + " request rejected: " + e.getMessage());
            throw e;
        }
        saveRequests();
        Logger.info("Student", student.getStudentId() + " submitted " + category + " request " + request.getRequestId());
        return request;
    }

    public List<Request> viewRequests(Student student) {
        List<Request> list = new ArrayList<>(student.viewRequests());
        list.sort(new RequestDateComparator()); // oldest first
        Logger.info("Student", student.getStudentId() + " viewed " + list.size() + " request(s)");
        return list;
    }

    /** Every student's requests, highest priority first — for the Academic Office Admin. */
    public List<Request> getAllRequests() {
        List<Request> all = new ArrayList<>();
        for (Student s : students) {
            all.addAll(s.viewRequests());
        }
        all.sort(new RequestPriorityComparator());
        return all;
    }

    /**
     * Writes all requests to data/requests.txt. Call this after the admin approves or
     * rejects a request (AcademicOfficeService only changes the status in memory).
     */
    public void saveRequests() {
        List<String> lines = new ArrayList<>();
        lines.add(REQUESTS_HEADER);
        for (Student s : students) {
            for (Request r : s.viewRequests()) {
                String type, detail1, detail2;
                if (r instanceof CourseClashRequest) {
                    CourseClashRequest c = (CourseClashRequest) r;
                    type = "CLASH";
                    detail1 = c.getConflictingSection().getSectionId();
                    detail2 = c.getRequestedSection().getSectionId();
                } else {
                    type = "GENERIC";
                    detail1 = ((GenericRequest) r).getCategory().name();
                    detail2 = "NONE";
                }
                lines.add(String.join("|", "REQUEST", r.getRequestId(), type, s.getStudentId(),
                        r.getRequestDate().toString(), r.getStatus().name(), String.valueOf(r.getPriority()),
                        detail1, detail2, clean(r.getDescription())));
            }
        }
        FileManager.writeAllLines(REQUESTS_FILE, lines);
    }

    private void loadRequests() {
        int loaded = 0;
        for (String line : FileManager.readLines(REQUESTS_FILE)) {
            String[] p = line.split("\\|", -1);
            if (p.length < 10 || !p[0].equals("REQUEST")) {
                continue;
            }
            Student student = findStudent(p[3]);
            if (student == null) {
                Logger.warn("StudentService", "Skipping request of unknown student: " + line);
                continue;
            }
            try {
                LocalDate date = LocalDate.parse(p[4]);
                RequestStatus status = RequestStatus.valueOf(p[5]);
                int priority = Integer.parseInt(p[6]);
                Request request;
                if (p[2].equals("CLASH")) {
                    Section current = findSection(p[7]);
                    Section wanted = findSection(p[8]);
                    if (current == null || wanted == null) {
                        Logger.warn("StudentService", "Skipping request with unknown section: " + line);
                        continue;
                    }
                    request = new CourseClashRequest(p[1], p[9], priority, current, wanted, date, status);
                } else {
                    request = new GenericRequest(p[1], p[9], priority, RequestCategory.valueOf(p[7]), date, status);
                }
                // Restored as-is (no re-validation): it was already checked when first submitted
                student.viewRequests().add(request);
                loaded++;
            } catch (RuntimeException e) {
                Logger.error("StudentService", "Invalid request record: " + line);
            }
        }
        Logger.info("StudentService", "Loaded " + loaded + " request(s) from file");
    }

    // ================================================================
    // ATTENDANCE (student view — records are marked by instructors)
    // ================================================================

    public List<Attendance> viewAttendance(Student student, Section section) {
        List<Attendance> records = new ArrayList<>();
        for (String line : FileManager.readLines(ATTENDANCE_FILE)) {
            String[] p = line.split("\\|", -1);
            if (p.length >= 5 && p[0].equals("ATTENDANCE")
                    && p[1].equals(student.getStudentId()) && p[2].equals(section.getSectionId())) {
                try {
                    records.add(new Attendance(student, section, LocalDate.parse(p[3]),
                            AttendanceStatus.valueOf(p[4])));
                } catch (RuntimeException e) {
                    Logger.error("StudentService", "Invalid attendance record: " + line);
                }
            }
        }
        Logger.info("Student", student.getStudentId() + " viewed " + records.size()
                + " attendance record(s) for " + section.getSectionId());
        return records;
    }

    /** Same rule as InstructorService: only PRESENT counts as attended. */
    public double viewAttendancePercentage(Student student, Section section) {
        List<Attendance> records = viewAttendance(student, section);
        if (records.isEmpty()) {
            return 0.0;
        }
        int present = 0;
        for (Attendance record : records) {
            if (record.getStatus() == AttendanceStatus.PRESENT) {
                present++;
            }
        }
        double percentage = present * 100.0 / records.size();
        Logger.info("Student", student.getStudentId() + " attendance in " + section.getSectionId()
                + ": " + String.format("%.1f", percentage) + "%");
        return percentage;
    }
}
