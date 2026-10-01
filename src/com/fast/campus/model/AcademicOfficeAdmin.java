package com.fast.campus.model;
import java.util.List;
import com.fast.campus.enums.RequestStatus;

/**
 * Concrete administrator responsible for managing courses, sections,
 * instructor assignments, room assignments, and student requests.
 *
 * <p>Owner: Hasan</p>
 */

public class AcademicOfficeAdmin extends Administrator {

    public AcademicOfficeAdmin(String id, String name, String email,
                               String phoneNumber, String adminId) {
        super(id, name, email, phoneNumber, adminId);
    }

    @Override
    public String getRole() {
        return "AcademicOfficeAdmin";
    }

    public void createCourse(Course course){
        if (course != null) {
            System.out.println("Course created successfully");
        }
        else{
            System.out.println("Course creation failed");
        }
    }

    public void updateCourse(Course course){
        if (course != null) {
            System.out.println("Course updated successfully");
        }
        else{
            System.out.println("Course update failed");
        }
    }

    public Course searchCourse(String courseCode){
        if (courseCode != null) {
            System.out.println("Course found successfully");
        }
        else{
            System.out.println("Course not found");
        }
        
    }

    public void createSection(Section section){
        if (section != null) {
            System.out.println("Section created successfully");
        }
        else{
            System.out.println("Section creation failed");
        }
    }

    public void updateSection(Section section){
        if (section != null) {
            System.out.println("Section updated successfully");
        }
    }

    public void setCapacity(Section section, int capacity){
        if (section != null && capacity > 0) {
            section.setCapacity(capacity);
        }
    }

    public void assignRoom(Section section, Schedule schedule){
        if (section != null && schedule != null) {
            section.setSchedule(schedule);
    }
    }

    public void assignInstructor(Section section, Instructor instructor){
        if (section != null && instructor != null) {
            section.assignInstructor(instructor);
    }
    }
    public void viewRequests(List<Request> requests){
        if (requests != null) {
            System.out.println("Requests viewed successfully");
        }
        else{
            System.out.println("Requests not found");
        }   
    }

    public void approveRequest(Request request){
        if (request != null) {
            request.setStatus(RequestStatus.APPROVED);
        }
    }

    public void rejectRequest(Request request){
        if (request != null) {
            request.setStatus(RequestStatus.REJECTED);
        }
    }
}
