package com.fast.campus.model;

import com.fast.campus.exception.AssessmentException;
import com.fast.campus.exception.CourseException;
import com.fast.campus.exception.InvalidRequestException;
import com.fast.campus.exception.UnauthorizedActionException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Abstract base for all students (NormalStudent, TeachingAssistant).
 *
 * <p>Owner: Kabeer</p>
 */
public abstract class Student extends Person {

    private String studentId;
    private int totalCreditHours;
    private List<Enrollment> enrollments;
    private List<Request> requests;

    // Helper lists kept in sync by Section.enroll()/drop(), which rely on them
    private List<Section> enrolledSections;
    private List<Course> registeredCourses;

    public Student(String id, String name, String email,
                   String phoneNumber, String studentId) {
        super(id, name, email, phoneNumber);
        this.studentId = studentId;
        this.totalCreditHours = 0;
        this.enrollments = new ArrayList<>();
        this.requests = new ArrayList<>();
        this.enrolledSections = new ArrayList<>();
        this.registeredCourses = new ArrayList<>();
    }

    // --- Domain operations ---

    public void register(Section section) throws CourseException {
        if (section == null) {
            throw new CourseException("Section cannot be empty");
        }
        if (enrolledSections.contains(section)) {
            throw new CourseException("Already registered in section " + section.getSectionId());
        }
        if (section.getCourse() != null && registeredCourses.contains(section.getCourse())) {
            throw new CourseException("Already registered for course " + section.getCourse().getCourseCode());
        }
        section.enroll(this); // checks capacity and timetable clash, then links student <-> section
        calculateTotalCreditHours();
    }

    public void drop(Section section) throws CourseException {
        if (section == null || !enrolledSections.contains(section)) {
            throw new CourseException("Not registered in this section");
        }
        section.drop(this); // cancels the enrollment and unlinks the section
        registeredCourses.remove(section.getCourse());
        calculateTotalCreditHours();
    }

    public int calculateTotalCreditHours() {
        int total = 0;
        for (Section section : enrolledSections) {
            if (section.getCourse() != null) {
                total += section.getCourse().getCreditHours();
            }
        }
        totalCreditHours = total;
        return total;
    }

    public List<Course> viewCourses() {
        return registeredCourses;
    }

    public List<Schedule> viewTimetable() {
        List<Schedule> timetable = new ArrayList<>();
        for (Section section : enrolledSections) {
            if (section.getSchedule() != null) {
                timetable.add(section.getSchedule());
            }
        }
        timetable.sort(Comparator.comparing(Schedule::getDay).thenComparing(Schedule::getStartTime));
        return timetable;
    }

    public Submission submitAssignment(Assignment assignment, String content)
            throws UnauthorizedActionException, AssessmentException {
        if (assignment == null || !enrolledSections.contains(assignment.getSection())) {
            throw new UnauthorizedActionException(getName(), "submit an assignment of a section they are not enrolled in");
        }
        if (content == null || content.isBlank()) {
            throw new AssessmentException("Submission content cannot be empty");
        }
        Submission submission = new Submission("S-" + System.currentTimeMillis(), assignment, this, content);
        submission.submit(); // SUBMITTED, or LATE if after the deadline (late work is accepted)
        assignment.addSubmission(submission);
        return submission;
    }

    public void submitCourseClashRequest(CourseClashRequest request) throws InvalidRequestException {
        if (request == null) {
            throw new InvalidRequestException("Request cannot be empty");
        }
        Section current = request.getConflictingSection();
        Section wanted = request.getRequestedSection();
        if (current == null || wanted == null) {
            throw new InvalidRequestException("Both sections must be given");
        }
        if (!enrolledSections.contains(current)) {
            throw new InvalidRequestException("Not registered in section " + current.getSectionId());
        }
        if (!wanted.hasClash(current)) { // "Check Section Clash" use case
            throw new InvalidRequestException("Sections " + current.getSectionId() + " and "
                    + wanted.getSectionId() + " do not clash, so no request is needed");
        }
        request.submit();
        requests.add(request);
    }

    public List<Request> viewRequests() {
        return requests;
    }

    // --- Getters ---

    public String getStudentId()                    { return studentId; }
    public int getTotalCreditHours()                { return totalCreditHours; }
    public List<Enrollment> getEnrollments()        { return enrollments; }
    public List<Section> getEnrolledSections()      { return enrolledSections; }
    public List<Course> getRegisteredCourses()      { return registeredCourses; }
}
