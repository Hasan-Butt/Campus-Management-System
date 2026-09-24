package com.fast.campus.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a course offered by the university.
 *
 * <p>Owner: Hasan</p>
 */
public class Course {

    private String courseCode;
    private String title;
    private int creditHours;
    private Set<Course> prerequisites;
    private List<Section> sections;

    public Course(String courseCode, String title, int creditHours) {
        this.courseCode = courseCode;
        this.title = title;
        this.creditHours = creditHours;
        this.prerequisites = new HashSet<>();
        this.sections = new ArrayList<>();
    }

    // --- Domain operations ---

    public void addPrerequisite(Course course) {
        prerequisites.add(course);
    }

    public void addSection(Section section) {
        sections.add(section);
    }

    // --- Getters ---

    public String getCourseCode()          { return courseCode; }
    public String getTitle()               { return title; }
    public int getCreditHours()            { return creditHours; }
    public Set<Course> getPrerequisites()  { return prerequisites; }
    public List<Section> getSections()     { return sections; }

    // --- Setters ---

    public void setTitle(String title)         { this.title = title; }
    public void setCreditHours(int creditHours) { this.creditHours = creditHours; }

    @Override
    public String toString() {
        return "Course[" + courseCode + " | " + title + " | " + creditHours + " cr]";
    }
}
