package com.fast.campus.service;

import com.fast.campus.enums.AttendanceStatus;
import com.fast.campus.exception.FYPException;
import com.fast.campus.exception.InvalidFYPEvaluationException;
import com.fast.campus.exception.InvalidFYPGroupException;
import com.fast.campus.exception.UnauthorizedActionException;
import com.fast.campus.model.*;
import com.fast.campus.util.FileManager;
import com.fast.campus.util.Logger;
import com.fast.campus.util.CampusRegistry;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Instructor and FYP use cases.
 *
 * <p>Owner: Saim</p>
 * Responsibilities:
 * <ul>
 *   <li>Instructor operations: view assigned courses, sections, and students</li>
 *   <li>Attendance: mark, update, calculate percentage</li>
 *   <li>TA assignment (PermanentInstructor only)</li>
 *   <li>FYP: group management, meeting scheduling, evaluation, feedback</li>
 *   <li>Persist to data/instructors.txt, data/fypgroups.txt,
 *       data/fypmeetings.txt, data/fyp_evaluations.txt</li>
 * </ul>
 */
public class InstructorService {

    private static final String INSTRUCTORS_FILE   = "data/instructors.txt";
    private static final String FYPGROUPS_FILE     = "data/fyp_groups.txt";
    private static final String FYPMEETINGS_FILE   = "data/fypmeetings.txt";
    private static final String FYPEVALUATIONS_FILE = "data/fyp_evaluations.txt";
    private static final String ATTENDANCE_FILE    = "data/attendance.txt";
    
    // In-memory storage for runtime operations
    private List<Instructor> instructors = CampusRegistry.instructors;
    private List<FYPGroup> fypGroups = CampusRegistry.fypGroups;
    private List<FYPMeeting> fypMeetings = CampusRegistry.fypMeetings;
    private List<FYPEvaluation> fypEvaluations = CampusRegistry.fypEvaluations;
    private List<Attendance> attendanceRecords = CampusRegistry.attendanceRecords;

    /**
     * Constructor - loads existing data from persistence files
     */
    public InstructorService() {
        loadInstructors();
        loadAttendance();
        loadFYPGroups();
        loadFYPMeetings();
        loadFYPEvaluations();
    }
    
    // ================================================================
    // DATA LOADING METHODS
    // ================================================================
    
    /**
     * Load instructor records from file on initialization.
     * Format: INSTRUCTOR|teacherId|name|email|phone|type
     */
    public void loadInstructorData() {
        List<String> lines = FileManager.readLines(INSTRUCTORS_FILE);
        // Note: Full reconstruction would require re-creating Instructor objects
        // For now, we log the count. Full implementation would parse and instantiate.
        Logger.info("InstructorService", "Loaded " + lines.size() + " instructor records");
    }

    /**
     * Load attendance records from file on initialization.
     * Format: ATTENDANCE|studentId|sectionId|date|status
     */
    public void loadAttendanceData() {
        List<String> lines = FileManager.readLines(ATTENDANCE_FILE);
        // Note: Full reconstruction would require Student/Section references
        Logger.info("InstructorService", "Loaded " + lines.size() + " attendance records");
    }
    
    /**
     * Load FYP groups from file on initialization.
     * Format: FYPGROUP|groupId|title|description|supervisorId
     */
    public void loadFYPGroupData() {
        List<String> lines = FileManager.readLines(FYPGROUPS_FILE);
        Logger.info("InstructorService", "Loaded " + lines.size() + " FYP groups");
    }
    
    /**
     * Load FYP meetings from file on initialization.
     * Format: FYPMEETING|meetingId|groupId|date|agenda|notes
     */
    public void loadFYPMeetingData() {
        List<String> lines = FileManager.readLines(FYPMEETINGS_FILE);
        Logger.info("InstructorService", "Loaded " + lines.size() + " FYP meetings");
    }
    
    /**
     * Load FYP evaluations from file on initialization.
     * Format: FYPEVALUATION|evaluationId|groupId|date|score|feedback
     */
    public void loadFYPEvaluationData() {
        List<String> lines = FileManager.readLines(FYPEVALUATIONS_FILE);
        Logger.info("InstructorService", "Loaded " + lines.size() + " FYP evaluations");
    }
    
    // ================================================================
    // INSTRUCTOR VIEWING OPERATIONS
    // ================================================================
    
    /**
     * View all courses assigned to an instructor.
     * 
     * @param instructor the instructor
     * @return list of unique courses from assigned sections
     */
    public List<Course> viewAssignedCourses(Instructor instructor) {
        if (instructor == null) {
            Logger.error("InstructorService", "Cannot view courses - instructor is null");
            return new ArrayList<>();
        }
        
        List<Course> courses = instructor.getAssignedSections().stream()
                .map(Section::getCourse)
                .distinct()
                .collect(Collectors.toList());
        
        Logger.info(instructor.getTeacherId(),
                "Viewed " + courses.size() + " assigned course(s)");
        
        return courses;
    }
    
    /**
     * View all sections assigned to an instructor.
     * 
     * @param instructor the instructor
     * @return list of assigned sections
     */
    public List<Section> viewAssignedSections(Instructor instructor) {
        if (instructor == null) {
            Logger.error("InstructorService", "Cannot view sections - instructor is null");
            return new ArrayList<>();
        }
        
        List<Section> sections = instructor.getAssignedSections();
        
        Logger.info(instructor.getTeacherId(),
                "Viewed " + sections.size() + " assigned section(s)");
        
        return sections;
    }
    
    /**
     * View all students enrolled in an instructor's sections.
     * 
     * @param instructor the instructor
     * @param section specific section (optional - null for all sections)
     * @return list of enrolled students
     */
    public List<Student> viewEnrolledStudents(Instructor instructor, Section section) {
        if (instructor == null) {
            Logger.error("InstructorService", "Cannot view students - instructor is null");
            return new ArrayList<>();
        }
        
        List<Student> students = new ArrayList<>();
        
        if (section != null) {
            // View students in specific section
            if (!instructor.getAssignedSections().contains(section)) {
                Logger.warn(instructor.getTeacherId(),
                        "Instructor not assigned to section " + section.getSectionId());
                return students;
            }
            students.addAll(section.getEnrolledStudents());
        } else {
            // View all students across all assigned sections
            for (Section sec : instructor.getAssignedSections()) {
                students.addAll(sec.getEnrolledStudents());
            }
        }
        
        Logger.info(instructor.getTeacherId(),
                "Viewed " + students.size() + " enrolled student(s)");
        
        return students;
    }


