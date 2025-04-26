package com.example.library.forms;
import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class DialogManager {

    private DialogManager() {
        // private constructor: don't allow creating instances
    }

    public static void showCreateClassDialog(ClassManager classManager) {
        JTextField nameField = new JTextField(15);
        JTextField instructorField = new JTextField(15);
        JTextField locationField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Class Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Instructor:"));
        panel.add(instructorField);
        panel.add(new JLabel("Room #:"));
        panel.add(locationField);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Create New Class",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String instructor = instructorField.getText().trim();
            String location = locationField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Class name cannot be empty.");
                return;
            }

            ClassCourse newClass = new ClassCourse(name, instructor, location);

            // 🔥 Pass the new class object to ClassManager to save
            classManager.saveNewClass(newClass);

            JOptionPane.showMessageDialog(null, "Class \"" + name + "\" created successfully!");
        }
    }
    public static List<CalendarEvent> showCreateEventDialog(CalendarEditor editor, LocalDate date) {
        List<CalendarEvent> newEvents = new ArrayList<>();

        JTextField titleField = new JTextField(15);
        JTextField timeField = new JTextField("HH:mm", 5);
        JTextField descriptionField = new JTextField(20);
        JTextField locationField = new JTextField(15);
        JComboBox<ClassCourse> classBox = new JComboBox<>(editor.classes.toArray(new ClassCourse[0]));

        JCheckBox recurringBox = new JCheckBox("Repeat Weekly?");
        JSpinner repeatWeeksSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 52, 1));
        repeatWeeksSpinner.setEnabled(false);

        recurringBox.addActionListener(e -> {
            repeatWeeksSpinner.setEnabled(recurringBox.isSelected());
        });

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
        panel.add(recurringBox);
        panel.add(new JLabel("Repeat for (weeks):"));
        panel.add(repeatWeeksSpinner);

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
                int repeatWeeks = (int) repeatWeeksSpinner.getValue();

                for (int i = 0; i < (recurringBox.isSelected() ? repeatWeeks : 1); i++) {
                    LocalDate eventDate = date.plusWeeks(i);
                    if (eventDate.isAfter(date.plusYears(1))) break; // Max 1-year cap

                    CalendarEvent newEvent = new CalendarEvent(
                            title,
                            eventDate,
                            time,
                            description,
                            location,
                            selectedClass
                    );
                    newEvents.add(newEvent);                }
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(null, "Invalid time format. Please use HH:mm.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        return newEvents;
    }
    public static void showModifyEventWindow(CalendarEditor editor, List<CalendarEvent> allEvents) {
        if (allEvents.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No events available to modify.");
            return;
        }

        // Sort descending (newest first)
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

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog();
        dialog.setTitle("Modify Events");
        dialog.setModal(true);
        dialog.setContentPane(panel);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);

        editButton.addActionListener(e -> {
            int index = eventJList.getSelectedIndex();
            if (index >= 0) {
                CalendarEvent selected = allEvents.get(index);
                dialog.dispose();
                editor.modifyEvent(selected);
            }
        });

        deleteButton.addActionListener(e -> {
            int index = eventJList.getSelectedIndex();
            if (index >= 0) {
                CalendarEvent selected = allEvents.get(index);
                dialog.dispose();
                editor.deleteEvent(selected);
            }
        });

        dialog.setVisible(true);
    }
    public static CalendarEvent showModifyEventDialog(CalendarEditor editor, CalendarEvent originalEvent) {
        JTextField titleField = new JTextField(originalEvent.getTitle());
        JTextField timeField = new JTextField(originalEvent.getTime().toString());
        JTextField descriptionField = new JTextField(originalEvent.getDescription());
        JTextField locationField = new JTextField(originalEvent.getLocation());
        JComboBox<ClassCourse> classBox = new JComboBox<>(editor.classes.toArray(new ClassCourse[0]));
        classBox.setSelectedItem(originalEvent.getCourse());

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
                "Edit Event",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                return new CalendarEvent(
                        titleField.getText().trim(),
                        originalEvent.getDate(), // Keep the same date
                        LocalTime.parse(timeField.getText().trim()),
                        descriptionField.getText().trim(),
                        locationField.getText().trim(),
                        (ClassCourse) classBox.getSelectedItem()
                );
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Update failed: " + e.getMessage());
            }
        }

        return null; // User cancelled or invalid input
    }



}
