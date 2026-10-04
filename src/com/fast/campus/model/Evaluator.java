package com.fast.campus.model;

/**
 * Contract for anyone who can evaluate/grade work (UML: «interface» Evaluator).
 *
 * <p>Owner: Saim</p>
 * <p>Implemented by PermanentInstructor (FYP evaluation) and TeachingAssistant
 * (assignment grading). It is an interface rather than an abstract class because
 * TeachingAssistant already extends Student, and a Java class can extend only one
 * class but implement many interfaces.</p>
 */
public interface Evaluator {

    /** Performs this evaluator's evaluation work (meaning depends on the role). */
    void evaluate();

    /** Unique ID of the evaluator (used when saving who gave feedback). */
    String getEvaluatorId();

    /** Display name of the evaluator. */
    String getEvaluatorName();
}
