package com.example.library.forms;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
//firebase
import com.example.library.forms.firebase.FirebaseSetup;
import com.example.library.forms.firebase.FirebaseUserService;
import com.google.firebase.database.FirebaseDatabase;


public class CalendarEditor extends JFrame implements HomePanelListener, CalendarPanelListener {
    public ClassManager classManager;
    private final JPanel mainPanel;
    private final CardLayout cardLayout;
    private JPanel calendarPanel;
    private JLabel monthLabel;
    public int currentYear;
    public int currentMonth;
    public final Map<String, List<CalendarEvent>> events = new HashMap<>();
    public DefaultListModel<String> eventListModel = new DefaultListModel<>();
    public final List<ClassCourse> classes = new ArrayList<>();
    public String currentUsername;
    private JButton modifyEventButton;
    public boolean isLoggedIn = false;
    public JButton goToCalendarButton = new JButton("View Calendar");
    public JButton loginButton = new JButton("Login");
    public JButton registerButton = new JButton("Register");
    public JButton deleteUserButton = new JButton("Delete User");


    public CalendarEditor() {
        //Apply UI Styles
        UIStyleManager.applyStyles();
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);


        HomePanel homePanelBuilder = new HomePanel(this, goToCalendarButton, loginButton, registerButton, deleteUserButton, eventListModel);
        JPanel homePanel = homePanelBuilder.createHomePanel();
        CalendarPanel calendarPanelBuilder = new CalendarPanel(this);
        JPanel calendarPanel = calendarPanelBuilder.createCalendarPanel();

        mainPanel.add(homePanel, "Home");
        mainPanel.add(calendarPanel, "Calendar");
        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "Home"); // Show home page initially
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);

    }

    //RYOSUKE changed modifyEvent func 4/27
    //AYDAN - APR 23
    private void modifyEvent(CalendarEvent event) {
        JTextField titleField = new JTextField(event.getTitle());
        JTextField timeField = new JTextField(event.getTime().toString());
        JTextField descriptionField = new JTextField(event.getDescription());
        JTextField locationField = new JTextField(event.getLocation());
        JComboBox<ClassCourse> classBox = new JComboBox<>(classes.toArray(new ClassCourse[0]));
        classBox.setSelectedItem(event.getCourse());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Time (HH:mm):"));
        panel.add(timeField);
        panel.add(new JLabel("Description:"));
        panel.add(descriptionField);
        panel.add(new JLabel("Location:"));
        panel.add(locationField);
        panel.add(new JLabel("Class:"));
        panel.add(classBox);

        int result = JOptionPane.showConfirmDialog(CalendarEditor.this, panel, "Edit Event", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                CalendarEvent updated = new CalendarEvent(
                        titleField.getText().trim(),
                        event.getDate(),
                        LocalTime.parse(timeField.getText().trim()),
                        descriptionField.getText().trim(),
                        locationField.getText().trim(),
                        (ClassCourse) classBox.getSelectedItem(),
                        event.getOwner(),         // Keep the same owner
                        event.getSharedWith()     // Keep the same sharedWith
                );

                // Remove old event locally
                events.get(event.getDate().toString()).remove(event);

                // Remove old event from Firebase for current user
                String oldEventId = event.getTitle() + "_" + event.getDate();
                FirebaseDatabase.getInstance().getReference()
                        .child("users").child(currentUsername).child("events").child(oldEventId)
                        .removeValueAsync();
                FirebaseDatabase.getInstance().getReference()
                        .child("users").child(currentUsername).child("upcomingTasks").child(oldEventId)
                        .removeValueAsync();

                // Save updated event for current user
                FirebaseUserService.saveEvent(currentUsername, updated);
                FirebaseUserService.saveUpcomingTask(currentUsername, updated);

                // Save updated event for owner if current user is not the owner
                if (!currentUsername.equals(event.getOwner())) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("users").child(event.getOwner()).child("events").child(oldEventId)
                            .removeValueAsync();
                    FirebaseDatabase.getInstance().getReference()
                            .child("users").child(event.getOwner()).child("upcomingTasks").child(oldEventId)
                            .removeValueAsync();
                    FirebaseUserService.saveEvent(event.getOwner(), updated);
                    FirebaseUserService.saveUpcomingTask(event.getOwner(), updated);
                }

                // Update all sharedWith users
                if (event.getSharedWith() != null) {
                    for (String sharedUser : event.getSharedWith().keySet()) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(sharedUser).child("events").child(oldEventId)
                                .removeValueAsync();
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(sharedUser).child("upcomingTasks").child(oldEventId)
                                .removeValueAsync();
                        FirebaseUserService.saveEvent(sharedUser, updated);
                        FirebaseUserService.saveUpcomingTask(sharedUser, updated);
                    }
                }

                // Add updated event locally
                events.computeIfAbsent(updated.getDate().toString(), k -> new ArrayList<>()).add(updated);

                JOptionPane.showMessageDialog(this, "Event updated and shared users updated.");
                updateCalendar(currentYear, currentMonth);
                updateTaskList();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage());
            }
        }
    }



    //AYDAN - APR 23
    private void deleteEvent(CalendarEvent event) {
        String dateKey = event.getDate().toString();
        List<CalendarEvent> dayEvents = events.getOrDefault(dateKey, new ArrayList<>());
        dayEvents.remove(event);

        if (dayEvents.isEmpty()) {
            events.remove(dateKey);
        }

        String eventId = event.getTitle() + "_" + event.getDate();
        FirebaseSetup.getDatabase()
                .getReference("users").child(currentUsername).child("events").child(eventId)
                .removeValueAsync();

        FirebaseSetup.getDatabase()
                .getReference("users").child(currentUsername).child("upcomingTasks").child(eventId)
                .removeValueAsync();

        JOptionPane.showMessageDialog(this, "Event deleted.");
        updateCalendar(currentYear, currentMonth);
        updateTaskList();
        modifyEventButton.setEnabled(!events.isEmpty()); // Optional if you kept this logic
    }


    //Ryosuke added share function 4/27
