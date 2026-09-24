package com.fast.campus.model;

/**
 * A request raised when a student's course sections have a schedule conflict.
 *
 * <p>Owner: Kabeer</p>
 */
public class CourseClashRequest extends Request {

    private Section conflictingSection;
    private Section requestedSection;

    public CourseClashRequest(String requestId, String description, int priority,
                              Section conflictingSection, Section requestedSection) {
        super(requestId, description, priority);
        this.conflictingSection = conflictingSection;
        this.requestedSection = requestedSection;
    }

    public String getConflictDetails() {
        return "Conflict between " + conflictingSection.getSectionId()
                + " and " + requestedSection.getSectionId();
    }

    @Override
    public String getDetails() {
        return "CourseClashRequest[" + requestId + "] — " + getConflictDetails();
    }

    // --- Getters ---

    public Section getConflictingSection() { return conflictingSection; }
    public Section getRequestedSection()   { return requestedSection; }
}
