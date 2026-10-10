package com.fast.campus.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * An assignment created by a Teaching Assistant for a section.
 *
 * <p>Owner: Kabeer</p>
 */
public class Assignment extends Assessment {

    /** Late work is accepted (marked LATE) for this many days after the deadline, then rejected. */
    public static final int LATE_GRACE_DAYS = 7;

    private Section section;
    private TeachingAssistant createdBy;
    private List<Submission> submissions;

    public Assignment(String id, String title, String description,
                      LocalDate deadline, double totalMarks,
                      Section section, TeachingAssistant createdBy) {
        super(id, title, description, deadline, totalMarks);
        this.section = section;
        this.createdBy = createdBy;
        this.submissions = new ArrayList<>();
    }

    // --- Domain operations ---

    public void addSubmission(Submission submission) {
        if (submission != null && !submissions.contains(submission)) {
            submissions.add(submission);
            if (submission.getAssignment() != this) {
                submission.setAssignment(this);
            }
        }
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public boolean isDeadlinePassed() {
        return LocalDate.now().isAfter(getDeadline());
    }

    /** True once the late-submission grace period after the deadline is over. */
    public boolean isClosed() {
        return LocalDate.now().isAfter(getDeadline().plusDays(LATE_GRACE_DAYS));
    }

    // --- Getters ---

    public Section getSection()              { return section; }
    public TeachingAssistant getCreatedBy()  { return createdBy; }

    @Override
    public String toString() {
        return "[" + getId() + "] " + getTitle() + " | section " + section.getSectionId()
                + " | due " + getDeadline()
                + (isClosed() ? " (closed)" : isDeadlinePassed() ? " (late submissions until "
                        + getDeadline().plusDays(LATE_GRACE_DAYS) + ")" : "")
                + " | " + getTotalMarks() + " marks | " + submissions.size() + " submission(s)";
    }
}
