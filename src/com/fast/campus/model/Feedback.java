package com.fast.campus.model;

import java.time.LocalDate;

/**
 * Feedback given by a Teaching Assistant or Instructor on a submission.
 *
 * <p>Owner: Kabeer</p>
 */
public class Feedback {

    private String feedbackId;
    private Evaluator evaluator;
    private String comments;
    private LocalDate date;

    public Feedback(String feedbackId, Evaluator evaluator, String comments) {
        this(feedbackId, evaluator, comments, LocalDate.now());
    }

    public Feedback(String feedbackId, Evaluator evaluator, String comments, LocalDate date) {
        this.feedbackId = feedbackId;
        this.evaluator = evaluator;
        this.comments = comments;
        this.date = date;
    }

    // --- Getters ---

    public String getFeedbackId()   { return feedbackId; }
    public Evaluator getEvaluator() { return evaluator; }
    public String getComments()     { return comments; }
    public LocalDate getDate()      { return date; }

    @Override
    public String toString() {
        String by = evaluator != null ? evaluator.getEvaluatorName() : "unknown";
        return "Feedback[" + feedbackId + " by " + by + "] " + comments;
    }
}
