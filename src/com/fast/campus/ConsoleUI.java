package com.fast.campus;

import com.fast.campus.enums.AttendanceStatus;
import com.fast.campus.enums.Day;
import com.fast.campus.enums.RequestCategory;
import com.fast.campus.exception.CampusException;
import com.fast.campus.model.*;
import com.fast.campus.service.AcademicOfficeService;
import com.fast.campus.service.InstructorService;
import com.fast.campus.service.StudentService;
import com.fast.campus.util.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private final AcademicOfficeService academicService;
    private final InstructorService instructorService;
    private final StudentService studentService;
    private final Scanner scanner;

    public ConsoleUI() {
        // Load order matters: courses/sections -> students (+enrollments, assignments, requests)
        // -> instructors (+attendance and FYP groups, which refer to students) -> section instructors
        this.academicService = new AcademicOfficeService();
        this.studentService = new StudentService(academicService);
        this.instructorService = new InstructorService();
        academicService.restoreInstructorAssignments();
        this.scanner = new Scanner(System.in);
    }

    private Section searchSection(String sectionId) {
        for (Section s : academicService.getSections()) {
            if (s.getSectionId().equals(sectionId)) return s;
        }
        return null;
    }

    // --- Input helpers (re-ask instead of crashing on bad input) ---

    private String prompt(String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }

    private int promptInt(String label, int min, int max) {
        while (true) {
            String text = prompt(label);
            try {
                int value = Integer.parseInt(text);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {
                // fall through to the message below
            }
            System.out.println("Please enter a whole number from " + min + " to " + max + ".");
        }
    }

    private double promptDouble(String label) {
        while (true) {
            try {
                return Double.parseDouble(prompt(label));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number, e.g. 85 or 87.5.");
            }
        }
    }

    private java.time.LocalDate promptDate(String label) {
        while (true) {
            try {
                return java.time.LocalDate.parse(prompt(label));
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("Please use the format YYYY-MM-DD, e.g. 2026-10-15.");
            }
        }
    }

    /** Prints a numbered heading line and each item, or a friendly message when empty. */
    private void printList(String title, List<?> items, String emptyMessage) {
        System.out.println("\n" + title);
        if (items.isEmpty()) {
            System.out.println("  " + emptyMessage);
        } else {
            for (Object item : items) System.out.println("  " + item);
        }
    }

    public void start() {
        Logger.info("System", "ConsoleUI Started");
        boolean running = true;
        while (running) {
            System.out.println("\n=== FAST Campus Management System ===");
            System.out.println("Select your role to login:");
            System.out.println("1. Academic Office Admin (Fateh)");
            System.out.println("2. Normal Student (Saim Arif)");
            System.out.println("3. Teaching Assistant (Ahmad Butt)");
            System.out.println("4. Permanent Instructor (Arslan Asif)");
            System.out.println("5. Visiting Instructor (Faizan)");
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": adminMenu(); break;
                case "2": studentMenu(); break;
                case "3": taMenu(); break;
                case "4": permanentInstructorMenu(); break;
                case "5": visitingInstructorMenu(); break;
                case "6": running = false; Logger.info("System", "System Exited"); break;
                default: System.out.println("Invalid choice.");
            }
        }
        System.out.println("Exiting System...");
    }

    // --- ADMIN MENU ---
    private void adminMenu() {
        Logger.info("Admin", "Logged in to Admin menu");
        while (true) {
            System.out.println("\n--- Academic Office Admin Menu ---");
            System.out.println("1. Create Course");
            System.out.println("2. Search Course");
            System.out.println("3. Create Section");
            System.out.println("4. Assign Instructor to Section");
            System.out.println("5. View/Process Academic Requests");
            System.out.println("6. User Management");
            System.out.println("7. Back to Main Menu");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();
            try {
                if (choice.equals("1")) {
                    System.out.print("Course Code: "); String code = scanner.nextLine();
                    System.out.print("Title: "); String title = scanner.nextLine();
                    System.out.print("Credit Hours: "); int ch = Integer.parseInt(scanner.nextLine());
                    academicService.createCourse(new Course(code, title, ch));
                    System.out.println("Course created successfully.");
                } else if (choice.equals("2")) {
                    System.out.print("Enter Course Code: ");
                    Course c = academicService.searchCourse(scanner.nextLine());
                    System.out.println(c != null ? c : "Course not found.");
                } else if (choice.equals("3")) {
                    System.out.print("Section ID (e.g. CS101-A): "); String secId = scanner.nextLine();
                    System.out.print("Capacity: "); int cap = Integer.parseInt(scanner.nextLine());
                    System.out.print("Course Code: "); Course c = academicService.searchCourse(scanner.nextLine());
                    if (c != null) {
                        Schedule sch = new Schedule(Day.MONDAY, "08:00", "09:30", "Room-1");
                        academicService.createSection(new Section(secId, cap, c, sch));
                        System.out.println("Section created successfully.");
                    } else {
                        System.out.println("Course not found.");
                    }
                } else if (choice.equals("4")) {
                    System.out.print("Section ID: "); String secId = scanner.nextLine();
                    System.out.print("Instructor ID: "); String instId = scanner.nextLine();
                    Instructor inst = instructorService.getInstructorById(instId);
                    if (inst != null) {
                        academicService.assignInstructor(secId, inst);
                        System.out.println("Instructor assigned successfully.");
                    } else {
                        System.out.println("Instructor not found.");
                    }
                } else if (choice.equals("5")) {
                    List<Request> reqs = studentService.getAllRequests();
                    System.out.println("Sort by: 1. Priority 2. Date");
                    String sort = scanner.nextLine();
                    if (sort.equals("1")) {
                        reqs.sort(new com.fast.campus.comparator.RequestPriorityComparator());
                    } else if (sort.equals("2")) {
                        reqs.sort(new com.fast.campus.comparator.RequestDateComparator());
                    }
                    printList("Requests:", reqs, "No requests submitted.");
                    String requestId = reqs.isEmpty() ? "" : prompt("Request ID to approve/reject (press Enter to skip): ");
                    if (!requestId.isEmpty()) {
                        Request selected = null;
                        for (Request r : reqs) {
                            if (r.getRequestId().equals(requestId)) selected = r;
                        }
                        if (selected == null) {
                            System.out.println("Request not found.");
                        } else {
                            String decision = prompt("1. Approve  2. Reject: ");
                            if (decision.equals("1")) {
                                academicService.approveRequest(selected);
                            } else if (decision.equals("2")) {
                                academicService.rejectRequest(selected);
                            } else {
                                System.out.println("Invalid choice — request left unchanged.");
                            }
                            // AcademicOfficeService only changes the status in memory; requests.txt is saved here
                            studentService.saveRequests();
                            System.out.println("Now: " + selected);
                        }
                    }
                } else if (choice.equals("6")) {
                    System.out.println("1. Create Student");
                    System.out.println("2. Create Permanent Instructor");
                    System.out.println("3. Create Visiting Instructor");
                    System.out.println("4. Promote Student to TA");
                    System.out.print("Choice: ");
                    String um = scanner.nextLine();
                    if (um.equals("1")) {
                        System.out.print("ID: "); String id = scanner.nextLine();
                        System.out.print("Name: "); String name = scanner.nextLine();
                        System.out.print("Email: "); String email = scanner.nextLine();
                        System.out.print("Phone: "); String phone = scanner.nextLine();
                        System.out.print("Student ID: "); String sId = scanner.nextLine();
                        // Through StudentService so it is validated (unique ID) and saved to students.txt
                        studentService.addStudent(new NormalStudent(id, name, email, phone, sId));
                        System.out.println("Student created.");
                        Logger.info("Admin", "Created Student: " + sId);
                    } else if (um.equals("2")) {
                        System.out.print("ID: "); String id = scanner.nextLine();
                        System.out.print("Name: "); String name = scanner.nextLine();
                        System.out.print("Email: "); String email = scanner.nextLine();
                        System.out.print("Phone: "); String phone = scanner.nextLine();
                        System.out.print("Teacher ID: "); String tId = scanner.nextLine();
                        com.fast.campus.util.CampusRegistry.instructors.add(new PermanentInstructor(id, name, email, phone, tId));
                        instructorService.saveInstructors();
                        System.out.println("Permanent Instructor created.");
                        Logger.info("Admin", "Created Permanent Instructor: " + tId);
                    } else if (um.equals("3")) {
                        System.out.print("ID: "); String id = scanner.nextLine();
                        System.out.print("Name: "); String name = scanner.nextLine();
                        System.out.print("Email: "); String email = scanner.nextLine();
                        System.out.print("Phone: "); String phone = scanner.nextLine();
                        System.out.print("Teacher ID: "); String tId = scanner.nextLine();
                        com.fast.campus.util.CampusRegistry.instructors.add(new VisitingInstructor(id, name, email, phone, tId));
                        instructorService.saveInstructors();
                        System.out.println("Visiting Instructor created.");
                        Logger.info("Admin", "Created Visiting Instructor: " + tId);
                    } else if (um.equals("4")) {
                        System.out.print("Student ID to promote: "); String sId = scanner.nextLine();
                        Student s = studentService.findStudent(sId);
                        if (s instanceof NormalStudent) {
                            System.out.print("Section ID the TA will assist: ");
                            Section sec = searchSection(scanner.nextLine());
                            if (sec == null) {
                                System.out.println("Section not found.");
                            } else {
                                // Keeps the student's enrollments/requests and saves the change
                                studentService.promoteToTA(s, sec);
                                System.out.println("Student promoted to TA of " + sec.getSectionId() + ".");
                                Logger.info("Admin", "Promoted Student to TA: " + sId);
                            }
                        } else {
                            System.out.println("Student not found or already a TA.");
                        }
                    }
                } else if (choice.equals("7")) {
                    break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // --- STUDENT MENU ---
    private void studentMenu() {
        System.out.print("Enter your Student ID: ");
        Student student = studentService.findStudent(scanner.nextLine());
        if (student == null) { System.out.println("Student not found!"); return; }

        Logger.info(student.getStudentId(), "Logged in to Student menu");
        while (true) {
            System.out.println("\n--- Student Menu (" + student.getName() + ", " + student.getStudentId()
                    + " | " + student.getTotalCreditHours() + " credit hours) ---");
            System.out.println(" 1. View Available Courses & Sections");
            System.out.println(" 2. Register for a Section");
            System.out.println(" 3. Drop a Course");
            System.out.println(" 4. View Registered Courses & Credit Hours");
            System.out.println(" 5. View Timetable");
            System.out.println(" 6. View Assignments");
            System.out.println(" 7. Submit an Assignment");
            System.out.println(" 8. View Attendance & Percentage");
            System.out.println(" 9. Submit Course Clash Request");
            System.out.println("10. Submit Other Request (professor / classmate / other)");
            System.out.println("11. View My Requests");
            System.out.println("12. View My Submissions & Grades");
            System.out.println(" 0. Back to Main Menu");

            String choice = prompt("Enter choice: ");
            try {
                switch (choice) {
                    case "1": {
                        System.out.println("\nAvailable courses and sections:");
                        for (Course course : academicService.getCourses()) {
                            System.out.println("  " + course);
                            for (Section section : course.getSections()) {
                                String when = section.getSchedule() != null
                                        ? section.getSchedule().getScheduleInfo() : "schedule not set";
                                System.out.println("      " + section.getSectionId() + " | " + when
                                        + " | " + section.getAvailableSeats() + "/" + section.getCapacity() + " seats free");
                            }
                        }
                        break;
                    }
                    case "2": {
                        Section section = searchSection(prompt("Section ID to register for: "));
                        if (section == null) { System.out.println("Section not found."); break; }
                        studentService.registerCourse(student, section);
                        System.out.println("Registered for " + section.getSectionId()
                                + ". Total credit hours: " + student.getTotalCreditHours());
                        break;
                    }
                    case "3": {
                        printList("Your sections:", student.getEnrolledSections(), "You are not registered in any section.");
                        if (student.getEnrolledSections().isEmpty()) break;
                        Section section = searchSection(prompt("Section ID to drop: "));
                        if (section == null) { System.out.println("Section not found."); break; }
                        studentService.dropCourse(student, section);
                        System.out.println("Dropped " + section.getSectionId()
                                + ". Total credit hours: " + student.getTotalCreditHours());
                        break;
                    }
                    case "4":
                        printList("Registered courses:", student.viewCourses(), "None yet.");
                        System.out.println("Total credit hours: " + student.calculateTotalCreditHours());
                        break;
                    case "5":
                        printList("Timetable:", studentService.viewTimetable(student), "No classes scheduled.");
                        break;
                    case "6":
                        printList("Your assignments (earliest deadline first):",
                                studentService.viewAssignments(student), "No assignments for your sections.");
                        break;
                    case "7": {
                        List<Assignment> mine = studentService.viewAssignments(student);
                        printList("Your assignments:", mine, "No assignments for your sections.");
                        if (mine.isEmpty()) break;
                        Assignment assignment = studentService.findAssignment(prompt("Assignment ID to submit: "));
                        if (assignment == null) { System.out.println("Assignment not found."); break; }
                        if (assignment.isDeadlinePassed()) {
                            System.out.println("Note: the deadline has passed — this will be recorded as LATE.");
                        }
                        Submission submission = studentService.submitAssignment(student, assignment,
                                prompt("Your submission (text or link): "));
                        System.out.println("Submitted: " + submission);
                        break;
                    }
                    case "8": {
                        printList("Your sections:", student.getEnrolledSections(), "You are not registered in any section.");
                        if (student.getEnrolledSections().isEmpty()) break;
                        Section section = searchSection(prompt("Section ID: "));
                        if (section == null) { System.out.println("Section not found."); break; }
                        printList("Attendance for " + section.getSectionId() + ":",
                                studentService.viewAttendance(student, section), "No attendance marked yet.");
                        System.out.printf("Attendance percentage: %.1f%%%n",
                                studentService.viewAttendancePercentage(student, section));
                        break;
                    }
                    case "9": {
                        Section current = searchSection(prompt("Section you are registered in: "));
                        Section wanted = searchSection(prompt("Section you want (that clashes): "));
                        if (current == null || wanted == null) { System.out.println("Section not found."); break; }
                        String description = prompt("Describe your request: ");
                        int priority = promptInt("Priority (1 = low ... 5 = urgent): ", 1, 5);
                        Request request = studentService.submitCourseClashRequest(student, current, wanted, description, priority);
                        System.out.println("Submitted: " + request);
                        break;
                    }
                    case "10": {
                        RequestCategory[] categories = RequestCategory.values();
                        for (int i = 0; i < categories.length; i++) {
                            System.out.println("  " + (i + 1) + ". " + categories[i]);
                        }
                        RequestCategory category = categories[promptInt("Category: ", 1, categories.length) - 1];
                        String description = prompt("Describe your request: ");
                        int priority = promptInt("Priority (1 = low ... 5 = urgent): ", 1, 5);
                        Request request = studentService.submitGenericRequest(student, category, description, priority);
                        System.out.println("Submitted: " + request);
                        break;
                    }
                    case "11":
                        printList("Your requests (oldest first):", studentService.viewRequests(student), "No requests yet.");
                        break;
                    case "12": {
                        List<Submission> mine = studentService.viewMySubmissions(student);
                        System.out.println("\nYour submissions (newest first):");
                        if (mine.isEmpty()) System.out.println("  You haven't submitted anything yet.");
                        for (Submission submission : mine) {
                            System.out.println("  " + submission.getAssignment().getTitle() + " — " + submission);
                        }
                        break;
                    }
                    case "0":
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (CampusException e) {
                System.out.println("Could not complete: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                Logger.error(student.getStudentId(), "Unexpected error in Student menu: " + e);
            }
        }
    }

    // --- TA MENU ---
    private void taMenu() {
        System.out.print("Enter your TA Student ID: ");
        Student s = studentService.findStudent(scanner.nextLine());
        if (!(s instanceof TeachingAssistant)) { System.out.println("You are not a TA!"); return; }
        TeachingAssistant ta = (TeachingAssistant) s;

        Logger.info(ta.getStudentId(), "Logged in to TA menu");
        while (true) {
            Section section = ta.getAssignedSection();
            System.out.println("\n--- TA Menu (" + ta.getName() + ", " + ta.getStudentId() + " | section "
                    + (section != null ? section.getSectionId() : "none assigned") + ") ---");
            System.out.println(" 1. View Assigned Section");
            System.out.println(" 2. View Enrolled Students");
            System.out.println(" 3. Create Assignment");
            System.out.println(" 4. View Section Assignments");
            System.out.println(" 5. View Submissions");
            System.out.println(" 6. Check Late Submissions");
            System.out.println(" 7. Evaluate a Submission (assign marks)");
            System.out.println(" 8. Give Feedback on a Submission");
            System.out.println(" 9. Grading Progress");
            System.out.println(" 0. Back to Main Menu");

            String choice = prompt("Enter choice: ");
            try {
                if (section == null && !choice.equals("0")) {
                    System.out.println("You have no assigned section yet — ask a Permanent Instructor or the Admin to assign you.");
                    continue;
                }
                switch (choice) {
                    case "1": {
                        String when = section.getSchedule() != null ? section.getSchedule().getScheduleInfo() : "schedule not set";
                        System.out.println("\nAssigned section: " + section);
                        System.out.println("  Course: " + section.getCourse());
                        System.out.println("  Schedule: " + when);
                        System.out.println("  Instructor: " + (section.getInstructor() != null ? section.getInstructor().getName() : "not assigned"));
                        break;
                    }
                    case "2":
                        printList("Students enrolled in " + section.getSectionId() + ":",
                                section.getEnrolledStudents(), "No students enrolled yet.");
                        break;
                    case "3": {
                        String title = prompt("Title: ");
                        String description = prompt("Description: ");
                        java.time.LocalDate deadline = promptDate("Deadline (YYYY-MM-DD): ");
                        double totalMarks = promptDouble("Total marks: ");
                        Assignment assignment = studentService.createAssignment(ta, title, description, deadline, totalMarks);
                        System.out.println("Created: " + assignment);
                        break;
                    }
                    case "4":
                        printList("Assignments for " + section.getSectionId() + ":",
                                sectionAssignments(section), "No assignments yet.");
                        break;
                    case "5": {
                        Assignment assignment = chooseAssignment(section);
                        if (assignment == null) break;
                        printList("Submissions for " + assignment.getTitle() + ":",
                                studentService.viewSubmissions(ta, assignment), "No submissions yet.");
                        break;
                    }
                    case "6": {
                        Assignment assignment = chooseAssignment(section);
                        if (assignment == null) break;
                        printList("Late submissions for " + assignment.getTitle() + ":",
                                studentService.checkLateSubmissions(ta, assignment), "No late submissions.");
                        break;
                    }
                    case "7": {
                        Submission submission = chooseSubmission(ta, section);
                        if (submission == null) break;
                        double marks = promptDouble("Marks (0 - " + submission.getAssignment().getTotalMarks() + "): ");
                        studentService.evaluateSubmission(ta, submission, marks);
                        String comments = prompt("Feedback (press Enter to skip): ");
                        if (!comments.isEmpty()) {
                            studentService.giveFeedback(ta, submission, comments);
                        }
                        System.out.println("Saved: " + submission);
                        break;
                    }
                    case "8": {
                        Submission submission = chooseSubmission(ta, section);
                        if (submission == null) break;
                        studentService.giveFeedback(ta, submission, prompt("Feedback: "));
                        System.out.println("Saved: " + submission);
                        break;
                    }
                    case "9":
                        System.out.println("\nGrading progress for your assignments:");
                        if (ta.getCreatedAssignments().isEmpty()) {
                            System.out.println("  You haven't created any assignments yet.");
                        }
                        ta.evaluate();
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (CampusException e) {
                System.out.println("Could not complete: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                Logger.error(ta.getStudentId(), "Unexpected error in TA menu: " + e);
            }
        }
    }

    // --- TA menu helpers ---

    private List<Assignment> sectionAssignments(Section section) {
        List<Assignment> result = new java.util.ArrayList<>();
        for (Assignment assignment : studentService.getAssignments()) {
            if (assignment.getSection() == section) result.add(assignment);
        }
        return result;
    }

    /** Lists the section's assignments and asks for one; returns null if none or not found. */
    private Assignment chooseAssignment(Section section) {
        List<Assignment> assignments = sectionAssignments(section);
        printList("Assignments for " + section.getSectionId() + ":", assignments, "No assignments yet.");
        if (assignments.isEmpty()) return null;
        Assignment assignment = studentService.findAssignment(prompt("Assignment ID: "));
        if (assignment == null || !assignments.contains(assignment)) {
            System.out.println("Assignment not found in your section.");
            return null;
        }
        return assignment;
    }

    /** Asks for an assignment, lists its submissions and asks for one; returns null if none or not found. */
    private Submission chooseSubmission(TeachingAssistant ta, Section section) throws CampusException {
        Assignment assignment = chooseAssignment(section);
        if (assignment == null) return null;
        List<Submission> submissions = studentService.viewSubmissions(ta, assignment);
        printList("Submissions for " + assignment.getTitle() + ":", submissions, "No submissions yet.");
        if (submissions.isEmpty()) return null;
        String id = prompt("Submission ID: ");
        for (Submission submission : submissions) {
            if (submission.getSubmissionId().equals(id)) return submission;
        }
        System.out.println("Submission not found.");
        return null;
    }

    // --- PERMANENT INSTRUCTOR MENU ---
    private void permanentInstructorMenu() {
        System.out.print("Enter your Instructor ID: ");
        Instructor inst = instructorService.getInstructorById(scanner.nextLine());
        if (!(inst instanceof PermanentInstructor)) { System.out.println("Not a Permanent Instructor!"); return; }
        PermanentInstructor pInst = (PermanentInstructor) inst;

        Logger.info(pInst.getTeacherId(), "Logged in to Permanent Instructor menu");
        while (true) {
            System.out.println("\n--- Permanent Instructor Menu (" + pInst.getName() + ") ---");
            System.out.println("1. View Assigned Sections & Enrolled Students");
            System.out.println("2. Mark/Update Attendance");
            System.out.println("3. Assign TA to Section");
            System.out.println("4. Supervise FYP (View/Eval/Schedule)");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();
            try {
                if (choice.equals("1")) {
                    instructorService.viewAssignedSections(pInst).forEach(sec -> {
                        System.out.println("Section: " + sec.getSectionId());
                        instructorService.viewEnrolledStudents(pInst, sec).forEach(st -> System.out.println("  - " + st.getName()));
                    });
                } else if (choice.equals("2")) {
                    System.out.print("Enter Section ID: "); Section sec = searchSection(scanner.nextLine());
                    System.out.print("Enter Student ID: "); Student st = studentService.findStudent(scanner.nextLine());
                    if (sec != null && st != null) {
                        System.out.print("Status (PRESENT/ABSENT/LATE): ");
                        AttendanceStatus status = AttendanceStatus.valueOf(scanner.nextLine().toUpperCase());
                        instructorService.markAttendance(pInst, st, sec, status);
                        System.out.println("Attendance marked.");
                    }
                } else if (choice.equals("3")) {
                    System.out.print("Enter Student ID to promote to TA: ");
                    Student st = studentService.findStudent(scanner.nextLine());
                    System.out.print("Enter Section ID: ");
                    Section sec = searchSection(scanner.nextLine());
                    if (st instanceof NormalStudent && sec != null) {
                        instructorService.assignTA(pInst, (NormalStudent) st, sec); // permission check + audit record
                        studentService.promoteToTA(st, sec); // replace the student with the TA in the registry and save
                        System.out.println("TA Assigned successfully.");
                    } else {
                        System.out.println("Invalid student type or section not found.");
                    }
                } else if (choice.equals("4")) {
                    fypMenu(pInst);
                } else if (choice.equals("5")) {
                    break;
                }
            } catch (CampusException e) {
                System.out.println("Campus Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // --- VISITING INSTRUCTOR MENU ---
    private void visitingInstructorMenu() {
        System.out.print("Enter your Instructor ID: ");
        Instructor inst = instructorService.getInstructorById(scanner.nextLine());
        if (!(inst instanceof VisitingInstructor)) { System.out.println("Not a Visiting Instructor!"); return; }
        VisitingInstructor vInst = (VisitingInstructor) inst;

        Logger.info(vInst.getTeacherId(), "Logged in to Visiting Instructor menu");
        while (true) {
            System.out.println("\n--- Visiting Instructor Menu (" + vInst.getName() + ") ---");
            System.out.println("1. View Assigned Sections & Enrolled Students");
            System.out.println("2. Mark/Update Attendance");
            System.out.println("3. Back to Main Menu");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();
            try {
                if (choice.equals("1")) {
                    instructorService.viewAssignedSections(vInst).forEach(sec -> {
                        System.out.println("Section: " + sec.getSectionId());
                        instructorService.viewEnrolledStudents(vInst, sec).forEach(st -> System.out.println("  - " + st.getName()));
                    });
                } else if (choice.equals("2")) {
                    System.out.print("Enter Section ID: "); Section sec = searchSection(scanner.nextLine());
                    System.out.print("Enter Student ID: "); Student st = studentService.findStudent(scanner.nextLine());
                    if (sec != null && st != null) {
                        System.out.print("Status (PRESENT/ABSENT/LATE): ");
                        AttendanceStatus status = AttendanceStatus.valueOf(scanner.nextLine().toUpperCase());
                        instructorService.markAttendance(vInst, st, sec, status);
                        System.out.println("Attendance marked.");
                    }
                } else if (choice.equals("3")) {
                    break;
                }
            } catch (CampusException e) {
                System.out.println("Campus Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // --- FYP SUPERVISION MENU ---
    private void fypMenu(PermanentInstructor pInst) {
    while (true) {
        System.out.println("\n--- FYP Supervision Menu ---");
        System.out.println("1. View Supervised FYP Groups");
        System.out.println("2. View FYP Group Details");
        System.out.println("3. Schedule FYP Meeting");
        System.out.println("4. Evaluate FYP Idea");
        System.out.println("5. Provide FYP Feedback");
        System.out.println("6. Back");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine();
        try {
            if (choice.equals("1")) {
                List<FYPGroup> groups = instructorService.getFYPGroupsBySupervisor(pInst);
                if (groups.isEmpty()) {
                    System.out.println("No groups under supervision");
                } else {
                    System.out.println("\n=== Supervised FYP Groups ===");
                    for (FYPGroup g : groups) {
                        System.out.println("  - " + g.getGroupId() + ": " + g.getTitle());
                    }
                }
            } else if (choice.equals("2")) {
                System.out.print("Enter FYP Group ID: ");
                FYPGroup group = pInst.viewFYPGroupDetails(scanner.nextLine());
                if (group != null) {
                    System.out.println("Title: " + group.getTitle());
                    System.out.println("Members: " + group.getMembers().size());
                    System.out.println("Meetings: " + group.getMeetings().size());
                }
            } else if (choice.equals("3")) {
                System.out.print("Enter FYP Group ID: ");
                FYPGroup group = pInst.viewFYPGroupDetails(scanner.nextLine());
                if (group != null) {
                    LocalDate date = promptDate("Meeting Date");
                    System.out.print("Agenda: ");
                    String agenda = scanner.nextLine();
                    FYPMeeting meeting = new FYPMeeting("M" + System.currentTimeMillis(), date, agenda);
                    instructorService.scheduleFYPMeeting(pInst, group, meeting);
                    System.out.println("Meeting scheduled.");
                }
            } else if (choice.equals("4")) {
                System.out.print("Enter FYP Group ID: ");
                FYPGroup group = pInst.viewFYPGroupDetails(scanner.nextLine());
                if (group != null) {
                    System.out.print("Score (0-100): ");
                    double score = Double.parseDouble(scanner.nextLine());
                    System.out.print("Evaluation feedback: ");
                    String feedback = scanner.nextLine();
                    FYPEvaluation eval = new FYPEvaluation("E" + System.currentTimeMillis());
                    instructorService.evaluateFYPIdea(pInst, group, eval, score, feedback);
                    System.out.println("Idea evaluated.");
                }
            } else if (choice.equals("5")) {
                System.out.print("Enter FYP Group ID: ");
                FYPGroup group = pInst.viewFYPGroupDetails(scanner.nextLine());
                if (group != null) {
                    System.out.print("Feedback: ");
                    instructorService.provideFYPFeedback(pInst, group, scanner.nextLine());
                    System.out.println("Feedback provided.");
                }
            } else if (choice.equals("6")) {
                break;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
}