    // ================================================================
    // ATTENDANCE MANAGEMENT
    // ================================================================

    /**
     * Marks attendance for a student in a section.
     * Validates that instructor is assigned to section and student is enrolled.
     * Prevents duplicate attendance entries for the same date.
     * 
     * @param instructor the instructor marking attendance
     * @param student the student whose attendance is being marked
     * @param section the section for which attendance is being marked
     * @param status the attendance status (PRESENT, ABSENT, LATE)
     * @throws UnauthorizedActionException if validation fails
     */
    public void markAttendance(Instructor instructor, Student student,
                               Section section, AttendanceStatus status)
            throws UnauthorizedActionException {

        // Validate required parameters
        if (instructor == null) {
            Logger.error("SERVICE", "Cannot mark attendance - instructor is null");
            throw new UnauthorizedActionException("Instructor cannot be null");
        }

        if (student == null) {
            Logger.error(instructor.getTeacherId(), "Cannot mark attendance - student is null");
            throw new UnauthorizedActionException("Student cannot be null");
        }

        if (section == null) {
            Logger.error(instructor.getTeacherId(), "Cannot mark attendance - section is null");
            throw new UnauthorizedActionException("Section cannot be null");
        }

        if (status == null) {
            Logger.error(instructor.getTeacherId(), "Cannot mark attendance - status is null");
            throw new UnauthorizedActionException("Attendance status cannot be null");
        }

        // Verify that the instructor is assigned to this section
        if (!instructor.getAssignedSections().contains(section)) {
            String errorMsg = "Instructor " + instructor.getTeacherId()
                    + " is not assigned to section " + section.getSectionId();
            Logger.error(instructor.getTeacherId(), errorMsg);
            throw new UnauthorizedActionException(errorMsg);
        }

        // Verify that the student is enrolled in this section
        if (!section.getEnrolledStudents().contains(student)) {
            String errorMsg = "Student " + student.getStudentId()
                    + " is not enrolled in section " + section.getSectionId();
            Logger.error(instructor.getTeacherId(), errorMsg);
            throw new UnauthorizedActionException(errorMsg);
        }

        // Attendance is marked for the current date
        LocalDate date = LocalDate.now();

        // Prevent duplicate attendance for the same student on the same date
        for (Attendance existing : attendanceRecords) {
            if (existing.getStudent().equals(student)
                    && existing.getSection().equals(section)
                    && existing.getDate().equals(date)) {
                String errorMsg = "Attendance already marked for student "
                        + student.getStudentId() + " on " + date;
                Logger.warn(instructor.getTeacherId(), errorMsg);
                throw new UnauthorizedActionException(errorMsg);
            }
        }

        // Create the attendance record
        Attendance attendance = new Attendance(student, section, date, status);
        attendanceRecords.add(attendance);

        // Delegate to the instructor model
        instructor.markAttendance(attendance, status);

        // Persist to file
        String record = String.format("ATTENDANCE|%s|%s|%s|%s",
                student.getStudentId(),
                section.getSectionId(),
                date.toString(),
                status.name());
        FileManager.appendLine(ATTENDANCE_FILE, record);

        // Log successful operation
        Logger.info(instructor.getTeacherId(),
                "Marked " + status + " for student " + student.getStudentId()
                        + " in section " + section.getSectionId() + " on " + date);
    }

    /**
     * Updates an existing attendance record with a new status.
     * Useful for correcting mistakes or late updates.
     * 
     * @param instructor the instructor updating the attendance
     * @param attendance the attendance record to update
     * @param newStatus the new attendance status
     * @throws UnauthorizedActionException if instructor not authorized
     */
    public void updateAttendance(Instructor instructor, Attendance attendance, AttendanceStatus newStatus)
            throws UnauthorizedActionException {
        
        if (instructor == null) {
            Logger.error("InstructorService", "Cannot update attendance - instructor is null");
            throw new UnauthorizedActionException("Instructor cannot be null");
        }
        
        if (attendance == null) {
            Logger.error(instructor.getTeacherId(), "Cannot update null attendance record");
            throw new UnauthorizedActionException("Attendance record cannot be null");
        }
        
        if (newStatus == null) {
            Logger.error(instructor.getTeacherId(), "Cannot update attendance with null status");
            throw new UnauthorizedActionException("New status cannot be null");
        }
        
        // Verify instructor is assigned to the section
        if (!instructor.getAssignedSections().contains(attendance.getSection())) {
            String errorMsg = "Instructor " + instructor.getTeacherId()
                    + " is not assigned to section " + attendance.getSection().getSectionId();
            Logger.error(instructor.getTeacherId(), errorMsg);
            throw new UnauthorizedActionException(errorMsg);
        }
        
        AttendanceStatus oldStatus = attendance.getStatus();
        attendance.setStatus(newStatus);
        
        // Delegate to the instructor model
        instructor.updateAttendance(attendance, newStatus);
        
        // Update file - rewrite all attendance records with the update
        List<String> updatedLines = new ArrayList<>();
        List<String> existingLines = FileManager.readLines(ATTENDANCE_FILE);
        
        for (String line : existingLines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 5 && parts[0].equals("ATTENDANCE")) {
                // Check if this is the record to update
                if (parts[1].equals(attendance.getStudent().getStudentId())
                        && parts[2].equals(attendance.getSection().getSectionId())
                        && parts[3].equals(attendance.getDate().toString())) {
                    // Update the status
                    line = String.format("ATTENDANCE|%s|%s|%s|%s",
                            parts[1], parts[2], parts[3], newStatus.name());
                }
            }
            updatedLines.add(line);
        }
        
