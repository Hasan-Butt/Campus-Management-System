package com.fast.campus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-time permanent instructor who can also supervise FYP groups
 * and assign Teaching Assistants.
 *
 * <p>Owner: Saim</p>
 */
public class PermanentInstructor extends Instructor implements Evaluator {

    private List<FYPGroup> supervisedGroups;

    public PermanentInstructor(String id, String name, String email,
                               String phoneNumber, String teacherId) {
        super(id, name, email, phoneNumber, teacherId);
        this.supervisedGroups = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return "PermanentInstructor";
    }

    // --- Evaluator ---

    /** Reports the evaluation status of every FYP group this instructor supervises. */
    @Override
    public void evaluate() {
        for (FYPGroup group : supervisedGroups) {
            System.out.println("  " + group.getTitle() + ": " + group.getEvaluations().size()
                    + " evaluation(s) recorded");
        }
    }

    @Override
    public String getEvaluatorId()   { return getTeacherId(); }

    @Override
    public String getEvaluatorName() { return getName(); }

    public void assignTA(NormalStudent student, Section section) {
        if (student != null && section != null) {
            TeachingAssistant ta = new TeachingAssistant(student);
            section.assignTA(ta);
        }
    }

    public List<FYPGroup> viewFYPGroups() {
        return supervisedGroups;
    }

    /** UML: viewFYPGroupDetails(group : FYPGroup) : String */
    public String viewFYPGroupDetails(FYPGroup group) {
        if (group == null || !supervisedGroups.contains(group)) {
            return "Not a group supervised by " + getName();
        }
        return group.getDetails();
    }

    public void scheduleFYPMeeting(FYPGroup group, FYPMeeting meeting) {
        if (group != null && meeting != null) {
            group.addMeeting(meeting);
        }
    }

    public void evaluateFYPIdea(FYPGroup group, FYPEvaluation evaluation) {
        if (group != null && evaluation != null) {
            group.addEvaluation(evaluation);
        }
    }

    /** UML: provideFYPFeedback(evaluation : FYPEvaluation, feedback : String) */
    public void provideFYPFeedback(FYPEvaluation evaluation, String feedback) {
        if (evaluation != null && feedback != null) {
            // Keep the original evaluation feedback and append the new comment to it
            String existing = evaluation.getFeedback();
            evaluation.addFeedback(existing == null || existing.isBlank() ? feedback : existing + "; " + feedback);
        }
    }

    public void addSupervisedGroup(FYPGroup group) {
        if (group != null && !supervisedGroups.contains(group)) {
            supervisedGroups.add(group);
            if (group.getSupervisor() != this) {
                group.assignSupervisor(this);
            }
        }
    }

    public List<FYPGroup> getSupervisedGroups() {
        return supervisedGroups;
    }
}
