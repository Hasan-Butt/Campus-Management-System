package com.fast.campus;

import com.fast.campus.enums.Day;
import com.fast.campus.exception.CourseException;
import com.fast.campus.model.*;
import com.fast.campus.service.AcademicOfficeService;
import com.fast.campus.service.InstructorService;
import com.fast.campus.service.StudentService;

/**
 * Application entry point for the Campus Management System.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        System.out.println("=== FAST Campus Management System ===\n");

        // Initialize services
        AcademicOfficeService academicService = new AcademicOfficeService();
        InstructorService instructorService = new InstructorService();
        StudentService studentService = new StudentService(academicService);

        System.out.println("\n--- Academic Office Admin Demo ---\n");

        try {
            // 1. Create Courses
            Course cs101 = new Course("CS101", "Introduction to Computing", 3);
            Course cs201 = new Course("CS201", "Data Structures", 3);
            Course cs301 = new Course("CS301", "Software Construction", 3);

            academicService.createCourse(cs101);
            academicService.createCourse(cs201);
            academicService.createCourse(cs301);

            // 2. Search Course
            System.out.println();
            Course found = academicService.searchCourse("CS201");
            System.out.println("  → Search result: " + found);

            // 3. Update Course
            System.out.println();
            academicService.updateCourse("CS101", "Intro to Programming", 4);

            // 4. Create Sections
            System.out.println();
            Schedule scheduleA = new Schedule(Day.MONDAY, "08:00", "09:30", "Room-101");
            Schedule scheduleB = new Schedule(Day.WEDNESDAY, "10:00", "11:30", "Room-202");

            Section secA = new Section("CS101-A", 40, cs101, scheduleA);
            Section secB = new Section("CS201-A", 35, cs201, scheduleB);

            academicService.createSection(secA);
            academicService.createSection(secB);

            // 5. Update Section Capacity
            System.out.println();
            academicService.updateSection("CS101-A", 50);

            // 6. Set Capacity via direct section reference
            System.out.println();
            academicService.setCapacity(secB, 45);

            // 7. Assign Room
            System.out.println();
            academicService.assignRoom("CS101-A", "Room-301");

            // 8. Assign Instructor
            System.out.println();
            PermanentInstructor prof = new PermanentInstructor(
                    "P001", "Dr. Ahmed Khan", "ahmed@fast.edu.pk", "0300-1234567", "INST-001");
            academicService.assignInstructor("CS101-A", prof);

            // Summary
            System.out.println("\n--- Summary ---");
            System.out.println("Total Courses: " + academicService.getTotalCourses());
            System.out.println("Total Sections: " + academicService.getTotalSections());
            System.out.println();

        } catch (CourseException e) {
            System.err.println("Error: " + e.getMessage());
        }

        System.out.println("=== System Ready ===");
    }
}