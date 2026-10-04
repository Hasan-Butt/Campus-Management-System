package com.fast.campus.service;

import com.fast.campus.enums.Day;
import com.fast.campus.enums.EnrollmentStatus;
import com.fast.campus.enums.RequestStatus;
import com.fast.campus.exception.CourseException;
import com.fast.campus.exception.CourseFullException;
import com.fast.campus.exception.CourseClashException;
import com.fast.campus.model.*;
import com.fast.campus.util.FileManager;
import com.fast.campus.util.Logger;
import com.fast.campus.util.CampusRegistry;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Academic Office Admin use cases.
 *
 * <p>Owner: Hasan</p>
 * Responsibilities:
 * <ul>
 *   <li>Course management: create, update, search</li>
 *   <li>Section management: create, update, set capacity</li>
 *   <li>Assign instructors and rooms to sections</li>
 *   <li>View, approve, and reject student requests</li>
 *   <li>Persist changes to data/courses.txt, data/sections.txt, data/enrollments.txt</li>
 *   <li>Log all significant operations via Logger</li>
 * </ul>
 */
public class AcademicOfficeService {

    private static final String COURSES_FILE     = "data/courses.txt";
    private static final String SECTIONS_FILE    = "data/sections.txt";
    private static final String ENROLLMENTS_FILE = "data/enrollments.txt";

    // In-memory storage for runtime operations
    private List<Course> courses = CampusRegistry.courses;
    private List<Section> sections = CampusRegistry.sections;
    private List<Request> requests = CampusRegistry.requests;

    // ================================================================
    // CONSTRUCTOR — LOAD EXISTING DATA
    // ================================================================

    /**
     * Constructor - loads existing data from persistence files.
     */
    public void loadAdmins() {}
    public void saveAdmins() {}

    public AcademicOfficeService() {
        loadCourseData();
        loadSectionData();
        loadEnrollmentData();
    }

    // ================================================================
    // DATA LOADING METHODS
    // ================================================================

