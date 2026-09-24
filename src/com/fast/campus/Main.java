package com.fast.campus;

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
        new AcademicOfficeService();
        new InstructorService();
        new StudentService();

        System.out.println("FAST Campus Management System started.");
    }
}