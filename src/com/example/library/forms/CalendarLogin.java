package com.example.library.forms;



import javax.swing.*;
import java.awt.*;

import java.util.Map;

public class CalendarLogin extends JPanel {
    private final JTextField userField;
    private final JPasswordField passField;
    private final JLabel messageLabel;
    private final Map<String, String> userDatabase;
    private boolean isLoggedIn = false;

    public interface LoginListener {
        void onLoginSuccess();
    }

    public CalendarLogin(Map<String, String> sharedDatabase, LoginListener listener) {
        this.userDatabase = sharedDatabase;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        JLabel passLabel = new JLabel("Password:");
        userField = new JTextField(15);
        passField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        messageLabel = new JLabel("", SwingConstants.CENTER);

        gbc.gridx = 0; gbc.gridy = 0;
        add(userLabel, gbc);
        gbc.gridx = 1;
        add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(passLabel, gbc);
        gbc.gridx = 1;
        add(passField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        add(loginButton, gbc);

        gbc.gridy = 3;
        add(registerButton, gbc);

        gbc.gridy = 4;
        add(messageLabel, gbc);

        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (userDatabase.containsKey(username) && userDatabase.get(username).equals(password)) {
                messageLabel.setText("Login successful!");
                isLoggedIn = true;
                listener.onLoginSuccess();
            } else {
                messageLabel.setText("Invalid credentials.");
            }
        });

        registerButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter username and password.");
            } else if (userDatabase.containsKey(username)) {
                messageLabel.setText("User already exists!");
            } else {
                userDatabase.put(username, password);
                messageLabel.setText("User registered successfully.");
                userField.setText("");
                passField.setText("");
            }
        });
    }



    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}