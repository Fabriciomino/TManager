package com.example.tmanager;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NotificacionUtil {

    public static void crear(String userUid,
                             String equipoId,
                             String tipo,
                             String titulo,
                             String mensaje,
                             @Nullable String eventoId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("userUid", userUid);
        data.put("equipoId", equipoId);
        data.put("tipo", tipo);
        data.put("titulo", titulo);
        data.put("mensaje", mensaje);
        data.put("eventoId", eventoId);
        data.put("leida", false);
        data.put("timestamp", new Timestamp(new Date()));

        db.collection("notificaciones")
                .add(data)
                .addOnFailureListener(e ->
                        Log.e("NOTIF", "Error creando notificación", e)
                );
    }
}



