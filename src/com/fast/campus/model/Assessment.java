package com.fast.campus.model;

import java.time.LocalDate;

/**
 * Abstract base for all assessments (assignments, quizzes, exams).
 *
 * <p>Owner: Kabeer</p>
 */
public abstract class Assessment {

    protected String id;
    protected String title;
    protected String description;
    protected LocalDate deadline;
    protected double totalMarks;

    public Assessment(String id, String title, String description,
                      LocalDate deadline, double totalMarks) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.totalMarks = totalMarks;
    }

    // --- Getters ---

    public String getId()           { return id; }
    public String getTitle()        { return title; }
    public String getDescription()  { return description; }
    public LocalDate getDeadline()  { return deadline; }
    public double getTotalMarks()   { return totalMarks; }
}
