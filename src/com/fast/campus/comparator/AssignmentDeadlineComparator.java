package com.fast.campus.comparator;

import com.fast.campus.model.Assignment;
import java.util.Comparator;

/**
 * Sorts assignments by deadline — earliest deadline first (ascending).
 */
public class AssignmentDeadlineComparator implements Comparator<Assignment> {
    @Override
    public int compare(Assignment o1, Assignment o2) {
        return o1.getDeadline().compareTo(o2.getDeadline());
    }
}
