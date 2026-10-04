package com.fast.campus.model;

import com.fast.campus.enums.RequestCategory;
import com.fast.campus.enums.RequestStatus;
import java.time.LocalDate;

/**
 * A general-purpose student request not covered by a specific subtype.
 *
 * <p>Owner: Kabeer</p>
 */
public class GenericRequest extends Request {

    private RequestCategory category;

    public GenericRequest(String requestId, String description, int priority,
                          RequestCategory category) {
        super(requestId, description, priority);
        this.category = category;
    }

    /** Used when loading a saved request with its original date and status. */
    public GenericRequest(String requestId, String description, int priority,
                          RequestCategory category, LocalDate requestDate, RequestStatus status) {
        super(requestId, description, priority, requestDate, status);
        this.category = category;
    }

    public RequestCategory getCategory() { return category; }

    @Override
    public String getDetails() {
        return "GenericRequest[" + getRequestId() + " | " + category + "] — " + getDescription();
    }
}
