package com.example.library.forms;

import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CalendarEditor extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel calendarPanel;
    private JLabel monthLabel;
    private int currentYear, currentMonth;
    private Map<String, String> events = new HashMap<>();

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
        JPanel homePanel = new JPanel(new BorderLayout());
        JLabel homeLabel = new JLabel("Canvas Platinum", SwingConstants.CENTER);
        JButton goToCalendarButton = new JButton("View Calendar");

        goToCalendarButton.addActionListener(e -> cardLayout.show(mainPanel, "Calendar"));
        homeLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
        homePanel.add(homeLabel, BorderLayout.NORTH);
        homePanel.add(goToCalendarButton, BorderLayout.SOUTH);
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
        JButton loginButton = new JButton("Login");
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
        headerPanel.add(loginButton, headerGbc);

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

        Calendar today = Calendar.getInstance();
        int todayYear = today.get(Calendar.YEAR);
        int todayMonth = today.get(Calendar.MONTH);
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("Arial", Font.PLAIN, 14));
            dayButton.setBackground(Color.WHITE);
            if (year == todayYear && month == todayMonth && day == todayDay) {
                dayButton.setBackground(Color.LIGHT_GRAY);
            }

            String key = year + "-" + month + "-" + day;
            int finalDay = day;
            dayButton.addActionListener(e -> {
                showEventDialog(finalDay, month, year);
                dayButton.setText(getButtonText(finalDay, key));
            });

            calendarPanel.add(dayButton);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private void showEventDialog(int day, int month, int year) {
        String key = year + "-" + month + "-" + day;
        String existingEvent = events.getOrDefault(key, "No Events");

        String newEvent = JOptionPane.showInputDialog(null,
                "Enter event for " + (month + 1) + "/" + day + "/" + year + ":\n(Current: " + existingEvent + ")",
                "Add/Edit Event",
                JOptionPane.PLAIN_MESSAGE
        );

        if (newEvent != null && !newEvent.trim().isEmpty()) {
            events.put(key, newEvent);
            JOptionPane.showMessageDialog(null,
                    "Event saved: " + newEvent,
                    "Event Added",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String getButtonText(int selectedDay, String key) {
        String event = events.getOrDefault(key, "");
        return "<html><center>" + selectedDay + "<br><font size='2'>" + event + "</font></center></html>";
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
