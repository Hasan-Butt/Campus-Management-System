package com.fast.campus;

import com.fast.campus.enums.AttendanceStatus;
import com.fast.campus.enums.Day;
import com.fast.campus.enums.RequestCategory;
import com.fast.campus.exception.CampusException;
import com.fast.campus.model.*;
import com.fast.campus.service.AcademicOfficeService;
import com.fast.campus.service.InstructorService;
import com.fast.campus.service.StudentService;

import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private final AcademicOfficeService academicService;
    private final InstructorService instructorService;
    private final StudentService studentService;
    private final Scanner scanner;

    public ConsoleUI() {
        this.academicService = new AcademicOfficeService();
        this.instructorService = new InstructorService();
        this.studentService = new StudentService(academicService);
        this.scanner = new Scanner(System.in);
    }

    private Section searchSection(String sectionId) {
        for (Section s : academicService.getSections()) {
            if (s.getSectionId().equals(sectionId)) return s;
        }
        return null;
    }

    public void start() {
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
                case "6": running = false; break;
                default: System.out.println("Invalid choice.");
            }
        }
        System.out.println("Exiting System...");
    }

    // --- ADMIN MENU ---
    private void adminMenu() {
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
                    for (Request r : reqs) System.out.println(r);
                    System.out.println("Note: Processing logic goes here in real UI.");
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
                        com.fast.campus.util.CampusRegistry.students.add(new NormalStudent(id, name, email, phone, sId));
                        System.out.println("Student created.");
                    } else if (um.equals("2")) {
                        System.out.print("ID: "); String id = scanner.nextLine();
                        System.out.print("Name: "); String name = scanner.nextLine();
                        System.out.print("Email: "); String email = scanner.nextLine();
                        System.out.print("Phone: "); String phone = scanner.nextLine();
                        System.out.print("Teacher ID: "); String tId = scanner.nextLine();
                        com.fast.campus.util.CampusRegistry.instructors.add(new PermanentInstructor(id, name, email, phone, tId));
                        System.out.println("Permanent Instructor created.");
                    } else if (um.equals("3")) {
                        System.out.print("ID: "); String id = scanner.nextLine();
                        System.out.print("Name: "); String name = scanner.nextLine();
                        System.out.print("Email: "); String email = scanner.nextLine();
                        System.out.print("Phone: "); String phone = scanner.nextLine();
                        System.out.print("Teacher ID: "); String tId = scanner.nextLine();
                        com.fast.campus.util.CampusRegistry.instructors.add(new VisitingInstructor(id, name, email, phone, tId));
                        System.out.println("Visiting Instructor created.");
                    } else if (um.equals("4")) {
                        System.out.print("Student ID to promote: "); String sId = scanner.nextLine();
                        Student s = studentService.findStudent(sId);
                        if (s instanceof NormalStudent) {
                            TeachingAssistant ta = new TeachingAssistant(s.getId(), s.getName(), s.getEmail(), s.getPhoneNumber(), s.getStudentId());
                            com.fast.campus.util.CampusRegistry.students.remove(s);
                            com.fast.campus.util.CampusRegistry.students.add(ta);
                            System.out.println("Student promoted to TA successfully.");
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

        while (true) {
            System.out.println("\n--- Student Menu (" + student.getName() + ") ---");
            System.out.println("1. Browse Courses");
            System.out.println("2. Register for Section");
            System.out.println("3. View Registered Courses & Credit Hours");
            System.out.println("4. View Assignments & Submit");
            System.out.println("5. View Attendance");
            System.out.println("6. Submit Course Clash Request");
            System.out.println("7. Back to Main Menu");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();
            try {
                if (choice.equals("1")) {
                    academicService.getCourses().forEach(System.out::println);
                } else if (choice.equals("2")) {
                    System.out.print("Enter Section ID: ");
                    Section section = searchSection(scanner.nextLine());
                    if (section != null) {
                        studentService.registerCourse(student, section);
                        System.out.println("Registered successfully.");
                    } else {
                        System.out.println("Section not found.");
                    }
                } else if (choice.equals("3")) {
                    student.getRegisteredCourses().forEach(System.out::println);
                    System.out.println("Total Credit Hours: " + student.getTotalCreditHours());
                } else if (choice.equals("4")) {
                    studentService.viewAssignments(student).forEach(System.out::println);
                    System.out.print("Enter Assignment ID to submit (or press Enter to skip): ");
                    String asgId = scanner.nextLine();
                    if (!asgId.isEmpty()) {
                        Assignment asg = studentService.findAssignment(asgId);
                        if (asg != null) {
                            System.out.print("Enter your submission content: ");
                            studentService.submitAssignment(student, asg, scanner.nextLine());
                            System.out.println("Submitted successfully.");
                        }
                    }
                } else if (choice.equals("5")) {
                    System.out.print("Enter Section ID: ");
                    Section sec = searchSection(scanner.nextLine());
                    if (sec != null) {
                        studentService.viewAttendance(student, sec).forEach(System.out::println);
                        System.out.println("Attendance %: " + studentService.viewAttendancePercentage(student, sec));
                    }
                } else if (choice.equals("6")) {
                    System.out.print("Enter Current Section ID: ");
                    Section current = searchSection(scanner.nextLine());
                    System.out.print("Enter Conflicting Section ID: ");
                    Section conflict = searchSection(scanner.nextLine());
                    if (current != null && conflict != null) {
                        if (!current.hasClash(conflict)) {
                            System.out.println("Validation Error: The selected sections do not have a schedule overlap. Request rejected.");
                        } else {
                            System.out.print("Enter description: ");
                            studentService.submitCourseClashRequest(student, current, conflict, scanner.nextLine(), 1);
                            System.out.println("Clash request submitted.");
                        }
                    }
                } else if (choice.equals("7")) {
                    break;
                }
            } catch (CampusException e) {
                System.out.println("Campus Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // --- TA MENU ---
    private void taMenu() {
        System.out.print("Enter your TA Student ID: ");
        Student s = studentService.findStudent(scanner.nextLine());
        if (!(s instanceof TeachingAssistant)) { System.out.println("You are not a TA!"); return; }
        TeachingAssistant ta = (TeachingAssistant) s;

        while (true) {
            System.out.println("\n--- TA Menu (" + ta.getName() + ") ---");
            System.out.println("1. View Assigned Section & Students");
            System.out.println("2. Create Assignment");
            System.out.println("3. Evaluate Submissions");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();
            try {
                if (choice.equals("1")) {
                    System.out.println("Assigned Section: " + (ta.getAssignedSection() != null ? ta.getAssignedSection().getSectionId() : "None"));
                    if (ta.getAssignedSection() != null) {
                        System.out.println("Enrolled Students:");
                        ta.getAssignedSection().getEnrolledStudents().forEach(System.out::println);
                    }
                } else if (choice.equals("2")) {
                    System.out.print("Title: "); String title = scanner.nextLine();
                    System.out.print("Description: "); String desc = scanner.nextLine();
                    System.out.print("Deadline (YYYY-MM-DD): "); String date = scanner.nextLine();
                    System.out.print("Total Marks: "); double marks = Double.parseDouble(scanner.nextLine());
                    studentService.createAssignment(ta, title, desc, java.time.LocalDate.parse(date), marks);
                    System.out.println("Assignment created.");
                } else if (choice.equals("3")) {
                    System.out.print("Enter Assignment ID to evaluate: ");
                    Assignment asg = studentService.findAssignment(scanner.nextLine());
                    if (asg != null) {
                        List<Submission> subs = studentService.viewSubmissions(ta, asg);
                        subs.sort(java.util.Comparator.comparing(Submission::getSubmissionDate));
                        for (Submission sub : subs) {
                            System.out.println(sub);
                            System.out.print("Enter marks (or press Enter to skip): ");
                            String m = scanner.nextLine();
                            if (!m.isEmpty()) {
                                studentService.evaluateSubmission(ta, sub, Double.parseDouble(m));
                                System.out.print("Enter feedback: ");
                                studentService.giveFeedback(ta, sub, scanner.nextLine());
                                System.out.println("Evaluation saved.");
                            }
                        }
                    }
                } else if (choice.equals("4")) {
                    break;
                }
            } catch (CampusException e) {
                System.out.println("Campus Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // --- PERMANENT INSTRUCTOR MENU ---
    private void permanentInstructorMenu() {
        System.out.print("Enter your Instructor ID: ");
        Instructor inst = instructorService.getInstructorById(scanner.nextLine());
        if (!(inst instanceof PermanentInstructor)) { System.out.println("Not a Permanent Instructor!"); return; }
        PermanentInstructor pInst = (PermanentInstructor) inst;

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
                        instructorService.assignTA(pInst, (NormalStudent) st, sec);
                        System.out.println("TA Assigned successfully.");
                    } else {
                        System.out.println("Invalid student type or section not found.");
                    }
                } else if (choice.equals("4")) {
                    System.out.println("FYP Groups under supervision:");
                    instructorService.getFYPGroupsBySupervisor(pInst).forEach(System.out::println);
                    System.out.println("(Further FYP operations omitted for brevity)");
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
}
