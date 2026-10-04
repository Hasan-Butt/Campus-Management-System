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
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}
