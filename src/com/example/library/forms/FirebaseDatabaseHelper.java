package com.example.library.forms;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDatabaseHelper {

    public static void saveUserData(String uid, Object data) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("calendarData"); // or another key

        ref.setValueAsync(data); // data can be a custom class or map
    }
}
