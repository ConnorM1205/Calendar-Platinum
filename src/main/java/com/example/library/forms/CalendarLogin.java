package com.example.library.forms;

import javax.swing.*;
import java.awt.*;
//firebase
import com.example.library.forms.firebase.FirebaseUserService;

public class CalendarLogin {

    public interface LoginListener {
        void onLoginSuccess(String username);
    }
    //login
    public static void showLoginDialog(LoginListener listener) {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        Object[] fields = {
                "Username:", userField,
                "Password:", passField
        };

        int result = JOptionPane.showConfirmDialog(null, fields, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (FirebaseUserService.loginUser(username, password)) {
                JOptionPane.showMessageDialog(null, "Login successful!");
                listener.onLoginSuccess(username);
            } else {
                JOptionPane.showMessageDialog(null, "Invalid credentials.");
            }
        }
    }
  //  RYOSUKE changed showRegisterDialog 4/27
    //register
    public static void showRegisterDialog() {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        Object[] fields = {
                "Username:", userField,
                "Password:", passField
        };

        int result = JOptionPane.showConfirmDialog(null, fields, "Register", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter both username and password.");
                return;
            }


            if (FirebaseUserService.userExists(username)) {
                JOptionPane.showMessageDialog(null, "User already exists. Please choose a different username.");
                return;
            }


            FirebaseUserService.saveUser(username, password);
            JOptionPane.showMessageDialog(null, "User registered successfully.");
        }
    }

    //delete
    public static void showDeleteUserDialog() {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        Object[] fields = {
                "Username:", userField,
                "Password:", passField
        };

        int result = JOptionPane.showConfirmDialog(null, fields, "Delete User", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (FirebaseUserService.loginUser(username, password)) {
                FirebaseUserService.deleteUser(username);
                JOptionPane.showMessageDialog(null, "User deleted successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "Invalid credentials. Cannot delete user.");
            }
        }
    }
}
