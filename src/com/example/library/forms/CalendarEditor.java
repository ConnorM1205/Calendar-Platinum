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

public class CalendarEditor extends JFrame {
    private final JPanel mainPanel;
    private final CardLayout cardLayout;
    private JPanel calendarPanel;
    private JLabel monthLabel;
    private int currentYear, currentMonth;
    private final Map<String, List<CalendarEvent>> events = new HashMap<>();
    private DefaultListModel<String> eventListModel;  // Add this as a class field
    private final List<ClassCourse> classes = new ArrayList<>();

    //user database: (SHIBUYA
    private final Map<String, String> userDatabase = new HashMap<>();
    //(SHIBUYA
    private boolean isLoggedIn = false;
    //(SHIBUYA
    private JButton goToCalendarButton;


    public CalendarEditor() {
        userDatabase.put("CS370", "7");   //default user
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        //login panel (SHIBUYA
        CalendarLogin loginPanel = new CalendarLogin(userDatabase, new CalendarLogin.LoginListener() {

            public void onLoginSuccess() {
                isLoggedIn = true;
                goToCalendarButton.setEnabled(true);
                cardLayout.show(mainPanel, "Calendar");
            }

        });

        //login panel on main(SHIBUYA
        mainPanel.add(loginPanel, "Login");


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
                cardLayout.show(mainPanel, "Login");
            }
        });

        //login(SHIBUYA
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> cardLayout.show(mainPanel, "Login"));
//register(SHIBUYA
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> cardLayout.show(mainPanel, "Login"));


        // --- Task List ---
        eventListModel = new DefaultListModel<>();  // Initialize the class field
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

        //login, register Panel and button (SHIBUYA)
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(loginButton);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(registerButton);


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



        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("Arial", Font.PLAIN, 14));
            dayButton.setBackground(Color.WHITE);

            // Update button text with event details if available
            String key = LocalDate.of(year, month + 1, day).toString();
            dayButton.setText(getButtonText(day, key));

            // Handle day button click
            int finalDay = day;
            dayButton.addActionListener(e -> showEventDialog(finalDay, month, year));
            calendarPanel.add(dayButton);
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
            JOptionPane.showMessageDialog(this, "Class \"" + name + "\" created.");
        }
    }

    private void updateTaskList() {
        eventListModel.clear();


        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1); // Monday of current week
        LocalDate endOfWeek = today.plusDays(6); // Sunday of current week

        // Add all events to the list
        for (Map.Entry<String, List<CalendarEvent>> entry : events.entrySet()) {
            List<CalendarEvent> eventList = entry.getValue();
            for (CalendarEvent event : eventList) {
                LocalDate eventDate = event.getDate();

                // Check if the event is within the current week (inclusive of start and end dates)
                if ((eventDate.isEqual(today) || eventDate.isAfter(today)) &&
                        (eventDate.isEqual(endOfWeek) || eventDate.isBefore(endOfWeek))) {

                    // Format the display to include the date
                    String formattedDate = eventDate.format(DateTimeFormatter.ofPattern("MM/dd"));
                    eventListModel.addElement(formattedDate + ": " + event.getTitle() + " - " + event.getTime());
                }
            }
        }
    }



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
                events.computeIfAbsent(date.toString(), k -> new ArrayList<>()).add(newEvent);
                JOptionPane.showMessageDialog(null, "Event added successfully!");
                updateCalendar(currentYear, currentMonth);
                updateTaskList();
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(null, "Invalid time format. Please use HH:mm.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }




    private String getButtonText(int selectedDay, String key) {
        // Fetch the list of events for the given key (date)
        List<CalendarEvent> eventList = events.getOrDefault(key, new ArrayList<>());

        // If there are no events, return the day number only
        if (eventList.isEmpty()) {
            return "<html><center>" + selectedDay + "</center></html>";
        }

        // Otherwise, build the string with event details
        StringBuilder eventDetails = new StringBuilder("<html><center>" + selectedDay + "<br>");

        // Loop through all the events for the given day and add their titles to the string
        for (CalendarEvent event : eventList) {
            // Assuming there's a 'title' property or field in the CalendarEvent class
            eventDetails.append("<font size='2'><font color = \"red\">").append(event.getTitle()).append("</font><br>");
        }

        eventDetails.append("</center></html>");
        return eventDetails.toString();
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
        SwingUtilities.invokeLater(() -> {
            CalendarEditor frame = new CalendarEditor();
            frame.setVisible(true);
            frame.setSize(1000, 900);
            frame.setLocationRelativeTo(null);
        });
    }
}