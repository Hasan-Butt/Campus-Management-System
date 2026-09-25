package com.fast.campus.model;

/**
 * Abstract base class for entities that can evaluate/grade work.
 * 
 * <p>Owner: Saim</p>
 * <p>This interface defines the contract for evaluation behavior.
 * Implementing classes include PermanentInstructor (for FYP evaluation)
 * and potentially TeachingAssistant (for assignment grading).</p>
 */
public abstract class Evaluator {

    /**
     * Performs an evaluation.
     * The specific evaluation logic is implemented by subclasses.
     * 
     * <p>Examples:
     * - PermanentInstructor evaluates FYP projects with scores and feedback
     * - TeachingAssistant evaluates assignment submissions with marks and comments
     * </p>
     */
    public abstract void evaluate();
    
    /**
     * Gets the evaluator's unique identifier.
     * @return the evaluator's ID
     */
    public abstract String getEvaluatorId();
    
   // return evaluator name
    public abstract String getEvaluatorName();
}
