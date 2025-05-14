
package com.example.library.forms.firebase;

import com.example.library.forms.CalendarEditor;
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

        // Check if user already exists
        ref.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    JOptionPane.showMessageDialog(null, "Username already exists.");
                } else {
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("password", hashedPassword);

                    ref.child("users").child(username).setValueAsync(userData);
                    System.out.println("User saved: " + username);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Error checking user: " + error.getMessage());
            }
        });
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

    //RYOSUKE 4/27 share event
    public static void saveEvent(String username, CalendarEvent event) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        String eventId = event.getTitle() + "_" + event.getDate();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", event.getTitle());
        eventData.put("date", event.getDate().toString());
        eventData.put("time", event.getTime().toString());
        eventData.put("description", event.getDescription());
        eventData.put("location", event.getLocation());
        eventData.put("classTitle", event.getCourse() != null ? event.getCourse().getTitle() : null);
        eventData.put("owner", event.getOwner());

        // Save sharedWith map if exists
        if (event.getSharedWith() != null && !event.getSharedWith().isEmpty()) {
            eventData.put("sharedWith", event.getSharedWith());
        }

        // Save under owner's events
        ref.child("users").child(username).child("events").child(eventId).setValueAsync(eventData);

        // Save to shared users　incomingShares
        if (event.getSharedWith() != null) {
            for (String sharedUser : event.getSharedWith().keySet()) {
                ref.child("users").child(sharedUser).child("incomingShares").child(eventId).setValueAsync(eventData);
            }
        }

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
                            String owner = eventSnap.child("owner").getValue(String.class);

                            // Read sharedWith if available
                            Map<String, String> sharedWith = new HashMap<>();
                            DataSnapshot sharedSnap = eventSnap.child("sharedWith");
                            if (sharedSnap.exists()) {
                                for (DataSnapshot entry : sharedSnap.getChildren()) {
                                    sharedWith.put(entry.getKey(), entry.getValue(String.class));
                                }
                            }

                            if (classTitle == null) {
                                System.out.println("⚠️ Skipping event: classTitle is null");
                                continue;
                            }

                            LocalDate date = LocalDate.parse(dateStr);
                            LocalTime time = LocalTime.parse(timeStr);

                            ClassCourse course = classList.stream()
                                    .filter(c -> c.getTitle().equals(classTitle))
                                    .findFirst()
                                    .orElse(null);
                            if (course == null) {
                                System.out.println("⚠️ Skipping event: Class not found for title → " + classTitle);
                                continue;
                            }

                            CalendarEvent event = new CalendarEvent(title, date, time, description, location, course, owner, sharedWith);
                            eventList.add(event);
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

                        model.addElement(date + " - " + title + " @ " + time);
                    }
                    System.out.println("Upcoming tasks loaded for " + username);
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


    public static boolean userExists(String username) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(username);
        final boolean[] exists = {false};
        CountDownLatch latch = new CountDownLatch(1);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                exists[0] = snapshot.exists();
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return exists[0];
    }


    // Ryosuke 4/28
    public static void saveIncomingShare(String username, CalendarEvent event) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        String eventId = event.getTitle() + "_" + event.getDate();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", event.getTitle());
        eventData.put("date", event.getDate().toString());
        eventData.put("time", event.getTime().toString());
        eventData.put("description", event.getDescription());
        eventData.put("location", event.getLocation());
        eventData.put("classTitle", event.getCourse() != null ? event.getCourse().getTitle() : null);
        eventData.put("owner", event.getOwner());

        ref.child("users").child(username).child("incomingShares").child(eventId).setValueAsync(eventData);

        System.out.println("Incoming share saved for user: " + username + ", event: " + eventId);
    }

    //RYOSUKE 4/27

    public static void checkIncomingShares(String username, CalendarEditor editor) {
        DatabaseReference ref = FirebaseSetup.getDatabase().getReference();
        ref.child("users").child(username).child("incomingShares").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<String> eventSummaries = new ArrayList<>();
                        List<DataSnapshot> eventSnapshots = new ArrayList<>();

                        for (DataSnapshot shareSnap : snapshot.getChildren()) {
                            String title = shareSnap.child("title").getValue(String.class);
                            String dateStr = shareSnap.child("date").getValue(String.class);
                            String fromUser = shareSnap.child("owner").getValue(String.class);

                            if (title != null && dateStr != null && fromUser != null) {
                                eventSummaries.add(title + " (from " + fromUser + " on " + dateStr + ")");
                                eventSnapshots.add(shareSnap);
                            }
                        }

                        if (eventSummaries.isEmpty()) {
                            return;
                        }

                        JCheckBox[] checkBoxes = new JCheckBox[eventSummaries.size()];
                        JPanel panel = new JPanel(new GridLayout(0, 1));
                        for (int i = 0; i < eventSummaries.size(); i++) {
                            checkBoxes[i] = new JCheckBox(eventSummaries.get(i));
                            panel.add(checkBoxes[i]);
                        }

                        int response = JOptionPane.showConfirmDialog(
                                editor,
                                new JScrollPane(panel),
                                "Incoming Shared Events - Select to Accept",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE
                        );

                        if (response == JOptionPane.OK_OPTION) {
                            for (int i = 0; i < checkBoxes.length; i++) {
                                DataSnapshot shareSnap = eventSnapshots.get(i);

                                // Always remove from incomingShares
                                FirebaseDatabase.getInstance().getReference()
                                        .child("users").child(username).child("incomingShares").child(shareSnap.getKey())
                                        .removeValueAsync();

                                if (checkBoxes[i].isSelected()) {
                                    String title = shareSnap.child("title").getValue(String.class);
                                    String dateStr = shareSnap.child("date").getValue(String.class);
                                    String timeStr = shareSnap.child("time").getValue(String.class);
                                    String location = shareSnap.child("location").getValue(String.class);
                                    String description = shareSnap.child("description").getValue(String.class);
                                    String classTitle = shareSnap.child("classTitle").getValue(String.class);
                                    String fromUser = shareSnap.child("owner").getValue(String.class);

                                    LocalDate eventDate = LocalDate.parse(dateStr);
                                    LocalTime eventTime = (timeStr != null) ? LocalTime.parse(timeStr) : LocalTime.of(0, 0);

                                    CalendarEvent newEvt = new CalendarEvent(
                                            title,
                                            eventDate,
                                            eventTime,
                                            description,
                                            location,
                                            editor.classes.stream()
                                                    .filter(c -> c.getTitle().equals(classTitle))
                                                    .findFirst()
                                                    .orElse(null),
                                            fromUser,
                                            new HashMap<>()
                                    );

                                    String eventId = title + "_" + dateStr;

                                    // Save to /events
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("users").child(username).child("events").child(eventId)
                                            .setValueAsync(new HashMap<String, Object>() {{
                                                put("title", newEvt.getTitle());
                                                put("date", newEvt.getDate().toString());
                                                put("time", newEvt.getTime().toString());
                                                put("description", newEvt.getDescription());
                                                put("location", newEvt.getLocation());
                                                put("classTitle", newEvt.getCourse() != null ? newEvt.getCourse().getTitle() : null);
                                                put("owner", newEvt.getOwner());
                                                put("sharedWith", new HashMap<String, String>());
                                            }});

                                    // Save to /upcomingTasks
                                    FirebaseUserService.saveUpcomingTask(username, newEvt);

                                    // Add to local CalendarEditor
                                    String key = eventDate.toString();
                                    editor.events.computeIfAbsent(key, k -> new ArrayList<>()).add(newEvt);

                                    JOptionPane.showMessageDialog(editor, "Event '" + title + "' has been added to your calendar and upcoming tasks!");
                                }
                            }

                            editor.updateCalendar(editor.currentYear, editor.currentMonth);
                            editor.updateTaskList();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println("Incoming share check cancelled: " + error.getMessage());
                    }
                }
        );
    }
}
