package com.example.library.forms;

import java.awt.*;

public class ClassCourse {
    private String title;
    private String location;
    private String instructor;

    public ClassCourse(String title, String location, String instructor) {
        this.title = title;
        this.location = location;
        this.instructor = instructor;
    }

    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public String getInstructor() { return instructor; }

    public void setTitle(String title) { this.title = title; }
    public void setLocation(String location) { this.location = location; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    @Override
    public String toString() {
        return title; // This helps show names in dropdowns
    }
}
