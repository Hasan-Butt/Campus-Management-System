package com.fast.campus.util;

import com.fast.campus.model.*;
import com.fast.campus.service.*;
import java.util.*;

public class CampusRegistry {
    public static final List<Administrator> admins = new ArrayList<>();
    public static final List<Student> students = new ArrayList<>();
    public static final List<Instructor> instructors = new ArrayList<>();
    public static final List<Course> courses = new ArrayList<>();
    public static final List<Section> sections = new ArrayList<>();
    public static final List<Enrollment> enrollments = new ArrayList<>();
    public static final List<Assignment> assignments = new ArrayList<>();
    public static final List<Submission> submissions = new ArrayList<>();
    public static final List<Request> requests = new ArrayList<>();
    public static final List<FYPGroup> fypGroups = new ArrayList<>();
    public static final List<FYPMeeting> fypMeetings = new ArrayList<>();
    public static final List<FYPEvaluation> fypEvaluations = new ArrayList<>();
    public static final List<Attendance> attendanceRecords = new ArrayList<>();

    public static Student findStudent(String id) {
        for (Student s : students) if (s.getStudentId().equals(id)) return s;
        return null;
    }
    
    public static Instructor findInstructor(String id) {
        for (Instructor i : instructors) if (i.getTeacherId().equals(id)) return i;
        return null;
    }
    
    public static Course findCourse(String code) {
        for (Course c : courses) if (c.getCourseCode().equals(code)) return c;
        return null;
    }
    
    public static Section findSection(String id) {
        for (Section s : sections) if (s.getSectionId().equals(id)) return s;
        return null;
    }
    
    public static FYPGroup findFYPGroup(String id) {
        for (FYPGroup g : fypGroups) if (g.getGroupId().equals(id)) return g;
        return null;
    }

    public static void loadAll(AcademicOfficeService ao, InstructorService ins, StudentService stu) {
        // Dependency order: courses -> sections -> instructors (+ section links) -> students
        // -> things that refer to students/sections. (ConsoleUI loads via the service
        // constructors in this same order.)
        ao.loadCourses();
        ao.loadSections();
        ins.loadInstructors();
        ao.restoreInstructorAssignments();
        stu.loadStudents();
        stu.loadEnrollments();
        stu.loadAssignments();
        stu.loadSubmissions();
        stu.loadRequests();
        ins.loadFYPGroups();
        ins.loadFYPMeetings();
        ins.loadFYPEvaluations();
        ins.loadAttendance();
    }
    
    public static void saveAll(AcademicOfficeService ao, InstructorService ins, StudentService stu) {
                ins.saveInstructors();
        stu.saveStudents();
        ao.saveCourses();
        ao.saveSections();
        stu.saveEnrollments();
        stu.saveAssignments();
        stu.saveSubmissions();
        stu.saveRequests();
        ins.saveFYPGroups();
        ins.saveFYPMeetings();
        ins.saveFYPEvaluations();
        ins.saveAttendance();
    }
}
