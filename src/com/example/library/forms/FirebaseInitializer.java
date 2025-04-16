package com.example.library.forms;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;

public class FirebaseInitializer {
    public static void initialize() {
        try {
            // Initialize Firebase Admin SDK (this requires a service account JSON file)
            FileInputStream serviceAccount = new FileInputStream("/Users/connormacdonald/Desktop/firebase/canvas-platinum-firebase-adminsdk-fbsvc-7005cf7416.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(com.google.auth.oauth2.GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Initialize Firebase app with the specified options
            FirebaseApp.initializeApp(options);

            System.out.println("✅ Firebase initialized.");
        } catch (Exception e) {
            System.err.println("Firebase initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
