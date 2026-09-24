package com.fast.campus.model;

import java.time.LocalDate;

/**
 * A scheduled meeting between an FYP group and its supervisor.
 *
 * <p>Owner: Saim</p>
 */
public class FYPMeeting {

    private String meetingId;
    private LocalDate meetingDate;
    private String agenda;
    private String notes;

    public FYPMeeting(String meetingId, LocalDate meetingDate, String agenda) {
        this.meetingId = meetingId;
        this.meetingDate = meetingDate;
        this.agenda = agenda;
        this.notes = "";
    }

    // --- Domain operations ---

    public String getMeetingDetails() {
        return "FYPMeeting[" + meetingId + " | " + meetingDate + " | " + agenda + "]";
    }

    public void updateNotes(String notes) {
        this.notes = notes;
    }

    // --- Getters ---

    public String getMeetingId()      { return meetingId; }
    public LocalDate getMeetingDate() { return meetingDate; }
    public String getAgenda()         { return agenda; }
    public String getNotes()          { return notes; }
}