//AYDAN - APR 23
    private void openModifyEventWindow() {
        List<CalendarEvent> allEvents = new ArrayList<>();
        for (List<CalendarEvent> evts : events.values()) {
            allEvents.addAll(evts);
        }

        if (allEvents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No events available to modify.");
            return;
        }

        // Sort descending
        allEvents.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (CalendarEvent evt : allEvents) {
            listModel.addElement(evt.getDate() + " - " + evt.getTitle());
        }

        JList<String> eventJList = new JList<>(listModel);
        eventJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(eventJList);

        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton shareButton = new JButton("Share"); // share

        editButton.addActionListener(e -> {
            int index = eventJList.getSelectedIndex();
            if (index >= 0) {
                CalendarEvent selected = allEvents.get(index);
                modifyEvent(selected);
            }
        });

        deleteButton.addActionListener(e -> {
            int index = eventJList.getSelectedIndex();
            if (index >= 0) {
                CalendarEvent selected = allEvents.get(index);
                deleteEvent(selected);
                listModel.remove(index);
            }
        });

        // share function
        shareButton.addActionListener(e -> {
            int index = eventJList.getSelectedIndex();
            if (index >= 0) {
                CalendarEvent selected = allEvents.get(index);

                JTextField sharedUsersField = new JTextField();
                JComboBox<String> permissionBox = new JComboBox<>(new String[]{"edit", "view"});

                JPanel sharePanel = new JPanel(new GridLayout(0, 1));
                sharePanel.add(new JLabel("Share with (usernames, comma-separated):"));
                sharePanel.add(sharedUsersField);
                sharePanel.add(new JLabel("Permission:"));
                sharePanel.add(permissionBox);

                int shareResult = JOptionPane.showConfirmDialog(this, sharePanel, "Share Event", JOptionPane.OK_CANCEL_OPTION);
                if (shareResult == JOptionPane.OK_OPTION) {
                    String[] users = sharedUsersField.getText().split(",");
                    String permission = (String) permissionBox.getSelectedItem();

                    Map<String, String> sharedWith = selected.getSharedWith() != null ? new HashMap<>(selected.getSharedWith()) : new HashMap<>();

                    for (String user : users) {
                        user = user.trim();
                        if (!user.isEmpty()) {
                            sharedWith.put(user, permission);
                        }
                    }

                    // Update the event object locally
                    CalendarEvent updatedEvent = new CalendarEvent(
                            selected.getTitle(),
                            selected.getDate(),
                            selected.getTime(),
                            selected.getDescription(),
                            selected.getLocation(),
                            selected.getCourse(),
                            selected.getOwner(),
                            sharedWith
                    );

                    // Save updated event
                    FirebaseUserService.saveEvent(currentUsername, updatedEvent);

                    // Also send to shared users' incomingShares
                    if (sharedWith != null) {
                        for (String sharedUser : sharedWith.keySet()) {
                            FirebaseUserService.saveIncomingShare(sharedUser, updatedEvent);
                        }
                    }

                    JOptionPane.showMessageDialog(this, "Event shared successfully!");
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(shareButton); //share

        panel.add(buttonPanel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Modify Events", JOptionPane.PLAIN_MESSAGE);
    }


    public void updateCalendar(int year, int month) {
        calendarPanel.removeAll();
        calendarPanel.setLayout(new GridLayout(0, 7, 1, 1));

        String[] months = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
        monthLabel.setText(months[month] + " " + year);

        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 14));
            calendarPanel.add(dayLabel);
        }

        Calendar calendar = new GregorianCalendar(year, month, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Fill empty spaces before the first day
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }


        //AYDAN: Altered this - APR 9
        for (int day = 1; day <= daysInMonth; day++) {
            JPanel dayCell = new JPanel(new BorderLayout());
            dayCell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            dayCell.setBackground(Color.WHITE);

            LocalDate date = LocalDate.of(year, month + 1, day);
            boolean isToday = date.equals(LocalDate.now());
            boolean hasEvents = events.containsKey(date.toString());

            // ---- Day Button ----
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("Arial", Font.PLAIN, 14));
            dayButton.setFocusPainted(false);
            dayButton.setContentAreaFilled(false);
            dayButton.setBorderPainted(false);

            if (hasEvents) {
                dayButton.setForeground(Color.BLUE); //Blue text
                dayCell.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2)); //Add blue border
            }


            if (isToday) {
                dayCell.setBackground(new Color(220, 220, 220)); // Light gray background for today
            }

            int finalDay = day;
            dayButton.addActionListener(e -> showEventsForDay(finalDay, month, year));

            // ---- Add Button (+) ----
            JButton addButton = new JButton("+");
            addButton.setMargin(new Insets(0, 0, 0, 0));
            addButton.setFont(new Font("Arial", Font.PLAIN, 10));
            addButton.setPreferredSize(new Dimension(20, 20));
            addButton.setFocusPainted(false);
            addButton.addActionListener(e -> showEventDialog(finalDay, month, year));

            JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            topRightPanel.setOpaque(false);
            topRightPanel.add(addButton);

            if (hasEvents) {
                JLabel iconLabel = new JLabel("📅", SwingConstants.CENTER);
                iconLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                dayCell.add(iconLabel, BorderLayout.SOUTH);
            }

            dayCell.add(topRightPanel, BorderLayout.NORTH);
            dayCell.add(dayButton, BorderLayout.CENTER);
            calendarPanel.add(dayCell);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private void createClass() {
        JTextField nameField = new JTextField(15);
        JTextField instructorTextField = new JTextField(15);
        JTextField locationTextField = new JTextField(15);
        JPanel panel = new JPanel(new GridLayout(0, 1));


        panel.add(new JLabel("Class: "));
        panel.add(nameField);
        panel.add(new JLabel("Instructor: "));
        panel.add(instructorTextField);
        panel.add(new JLabel("Room #: "));
        panel.add(locationTextField);


        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Create New Class",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String instructor = instructorTextField.getText().trim();
            String location = locationTextField.getText().trim();

            ClassCourse newClass = new ClassCourse(name, instructor, location); // You can let users pick color later
            classes.add(newClass);


            FirebaseUserService.saveClass(currentUsername, newClass);

            JOptionPane.showMessageDialog(this, "Class \"" + name + "\" created.");
        }
    }

    //AYDAN: added this func. - APR 9
    private void showEventsForDay(int day, int month, int year) {
        LocalDate date = LocalDate.of(year, month + 1, day);
        List<CalendarEvent> eventList = events.getOrDefault(date.toString(), new ArrayList<>());

        if (eventList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No events for " + date);
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (CalendarEvent event : eventList) {
            sb.append("Title: ").append(event.getTitle()).append("\n");
            sb.append("Time: ").append(event.getTime()).append("\n");
            sb.append("Location: ").append(event.getLocation()).append("\n");
            sb.append("Description: ").append(event.getDescription()).append("\n\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Events on " + date, JOptionPane.INFORMATION_MESSAGE);
    }


    public void updateTaskList() {
        // read from firebase

        if (currentUsername == null || currentUsername.isEmpty()) {
            System.out.println("currentUsername is null. Skipping task list update.");
            return;
        }

        FirebaseUserService.loadUpcomingTasks(currentUsername, eventListModel);
    }

    //RYOSUKE changed showEventDialog 4/27
    private void showEventDialog(int day, int month, int year) {
        LocalDate date = LocalDate.of(year, month + 1, day);

        // Create the input fields
        JTextField titleField = new JTextField(15);
        JTextField timeField = new JTextField("HH:mm", 5);
        JTextField descriptionField = new JTextField(20);
        JTextField locationField = new JTextField(15);
        JComboBox<ClassCourse> classBox = new JComboBox<>(classes.toArray(new ClassCourse[0]));

        // AYDAN - APR 23
        JCheckBox recurringBox = new JCheckBox("Repeat Weekly?");
        JSpinner repeatWeeksSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 52, 1)); // 1–52 weeks
        repeatWeeksSpinner.setEnabled(false); // Disabled by default

        recurringBox.addActionListener(e -> {
            repeatWeeksSpinner.setEnabled(recurringBox.isSelected());
        });

        // Share fields
        JTextField sharedUsersField = new JTextField(30); // usernames separated by commas
        JComboBox<String> permissionBox = new JComboBox<>(new String[]{"edit", "view"}); // permission selection

        // Layout panel
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Time (HH:mm):"));
        panel.add(timeField);
        panel.add(new JLabel("Description:"));
        panel.add(descriptionField);
        panel.add(new JLabel("Location:"));
        panel.add(locationField);
        panel.add(new JLabel("Class:"));
        panel.add(classBox);

        // AYDAN - APR 23
        panel.add(recurringBox);
        panel.add(new JLabel("Repeat for (weeks):"));
        panel.add(repeatWeeksSpinner);

        // Add share fields
        panel.add(new JLabel("Share with (usernames, comma-separated):"));
        panel.add(sharedUsersField);
        panel.add(new JLabel("Permission for shared users:"));
        panel.add(permissionBox);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Add Event for " + date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                String title = titleField.getText().trim();
                LocalTime time = LocalTime.parse(timeField.getText().trim());
                String description = descriptionField.getText().trim();
                String location = locationField.getText().trim();
                ClassCourse selectedClass = (ClassCourse) classBox.getSelectedItem();

                // Build sharedWith map
                Map<String, String> sharedWith = new HashMap<>();
                String[] users = sharedUsersField.getText().trim().split(",");
                String permission = (String) permissionBox.getSelectedItem();

                for (String user : users) {
                    user = user.trim();
                    if (!user.isEmpty()) {
                        sharedWith.put(user, permission);
                    }
                }

                // AYDAN - APR 23
                List<CalendarEvent> newEvents = new ArrayList<>();
                int repeatWeeks = (int) repeatWeeksSpinner.getValue();

                for (int i = 0; i < (recurringBox.isSelected() ? repeatWeeks : 1); i++) {
                    LocalDate eventDate = date.plusWeeks(i);
                    if (eventDate.isAfter(date.plusYears(1))) break; // Max 1-year cap

                    CalendarEvent evt = new CalendarEvent(
                            title,
                            eventDate,
                            time,
                            description,
                            location,
                            selectedClass,
                            currentUsername,    // Set owner to current user
                            sharedWith          // Set shared users
                    );

                    newEvents.add(evt);
                }

                // AYDAN - APR 24
                for (CalendarEvent evt : newEvents) {
                    FirebaseUserService.saveEvent(currentUsername, evt);
                    FirebaseUserService.saveUpcomingTask(currentUsername, evt);

                    String key = evt.getDate().toString();
                    events.computeIfAbsent(key, k -> new ArrayList<>()).add(evt);
                }
                JOptionPane.showMessageDialog(null, "Recurring event(s) added successfully!");
                updateCalendar(currentYear, currentMonth);
                updateTaskList();

            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(null, "Invalid time format. Please use HH:mm.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void changeMonth(int delta) {
        currentMonth += delta;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        } else if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        updateCalendar(currentYear, currentMonth);
    }


    public static void main(String[] args) {
        FirebaseSetup.initialize();  //firebase
        //FirebaseUserService.saveUser("CS370", "7");
        SwingUtilities.invokeLater(() -> {
            CalendarEditor frame = new CalendarEditor();
            frame.setVisible(true);
            frame.setSize(1000, 900);
            frame.setLocationRelativeTo(null);
        });
    }

