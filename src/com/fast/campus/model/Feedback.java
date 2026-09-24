package com.fast.campus.model;

import java.time.LocalDate;

/**
 * Feedback given by a Teaching Assistant or Instructor on a submission.
 *
 * <p>Owner: Kabeer</p>
 */
public class Feedback {

    private String feedbackId;
    private String evaluator;   // Name/ID of the evaluator
    private String comments;
    private LocalDate date;

    public Feedback(String feedbackId, String evaluator, String comments) {
        this.feedbackId = feedbackId;
        this.evaluator = evaluator;
        this.comments = comments;
        this.date = LocalDate.now();
    }

    // --- Getters ---

    public String getFeedbackId() { return feedbackId; }
    public String getEvaluator()  { return evaluator; }
    public String getComments()   { return comments; }
    public LocalDate getDate()    { return date; }

    @Override
    public String toString() {
        return "Feedback[" + feedbackId + " by " + evaluator + "] " + comments;
    }
}
