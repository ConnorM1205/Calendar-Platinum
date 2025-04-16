package com.example.library.forms;

import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.google.firebase.auth.FirebaseAuth;


public class CalendarLogin extends JPanel {
    private final JTextField userField;
    private final JPasswordField passField;
    private final JLabel messageLabel;
    private boolean isLoggedIn = false;

    private static final String API_KEY = "AIzaSyA2yrLXhZrVUCvjyUmJjcGMTbPDpEiZj0E"; // 🔑 Replace this!

    public interface LoginListener {
        void onLoginSuccess();
    }

    public CalendarLogin(LoginListener listener) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Email:");
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
            String email = userField.getText();
            String password = new String(passField.getPassword());

            if (firebaseLogin(email, password)) {
                messageLabel.setText("Login successful!");
                isLoggedIn = true;
                listener.onLoginSuccess();
            } else {
                messageLabel.setText("Invalid email or password.");
            }
        });

        registerButton.addActionListener(e -> {
            String email = userField.getText();
            String password = new String(passField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter email and password.");
            } else if (firebaseRegister(email, password)) {
                messageLabel.setText("User registered successfully.");
                userField.setText("");
                passField.setText("");
            } else {
                messageLabel.setText("Registration failed.");
            }
        });
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    private boolean firebaseLogin(String email, String password) {
        try {
            URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("email", email);
            requestBody.addProperty("password", password);
            requestBody.addProperty("returnSecureToken", true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int responseCode = conn.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean firebaseRegister(String email, String password) {
        try {
            URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("email", email);
            requestBody.addProperty("password", password);
            requestBody.addProperty("returnSecureToken", true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int responseCode = conn.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
