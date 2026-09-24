package com.fast.campus.comparator;

import com.fast.campus.model.FYPMeeting;
import java.util.Comparator;

/**
 * Sorts FYP meetings by date — earliest first (ascending).
 */
public class FYPMeetingDateComparator implements Comparator<FYPMeeting> {
    @Override
    public int compare(FYPMeeting o1, FYPMeeting o2) {
        return o1.getMeetingDate().compareTo(o2.getMeetingDate());
    }
}
