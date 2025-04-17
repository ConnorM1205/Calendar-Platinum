package com.example.library.forms;

import java.awt.*;

public class ClassCourse {
    private String title;
    private final Color color;
    private String location;
    private String instructor;

    public ClassCourse (String title, Color color, String location, String instructor) {
        this.title = title;
        this.color = color;
        this.location = location;
        this.instructor = instructor;

    }
    public String getTitle() {return title;}
    public Color getColor() {return color;}
    public String getLocation() {return location;}
    public String getInstructor() {return instructor;}

    @Override
    public String toString() {
        return title; // This helps show names in dropdowns
    }
}
