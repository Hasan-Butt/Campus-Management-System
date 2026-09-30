package com.fast.campus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-time permanent instructor who can also supervise FYP groups
 * and assign Teaching Assistants.
 *
 * <p>Owner: Saim</p>
 */
public class PermanentInstructor extends Instructor {

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

    public void assignTA(NormalStudent student, Section section) {
        if (student != null && section != null) {
            TeachingAssistant ta = new TeachingAssistant(student);
            section.assignTA(ta);
        }
    }

    public List<FYPGroup> viewFYPGroups() {
        return supervisedGroups;
    }

    public FYPGroup viewFYPGroupDetails(String groupId) {
        for (FYPGroup group : supervisedGroups) {
            if (group.getGroupId().equals(groupId)) {
                return group;
            }
        }
        return null;
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

    public void provideFYPFeedback(FYPGroup group, String feedback) {
        if (group != null && feedback != null) {
            FYPEvaluation evaluation = new FYPEvaluation("EVAL_" + System.currentTimeMillis());
            evaluation.addFeedback(feedback);
            group.addEvaluation(evaluation);
        }
    }

    public void addSupervisedGroup(FYPGroup group) {
        if (group != null && !supervisedGroups.contains(group)) {
            supervisedGroups.add(group);
        }
    }

    public List<FYPGroup> getSupervisedGroups() {
        return supervisedGroups;
    }
}
