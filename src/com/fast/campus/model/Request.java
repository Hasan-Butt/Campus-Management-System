package com.fast.campus.model;

import com.fast.campus.enums.RequestStatus;
import java.time.LocalDate;

/**
 * Abstract base class for all student requests.
 *
 * <p>Owner: Kabeer</p>
 */
public abstract class Request {

    private String requestId;
    private LocalDate requestDate;
    private String description;
    private RequestStatus status;
    private int priority;

    public Request(String requestId, String description, int priority) {
        this(requestId, description, priority, LocalDate.now(), RequestStatus.PENDING);
    }

    /** Used when loading a saved request with its original date and status. */
    public Request(String requestId, String description, int priority,
                   LocalDate requestDate, RequestStatus status) {
        this.requestId = requestId;
        this.requestDate = requestDate;
        this.description = description;
        this.status = status;
        this.priority = priority;
    }

    // --- Domain operations ---

    public void submit() {
        this.status = RequestStatus.PENDING;
    }

    public abstract String getDetails();

    // --- Getters / Setters ---

    public String getRequestId()          { return requestId; }
    public LocalDate getRequestDate()     { return requestDate; }
    public String getDescription()        { return description; }
    public RequestStatus getStatus()      { return status; }
    public int getPriority()              { return priority; }

    public void setStatus(RequestStatus status) { this.status = status; }

    @Override
    public String toString() {
        return getDetails() + " | " + status + " | priority " + priority + " | " + requestDate;
    }
}