    /**
     * Loads course records from data/courses.txt.
     * Format: COURSE|courseCode|title|creditHours
     */
    public void loadCourseData() {
        List<String> lines = FileManager.readLines(COURSES_FILE);
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 4 && parts[0].equals("COURSE")) {
                try {
                    String courseCode = parts[1].trim();
                    String title = parts[2].trim();
                    int creditHours = Integer.parseInt(parts[3].trim());

                    // Skip if already in memory
                    if (findCourseByCode(courseCode) == null) {
                        Course course = new Course(courseCode, title, creditHours);
                        courses.add(course);
                    }
                } catch (NumberFormatException e) {
                    Logger.error("AcademicOfficeService",
                            "Invalid credit hours in course record: " + line);
                }
            }
        }
        Logger.info("AcademicOfficeService",
                "Loaded " + courses.size() + " course(s) from file");
    }

    /**
     * Loads section records from data/sections.txt.
     * Format: SECTION|sectionId|courseCode|capacity|day|startTime|endTime|room
     */
    public void loadSectionData() {
        List<String> lines = FileManager.readLines(SECTIONS_FILE);
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 8 && parts[0].equals("SECTION")) {
                try {
                    String sectionId = parts[1].trim();
                    String courseCode = parts[2].trim();
                    int capacity = Integer.parseInt(parts[3].trim());
                    Day day = Day.valueOf(parts[4].trim().toUpperCase());
                    String startTime = parts[5].trim();
                    String endTime = parts[6].trim();
                    String room = parts[7].trim();

                    // Skip if already in memory
                    if (findSectionById(sectionId) == null) {
                        Course course = findCourseByCode(courseCode);
                        Schedule schedule = new Schedule(day, startTime, endTime, room);
                        Section section = new Section(sectionId, capacity, course, schedule);

                        sections.add(section);

                        // Link section to course if course exists
                        if (course != null && !course.getSections().contains(section)) {
                            course.addSection(section);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Logger.error("AcademicOfficeService",
                            "Invalid section record: " + line + " — " + e.getMessage());
                }
            }
        }
        Logger.info("AcademicOfficeService",
                "Loaded " + sections.size() + " section(s) from file");
    }

    /**
     * Loads enrollment records from data/enrollments.txt.
     * Format: ENROLLMENT|enrollmentId|studentId|sectionId|date|status
     * Note: Full reconstruction requires Student references from StudentService.
     */
    public void loadEnrollmentData() {
        List<String> lines = FileManager.readLines(ENROLLMENTS_FILE);
        Logger.info("AcademicOfficeService",
                "Loaded " + lines.size() + " enrollment record(s) from file");
    }

    // ================================================================
    // COURSE MANAGEMENT
    // ================================================================

    /**
     * Creates a new course and persists it to the data file.
     * Validates that the course is not null, has a valid code, and does not already exist.
     *
     * @param course the course to create
     * @throws CourseException if validation fails or course already exists
     */
    public void createCourse(Course course) throws CourseException {
        // Validate input
        if (course == null) {
            Logger.error("AcademicOfficeService", "Cannot create course — course is null");
            throw new CourseException("Course cannot be null");
        }

        if (course.getCourseCode() == null || course.getCourseCode().trim().isEmpty()) {
            Logger.error("AcademicOfficeService",
                    "Cannot create course — course code is null or empty");
            throw new CourseException("Course code cannot be null or empty");
        }

        if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
            Logger.error("AcademicOfficeService",
                    "Cannot create course — title is null or empty");
            throw new CourseException("Course title cannot be null or empty");
        }

        if (course.getCreditHours() <= 0) {
            Logger.error("AcademicOfficeService",
                    "Cannot create course — credit hours must be positive");
            throw new CourseException("Credit hours must be greater than 0");
        }

        // Check for duplicate course code
        Course existing = findCourseByCode(course.getCourseCode());
        if (existing != null) {
            String errorMsg = "Course with code " + course.getCourseCode() + " already exists";
            Logger.error("AcademicOfficeService", errorMsg);
            throw new CourseException(errorMsg);
        }

        // Add to in-memory list
        courses.add(course);

        // Persist to file
        String record = String.format("COURSE|%s|%s|%d",
                course.getCourseCode(),
                course.getTitle(),
                course.getCreditHours());
        FileManager.appendLine(COURSES_FILE, record);

        // Log successful creation
        Logger.info("AcademicOfficeAdmin",
                "Course created: " + course.getCourseCode()
                + " — " + course.getTitle()
                + " (" + course.getCreditHours() + " credit hours)");
    }

    /**
     * Updates an existing course's title and credit hours.
     * Finds the course by its code and applies the changes.
     *
     * @param courseCode     the code of the course to update
     * @param newTitle       the new title (null to keep existing)
     * @param newCreditHours the new credit hours (0 or negative to keep existing)
     * @throws CourseException if the course is not found
     */
    public void updateCourse(String courseCode, String newTitle, int newCreditHours)
            throws CourseException {

        if (courseCode == null || courseCode.trim().isEmpty()) {
            Logger.error("AcademicOfficeService",
                    "Cannot update course — course code is null or empty");
            throw new CourseException("Course code cannot be null or empty");
        }

        Course course = findCourseByCode(courseCode);
        if (course == null) {
            String errorMsg = "Course not found: " + courseCode;
            Logger.error("AcademicOfficeService", errorMsg);
            throw new CourseException(errorMsg);
        }

        // Apply updates
        String oldTitle = course.getTitle();
        int oldCredits = course.getCreditHours();

        if (newTitle != null && !newTitle.trim().isEmpty()) {
            course.setTitle(newTitle);
        }
        if (newCreditHours > 0) {
            course.setCreditHours(newCreditHours);
        }

        // Update the file — rewrite all course records
        rewriteCoursesFile();

        // Log the update
        Logger.info("AcademicOfficeAdmin",
                "Course updated: " + courseCode
                + " [Title: " + oldTitle + " → " + course.getTitle()
                + ", Credits: " + oldCredits + " → " + course.getCreditHours() + "]");
    }

    /**
     * Searches for a course by its course code.
     *
     * @param courseCode the course code to search for
     * @return the matching course, or null if not found
     */
    public Course searchCourse(String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            Logger.warn("AcademicOfficeService",
                    "Cannot search course — course code is null or empty");
            return null;
        }

        Course course = findCourseByCode(courseCode);
        if (course != null) {
            Logger.info("AcademicOfficeAdmin",
                    "Course found: " + course.getCourseCode()
                    + " — " + course.getTitle());
        } else {
            Logger.warn("AcademicOfficeAdmin",
                    "Course not found: " + courseCode);
        }

        return course;
    }

    // ================================================================
    // SECTION MANAGEMENT
    // ================================================================

    /**
     * Creates a new section for an existing course and persists it.
     *
     * @param section the section to create
     * @throws CourseException if the section is null, has invalid data, or the section ID is duplicated
     */
    public void createSection(Section section) throws CourseException {
        if (section == null) {
            Logger.error("AcademicOfficeService", "Cannot create section — section is null");
            throw new CourseException("Section cannot be null");
        }

        if (section.getSectionId() == null || section.getSectionId().trim().isEmpty()) {
            Logger.error("AcademicOfficeService",
                    "Cannot create section — section ID is null or empty");
            throw new CourseException("Section ID cannot be null or empty");
        }

        if (section.getCourse() == null) {
            Logger.error("AcademicOfficeService",
                    "Cannot create section — course reference is null");
            throw new CourseException("Section must be associated with a course");
        }

        if (section.getCapacity() <= 0) {
            Logger.error("AcademicOfficeService",
                    "Cannot create section — capacity must be positive");
            throw new CourseException("Section capacity must be greater than 0");
        }

        // Check for duplicate section ID
        Section existing = findSectionById(section.getSectionId());
        if (existing != null) {
            String errorMsg = "Section with ID " + section.getSectionId() + " already exists";
            Logger.error("AcademicOfficeService", errorMsg);
            throw new CourseException(errorMsg);
        }

        // Add to in-memory list
        sections.add(section);

        // Link section to its course
        Course course = section.getCourse();
        if (!course.getSections().contains(section)) {
            course.addSection(section);
        }

        // Persist to file
        persistSection(section);

        // Log successful creation
        Logger.info("AcademicOfficeAdmin",
                "Section created: " + section.getSectionId()
                + " for course " + course.getCourseCode()
                + " (Capacity: " + section.getCapacity() + ")");
    }

    /**
     * Updates a section's capacity.
     *
     * @param sectionId   the ID of the section to update
     * @param newCapacity the new capacity
     * @throws CourseException if the section is not found or capacity is invalid
     */
    public void updateSection(String sectionId, int newCapacity) throws CourseException {
        if (sectionId == null || sectionId.trim().isEmpty()) {
            Logger.error("AcademicOfficeService",
                    "Cannot update section — section ID is null or empty");
            throw new CourseException("Section ID cannot be null or empty");
        }

        Section section = findSectionById(sectionId);
        if (section == null) {
            String errorMsg = "Section not found: " + sectionId;
            Logger.error("AcademicOfficeService", errorMsg);
            throw new CourseException(errorMsg);
        }

        if (newCapacity <= 0) {
            Logger.error("AcademicOfficeService",
                    "Cannot update section — capacity must be positive");
            throw new CourseException("Section capacity must be greater than 0");
        }

        int currentEnrolled = section.getEnrollments().size();
        if (newCapacity < currentEnrolled) {
            Logger.warn("AcademicOfficeService",
                    "New capacity (" + newCapacity + ") is less than current enrollment ("
                    + currentEnrolled + ") in section " + sectionId);
        }

        int oldCapacity = section.getCapacity();
        section.setCapacity(newCapacity);

        // Update in file
        rewriteSectionsFile();

        Logger.info("AcademicOfficeAdmin",
                "Section updated: " + sectionId
                + " [Capacity: " + oldCapacity + " → " + newCapacity + "]");
    }

    /**
     * Sets the capacity for a section.
     *
     * @param section  the section
     * @param capacity the new capacity
     * @throws CourseException if validation fails
     */
    public void setCapacity(Section section, int capacity) throws CourseException {
        if (section == null) {
            Logger.error("AcademicOfficeService",
                    "Cannot set capacity — section is null");
            throw new CourseException("Section cannot be null");
        }

        if (capacity <= 0) {
            Logger.error("AcademicOfficeService",
                    "Cannot set capacity — must be positive");
            throw new CourseException("Capacity must be greater than 0");
        }

        int oldCapacity = section.getCapacity();
        section.setCapacity(capacity);

        // Update in file
        rewriteSectionsFile();

        Logger.info("AcademicOfficeAdmin",
                "Section capacity updated: " + section.getSectionId()
                + " [" + oldCapacity + " → " + capacity + "]");
    }

    /**
     * Assigns a room to a section via a Schedule.
     * If the section already has a schedule, updates the room.
     * If not, sets the provided schedule.
     *
     * @param sectionId the section ID
     * @param room      the room to assign
     * @throws CourseException if section not found
     */
    public void assignRoom(String sectionId, String room) throws CourseException {
        if (sectionId == null || sectionId.trim().isEmpty()) {
            Logger.error("AcademicOfficeService",
                    "Cannot assign room — section ID is null or empty");
            throw new CourseException("Section ID cannot be null or empty");
        }

        if (room == null || room.trim().isEmpty()) {
            Logger.error("AcademicOfficeService",
                    "Cannot assign room — room is null or empty");
            throw new CourseException("Room cannot be null or empty");
        }

        Section section = findSectionById(sectionId);
        if (section == null) {
            String errorMsg = "Section not found: " + sectionId;
            Logger.error("AcademicOfficeService", errorMsg);
            throw new CourseException(errorMsg);
        }

        if (section.getSchedule() != null) {
            section.getSchedule().setRoom(room);
        } else {
            Logger.warn("AcademicOfficeService",
                    "Section " + sectionId + " has no schedule — cannot assign room without a schedule");
            throw new CourseException("Section " + sectionId
                    + " has no schedule. Create a schedule before assigning a room.");
        }

        // Update in file
        rewriteSectionsFile();

        Logger.info("AcademicOfficeAdmin",
                "Room assigned: " + room + " → section " + sectionId);
    }

    /**
     * Assigns a room via a Schedule object (overload for model compatibility).
     *
     * @param section  the section
     * @param schedule the schedule containing the room
     * @throws CourseException if validation fails
     */
    public void assignRoom(Section section, Schedule schedule) throws CourseException {
        if (section == null) {
            Logger.error("AcademicOfficeService",
                    "Cannot assign room — section is null");
            throw new CourseException("Section cannot be null");
        }
        if (schedule == null) {
            Logger.error("AcademicOfficeService",
                    "Cannot assign room — schedule is null");
            throw new CourseException("Schedule cannot be null");
        }

        section.setSchedule(schedule);

        // Ensure section is tracked
        if (findSectionById(section.getSectionId()) == null) {
            sections.add(section);
        }

        // Update in file
        rewriteSectionsFile();

        Logger.info("AcademicOfficeAdmin",
                "Room assigned: " + schedule.getRoom()
                + " → section " + section.getSectionId()
                + " (" + schedule.getScheduleInfo() + ")");
    }

    /**
     * Assigns an instructor to a section.
     * Updates both the section model and the instructor's assigned sections list.
     *
     * @param sectionId  the section ID
     * @param instructor the instructor to assign
     * @throws CourseException if section not found or instructor is null
     */
    public void assignInstructor(String sectionId, Instructor instructor) throws CourseException {
        if (sectionId == null || sectionId.trim().isEmpty()) {
            Logger.error("AcademicOfficeService",
                    "Cannot assign instructor — section ID is null or empty");
            throw new CourseException("Section ID cannot be null or empty");
        }

        if (instructor == null) {
            Logger.error("AcademicOfficeService",
                    "Cannot assign instructor — instructor is null");
            throw new CourseException("Instructor cannot be null");
        }

        Section section = findSectionById(sectionId);
        if (section == null) {
            String errorMsg = "Section not found: " + sectionId;
            Logger.error("AcademicOfficeService", errorMsg);
            throw new CourseException(errorMsg);
        }

        // Log if replacing an existing instructor
        if (section.getInstructor() != null) {
            Logger.warn("AcademicOfficeService",
                    "Replacing instructor " + section.getInstructor().getName()
                    + " with " + instructor.getName()
                    + " in section " + sectionId);
        }

        // Assign at model level
        section.assignInstructor(instructor);
        instructor.addSection(section);

        // Update in file
        rewriteSectionsFile();

        Logger.info("AcademicOfficeAdmin",
                "Instructor assigned: " + instructor.getName()
                + " (ID: " + instructor.getTeacherId() + ")"
                + " → section " + sectionId
                + " [" + section.getCourse().getCourseCode() + "]");
    }

    /**
     * Assigns an instructor to a section (overload accepting Section object).
     *
     * @param section    the section
     * @param instructor the instructor to assign
     * @throws CourseException if validation fails
     */
    public void assignInstructor(Section section, Instructor instructor) throws CourseException {
        if (section == null) {
            Logger.error("AcademicOfficeService",
                    "Cannot assign instructor — section is null");
            throw new CourseException("Section cannot be null");
        }
        if (instructor == null) {
            Logger.error("AcademicOfficeService",
                    "Cannot assign instructor — instructor is null");
            throw new CourseException("Instructor cannot be null");
        }

        // Assign at model level
        section.assignInstructor(instructor);
        instructor.addSection(section);

        // Ensure section is tracked
        if (findSectionById(section.getSectionId()) == null) {
            sections.add(section);
        }

        // Update in file
        rewriteSectionsFile();

        Logger.info("AcademicOfficeAdmin",
                "Instructor assigned: " + instructor.getName()
                + " (ID: " + instructor.getTeacherId() + ")"
                + " → section " + section.getSectionId()
                + " [" + section.getCourse().getCourseCode() + "]");
    }

    // ================================================================
    // REQUEST MANAGEMENT
    // ================================================================

    /**
     * Displays all pending requests.
     *
     * @param requests the list of requests to view
     * @return list of pending requests
     */
    public List<Request> viewRequests(List<Request> requests) {
        if (requests == null || requests.isEmpty()) {
            Logger.warn("AcademicOfficeAdmin", "No requests to view");
            return new ArrayList<>();
        }

        List<Request> pendingRequests = requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING)
                .collect(Collectors.toList());

        Logger.info("AcademicOfficeAdmin",
                "Viewing " + pendingRequests.size() + " pending request(s) out of "
                + requests.size() + " total");

        for (Request request : pendingRequests) {
            System.out.println("  [" + request.getRequestId() + "] "
                    + request.getDescription()
                    + " (Status: " + request.getStatus()
                    + ", Priority: " + request.getPriority()
                    + ", Date: " + request.getRequestDate() + ")");
        }

        return pendingRequests;
    }

    /**
     * Approves a student request.
     * Sets the status to APPROVED and logs the action.
     *
     * @param request the request to approve
     * @throws CourseException if request is null or not in PENDING status
     */
    public void approveRequest(Request request) throws CourseException {
        if (request == null) {
            Logger.error("AcademicOfficeService",
                    "Cannot approve request — request is null");
            throw new CourseException("Request cannot be null");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            String errorMsg = "Request " + request.getRequestId()
                    + " is already " + request.getStatus() + " — cannot approve";
            Logger.warn("AcademicOfficeAdmin", errorMsg);
            throw new CourseException(errorMsg);
        }

        // Update status at model level
        request.setStatus(RequestStatus.APPROVED);

        Logger.info("AcademicOfficeAdmin",
                "Request approved: " + request.getRequestId()
                + " — " + request.getDescription());
    }

    /**
     * Rejects a student request.
     * Sets the status to REJECTED and logs the action.
     *
     * @param request the request to reject
     * @throws CourseException if request is null or not in PENDING status
     */
    public void rejectRequest(Request request) throws CourseException {
        if (request == null) {
            Logger.error("AcademicOfficeService",
                    "Cannot reject request — request is null");
            throw new CourseException("Request cannot be null");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            String errorMsg = "Request " + request.getRequestId()
                    + " is already " + request.getStatus() + " — cannot reject";
            Logger.warn("AcademicOfficeAdmin", errorMsg);
            throw new CourseException(errorMsg);
        }

        // Update status at model level
        request.setStatus(RequestStatus.REJECTED);

        Logger.info("AcademicOfficeAdmin",
                "Request rejected: " + request.getRequestId()
                + " — " + request.getDescription());
    }

    // ================================================================
    // ENROLLMENT PERSISTENCE
    // ================================================================

    /**
     * Persists an enrollment record to the enrollments file.
     *
     * @param enrollment the enrollment to persist
     */
    public void saveEnrollment(Enrollment enrollment) {
        if (enrollment == null) {
            Logger.error("AcademicOfficeService", "Cannot save null enrollment");
            return;
        }

        String record = String.format("ENROLLMENT|%s|%s|%s|%s|%s",
                enrollment.getEnrollmentId(),
                enrollment.getStudent() != null ? enrollment.getStudent().getStudentId() : "UNKNOWN",
                enrollment.getSection() != null ? enrollment.getSection().getSectionId() : "UNKNOWN",
                enrollment.getEnrollmentDate().toString(),
                enrollment.getStatus().name());
        FileManager.appendLine(ENROLLMENTS_FILE, record);

        Logger.info("AcademicOfficeAdmin",
                "Enrollment saved: " + enrollment.getEnrollmentId()
                + " [" + enrollment.getStudent().getName()
                + " → " + enrollment.getSection().getSectionId() + "]");
    }

    // ================================================================
    // FILE PERSISTENCE HELPERS
    // ================================================================

    /**
     * Rewrites the entire courses file from in-memory data.
     */
    private void rewriteCoursesFile() {
        List<String> lines = new ArrayList<>();
        for (Course course : courses) {
            String record = String.format("COURSE|%s|%s|%d",
                    course.getCourseCode(),
                    course.getTitle(),
                    course.getCreditHours());
            lines.add(record);
        }
        FileManager.writeAllLines(COURSES_FILE, lines);
    }

    /**
     * Rewrites the entire sections file from in-memory data.
     */
    private void rewriteSectionsFile() {
        List<String> lines = new ArrayList<>();
        for (Section section : sections) {
            lines.add(formatSectionRecord(section));
        }
        FileManager.writeAllLines(SECTIONS_FILE, lines);
    }

    /**
     * Persists a single section record to the file.
     */
    private void persistSection(Section section) {
        String record = formatSectionRecord(section);
        FileManager.appendLine(SECTIONS_FILE, record);
    }

    /**
     * Formats a section into a pipe-delimited record string.
     * Format: SECTION|sectionId|courseCode|capacity|day|startTime|endTime|room
     */
    private String formatSectionRecord(Section section) {
        String courseCode = section.getCourse() != null
                ? section.getCourse().getCourseCode() : "NONE";
        String day = "MONDAY";
        String startTime = "00:00";
        String endTime = "00:00";
        String room = "TBD";

        if (section.getSchedule() != null) {
            Schedule sch = section.getSchedule();
            day = sch.getDay().name();
            startTime = sch.getStartTime().toString();
            endTime = sch.getEndTime().toString();
            room = sch.getRoom() != null ? sch.getRoom() : "TBD";
        }

        String instructorId = section.getInstructor() != null
                ? section.getInstructor().getTeacherId() : "NONE";

        return String.format("SECTION|%s|%s|%d|%s|%s|%s|%s|%s",
                section.getSectionId(),
                courseCode,
                section.getCapacity(),
                day,
                startTime,
                endTime,
                room,
                instructorId);
    }

    // ================================================================
    // QUERY & HELPER METHODS
    // ================================================================

    /**
     * Finds a course by its course code (case-insensitive).
     *
     * @param courseCode the course code to search for
     * @return the course, or null if not found
     */
    private Course findCourseByCode(String courseCode) {
        if (courseCode == null) return null;
        for (Course course : courses) {
            if (course.getCourseCode().equalsIgnoreCase(courseCode)) {
                return course;
            }
        }
        return null;
    }

    /**
     * Finds a section by its ID.
     *
     * @param sectionId the section ID to search for
     * @return the section, or null if not found
     */
    private Section findSectionById(String sectionId) {
        if (sectionId == null) return null;
        for (Section section : sections) {
            if (section.getSectionId().equals(sectionId)) {
                return section;
            }
        }
        return null;
    }

    // ================================================================
    // GETTERS (for external access to in-memory data)
    // ================================================================

    /**
     * Returns all courses in the system.
     */
    public List<Course> getCourses() {
        return new ArrayList<>(courses);
    }

    /**
     * Returns all sections in the system.
     */
    public List<Section> getSections() {
        return new ArrayList<>(sections);
    }

    /**
     * Returns the total number of courses.
     */
    public int getTotalCourses() {
        return courses.size();
    }

    /**
     * Returns the total number of sections.
     */
    public int getTotalSections() {
        return sections.size();
    }

    public void loadCourses() {
        courses.clear();
        List<String> lines = FileManager.readLines("data/courses.txt");
        for (String line : lines) {
            if (line.startsWith("#") || line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length >= 4 && parts[0].equals("COURSE")) {
                Course c = new Course(parts[1], parts[2], Integer.parseInt(parts[3]));
                courses.add(c);
            }
        }
    }

    public void saveCourses() {
        List<String> lines = new ArrayList<>();
        lines.add("# Format: COURSE|courseCode|title|creditHours");
        for (Course c : courses) {
            lines.add("COURSE|" + c.getCourseCode() + "|" + c.getTitle() + "|" + c.getCreditHours());
        }
        FileManager.writeAllLines("data/courses.txt", lines);
    }

    public void loadSections() {
        sections.clear();
        List<String> lines = FileManager.readLines("data/sections.txt");
        for (String line : lines) {
            if (line.startsWith("#") || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 8 && p[0].equals("SECTION")) {
                Course c = CampusRegistry.findCourse(p[3]);
                if (c == null) continue;
                
                com.fast.campus.enums.Day day = com.fast.campus.enums.Day.valueOf(p[4]);
                Schedule s = new Schedule(day, p[5], p[6], p[7]);
                Section sec = new Section(p[1], Integer.parseInt(p[2]), c, s);
                if (p.length > 8 && !p[8].equals("null")) {
                    Instructor inst = CampusRegistry.findInstructor(p[8]);
                    if (inst != null) {
                        sec.assignInstructor(inst);
                        inst.addSection(sec);
                    }
                }
                sections.add(sec);
            }
        }
    }

    public void saveSections() {
        List<String> lines = new ArrayList<>();
        lines.add("# Format: SECTION|sectionId|capacity|courseCode|day|startTime|endTime|room|instructorId");
        for (Section s : sections) {
            String inst = (s.getInstructor() != null) ? s.getInstructor().getTeacherId() : "null";
            lines.add("SECTION|" + s.getSectionId() + "|" + s.getCapacity() + "|" + s.getCourse().getCourseCode() + "|" + 
                      s.getSchedule().getDay() + "|" + s.getSchedule().getStartTime() + "|" + s.getSchedule().getEndTime() + "|" + 
                      s.getSchedule().getRoom() + "|" + inst);
        }
        FileManager.writeAllLines("data/sections.txt", lines);
    }

}
