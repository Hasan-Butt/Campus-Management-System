package com.fast.campus.model;

import com.fast.campus.enums.RequestStatus;
import java.time.LocalDate;

/**
 * Abstract base class for all student requests.
 *
 * <p>Owner: Kabeer</p>
 */
public abstract class Request {

    protected String requestId;
    protected LocalDate requestDate;
    protected String description;
    protected RequestStatus status;
    protected int priority;

    public Request(String requestId, String description, int priority) {
        this.requestId = requestId;
        this.requestDate = LocalDate.now();
        this.description = description;
        this.status = RequestStatus.PENDING;
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
}