//RYOSUKE changed onLoginButtonClicked 4/27
    @Override
    public void onLoginButtonClicked() {
        if (isLoggedIn) {
            JOptionPane.showMessageDialog(null, "You are already logged in.");
            return;
        }

        CalendarLogin.showLoginDialog(username -> {
            isLoggedIn = true;
            currentUsername = username;

            goToCalendarButton.setEnabled(true);
            loginButton.setEnabled(false);

            classes.clear();
            classManager = new ClassManager(this, classes, currentUsername);
            classes.addAll(FirebaseUserService.loadClasses(username));

            List<CalendarEvent> loadedEvents = FirebaseUserService.loadEvents(username, classes);
            events.clear();
            for (CalendarEvent event : loadedEvents) {
                String key = event.getDate().toString();
                events.computeIfAbsent(key, k -> new ArrayList<>()).add(event);
            }

            FirebaseUserService.loadUpcomingTasks(currentUsername, eventListModel);


            FirebaseUserService.checkIncomingShares(currentUsername, this);


            updateCalendar(currentYear, currentMonth);
            updateTaskList();

            cardLayout.show(mainPanel, "Calendar");
        });
    }


    @Override
    public void onRegisterButtonClicked() {
        CalendarLogin.showRegisterDialog();
    }

    @Override
    public void onDeleteUserButtonClicked() {
        CalendarLogin.showDeleteUserDialog();
    }

    @Override
    public void onCalendarButtonClicked() {
        if (isLoggedIn) {
            cardLayout.show(mainPanel, "Calendar");
        } else {
            JOptionPane.showMessageDialog(null, "Please log in first.");
        }
    }

    @Override
    public void onCreateClass() {
        createClass();
    }

    @Override
    public void onManageClass() {
        classManager.openModifyClassWindow();
    }

    @Override
    public void onManageEvents() {
        openModifyEventWindow();
    }

    @Override
    public void onLogout() {
        isLoggedIn = false;
        currentUsername = null;
        goToCalendarButton.setEnabled(false);
        loginButton.setEnabled(true);
        eventListModel.clear();
        cardLayout.show(mainPanel, "Home");
    }

    @Override
    public void onHome() {
        cardLayout.show(mainPanel, "Home");
    }

    @Override
    public void onChangeMonth(int delta) {
        changeMonth(delta);
    }

    @Override
    public void updateCalendar(JPanel calendarPanel, JLabel monthLabel) {
        this.calendarPanel = calendarPanel;
        this.monthLabel = monthLabel;
        updateCalendar(currentYear, currentMonth);
    }

    @Override// Christian // Next Days button
    public void onNext7DaysButtonClicked() {
        showNext7Days();
    }
