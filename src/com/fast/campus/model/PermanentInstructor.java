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

    public void assignTAAsStudent(NormalStudent student, Section section) {
        if (student != null && section != null) {
            section.assignTA(student);
        }
    }

    public List<FYPGroup> viewFYPGroup() {
        return supervisedGroups;
    }

    public void scheduleFYPMeeting(FYPGroup group, FYPMeeting meeting) {
        if (group != null && meeting != null) {
            group.addMeeting(meeting);
        }
    }

    public void evaluateFYPIdea(FYPGroup group) {
    }

    public void provideFYPFeedback(FYPGroup group, String feedback) {
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
