package com.fast.campus.comparator;

import com.fast.campus.model.Request;
import java.util.Comparator;

/**
 * Sorts requests by submission date — oldest first (ascending).
 */
public class RequestDateComparator implements Comparator<Request> {
    @Override
    public int compare(Request o1, Request o2) {
        return o1.getRequestDate().compareTo(o2.getRequestDate());
    }
}