        FileManager.writeAllLines(ATTENDANCE_FILE, updatedLines);
        
        Logger.info(instructor.getTeacherId(), 
                "Attendance updated for " + attendance.getStudent().getName()
                + " from " + oldStatus + " to " + newStatus
                + " on " + attendance.getDate());
    }

    /**
     * Calculates the attendance percentage for a student in a section.
     * Percentage = (number of PRESENT sessions / total sessions) * 100
     * 
     * @param student the student
     * @param section the section
     * @return the attendance percentage (0.0 to 100.0)
     */
    public double calculateAttendancePercentage(Student student, Section section) {
        if (student == null || section == null) {
            Logger.error("InstructorService", 
                    "Cannot calculate attendance - student or section is null");
            return 0.0;
        }
        
        int totalSessions = 0;
        int presentCount = 0;
        
        // Count attendance records for this student in this section from memory
        for (Attendance record : attendanceRecords) {
            if (record.getStudent().equals(student) 
                    && record.getSection().equals(section)) {
                totalSessions++;
                if (record.getStatus() == AttendanceStatus.PRESENT) {
                    presentCount++;
                }
            }
        }
        
        // Also check file for any records not in memory
        List<String> lines = FileManager.readLines(ATTENDANCE_FILE);
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 5 && parts[0].equals("ATTENDANCE")) {
                if (parts[1].equals(student.getStudentId())
                        && parts[2].equals(section.getSectionId())) {
                    // Check if already counted in memory to avoid duplicates
                    boolean alreadyCounted = false;
                    for (Attendance record : attendanceRecords) {
                        if (record.getStudent().getStudentId().equals(parts[1])
                                && record.getSection().getSectionId().equals(parts[2])
                                && record.getDate().toString().equals(parts[3])) {
                            alreadyCounted = true;
                            break;
                        }
                    }
                    
                    if (!alreadyCounted) {
                        totalSessions++;
                        if (parts[4].equals(AttendanceStatus.PRESENT.name())) {
                            presentCount++;
                        }
                    }
                }
            }
        }
        
        if (totalSessions == 0) {
            Logger.warn("InstructorService", 
                    "No attendance records found for student " + student.getStudentId()
                    + " in section " + section.getSectionId());
            return 0.0;
        }
        
        double percentage = (presentCount * 100.0) / totalSessions;
        Logger.info("InstructorService",
                "Attendance percentage for " + student.getStudentId()
                + " in section " + section.getSectionId() + ": "
                + String.format("%.2f%%", percentage)
                + " (" + presentCount + "/" + totalSessions + ")");
        
        return percentage;
    }


    // ================================================================
    // TEACHING ASSISTANT ASSIGNMENT (PermanentInstructor only)
    // ================================================================

    /**
     * Assigns a Teaching Assistant to a section (PermanentInstructor only).
     * Converts a NormalStudent to a TeachingAssistant role for the specified section.
     * Only permanent instructors have the authority to assign TAs.
     * 
     * @param instructor the permanent instructor making the assignment
     * @param student the student to assign as TA
     * @param section the section to assign the TA to
     * @throws UnauthorizedActionException if instructor is not permanent or other validation fails
     */
    public void assignTA(PermanentInstructor instructor, NormalStudent student, Section section)
            throws UnauthorizedActionException {
        
        if (instructor == null) {
            Logger.error("InstructorService", "Cannot assign TA - instructor is null");
            throw new UnauthorizedActionException("Instructor cannot be null");
        }
        
        if (student == null) {
            Logger.error(instructor.getTeacherId(), "Cannot assign TA - student is null");
            throw new UnauthorizedActionException("Student cannot be null");
        }
        
        if (section == null) {
            Logger.error(instructor.getTeacherId(), "Cannot assign TA - section is null");
            throw new UnauthorizedActionException("Section cannot be null");
        }
        
        // Verify the section doesn't already have a TA
        if (section.getTeachingAssistant() != null) {
            Logger.warn(instructor.getTeacherId(),
                    "Section " + section.getSectionId() + " already has a TA assigned: "
                    + section.getTeachingAssistant().getName());
        }
        
        instructor.assignTA(student, section); // also links the new TA to the section

        // The TA (role + section) is persisted in students.txt by StudentService.promoteToTA,
        // so nothing is appended to instructors.txt here (it only holds INSTRUCTOR records).

        Logger.info(instructor.getTeacherId(),
                "TA assigned: " + student.getName() 
                + " (ID: " + student.getStudentId() + ") to section " 
                + section.getSectionId());
    }
    
    // ================================================================
    // FYP GROUP MANAGEMENT
    // ================================================================

    /**
     * Creates and persists a new FYP group.
     * Validates group information and ensures no duplicate group IDs exist.
     * 
     * @param group the FYP group to create
     * @return the created FYP group
     * @throws InvalidFYPGroupException if validation fails
     */
    public FYPGroup createFYPGroup(FYPGroup group) throws InvalidFYPGroupException {
        if (group == null) {
            Logger.error("InstructorService", "Cannot create null FYP group");
            throw new InvalidFYPGroupException("FYP group cannot be null");
        }
        
        if (group.getGroupId() == null || group.getGroupId().trim().isEmpty()) {
            Logger.error("InstructorService", "Cannot create FYP group with null or empty ID");
            throw new InvalidFYPGroupException("FYP group ID cannot be null or empty");
        }
        
        if (group.getTitle() == null || group.getTitle().trim().isEmpty()) {
            Logger.error("InstructorService", "Cannot create FYP group with null or empty title");
            throw new InvalidFYPGroupException("FYP group title cannot be null or empty");
        }
        
        // Check for duplicate group ID
        for (FYPGroup existing : fypGroups) {
            if (existing.getGroupId().equals(group.getGroupId())) {
                String errorMsg = "FYP group with ID " + group.getGroupId() + " already exists";
                Logger.error("InstructorService", errorMsg);
                throw new InvalidFYPGroupException(errorMsg);
            }
        }
        
        // Add to in-memory storage
        fypGroups.add(group);
        
        // Persist to file (same format as saveFYPGroups/loadFYPGroups)
        saveFYPGroups();
        String supervisorId = group.getSupervisor() != null
                ? group.getSupervisor().getTeacherId()
                : "NONE";
        int memberCount = group.getMembers() != null ? group.getMembers().size() : 0;

        Logger.info("InstructorService", 
                "FYP group created: " + group.getGroupId() 
                + " - " + group.getTitle()
                + " (Members: " + memberCount + ", Supervisor: " + supervisorId + ")");
        
        return group;
    }
    
    /**
     * Adds a student member to an FYP group.
     * 
     * @param group the FYP group
     * @param student the student to add
     * @throws InvalidFYPGroupException if validation fails
     */
    public void addMemberToFYPGroup(FYPGroup group, Student student) throws InvalidFYPGroupException {
        if (group == null) {
            Logger.error("InstructorService", "Cannot add member - group is null");
            throw new InvalidFYPGroupException("FYP group cannot be null");
        }
        
        if (student == null) {
            Logger.error("InstructorService", "Cannot add null student to FYP group");
            throw new InvalidFYPGroupException("Student cannot be null");
        }
        
        // Check if student is already a member
        if (group.getMembers().contains(student)) {
            Logger.warn("InstructorService",
                    "Student " + student.getStudentId() + " is already a member of group " + group.getGroupId());
            return;
        }
        
        group.addMember(student);
        
        // Update the group record in file
        updateFYPGroupInFile(group);
        
        Logger.info("InstructorService",
                "Added student " + student.getName() + " to FYP group " + group.getGroupId());
    }
    
    /**
     * Removes a student member from an FYP group.
     * 
     * @param group the FYP group
     * @param student the student to remove
     * @throws InvalidFYPGroupException if validation fails
     */
    public void removeMemberFromFYPGroup(FYPGroup group, Student student) throws InvalidFYPGroupException {
        if (group == null) {
            Logger.error("InstructorService", "Cannot remove member - group is null");
            throw new InvalidFYPGroupException("FYP group cannot be null");
        }
        
        if (student == null) {
            Logger.error("InstructorService", "Cannot remove null student from FYP group");
            throw new InvalidFYPGroupException("Student cannot be null");
        }
        
        group.removeMember(student);
        
        // Update the group record in file
        updateFYPGroupInFile(group);
        
        Logger.info("InstructorService",
                "Removed student " + student.getName() + " from FYP group " + group.getGroupId());
    }
    
    /**
     * Assigns a supervisor to an FYP group.
     * 
     * @param group the FYP group
     * @param supervisor the permanent instructor to supervise
     * @throws InvalidFYPGroupException if validation fails
     */
    public void assignSupervisorToFYPGroup(FYPGroup group, PermanentInstructor supervisor)
            throws InvalidFYPGroupException {
        
        if (group == null) {
            Logger.error("InstructorService", "Cannot assign supervisor - group is null");
            throw new InvalidFYPGroupException("FYP group cannot be null");
        }
        
        if (supervisor == null) {
            Logger.error("InstructorService", "Cannot assign null supervisor to FYP group");
            throw new InvalidFYPGroupException("Supervisor cannot be null");
        }
        
        group.assignSupervisor(supervisor);
        
        // Update the group record in file
        updateFYPGroupInFile(group);
        
        Logger.info("InstructorService",
                "Assigned supervisor " + supervisor.getName() 
                + " to FYP group " + group.getGroupId());
    }
    
    /**
     * Helper method to update an FYP group record in the file.
     */
    private void updateFYPGroupInFile(FYPGroup group) {
        // Rewrite the whole file in the one format loadFYPGroups understands
        saveFYPGroups();
    }


    // ================================================================
    // FYP MEETING MANAGEMENT
    // ================================================================

    /**
     * Schedules a meeting for an FYP group.
     * Only permanent instructors who supervise the group can schedule meetings.
     * 
     * @param instructor the permanent instructor scheduling the meeting
     * @param group the FYP group
     * @param meeting the meeting to schedule
     * @throws UnauthorizedActionException if instructor doesn't supervise the group
     */
    public void scheduleFYPMeeting(PermanentInstructor instructor, FYPGroup group, FYPMeeting meeting)
            throws UnauthorizedActionException {
        
        if (instructor == null) {
            Logger.error("InstructorService", "Cannot schedule meeting - instructor is null");
            throw new UnauthorizedActionException("Instructor cannot be null");
        }
        
        if (group == null) {
            Logger.error(instructor.getTeacherId(), "Cannot schedule meeting - group is null");
            throw new UnauthorizedActionException("FYP group cannot be null");
        }
        
        if (meeting == null) {
            Logger.error(instructor.getTeacherId(), "Cannot schedule meeting - meeting is null");
            throw new UnauthorizedActionException("Meeting cannot be null");
        }
        
        // Verify that the instructor supervises this group
        if (group.getSupervisor() == null || !group.getSupervisor().equals(instructor)) {
            String errorMsg = "Instructor " + instructor.getTeacherId()
                    + " is not the supervisor of FYP group " + group.getGroupId();
            Logger.error(instructor.getTeacherId(), errorMsg);
            throw new UnauthorizedActionException(errorMsg);
        }
        
        // Add meeting to group
        group.addMeeting(meeting);
        fypMeetings.add(meeting);
        
        // Persist to file
        String record = String.format("FYPMEETING|%s|%s|%s|%s|%s",
                meeting.getMeetingId(),
                group.getGroupId(),
                meeting.getMeetingDate().toString(),
                meeting.getAgenda() != null ? meeting.getAgenda() : "",
                meeting.getNotes() != null ? meeting.getNotes() : "");
        FileManager.appendLine(FYPMEETINGS_FILE, record);
        
        Logger.info(instructor.getTeacherId(),
                "FYP meeting scheduled: " + meeting.getMeetingId()
                + " for group " + group.getGroupId()
                + " on " + meeting.getMeetingDate()
                + " - Agenda: " + meeting.getAgenda());
    }
    
    /**
     * Updates notes for an FYP meeting.
     * 
     * @param instructor the instructor updating the notes
     * @param group the FYP group
     * @param meeting the meeting to update
     * @param notes the notes to add/update
     * @throws UnauthorizedActionException if instructor doesn't supervise the group
     */
    public void updateFYPMeetingNotes(PermanentInstructor instructor, FYPGroup group,
                                      FYPMeeting meeting, String notes)
            throws UnauthorizedActionException {
        
        if (instructor == null) {
            Logger.error("InstructorService", "Cannot update meeting notes - instructor is null");
            throw new UnauthorizedActionException("Instructor cannot be null");
        }
        
        if (group == null) {
            Logger.error(instructor.getTeacherId(), "Cannot update meeting notes - group is null");
            throw new UnauthorizedActionException("FYP group cannot be null");
        }
        
        if (meeting == null) {
            Logger.error(instructor.getTeacherId(), "Cannot update meeting notes - meeting is null");
            throw new UnauthorizedActionException("Meeting cannot be null");
        }
        
        // Verify that the instructor supervises this group
        if (group.getSupervisor() == null || !group.getSupervisor().equals(instructor)) {
            String errorMsg = "Instructor " + instructor.getTeacherId()
                    + " is not the supervisor of FYP group " + group.getGroupId();
            Logger.error(instructor.getTeacherId(), errorMsg);
            throw new UnauthorizedActionException(errorMsg);
        }
        
        // Update meeting notes
        meeting.updateNotes(notes);
        
        // Update in file
        List<String> lines = FileManager.readLines(FYPMEETINGS_FILE);
        List<String> updatedLines = new ArrayList<>();
        
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 2 && parts[0].equals("FYPMEETING") 
                    && parts[1].equals(meeting.getMeetingId())) {
                // Update this meeting's record
                line = String.format("FYPMEETING|%s|%s|%s|%s|%s",
                        meeting.getMeetingId(),
                        group.getGroupId(),
                        meeting.getMeetingDate().toString(),
                        meeting.getAgenda() != null ? meeting.getAgenda() : "",
                        notes != null ? notes : "");
            }
            updatedLines.add(line);
        }
        
        FileManager.writeAllLines(FYPMEETINGS_FILE, updatedLines);
        
        Logger.info(instructor.getTeacherId(),
                "Updated notes for FYP meeting " + meeting.getMeetingId()
                + " of group " + group.getGroupId());
    }
    
    // ================================================================
    // FYP EVALUATION MANAGEMENT
    // ================================================================

    /**
     * Evaluates an FYP group's idea or progress.
     * Only the supervising permanent instructor can evaluate.
     * Scores are typically in the range 0-100.
     * 
     * @param instructor the permanent instructor evaluating
     * @param group the FYP group being evaluated
     * @param evaluation the evaluation object
     * @param score the score to assign (typically 0-100)
     * @param feedback the feedback to provide
     * @throws UnauthorizedActionException if instructor doesn't supervise the group
     */
    public void evaluateFYPIdea(PermanentInstructor instructor, FYPGroup group,
                                FYPEvaluation evaluation, double score, String feedback)
            throws UnauthorizedActionException, InvalidFYPEvaluationException {

        if (instructor == null) {
            Logger.error("InstructorService", "Cannot evaluate FYP - instructor is null");
            throw new UnauthorizedActionException("Instructor cannot be null");
        }

        if (group == null) {
            Logger.error(instructor.getTeacherId(), "Cannot evaluate FYP - group is null");
            throw new UnauthorizedActionException("FYP group cannot be null");
        }

        if (evaluation == null) {
            Logger.error(instructor.getTeacherId(), "Cannot evaluate FYP - evaluation is null");
            throw new InvalidFYPEvaluationException("Evaluation cannot be null");
        }
        
        // Verify that the instructor supervises this group
        if (group.getSupervisor() == null || !group.getSupervisor().equals(instructor)) {
            String errorMsg = "Instructor " + instructor.getTeacherId()
                    + " is not the supervisor of FYP group " + group.getGroupId();
            Logger.error(instructor.getTeacherId(), errorMsg);
            throw new UnauthorizedActionException(errorMsg);
        }
        
        // Validate score: must be 0-100
        if (score < 0 || score > 100) {
            Logger.error(instructor.getTeacherId(),
                    "FYP evaluation score " + score + " is outside the range [0-100]");
            throw new InvalidFYPEvaluationException("Score must be between 0 and 100");
        }
        
        // Perform evaluation
        evaluation.evaluate(score);
        evaluation.addFeedback(feedback);
        group.addEvaluation(evaluation);
        fypEvaluations.add(evaluation);
        
        // Persist (one format for save and load)
        saveFYPEvaluations();

        Logger.info(instructor.getTeacherId(),
                "FYP evaluated for group: " + group.getGroupId()
                + " - Score: " + String.format("%.2f/100", score)
                + " - Evaluation ID: " + evaluation.getEvaluationId());
    }
    
    /**
     * Adds (or replaces) the feedback on one of a group's evaluations
     * (UML: PermanentInstructor.provideFYPFeedback(evaluation, feedback)).
     *
     * @param instructor the permanent instructor providing feedback
     * @param group the FYP group the evaluation belongs to
     * @param evaluation the evaluation to attach the feedback to
     * @param feedback the feedback text
     * @throws UnauthorizedActionException if instructor doesn't supervise the group
     */
    public void provideFYPFeedback(PermanentInstructor instructor, FYPGroup group,
                                   FYPEvaluation evaluation, String feedback)
            throws UnauthorizedActionException {
        
        if (instructor == null) {
            Logger.error("InstructorService", "Cannot provide feedback - instructor is null");
            throw new UnauthorizedActionException("Instructor cannot be null");
        }
        
        if (group == null) {
            Logger.error(instructor.getTeacherId(), "Cannot provide feedback - group is null");
            throw new UnauthorizedActionException("FYP group cannot be null");
        }
        
        if (feedback == null || feedback.trim().isEmpty()) {
            Logger.error(instructor.getTeacherId(), "Cannot provide empty feedback");
            throw new UnauthorizedActionException("Feedback cannot be empty");
        }
        
        // Verify that the instructor supervises this group
        if (group.getSupervisor() == null || !group.getSupervisor().equals(instructor)) {
            String errorMsg = "Instructor " + instructor.getTeacherId()
                    + " is not the supervisor of FYP group " + group.getGroupId();
            Logger.error(instructor.getTeacherId(), errorMsg);
            throw new UnauthorizedActionException(errorMsg);
        }
        
        if (evaluation == null || !group.getEvaluations().contains(evaluation)) {
            Logger.error(instructor.getTeacherId(), "Evaluation not found in group " + group.getGroupId());
            throw new UnauthorizedActionException("Evaluation does not belong to FYP group " + group.getGroupId());
        }

        instructor.provideFYPFeedback(evaluation, feedback);
        saveFYPEvaluations(); // feedback is stored with its evaluation, so it survives a restart

        Logger.info(instructor.getTeacherId(),
                "Provided feedback for FYP group " + group.getGroupId()
                + ": " + feedback.substring(0, Math.min(50, feedback.length())) + "...");
    }
    
    // ================================================================
    // INSTRUCTOR PERSISTENCE
    // ================================================================
    
    /**
     * Persists a new instructor to the file.
     * 
     * @param instructor the instructor to persist
     */
    public void saveInstructor(Instructor instructor) {
        if (instructor == null) {
            Logger.error("InstructorService", "Cannot save null instructor");
            return;
        }
        
        // Check if instructor already exists
        for (Instructor existing : instructors) {
            if (existing.getTeacherId().equals(instructor.getTeacherId())) {
                Logger.warn("InstructorService",
                        "Instructor " + instructor.getTeacherId() + " already exists - updating");
                updateInstructor(instructor);
                return;
            }
        }
        
        instructors.add(instructor);
        
        String type = instructor instanceof PermanentInstructor ? "PERMANENT" : "VISITING";
        String record = String.format("INSTRUCTOR|%s|%s|%s|%s|%s",
                instructor.getTeacherId(),
                instructor.getName(),
                instructor.getEmail(),
                instructor.getPhone(),
                type);
        
        FileManager.appendLine(INSTRUCTORS_FILE, record);
        
        Logger.info("InstructorService",
                "Saved instructor: " + instructor.getName()
                + " (" + type + " - ID: " + instructor.getTeacherId() + ")");
    }
    
    /**
     * Updates an existing instructor in the file.
     * 
     * @param instructor the instructor to update
     */
    public void updateInstructor(Instructor instructor) {
        if (instructor == null) {
            Logger.error("InstructorService", "Cannot update null instructor");
            return;
        }
        
        List<String> lines = FileManager.readLines(INSTRUCTORS_FILE);
        List<String> updatedLines = new ArrayList<>();
        
        boolean found = false;
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 2 && parts[0].equals("INSTRUCTOR") 
                    && parts[1].equals(instructor.getTeacherId())) {
                // Update this instructor's record
                String type = instructor instanceof PermanentInstructor ? "PERMANENT" : "VISITING";
                line = String.format("INSTRUCTOR|%s|%s|%s|%s|%s",
                        instructor.getTeacherId(),
                        instructor.getName(),
                        instructor.getEmail(),
                        instructor.getPhone(),
                        type);
                found = true;
            }
            updatedLines.add(line);
        }
        
        FileManager.writeAllLines(INSTRUCTORS_FILE, updatedLines);
        
        if (found) {
            Logger.info("InstructorService",
                    "Updated instructor: " + instructor.getName() + " (ID: " + instructor.getTeacherId() + ")");
        } else {
            Logger.warn("InstructorService",
                    "Instructor " + instructor.getTeacherId() + " not found in file - no update performed");
        }
    }
    
     
    // ================================================================
    // QUERY & HELPER METHODS
    // ================================================================
    
    /**
     * Retrieves an FYP group by its ID.
     * 
     * @param groupId the group ID to search for
     * @return the FYP group, or null if not found
     */
    public FYPGroup getFYPGroupById(String groupId) {
        for (FYPGroup group : fypGroups) {
            if (group.getGroupId().equals(groupId)) {
                return group;
            }
        }
        Logger.warn("InstructorService", "FYP group not found: " + groupId);
        return null;
    }
    
    /**
     * Gets all FYP groups in the system.
     * 
     * @return list of all FYP groups
     */
    public List<FYPGroup> getAllFYPGroups() {
        return new ArrayList<>(fypGroups);
    }
    
    /**
     * Gets FYP groups supervised by a specific instructor.
     * 
     * @param instructor the permanent instructor
     * @return list of supervised FYP groups
     */
    public List<FYPGroup> getFYPGroupsBySupervisor(PermanentInstructor instructor) {
        if (instructor == null) {
            return new ArrayList<>();
        }
        
        List<FYPGroup> supervisedGroups = new ArrayList<>();
        for (FYPGroup group : fypGroups) {
            if (group.getSupervisor() != null 
                    && group.getSupervisor().equals(instructor)) {
                supervisedGroups.add(group);
            }
        }
        
        return supervisedGroups;
    }
    
    /**
     * Gets all attendance records for a student in a section.
     * 
     * @param student the student
     * @param section the section
     * @return list of attendance records
     */
    public List<Attendance> getAttendanceRecords(Student student, Section section) {
        List<Attendance> records = new ArrayList<>();
        
        if (student == null || section == null) {
            return records;
        }
        
        for (Attendance attendance : attendanceRecords) {
            if (attendance.getStudent().equals(student)
                    && attendance.getSection().equals(section)) {
                records.add(attendance);
            }
        }
        
        return records;
    }
    
    /**
     * Gets all attendance records for a specific date in a section.
     * Useful for generating attendance reports.
     * 
     * @param section the section
     * @param date the date
     * @return list of attendance records for that date
     */
    public List<Attendance> getAttendanceByDate(Section section, LocalDate date) {
        List<Attendance> records = new ArrayList<>();
        
        if (section == null || date == null) {
            return records;
        }
        
        for (Attendance attendance : attendanceRecords) {
            if (attendance.getSection().equals(section)
                    && attendance.getDate().equals(date)) {
                records.add(attendance);
            }
        }
        
        return records;
    }
    
   
    public List<FYPMeeting> getMeetingsForGroup(FYPGroup group) {
        if (group == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(group.getMeetings());
    }
    
   
    public List<FYPEvaluation> getEvaluationsForGroup(FYPGroup group) {
        if (group == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(group.getEvaluations());
    }
    
    
    public Instructor getInstructorById(String teacherId) {
        for (Instructor instructor : instructors) {
            if (instructor.getTeacherId().equals(teacherId)) {
                return instructor;
            }
        }
        Logger.warn("InstructorService", "Instructor not found: " + teacherId);
        return null;
    }
    
    
    public List<Instructor> getAllInstructors() {
        return new ArrayList<>(instructors);
    }
    
    

    public int getTotalSessionsForSection(Section section) {
        if (section == null) {
            return 0;
        }
        
        List<LocalDate> uniqueDates = new ArrayList<>();
        for (Attendance attendance : attendanceRecords) {
            if (attendance.getSection().equals(section) 
                    && !uniqueDates.contains(attendance.getDate())) {
                uniqueDates.add(attendance.getDate());
            }
        }
        
        return uniqueDates.size();
    }
    
   // attendance summary 
    public String generateAttendanceSummary(Section section) {
        if (section == null) {
            return "Invalid section";
        }
        
        int totalSessions = getTotalSessionsForSection(section);
        List<Student> students = section.getEnrolledStudents();
        
        StringBuilder summary = new StringBuilder();
        summary.append("=== Attendance Summary for Section ").append(section.getSectionId()).append(" ===\n");
        summary.append("Course: ").append(section.getCourse().getCourseCode()).append("\n");
        summary.append("Total Sessions: ").append(totalSessions).append("\n");
        summary.append("Total Students: ").append(students.size()).append("\n\n");
        
        for (Student student : students) {
            double percentage = calculateAttendancePercentage(student, section);
            summary.append(String.format("%-20s %-15s: %.2f%%\n",
                    student.getName(),
                    "(" + student.getStudentId() + ")",
                    percentage));
        }
        
        return summary.toString();
    }

    public void loadInstructors() {
        instructors.clear();
        List<String> lines = FileManager.readLines("data/instructors.txt");
        for (String line : lines) {
            if (line.startsWith("#") || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 7 && p[0].equals("INSTRUCTOR")) {
                if (p[1].equals("PERM")) {
                    instructors.add(new PermanentInstructor(p[2], p[3], p[4], p[5], p[6]));
                } else if (p[1].equals("VISIT")) {
                    instructors.add(new VisitingInstructor(p[2], p[3], p[4], p[5], p[6]));
                }
            }
        }
    }

    public void saveInstructors() {
        List<String> lines = new ArrayList<>();
        lines.add("# Format: INSTRUCTOR|type(PERM/VISIT)|id|name|email|phone|teacherId");
        for (Instructor i : instructors) {
            String type = (i instanceof PermanentInstructor) ? "PERM" : "VISIT";
            lines.add("INSTRUCTOR|" + type + "|" + i.getId() + "|" + i.getName() + "|" + i.getEmail() + "|" + i.getPhone() + "|" + i.getTeacherId());
        }
        FileManager.writeAllLines("data/instructors.txt", lines);
    }

    public void loadFYPGroups() {
        fypGroups.clear();
        List<String> lines = FileManager.readLines(FYPGROUPS_FILE);
        for (String line : lines) {
            if (line.startsWith("#") || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 4 && p[0].equals("FYPGROUP")) {
                // Format: FYPGROUP|groupId|title|supervisorId|memberStudentIds(comma-separated)|description
                FYPGroup g = new FYPGroup(p[1], p[2], p.length > 5 ? p[5] : "");
                Instructor inst = CampusRegistry.findInstructor(p[3]);
                if (inst instanceof PermanentInstructor) {
                    g.assignSupervisor((PermanentInstructor) inst);
                    ((PermanentInstructor) inst).addSupervisedGroup(g);
                }
                if (p.length > 4 && !p[4].isBlank()) {
                    for (String studentId : p[4].split(",")) {
                        Student member = CampusRegistry.findStudent(studentId.trim());
                        if (member != null) {
                            g.addMember(member);
                        } else {
                            Logger.warn("InstructorService", "FYP group " + p[1] + ": unknown member " + studentId);
                        }
                    }
                }
                fypGroups.add(g);
            }
        }
        Logger.info("InstructorService", "Loaded " + fypGroups.size() + " FYP groups from " + FYPGROUPS_FILE);
    }

    public void saveFYPGroups() {
        List<String> lines = new ArrayList<>();
        lines.add("# Format: FYPGROUP|groupId|title|supervisorId|memberStudentIds(comma-separated)|description");
        for (FYPGroup g : fypGroups) {
            String sup = (g.getSupervisor() != null) ? g.getSupervisor().getTeacherId() : "null";
            List<String> memberIds = new ArrayList<>();
            for (Student member : g.getMembers()) {
                memberIds.add(member.getStudentId());
            }
            String description = g.getDescription() != null ? g.getDescription().replace("|", "/") : "";
            lines.add("FYPGROUP|" + g.getGroupId() + "|" + g.getTitle().replace("|", "/") + "|" + sup
                    + "|" + String.join(",", memberIds) + "|" + description);
        }
        FileManager.writeAllLines(FYPGROUPS_FILE, lines);
        Logger.info("InstructorService", "Saved " + fypGroups.size() + " FYP groups to " + FYPGROUPS_FILE);
    }

    public void loadFYPMeetings() {
        fypMeetings.clear();
        List<String> lines = FileManager.readLines(FYPMEETINGS_FILE);
        for (String line : lines) {
            if (line.startsWith("#") || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 5 && p[0].equals("FYPMEETING")) {
                // Format: FYPMEETING|meetingId|groupId|date|agenda|notes
                FYPGroup group = getFYPGroupById(p[2]);
                if (group != null) {
                    LocalDate meetingDate = LocalDate.parse(p[3]);
                    FYPMeeting meeting = new FYPMeeting(p[1], meetingDate, p[4]);
                    if (p.length > 5 && !p[5].isEmpty()) {
                        meeting.updateNotes(p[5]);
                    }
                    fypMeetings.add(meeting);
                    group.addMeeting(meeting);
                }
            }
        }
        Logger.info("InstructorService", "Loaded " + fypMeetings.size() + " FYP meetings from " + FYPMEETINGS_FILE);
    }

    public void saveFYPMeetings() {
        List<String> lines = new ArrayList<>();
        lines.add("# Format: FYPMEETING|meetingId|groupId|date|agenda|notes");
        for (FYPMeeting meeting : fypMeetings) {
            // Find the group this meeting belongs to
            String groupId = "UNKNOWN";
            for (FYPGroup group : fypGroups) {
                if (group.getMeetings().contains(meeting)) {
                    groupId = group.getGroupId();
                    break;
                }
            }
            lines.add("FYPMEETING|" + meeting.getMeetingId() + "|" + groupId + "|" 
                    + meeting.getMeetingDate().toString() + "|" 
                    + (meeting.getAgenda() != null ? meeting.getAgenda() : "") + "|" 
                    + (meeting.getNotes() != null ? meeting.getNotes() : ""));
        }
        FileManager.writeAllLines(FYPMEETINGS_FILE, lines);
        Logger.info("InstructorService", "Saved " + fypMeetings.size() + " FYP meetings to " + FYPMEETINGS_FILE);
    }

    public void loadFYPEvaluations() {
        fypEvaluations.clear();
        List<String> lines = FileManager.readLines(FYPEVALUATIONS_FILE);
        for (String line : lines) {
            if (line.startsWith("#") || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 6 && p[0].equals("FYPEVALUATION")) {
                // Format: FYPEVALUATION|evaluationId|groupId|instructorId|date|score|feedback
                FYPGroup group = getFYPGroupById(p[2]);
                if (group != null) {
                    // Restore with the original date, score, feedback and evaluator
                    Instructor inst = CampusRegistry.findInstructor(p[3]);
                    Evaluator evaluator = inst instanceof Evaluator ? (Evaluator) inst : null;
                    String feedback = (p.length > 6 && !p[6].isEmpty()) ? p[6] : null;
                    FYPEvaluation evaluation = new FYPEvaluation(p[1], evaluator,
                            LocalDate.parse(p[4]), Double.parseDouble(p[5]), feedback);
                    fypEvaluations.add(evaluation);
                    group.addEvaluation(evaluation);
                }
            }
        }
        Logger.info("InstructorService", "Loaded " + fypEvaluations.size() + " FYP evaluations from " + FYPEVALUATIONS_FILE);
    }

    public void saveFYPEvaluations() {
        List<String> lines = new ArrayList<>();
        lines.add("# Format: FYPEVALUATION|evaluationId|groupId|instructorId|date|score|feedback");
        for (FYPEvaluation eval : fypEvaluations) {
            // Find the group this evaluation belongs to
            String groupId = "UNKNOWN";
            String instructorId = eval.getEvaluator() != null ? eval.getEvaluator().getEvaluatorId() : "UNKNOWN";
            for (FYPGroup group : fypGroups) {
                if (group.getEvaluations().contains(eval)) {
                    groupId = group.getGroupId();
                    if (eval.getEvaluator() == null && group.getSupervisor() != null) {
                        instructorId = group.getSupervisor().getTeacherId();
                    }
                    break;
                }
            }
            String feedback = eval.getFeedback() != null ? eval.getFeedback().replace("|", "/") : "";
            lines.add("FYPEVALUATION|" + eval.getEvaluationId() + "|" + groupId + "|"
                    + instructorId + "|"
                    + eval.getEvaluationDate().toString() + "|"
                    + eval.getScore() + "|"
                    + feedback);
        }
        FileManager.writeAllLines(FYPEVALUATIONS_FILE, lines);
        Logger.info("InstructorService", "Saved " + fypEvaluations.size() + " FYP evaluations to " + FYPEVALUATIONS_FILE);
    }

    public void loadAttendance() {
        attendanceRecords.clear();
        List<String> lines = FileManager.readLines(ATTENDANCE_FILE);
        for (String line : lines) {
            if (line.startsWith("#") || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 5 && p[0].equals("ATTENDANCE")) {
                // Format: ATTENDANCE|studentId|sectionId|date|status
                Student student = CampusRegistry.findStudent(p[1]);
                Section section = CampusRegistry.findSection(p[2]);
                if (student != null && section != null) {
                    LocalDate date = LocalDate.parse(p[3]);
                    AttendanceStatus status = AttendanceStatus.valueOf(p[4]);
                    Attendance attendance = new Attendance(student, section, date, status);
                    attendanceRecords.add(attendance);
                }
            }
        }
        Logger.info("InstructorService", "Loaded " + attendanceRecords.size() + " attendance records from " + ATTENDANCE_FILE);
    }

    public void saveAttendance() {
        List<String> lines = new ArrayList<>();
        lines.add("# Format: ATTENDANCE|studentId|sectionId|date|status");
        for (Attendance attendance : attendanceRecords) {
            lines.add("ATTENDANCE|" + attendance.getStudent().getStudentId() + "|" 
                    + attendance.getSection().getSectionId() + "|" 
                    + attendance.getDate().toString() + "|" 
                    + attendance.getStatus().name());
        }
        FileManager.writeAllLines(ATTENDANCE_FILE, lines);
        Logger.info("InstructorService", "Saved " + attendanceRecords.size() + " attendance records to " + ATTENDANCE_FILE);
    }

}
