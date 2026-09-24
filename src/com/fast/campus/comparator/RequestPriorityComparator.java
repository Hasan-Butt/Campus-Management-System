package com.fast.campus.comparator;

import com.fast.campus.model.Request;
import java.util.Comparator;

/**
 * Sorts requests by priority — higher priority value first (descending).
 */
public class RequestPriorityComparator implements Comparator<Request> {
    @Override
    public int compare(Request o1, Request o2) {
        return Integer.compare(o2.getPriority(), o1.getPriority());
    }
}
