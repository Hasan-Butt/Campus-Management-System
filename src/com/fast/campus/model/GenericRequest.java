package com.fast.campus.model;

import com.fast.campus.enums.RequestCategory;

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

    public RequestCategory getCategory() { return category; }

    @Override
    public String getDetails() {
        return "GenericRequest[" + requestId + " | " + category + "] — " + description;
    }
}
