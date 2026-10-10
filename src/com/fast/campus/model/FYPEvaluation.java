package com.fast.campus.model;

import java.time.LocalDate;

/**
 * Records an FYP group's idea evaluation by a supervisor.
 *
 * <p>Owner: Saim</p>
 * <p>UML: FYPEvaluation is "evaluated by" exactly one Evaluator.</p>
 */
public class FYPEvaluation {

    private String evaluationId;
    private LocalDate evaluationDate;
    private double score;
    private String feedback;
    private Evaluator evaluator;

    public FYPEvaluation(String evaluationId) {
        this(evaluationId, null);
    }

    public FYPEvaluation(String evaluationId, Evaluator evaluator) {
        this(evaluationId, evaluator, LocalDate.now(), 0, null);
    }

    /** Used when loading a saved evaluation with its original date, score and feedback. */
    public FYPEvaluation(String evaluationId, Evaluator evaluator, LocalDate evaluationDate,
                         double score, String feedback) {
        this.evaluationId = evaluationId;
        this.evaluator = evaluator;
        this.evaluationDate = evaluationDate;
        this.score = score;
        this.feedback = feedback;
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
    public Evaluator getEvaluator()      { return evaluator; }
}
