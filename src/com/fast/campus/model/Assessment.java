package com.fast.campus.model;

import java.time.LocalDate;

/**
 * Abstract base for all assessments (assignments, quizzes, exams).
 *
 * <p>Owner: Kabeer</p>
 */
public abstract class Assessment {

    private String id;
    private String title;
    private String description;
    private LocalDate deadline;
    private double totalMarks;

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
