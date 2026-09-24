package com.fast.campus.model;

/**
 * Abstract base class for every person in the campus system.
 * Extended by Administrator, Instructor, and Student hierarchies.
 */
public abstract class Person {

    protected String id;
    protected String name;
    protected String email;
    protected String phoneNumber;

    public Person(String id, String name, String email, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Getters
    public String getId()          { return id; }
    public String getName()        { return name; }
    public String getEmail()       { return email; }
    public String getPhoneNumber() { return phoneNumber; }

    // Setters
    public void setName(String name)               { this.name = name; }
    public void setEmail(String email)             { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /**
     * Returns the role label for this person (e.g., "Student", "Instructor").
     */
    public abstract String getRole();

    @Override
    public String toString() {
        return getRole() + " [id=" + id + ", name=" + name + "]";
    }
}
