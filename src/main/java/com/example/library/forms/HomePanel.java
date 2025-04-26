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

        // Logo
        ImageIcon icon = new ImageIcon("CSU_San_Marcos_seal.svg.png");
        Image scaledImage = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel imageLabel = new JLabel(scaledIcon);

        JLabel homeLabel = new JLabel("Canvas Platinum", SwingConstants.CENTER);
        homeLabel.setFont(new Font("Montserrat", Font.BOLD, 20));

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

        // --- Center Section ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(imageLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(homeLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(goToCalendarButton);

        // --- Login/Register/Delete Buttons ---
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

        // --- Right Section (Upcoming Tasks Placeholder) ---
        JPanel taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        JLabel taskLabel = new JLabel("Upcoming Tasks");
        JList<String> taskList = new JList<>(eventListModel);
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setPreferredSize(new Dimension(200, 100));

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
}
