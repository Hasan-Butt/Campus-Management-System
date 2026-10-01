package com.fast.campus.model;

import com.fast.campus.enums.Day;
import java.time.LocalTime;

/**
 * Represents the scheduled time slot and room for a section.
 *
 * <p>Owner: Hasan</p>
 */
public class Schedule {

    private Day day;
    private LocalTime startTime; // e.g., "08:00"
    private LocalTime endTime;   // e.g., "09:30"
    private String room;

    public Schedule(Day day, String startTime, String endTime, String room) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
    }

    /**
     * Checks whether this schedule overlaps with another schedule.
     *
     * @param schedule the schedule to compare against
     * @return true if there is a time/day clash
     */
    public boolean hasClash(Schedule schedule) {
        if (this.day != schedule.day) return false;
        return this.startTime.isBefore(schedule.endTime)
                && schedule.startTime.isBefore(this.endTime);
    }

    public String getScheduleInfo() {
        return day + " " + startTime + "-" + endTime + " @ " + room;
    }

    // --- Getters ---

    public Day getDay()        { return day; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime()   { return endTime; }
    public String getRoom()      { return room; }

    // --- Setters ---

    public void setRoom(String room)           { this.room = room; }
    public void setDay(Day day)                { this.day = day; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime)     { this.endTime = endTime; }

    @Override
    public String toString() {
        return getScheduleInfo();
    }
}
