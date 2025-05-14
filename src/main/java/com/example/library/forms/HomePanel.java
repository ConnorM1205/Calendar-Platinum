package com.example.library.forms;

import javax.swing.*;
import java.awt.*;

public class HomePanel {

    private final HomePanelListener listener;
    private JButton loginButton;
    private JButton registerButton;
    private JButton deleteUserButton;
    private JButton goToCalendarButton;
    private final DefaultListModel<String> eventListModel;

    public HomePanel(HomePanelListener listener, JButton goToCalendarButton, JButton loginButton, JButton registerButton, JButton deleteUserButton, DefaultListModel<String> eventListModel) {
        this.listener = listener;
        this.goToCalendarButton = goToCalendarButton;
        this.loginButton = loginButton;
        this.registerButton = registerButton;
        this.deleteUserButton = deleteUserButton;
        this.eventListModel = eventListModel;
    }

    public JPanel createHomePanel() {
        JPanel homePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        ImageIcon icon = new ImageIcon("ca.png"); // Update to your new image path
        Image scaledImage = icon.getImage().getScaledInstance(650, 200, Image.SCALE_SMOOTH); // Adjust width/height as needed
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel imageLabel = new JLabel(scaledIcon, SwingConstants.CENTER); // Ensure image is centered
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel homeLabel = new JLabel("Canvas Platinum", SwingConstants.CENTER);
        homeLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
        homeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Create Buttons ---
        goToCalendarButton = new JButton("View Calendar");
        goToCalendarButton.setEnabled(true);
        goToCalendarButton.addActionListener(e -> listener.onCalendarButtonClicked());

        loginButton = new JButton("Login");
        loginButton.setPreferredSize(goToCalendarButton.getPreferredSize());
        loginButton.addActionListener(e -> listener.onLoginButtonClicked());

        registerButton = new JButton("Register");
        registerButton.setPreferredSize(goToCalendarButton.getPreferredSize());
        registerButton.addActionListener(e -> listener.onRegisterButtonClicked());

        deleteUserButton = new JButton("Delete User");
        deleteUserButton.setPreferredSize(goToCalendarButton.getPreferredSize());
        deleteUserButton.addActionListener(e -> listener.onDeleteUserButtonClicked());

        // --- Horizontal Button Panel ---
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Add glue to keep buttons centered even with varying panel sizes
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(goToCalendarButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(loginButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(registerButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(deleteUserButton);
        buttonPanel.add(Box.createHorizontalGlue());

        // --- Upcoming Tasks Section ---
        JPanel taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel taskLabel = new JLabel("Upcoming Tasks", SwingConstants.CENTER);
        JList<String> taskList = new JList<>(eventListModel);
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setPreferredSize(new Dimension(200, 100));

        taskLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        taskPanel.add(taskLabel);
        taskPanel.add(Box.createVerticalStrut(5));
        taskPanel.add(scrollPane);

        // --- Center Section ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(imageLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(homeLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(buttonPanel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(taskPanel);

        // --- GridBagConstraints for centering ---
        c.insets = new Insets(20, 20, 20, 20);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE; // Prevent stretching to fill the cell
        homePanel.add(centerPanel, c);

        // Add horizontal glue to the homePanel to ensure centerPanel stays centered
        homePanel.setLayout(new GridBagLayout());
        c.weightx = 1.0;
        c.weighty = 1.0;
        homePanel.add(centerPanel, c);

        return homePanel;
    }
}