package com.fast.campus.service;

import com.fast.campus.exception.CourseException;
import com.fast.campus.exception.CourseFullException;
import com.fast.campus.model.*;
import com.fast.campus.util.FileManager;
import com.fast.campus.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for Academic Office Admin use cases.
 *
 * <p>Owner: Hasan</p>
 * Responsibilities:
 * <ul>
 *   <li>Create / update / search courses</li>
 *   <li>Create / update sections</li>
 *   <li>Assign instructors and rooms</li>
 *   <li>Approve / reject student requests</li>
 *   <li>Persist changes to data/courses.txt, data/sections.txt, data/enrollments.txt</li>
 * </ul>
 */
public class AcademicOfficeService {

    private static final String COURSES_FILE    = "data/courses.txt";
    private static final String SECTIONS_FILE   = "data/sections.txt";
    private static final String ENROLLMENTS_FILE = "data/enrollments.txt";

    private List<Course>  courses  = new ArrayList<>();
    private List<Section> sections = new ArrayList<>();

    // ------------------------------------------------------------------ Course

    public void createCourse(Course course) {
        courses.add(course);
        Logger.info("AcademicOfficeAdmin", "Course created: " + course.getCourseCode());
        // TODO: Hasan — persist to COURSES_FILE
    }

    public void updateCourse(String courseCode, String newTitle, int newCreditHours) {
        courses.stream()
               .filter(c -> c.getCourseCode().equals(courseCode))
               .findFirst()
               .ifPresent(c -> {
                   c.setTitle(newTitle);
                   c.setCreditHours(newCreditHours);
                   Logger.info("AcademicOfficeAdmin", "Course updated: " + courseCode);
                   // TODO: Hasan — persist changes
               });
    }

    public Course searchCourse(String courseCode) {
        return courses.stream()
                      .filter(c -> c.getCourseCode().equalsIgnoreCase(courseCode))
                      .findFirst()
                      .orElse(null);
    }

    // ----------------------------------------------------------------- Section

    public void createSection(Section section) {
        sections.add(section);
        Logger.info("AcademicOfficeAdmin", "Section created: " + section.getSectionId());
        // TODO: Hasan — persist to SECTIONS_FILE
    }

    public void updateSection(String sectionId, int newCapacity) {
        sections.stream()
                .filter(s -> s.getSectionId().equals(sectionId))
                .findFirst()
                .ifPresent(s -> {
                    s.setCapacity(newCapacity);
                    Logger.info("AcademicOfficeAdmin", "Section updated: " + sectionId);
                });
    }

    public void assignInstructor(String sectionId, Instructor instructor) {
        sections.stream()
                .filter(s -> s.getSectionId().equals(sectionId))
                .findFirst()
                .ifPresent(s -> {
                    s.assignInstructor(instructor);
                    Logger.info("AcademicOfficeAdmin",
                            "Instructor " + instructor.getName() + " assigned to section " + sectionId);
                });
    }

    public void assignRoom(String sectionId, String room) {
        sections.stream()
                .filter(s -> s.getSectionId().equals(sectionId))
                .findFirst()
                .ifPresent(s -> {
                    if (s.getSchedule() != null) {
                        s.getSchedule().setRoom(room);
                        Logger.info("AcademicOfficeAdmin", "Room " + room + " assigned to section " + sectionId);
                    }
                });
    }

    // ----------------------------------------------------------------- Requests

    public void viewRequests(List<Request> requests) {
        requests.forEach(r -> System.out.println(r.getDetails()));
    }

    public void approveRequest(Request request) {
        request.setStatus(com.fast.campus.enums.RequestStatus.APPROVED);
        Logger.info("AcademicOfficeAdmin", "Request approved: " + request.getRequestId());
    }

    public void rejectRequest(Request request) {
        request.setStatus(com.fast.campus.enums.RequestStatus.REJECTED);
        Logger.info("AcademicOfficeAdmin", "Request rejected: " + request.getRequestId());
    }

    // ------------------------------------------------------------------ Getters

    public List<Course> getCourses()   { return courses; }
    public List<Section> getSections() { return sections; }
}
