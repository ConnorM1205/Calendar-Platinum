package com.example.library.forms;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public class CalendarEvent {
    private final String title;
    private final LocalDate date;
    private final LocalTime time;
    private final String description;
    private final String location;
    private final ClassCourse course;

    //RYOSUKE 4/27 Sharing feature

    private final String owner; // who created the event
    private final Map<String, String> sharedWith; // map of username -> permission ("edit" or "view")

    // Constructor (full)
    public CalendarEvent(String title, LocalDate date, LocalTime time, String description, String location, ClassCourse course, String owner, Map<String, String> sharedWith) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.description = description;
        this.location = location;
        this.course = course;
       //RYOSUKE 4/27
        this.owner = owner;
        this.sharedWith = sharedWith;
    }


    public CalendarEvent(String title, LocalDate date, LocalTime time, String description, String location, ClassCourse course) {
        this(title, date, time, description, location, course, null, null);
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public ClassCourse getCourse() {
        return course;
    }

    //RYOSUKE 4/27
    public String getOwner() {
        return owner;
    }

    public Map<String, String> getSharedWith() {
        return sharedWith;
    }

   // RYOSUKE 4/27

    // Helper methods
    public boolean canView(String username) {
        if (owner != null && owner.equals(username)) return true;
        if (sharedWith != null && sharedWith.containsKey(username)) return true;
        return false;
    }

    public boolean canEdit(String username) {
        if (owner != null && owner.equals(username)) return true;
        if (sharedWith != null && "edit".equalsIgnoreCase(sharedWith.get(username))) return true;
        return false;
    }
}
