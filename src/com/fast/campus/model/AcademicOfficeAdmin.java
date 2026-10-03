package com.fast.campus.model;
import java.util.List;
import com.fast.campus.enums.RequestStatus;
import com.fast.campus.exception.CourseException;
import com.fast.campus.service.AcademicOfficeService;

/**
 * Concrete administrator responsible for managing courses, sections,
 * instructor assignments, room assignments, and student requests.
 *
 * <p>Owner: Hasan</p>
 */

public class AcademicOfficeAdmin extends Administrator {

    private final AcademicOfficeService service;

    public AcademicOfficeAdmin(String id, String name, String email,
                               String phoneNumber, String adminId,
                               AcademicOfficeService service) {
        super(id, name, email, phoneNumber, adminId);
        this.service = service;
    }

    @Override
    public String getRole() {
        return "AcademicOfficeAdmin";
    }

    public void createCourse(Course course){
        try {
            service.createCourse(course);
        } catch (CourseException e) {
            System.out.println("Course creation failed: " + e.getMessage());
        }
    }

    public void updateCourse(Course course){
        if (course == null) {
            System.out.println("Course update failed: course is null");
            return;
        }
        try {
            service.updateCourse(course.getCourseCode(), course.getTitle(), course.getCreditHours());
        } catch (CourseException e) {
            System.out.println("Course update failed: " + e.getMessage());
        }
    }

    public Course searchCourse(String courseCode){
        Course course = service.searchCourse(courseCode);
        if (course != null) {
            System.out.println("Course found successfully");
        } else {
            System.out.println("Course not found");
        }
        return course;
    }

    public void createSection(Section section){
        try {
            service.createSection(section);
        } catch (CourseException e) {
            System.out.println("Section creation failed: " + e.getMessage());
        }
    }

    public void updateSection(Section section){
        if (section == null) {
            System.out.println("Section update failed: section is null");
            return;
        }
        try {
            service.updateSection(section.getSectionId(), section.getCapacity());
        } catch (CourseException e) {
            System.out.println("Section update failed: " + e.getMessage());
        }
    }

    public void setCapacity(Section section, int capacity){
        try {
            service.setCapacity(section, capacity);
        } catch (CourseException e) {
            System.out.println("Set capacity failed: " + e.getMessage());
        }
    }

    public void assignRoom(Section section, Schedule schedule){
        try {
            service.assignRoom(section, schedule);
        } catch (CourseException e) {
            System.out.println("Room assignment failed: " + e.getMessage());
        }
    }

    public void assignInstructor(Section section, Instructor instructor){
        try {
            service.assignInstructor(section, instructor);
        } catch (CourseException e) {
            System.out.println("Instructor assignment failed: " + e.getMessage());
        }
    }

    public List<Request> viewRequests(List<Request> requests){
        return service.viewRequests(requests);
    }

    public void approveRequest(Request request){
        try {
            service.approveRequest(request);
        } catch (CourseException e) {
            System.out.println("Approve request failed: " + e.getMessage());
        }
    }

    public void rejectRequest(Request request){
        try {
            service.rejectRequest(request);
        } catch (CourseException e) {
            System.out.println("Reject request failed: " + e.getMessage());
        }
    }
}
