package com.example.library.forms;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Calendar;

public class CalendarPanel {

    private final CalendarPanelListener listener;
    private final JPanel calendarPanel;
    private final JLabel monthLabel;

    public CalendarPanel(CalendarPanelListener listener) {
        this.listener = listener;
        this.calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        this.monthLabel = new JLabel("Month");
    }

    public JPanel createCalendarPanel() {
        JPanel calendarPage = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JPanel headerPanel = new JPanel(new GridBagLayout());

        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");

        // Dropdown Menu
        String[] menuOptions = {"Menu", "Create Class", "Manage Class", "Manage Events", "Logout", "Home", "Next 7 Days"};
        JComboBox<String> menuDropdown = new JComboBox<>(menuOptions);
        menuDropdown.setPreferredSize(new Dimension(120, 30));

        menuDropdown.addActionListener(e -> {
            String selected = (String) menuDropdown.getSelectedItem();
            if (selected == null) return;

            switch (selected) {
                case "Create Class":
                    listener.onCreateClass();
                    break;
                case "Manage Class":
                    listener.onManageClass();
                    break;
                case "Manage Events":
                    listener.onManageEvents();
                    break;
                case "Logout":
                    listener.onLogout();
                    break;
                case "Home":
                    listener.onHome();
                    break;
                case "Next 7 Days":
                    listener.onNext7DaysButtonClicked(); // Christian // Next 7 Days button
                    break;

                default:
                    break;
            }
            menuDropdown.setSelectedIndex(0); // Reset menu
        });

        // Panels for layout
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.add(menuDropdown);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        centerPanel.add(prevButton);
        centerPanel.add(monthLabel);
        centerPanel.add(nextButton);

        // Header Layout
        GridBagConstraints headerGbc = new GridBagConstraints();
        headerGbc.insets = new Insets(5, 5, 5, 5);

        headerGbc.gridx = 0;
        headerGbc.gridy = 0;
        headerGbc.anchor = GridBagConstraints.WEST;
        headerPanel.add(leftPanel, headerGbc);

        headerGbc.gridx = 1;
        headerGbc.anchor = GridBagConstraints.CENTER;
        headerPanel.add(centerPanel, headerGbc);

        // Calendar Panel setup
        calendarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        calendarPanel.setBackground(Color.WHITE);

        listener.updateCalendar(calendarPanel, monthLabel); // Ask listener to fill calendar grid

        // Navigation buttons
        prevButton.addActionListener(e -> listener.onChangeMonth(-1));
        nextButton.addActionListener(e -> listener.onChangeMonth(1));

        // Main layout
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
}
