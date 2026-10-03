package com.fast.campus.model;

import com.fast.campus.enums.RequestStatus;
import java.time.LocalDate;

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

    /** Used when loading a saved request with its original date and status. */
    public CourseClashRequest(String requestId, String description, int priority,
                              Section conflictingSection, Section requestedSection,
                              LocalDate requestDate, RequestStatus status) {
        super(requestId, description, priority, requestDate, status);
        this.conflictingSection = conflictingSection;
        this.requestedSection = requestedSection;
    }

    public String getConflictDetails() {
        return "Conflict between " + conflictingSection.getSectionId()
                + " and " + requestedSection.getSectionId();
    }

    @Override
    public String getDetails() {
        return "CourseClashRequest[" + getRequestId() + "] — " + getConflictDetails();
    }

    // --- Getters ---

    public Section getConflictingSection() { return conflictingSection; }
    public Section getRequestedSection()   { return requestedSection; }
}
