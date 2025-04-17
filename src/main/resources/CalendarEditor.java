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
//firebade
import com.example.library.forms.firebase.FirebaseSetup;
import com.example.library.forms.firebase.FirebaseUserService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


public class CalendarEditor extends JFrame {
    private final JPanel mainPanel;
    private final CardLayout cardLayout;
    private JPanel calendarPanel;
    private JLabel monthLabel;
    private int currentYear, currentMonth;
    private final Map<String, List<CalendarEvent>> events = new HashMap<>();
    private DefaultListModel<String> eventListModel = new DefaultListModel<>();
    // Add this as a class field
    private final List<ClassCourse> classes = new ArrayList<>();
    //firebase Shibuya
    private String currentUsername;

    //(SHIBUYA
    private boolean isLoggedIn = false;
    //(SHIBUYA
    private JButton goToCalendarButton;


    public CalendarEditor() {
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);


        JPanel homePanel = createHomePanel();

        JPanel calendarPage = createCalendarPanel();

        mainPanel.add(homePanel, "Home");
        mainPanel.add(calendarPage, "Calendar");

        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "Home"); // Show home page initially
    }



    private JPanel createHomePanel() {
        JPanel homePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // --- Logo Image ---
        ImageIcon icon = new ImageIcon("CSU_San_Marcos_seal.svg.png");
        Image scaledImage = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel imageLabel = new JLabel(scaledIcon);

        // --- Title Label ---
        JLabel homeLabel = new JLabel("Canvas Platinum", SwingConstants.CENTER);
        homeLabel.setFont(new Font("Montserrat", Font.BOLD, 20));


        // --- Calendar Button ---
        //can't view calendar before login (SHIBUYA)
        goToCalendarButton = new JButton("View Calendar");
        goToCalendarButton.setEnabled(false);
        goToCalendarButton.addActionListener(e -> {
            if (isLoggedIn) {
                cardLayout.show(mainPanel, "Calendar");
            } else {
                JOptionPane.showMessageDialog(null, "Please log in first.");
            }
        });

       //login buttun
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(goToCalendarButton.getPreferredSize());
        loginButton.addActionListener(e -> CalendarLogin.showLoginDialog(username -> {
            isLoggedIn = true;
            currentUsername = username;
            goToCalendarButton.setEnabled(true);

            classes.clear();
            classes.addAll(FirebaseUserService.loadClasses(username));
            List<CalendarEvent> loadedEvents = FirebaseUserService.loadEvents(username, classes);
            events.clear();
            for (CalendarEvent event : loadedEvents) {
                String key = event.getDate().toString();
                events.computeIfAbsent(key, k -> new ArrayList<>()).add(event);
            }

            FirebaseUserService.loadUpcomingTasks(currentUsername, eventListModel);

            updateCalendar(currentYear, currentMonth);
            updateTaskList();
            cardLayout.show(mainPanel, "Calendar");
        }));
//register and delete button
        JButton registerButton = new JButton("Register");
        registerButton.setPreferredSize(goToCalendarButton.getPreferredSize());
        registerButton.addActionListener(e -> CalendarLogin.showRegisterDialog());

        JButton deleteUserButton = new JButton("Delete User");
        deleteUserButton.setPreferredSize(goToCalendarButton.getPreferredSize());
        deleteUserButton.addActionListener(e -> CalendarLogin.showDeleteUserDialog());



        // --- Task List ---
      //  eventListModel = new DefaultListModel<>();  // Initialize the class field
        updateTaskList();



// Create a JList to display event titles
        JList<String> taskList = new JList<>(eventListModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setLayoutOrientation(JList.VERTICAL);
        taskList.setVisibleRowCount(-1); // Display all rows
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setPreferredSize(new Dimension(200, 100));
        JLabel taskLabel = new JLabel("Upcoming Tasks");

        // --- Center column (logo, label, button) ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(imageLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(homeLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(goToCalendarButton);

        //login, register and delete Panel and button (SHIBUYA)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteUserButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(loginButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(registerButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(deleteUserButton);

        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(buttonPanel);


        c.insets = new Insets(20, 20, 20, 20);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        homePanel.add(centerPanel, c);


        // --- Right-aligned task list ---
        JPanel taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        taskPanel.add(taskLabel);
        taskPanel.add(Box.createVerticalStrut(5));
        taskPanel.add(scrollPane);

        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        homePanel.add(taskPanel, c);

        return homePanel;
    }



    private JPanel createCalendarPanel() {
        JPanel calendarPage = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);

        JPanel headerPanel = new JPanel(new GridBagLayout());

        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        JButton classButton = new JButton("Create Class");
        JButton logoutButton = new JButton("Logout");
        JButton homeButton = new JButton("Home");

        monthLabel = new JLabel("Month");

        // Add components to header panel
        GridBagConstraints headerGbc = new GridBagConstraints();
        headerGbc.insets = new Insets(5, 5, 5, 5);
        headerGbc.gridx = 0;
        headerGbc.gridy = 0;
        headerGbc.anchor = GridBagConstraints.WEST;
        headerGbc.weightx = 1.0;
        headerPanel.add(classButton, headerGbc);

        headerGbc.gridx = 1;
        headerGbc.anchor = GridBagConstraints.CENTER;
        headerGbc.weightx = 0;
        headerPanel.add(prevButton, headerGbc);

        headerGbc.gridx = 2;
        headerGbc.anchor = GridBagConstraints.CENTER;
        headerGbc.weightx = 0;
        headerPanel.add(monthLabel, headerGbc);

        headerGbc.gridx = 3;
        headerGbc.anchor = GridBagConstraints.CENTER;
        headerGbc.weightx = 0;
        headerPanel.add(nextButton, headerGbc);

        headerGbc.gridx = 4;
        headerGbc.anchor = GridBagConstraints.EAST;
        headerGbc.weightx = 1.0;
        headerPanel.add(logoutButton, headerGbc);

        headerGbc.gridx = 5;
        headerGbc.anchor = GridBagConstraints.EAST;
        headerPanel.add(homeButton, headerGbc);

        // Calendar Panel
        calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        calendarPanel.setBackground(Color.WHITE);

        updateCalendar(currentYear, currentMonth);

        // Button Listeners
        prevButton.addActionListener(e -> changeMonth(-1));
        nextButton.addActionListener(e -> changeMonth(1));
        homeButton.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
        classButton.addActionListener(e -> createClass());
        //logout (SHIBUYA
        logoutButton.addActionListener(e -> {
            isLoggedIn = false;
            goToCalendarButton.setEnabled(false);
            cardLayout.show(mainPanel, "Home");
        });
        // Layout for Calendar Page
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        calendarPage.add(headerPanel, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        calendarPage.add(calendarPanel, gbc);

        return calendarPage;
    }

    private void updateCalendar(int year, int month) {
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



        // for (int day = 1; day <= daysInMonth; day++) {
        //     JButton dayButton = new JButton(String.valueOf(day));
        //     dayButton.setFont(new Font("Arial", Font.PLAIN, 14));
        //     dayButton.setBackground(Color.WHITE);

        //     // Update button text with event details if available
        //     String key = LocalDate.of(year, month + 1, day).toString();
        //     dayButton.setText(getButtonText(day, key));

        //     // Handle day button click
        //     int finalDay = day;
        //     dayButton.addActionListener(e -> showEventDialog(finalDay, month, year));
        //     calendarPanel.add(dayButton);
        // }
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

            // ---- Optional Calendar Icon for Event Days ----
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
        Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.cyan, Color.magenta, Color.PINK};
        String[] ColorNames = {"Red", "Green", "Blue", "Yellow", "Orange", "Cyan", "Magenta", "Pink"};
        JTextField nameField = new JTextField(15);
        JTextField instructorTextField = new JTextField(15);
        JTextField locationTextField = new JTextField(15);
        JComboBox<Color> colorBox = new JComboBox<>(colors);        JPanel panel = new JPanel(new GridLayout(0,1));



        panel.add(new JLabel("Class: "));
        panel.add(nameField);
        panel.add(new JLabel("Instructor: "));
        panel.add(instructorTextField);
        panel.add(new JLabel("Room #: "));
        panel.add(locationTextField);
        panel.add(new JLabel("Color: "));
        panel.add(colorBox, BorderLayout.CENTER);


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

            ClassCourse newClass = new ClassCourse(name, Color.CYAN, instructor, location); // You can let users pick color later
            classes.add(newClass);

//firebase Shibuya save in fire base
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

    // private void updateTaskList() {
    //     eventListModel.clear();


    //     LocalDate today = LocalDate.now();
    //     LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1); // Monday of current week
    //     LocalDate endOfWeek = today.plusDays(6); // Sunday of current week

    //     // Add all events to the list
    //     for (Map.Entry<String, List<CalendarEvent>> entry : events.entrySet()) {
    //         List<CalendarEvent> eventList = entry.getValue();
    //         for (CalendarEvent event : eventList) {
    //             LocalDate eventDate = event.getDate();

    //             // Check if the event is within the current week (inclusive of start and end dates)
    //             if ((eventDate.isEqual(today) || eventDate.isAfter(today)) &&
    //                     (eventDate.isEqual(endOfWeek) || eventDate.isBefore(endOfWeek))) {

    //                 // Format the display to include the date
    //                 String formattedDate = eventDate.format(DateTimeFormatter.ofPattern("MM/dd"));
    //                 eventListModel.addElement(formattedDate + ": " + event.getTitle() + " - " + event.getTime());
    //             }
    //         }
    //     }
    // }

    private void updateTaskList() {
        // read from firbase
        FirebaseUserService.loadUpcomingTasks(currentUsername, eventListModel);
    }

    /*
    private void updateTaskList() {

            if (currentUsername == null || currentUsername.isEmpty()) return;


        //AYDAN: changed how home page task viewer functions, only shows upcoming tasks for future / current ACTUAL day, other tasks before are considered passed - APR 9

        // LocalDate today = LocalDate.now();
        // LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1); // Monday of current week
        // LocalDate endOfWeek = startOfWeek.plusDays(6); // Sunday of current week

        // // Add all events to the list
        // for (Map.Entry<String, List<CalendarEvent>> entry : events.entrySet()) {
        //  List<CalendarEvent> eventList = entry.getValue();
        //     for (CalendarEvent event : eventList) {
        //         LocalDate eventDate = event.getDate();

        //         // Check if the event is within the current week (inclusive of start and end dates)
        //         if ((eventDate.isEqual(startOfWeek) || eventDate.isAfter(startOfWeek)) &&
        //             (eventDate.isEqual(endOfWeek) || eventDate.isBefore(endOfWeek))) {


        eventListModel.clear();
        LocalDate today = LocalDate.now();

        //Future events into a flat list
        List<CalendarEvent> upcomingEvents = new ArrayList<>();
        for (List<CalendarEvent> eventList : events.values()) {
            for (CalendarEvent event : eventList) {
                if (!event.getDate().isBefore(today)) {
                    upcomingEvents.add(event);
                }
            }
        }
        //Sort the events by date
        upcomingEvents.sort(Comparator.comparing(CalendarEvent::getDate).thenComparing(CalendarEvent::getTime));

        //Add to the list model
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        for (CalendarEvent event : upcomingEvents) {
            String formattedDate = event.getDate().format(formatter);
            eventListModel.addElement(formattedDate + " - " + event.getTitle());
        }
        // // Format the display to include the date
        // String formattedDate = eventDate.format(DateTimeFormatter.ofPattern("MM/dd"));
        // eventListModel.addElement(formattedDate + " - " + event.getTitle());
    }
//firebase Shibuya
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
*/


    private void showEventDialog(int day, int month, int year) {
        LocalDate date = LocalDate.of(year, month + 1, day);

        // Create the input fields
        JTextField titleField = new JTextField(15);
        JTextField timeField = new JTextField("HH:mm", 5);
        JTextField descriptionField = new JTextField(20);
        JTextField locationField = new JTextField(15);
        JComboBox<ClassCourse> classBox = new JComboBox<>(classes.toArray(new ClassCourse[0]));

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

                // Create new event and add it to the map
                ClassCourse selectedClass = (ClassCourse) classBox.getSelectedItem();
                CalendarEvent newEvent = new CalendarEvent(title, date, time, description, location, selectedClass);

                //firebase Shibuya　save event and upcoming
                FirebaseUserService.saveEvent(currentUsername, newEvent);
                FirebaseUserService.saveUpcomingTask(currentUsername, newEvent);


                events.computeIfAbsent(date.toString(), k -> new ArrayList<>()).add(newEvent);
                JOptionPane.showMessageDialog(null, "Event added successfully!");
                updateCalendar(currentYear, currentMonth);
                updateTaskList();
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(null, "Invalid time format. Please use HH:mm.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    //AYDAN commented out for now, not needed atm - APR 9
    // private String getButtonText(int selectedDay, String key) {
    //     // Fetch the list of events for the given key (date)
    //     List<CalendarEvent> eventList = events.getOrDefault(key, new ArrayList<>());

    //     // If there are no events, return the day number only
    //     if (eventList.isEmpty()) {
    //         return "<html><center>" + selectedDay + "</center></html>";
    //     }

    //     // Otherwise, build the string with event details
    //     StringBuilder eventDetails = new StringBuilder("<html><center>" + selectedDay + "<br>");

    //     // Loop through all the events for the given day and add their titles to the string
    //     for (CalendarEvent event : eventList) {
    //         // Assuming there's a 'title' property or field in the CalendarEvent class
    //         eventDetails.append("<font size='2'><font color = \"red\">").append(event.getTitle()).append("</font><br>");
    //     }

    //     eventDetails.append("</center></html>");
    //     return eventDetails.toString();
    // }




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
        FirebaseUserService.saveUser("CS370", "7");
        SwingUtilities.invokeLater(() -> {
            CalendarEditor frame = new CalendarEditor();
            frame.setVisible(true);
            frame.setSize(1000, 900);
            frame.setLocationRelativeTo(null);
        });
    }
}