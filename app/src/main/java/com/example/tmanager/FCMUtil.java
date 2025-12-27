package com.example.tmanager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class FCMUtil {

    public static void guardarToken() {

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {

                    String uid = FirebaseAuth.getInstance().getUid();
                    if (uid == null) return;

                    FirebaseFirestore.getInstance()
                            .collection("usuarios")
                            .document(uid)
                            .update("fcmToken", token);
                });
    }
}
