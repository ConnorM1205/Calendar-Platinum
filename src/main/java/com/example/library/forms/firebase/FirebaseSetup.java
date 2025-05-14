package com.example.library.forms.firebase;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;
import java.io.IOException;

public class FirebaseSetup {
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;

        try {
            InputStream serviceAccount = FirebaseSetup.class
                    .getClassLoader()
                    .getResourceAsStream("serviceAccountKey.json");

            if (serviceAccount == null) {
                throw new RuntimeException("serviceAccountKey.json not found in resources!");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://canvas-platinum-default-rtdb.firebaseio.com/")
                    .build();

            FirebaseApp.initializeApp(options);
            initialized = true;
            System.out.println("Firebase Realtime Database initialized!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance();
    }
}
