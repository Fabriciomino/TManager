package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class NotificacionesActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private NotificacionesAdapter adapter;
    private List<NotificacionModel> lista = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        recycler = findViewById(R.id.recyclerNotificaciones);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificacionesAdapter(this, lista);
        recycler.setAdapter(adapter);

        // 🔔 CLICK EN NOTIFICACIÓN
        adapter.setOnNotificacionClickListener(this::onNotificacionClick);

        db = FirebaseFirestore.getInstance();

        cargarNotificaciones();
    }

    // =====================================================
    //        CARGAR NOTIFICACIONES DEL USUARIO
    // =====================================================
    private void cargarNotificaciones() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("notificaciones")
                .whereEqualTo("userUid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {

                    if (snap == null) return;

                    lista.clear();

                    for (DocumentSnapshot d : snap) {
                        NotificacionModel n = d.toObject(NotificacionModel.class);
                        n.id = d.getId();
                        lista.add(n);
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    // =====================================================
    //        CLICK EN UNA NOTIFICACIÓN
    // =====================================================
    private void onNotificacionClick(NotificacionModel n) {

        // 1️⃣ MARCAR COMO LEÍDA
        if (!n.leida) {
            db.collection("notificaciones")
                    .document(n.id)
                    .update("leida", true);
        }

        // 2️⃣ ABRIR EVENTO SI EXISTE
        if (n.eventoId != null) {

            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("open", "eventos");
            i.putExtra("eventoId", n.eventoId);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }

        // 3️⃣ CERRAR NOTIFICACIONES
        finish();
    }
}
