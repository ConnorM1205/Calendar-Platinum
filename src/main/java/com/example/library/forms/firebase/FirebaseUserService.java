package com.example.library.forms.firebase;

import com.example.library.forms.CalendarEvent;
import com.example.library.forms.ClassCourse;
import com.google.firebase.database.*;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class FirebaseUserService {

    public static void saveUser(String username, String password) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Map<String, Object> userData = new HashMap<>();
        userData.put("password", hashedPassword);

        ref.child("users").child(username).setValueAsync(userData);
        System.out.println("User saved: " + username);
    }

    public static boolean loginUser(String username, String password) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};

        ref.child("users").child(username).child("password").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String storedHashed = snapshot.getValue(String.class);
                        if (storedHashed != null && BCrypt.checkpw(password, storedHashed)) {
                            result[0] = true;
                            System.out.println("Login success: " + username);
                        } else {
                            System.out.println("Login failed");
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println("Firebase error: " + error.getMessage());
                        latch.countDown();
                    }
                }
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result[0];
    }

    public static void saveClass(String username, ClassCourse course) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        String classId = course.getTitle();

        Map<String, Object> classData = new HashMap<>();
        classData.put("title", course.getTitle());
        classData.put("location", course.getLocation());
        classData.put("instructor", course.getInstructor());

        ref.child("users").child(username).child("classes").child(classId).setValueAsync(classData);
        System.out.println("Class saved: " + classId);
    }

    public static List<ClassCourse> loadClasses(String username) {
        List<ClassCourse> classList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        CountDownLatch latch = new CountDownLatch(1);

        ref.child("users").child(username).child("classes").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot classSnap : snapshot.getChildren()) {
                            String title = classSnap.child("title").getValue(String.class);
                            String location = classSnap.child("location").getValue(String.class);
                            String instructor = classSnap.child("instructor").getValue(String.class);
                            Integer colorRGB = classSnap.child("colorRGB").getValue(Integer.class);
                            Color color = new Color(colorRGB != null ? colorRGB : Color.CYAN.getRGB());

                            classList.add(new ClassCourse(title, instructor, location));
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println("Load class error: " + error.getMessage());
                        latch.countDown();
                    }
                }
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return classList;
    }

    public static void saveEvent(String username, CalendarEvent event) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        String eventId = event.getTitle() + "_" + event.getDate();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", event.getTitle());
        eventData.put("date", event.getDate().toString());
        eventData.put("time", event.getTime().toString());
        eventData.put("description", event.getDescription());
        eventData.put("location", event.getLocation());
        eventData.put("classTitle", event.getCourse().getTitle());

        ref.child("users").child(username).child("events").child(eventId).setValueAsync(eventData);
        System.out.println("Event saved: " + eventId);
    }

    public static List<CalendarEvent> loadEvents(String username, List<ClassCourse> classList) {
        List<CalendarEvent> eventList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        CountDownLatch latch = new CountDownLatch(1);

        ref.child("users").child(username).child("events").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot eventSnap : snapshot.getChildren()) {
                            String title = eventSnap.child("title").getValue(String.class);
                            String dateStr = eventSnap.child("date").getValue(String.class);
                            String timeStr = eventSnap.child("time").getValue(String.class);
                            String description = eventSnap.child("description").getValue(String.class);
                            String location = eventSnap.child("location").getValue(String.class);
                            String classTitle = eventSnap.child("classTitle").getValue(String.class);

                            LocalDate date = LocalDate.parse(dateStr);
                            LocalTime time = LocalTime.parse(timeStr);

                            ClassCourse course = classList.stream()
                                    .filter(c -> c.getTitle().equals(classTitle))
                                    .findFirst().orElse(null);

                            eventList.add(new CalendarEvent(title, date, time, description, location, course));
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println("Load event error: " + error.getMessage());
                        latch.countDown();
                    }
                }
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return eventList;
    }

    public static void saveUpcomingTask(String username, CalendarEvent event) {
        try {
            DatabaseReference dbRef = FirebaseSetup.getDatabase()
                    .getReference("users")
                    .child(username)
                    .child("upcomingTasks");

            String taskId = event.getTitle() + "_" + event.getDate();
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("title", event.getTitle());
            taskData.put("date", event.getDate().toString());
            taskData.put("time", event.getTime().toString());

            dbRef.child(taskId).setValueAsync(taskData);
            System.out.println("Task saved to upcoming list for " + username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void loadUpcomingTasks(String username, DefaultListModel<String> model) {
        if (username == null || username.isEmpty()) {
            return;
        }

        try {
            DatabaseReference dbRef = FirebaseSetup.getDatabase()
                    .getReference("users")
                    .child(username)
                    .child("upcomingTasks");

            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    model.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String title = child.child("title").getValue(String.class);
                        String date = child.child("date").getValue(String.class);
                        String time = child.child("time").getValue(String.class);

                        if (title != null && date != null && time != null) {
                            model.addElement(date + " - " + title + " @ " + time);
                        }
                    }
                    System.out.println("Loaded upcoming tasks for " + username);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.err.println("Failed to load tasks: " + error.getMessage());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteUser(String username) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.child("users").child(username).removeValueAsync();
            System.out.println("User deleted: " + username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
