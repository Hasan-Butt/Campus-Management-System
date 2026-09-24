package com.fast.campus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * An FYP (Final Year Project) group supervised by a PermanentInstructor.
 *
 * <p>Owner: Saim</p>
 */
public class FYPGroup {

    private String groupId;
    private String title;
    private String description;
    private List<Student> members;
    private PermanentInstructor supervisor;
    private List<FYPMeeting> meetings;
    private List<FYPEvaluation> evaluations;

    public FYPGroup(String groupId, String title, String description) {
        this.groupId = groupId;
        this.title = title;
        this.description = description;
        this.members = new ArrayList<>();
        this.meetings = new ArrayList<>();
        this.evaluations = new ArrayList<>();
    }

    // --- Domain operations ---

    public void addMember(Student student) {
        members.add(student);
    }

    public void removeMember(Student student) {
        members.remove(student);
    }

    public List<Student> getMembers() { return members; }

    public void assignSupervisor(PermanentInstructor supervisor) {
        this.supervisor = supervisor;
    }

    public void addMeeting(FYPMeeting meeting) {
        meetings.add(meeting);
    }

    public void addEvaluation(FYPEvaluation evaluation) {
        evaluations.add(evaluation);
    }

    public String getDetails() {
        return "FYPGroup[" + groupId + " | " + title + " | members=" + members.size() + "]";
    }

    // --- Getters ---

    public String getGroupId()                    { return groupId; }
    public String getTitle()                      { return title; }
    public String getDescription()                { return description; }
    public PermanentInstructor getSupervisor()    { return supervisor; }
    public List<FYPMeeting> getMeetings()         { return meetings; }
    public List<FYPEvaluation> getEvaluations()   { return evaluations; }
}