// Christian added showNextDays function
    private void showNext7Days() {
        calendarPanel.removeAll();
        calendarPanel.setLayout(new GridLayout(0, 7, 1, 1));

        // Add day labels
        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(UIManager.getFont("Label.font"));
            dayLabel.setForeground(UIManager.getColor("Label.foreground"));
            calendarPanel.add(dayLabel);
        }

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            JPanel dayCell = new JPanel(new BorderLayout());
            dayCell.setBorder(BorderFactory.createLineBorder(UIManager.getColor("TextField.border")));
            dayCell.setBackground(UIManager.getColor("Panel.background"));

            JButton dayButton = new JButton(String.valueOf(date.getDayOfMonth()));
            dayButton.setFont(UIManager.getFont("Button.font"));
            dayButton.setForeground(UIManager.getColor("Button.foreground"));
            dayButton.setBackground(UIManager.getColor("Button.background"));
            dayButton.setFocusPainted(false);
            dayButton.setContentAreaFilled(false);
            dayButton.setBorderPainted(false);
            dayButton.addActionListener(e -> showEventsForDay(date.getDayOfMonth(), date.getMonthValue() - 1, date.getYear()));

            JButton addButton = new JButton("+");
            addButton.setFont(UIManager.getFont("Button.font"));
            addButton.setForeground(UIManager.getColor("Button.foreground"));
            addButton.setBackground(UIManager.getColor("Button.background"));
            addButton.setFocusPainted(false);
            addButton.setContentAreaFilled(true); // Ensure the button is filled
            addButton.setBorderPainted(true);
            addButton.addActionListener(e -> showEventDialog(date.getDayOfMonth(), date.getMonthValue() - 1, date.getYear()));

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            topPanel.setOpaque(false);
            topPanel.add(addButton);

            dayCell.add(topPanel, BorderLayout.NORTH);
            dayCell.add(dayButton, BorderLayout.CENTER);
            calendarPanel.add(dayCell);
        }

        // Update month label
        monthLabel.setText("Next 7 Days");
        monthLabel.setFont(UIManager.getFont("Label.font"));
        monthLabel.setForeground(UIManager.getColor("Label.foreground"));

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }


}

