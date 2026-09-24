package com.fast.campus.model;

import java.time.LocalDate;

/**
 * Records an FYP group's idea evaluation by a supervisor.
 *
 * <p>Owner: Saim</p>
 */
public class FYPEvaluation {

    private String evaluationId;
    private LocalDate evaluationDate;
    private double score;
    private String feedback;

    public FYPEvaluation(String evaluationId) {
        this.evaluationId = evaluationId;
        this.evaluationDate = LocalDate.now();
    }

    // --- Domain operations ---

    public void evaluate(double score) {
        this.score = score;
        this.evaluationDate = LocalDate.now();
    }

    public void addFeedback(String feedback) {
        this.feedback = feedback;
    }

    // --- Getters ---

    public String getEvaluationId()      { return evaluationId; }
    public LocalDate getEvaluationDate() { return evaluationDate; }
    public double getScore()             { return score; }
    public String getFeedback()          { return feedback; }
}
