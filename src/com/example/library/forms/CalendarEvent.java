package com.example.library.forms;

import java.time.LocalDate;
import java.time.LocalTime;

public class CalendarEvent {
    private final String title;
    private final LocalDate date;
    private final LocalTime time;
    private final String description;
    private final String location;
    private final ClassCourse course;

    public CalendarEvent(String title, LocalDate date, LocalTime time, String description, String location, ClassCourse course) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.description = description;
        this.location = location;
        this.course = course;
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

    public ClassCourse getCourse() {return course;}
}
