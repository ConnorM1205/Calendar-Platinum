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
    // Add this as a class field
    public final List<ClassCourse> classes = new ArrayList<>();
    //firebase Shibuya
    public String currentUsername;

    //AYDAN - APR 23
    private JButton modifyEventButton;

    //(SHIBUYA
    public boolean isLoggedIn = false;
    //(SHIBUYA
    public JButton goToCalendarButton = new JButton("View Calendar");
    public JButton loginButton = new JButton("Login");
    public JButton registerButton = new JButton("Register");
    public JButton deleteUserButton = new JButton("Delete User");


    public CalendarEditor() {

        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);


        HomePanel homePanelBuilder = new HomePanel(this, goToCalendarButton, loginButton, registerButton, deleteUserButton, eventListModel);        JPanel homePanel = homePanelBuilder.createHomePanel();
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




    //AYDAN - APR 23
    void modifyEvent(CalendarEvent event) {
        CalendarEvent updated = DialogManager.showModifyEventDialog(this, event);

        if (updated == null) {
            return; // User cancelled
        }

        // Remove old event from local
        events.get(event.getDate().toString()).remove(event);

        // Remove old event from Firebase
        String oldEventId = event.getTitle() + "_" + event.getDate();
        FirebaseSetup.getDatabase()
                .getReference("users").child(currentUsername).child("events").child(oldEventId)
                .removeValueAsync();
        FirebaseSetup.getDatabase()
                .getReference("users").child(currentUsername).child("upcomingTasks").child(oldEventId)
                .removeValueAsync();

        // Save updated event
        FirebaseUserService.saveEvent(currentUsername, updated);
        FirebaseUserService.saveUpcomingTask(currentUsername, updated);

        // Add updated event to local map
        events.computeIfAbsent(updated.getDate().toString(), k -> new ArrayList<>()).add(updated);

        JOptionPane.showMessageDialog(this, "Event updated.");
        updateCalendar(currentYear, currentMonth);
        updateTaskList();
    }

    //AYDAN - APR 23
    void deleteEvent(CalendarEvent event) {
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


    //AYDAN - APR 23
    private void openModifyEventWindow() {
        List<CalendarEvent> allEvents = new ArrayList<>();
        for (List<CalendarEvent> evts : events.values()) {
            allEvents.addAll(evts);
        }

        DialogManager.showModifyEventWindow(this, allEvents);
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


    private void showEventDialog(int day, int month, int year) {
        LocalDate date = LocalDate.of(year, month + 1, day);

        List<CalendarEvent> newEvents = DialogManager.showCreateEventDialog(this, date);

        if (newEvents.isEmpty()) {
            return; // User cancelled
        }

        for (CalendarEvent evt : newEvents) {
            FirebaseUserService.saveEvent(currentUsername, evt);
            FirebaseUserService.saveUpcomingTask(currentUsername, evt);

            String key = evt.getDate().toString();
            events.computeIfAbsent(key, k -> new ArrayList<>()).add(evt);
        }

        JOptionPane.showMessageDialog(this, "Recurring event(s) added successfully!");
        updateCalendar(currentYear, currentMonth);
        updateTaskList();
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
        DialogManager.showCreateClassDialog(classManager);
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
}




