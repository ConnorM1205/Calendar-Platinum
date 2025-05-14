package com.example.library.forms;

import com.example.library.forms.firebase.FirebaseSetup;
import com.example.library.forms.firebase.FirebaseUserService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ClassManager {
    private final CalendarEditor editor;
    private final List<ClassCourse> classes;
    private final String currentUsername;

    public ClassManager(CalendarEditor editor, List<ClassCourse> classes, String currentUsername) {
        this.editor = editor;
        this.classes = classes;
        this.currentUsername = currentUsername;
    }

    public void openModifyClassWindow() {
        if (classes.isEmpty()) {
            JOptionPane.showMessageDialog(editor, "No classes available to modify.");
            return;
        }

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (ClassCourse course : classes) {
            listModel.addElement(course.getTitle() + " - " + course.getInstructor());
        }

        JList<String> classJList = new JList<>(listModel);
        classJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(classJList);

        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");

        editButton.addActionListener(e -> {
            int index = classJList.getSelectedIndex();
            if (index >= 0) {
                ClassCourse selected = classes.get(index);
                modifyClass(selected);
                listModel.set(index, selected.getTitle() + " - " + selected.getInstructor()); // Refresh list display
            }
        });

        deleteButton.addActionListener(e -> {
            int index = classJList.getSelectedIndex();
            if (index >= 0) {
                ClassCourse selected = classes.get(index);
                deleteClass(selected);
                listModel.remove(index);
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(editor, panel, "Modify Classes", JOptionPane.PLAIN_MESSAGE);
    }

    private void deleteClass(ClassCourse course) {
        classes.remove(course);

        // Remove from Firebase
        String classId = course.getTitle(); // Assuming class names are unique
        FirebaseSetup.getDatabase()
                .getReference("users").child(currentUsername).child("classes").child(classId)
                .removeValueAsync();

        JOptionPane.showMessageDialog(editor, "Class deleted.");
    }

    private void modifyClass(ClassCourse course) {
        JTextField nameField = new JTextField(course.getTitle());
        JTextField instructorField = new JTextField(course.getInstructor());
        JTextField locationField = new JTextField(course.getLocation());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Class Name:")); panel.add(nameField);
        panel.add(new JLabel("Instructor:")); panel.add(instructorField);
        panel.add(new JLabel("Room #:")); panel.add(locationField);

        int result = JOptionPane.showConfirmDialog(editor, panel, "Edit Class", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // Remove old class from Firebase
                String oldClassId = course.getTitle();
                FirebaseSetup.getDatabase()
                        .getReference("users").child(currentUsername).child("classes").child(oldClassId)
                        .removeValueAsync();

                // Update local object
                course.setTitle(nameField.getText().trim());
                course.setInstructor(instructorField.getText().trim());
                course.setLocation(locationField.getText().trim());

                // Save updated class to Firebase
                FirebaseUserService.saveClass(currentUsername, course);

                JOptionPane.showMessageDialog(editor, "Class updated.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(editor, "Update failed: " + e.getMessage());
            }
        }
    }
}
