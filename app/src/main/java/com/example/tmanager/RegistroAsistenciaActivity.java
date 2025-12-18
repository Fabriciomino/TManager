package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class RegistroAsistenciaActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private RegistroAsistenciaAdapter adapter;
    private List<EventModel> eventos = new ArrayList<>();

    private FirebaseFirestore db;
    private String equipoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_asistencia);

        recycler = findViewById(R.id.recyclerEventos);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RegistroAsistenciaAdapter(this, eventos, ev -> {
            Intent i = new Intent(this, DetalleAsistenciaEventoActivity.class);
            i.putExtra("eventoId", ev.getId());
            startActivity(i);
        });

        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        equipoId = getSharedPreferences("EQUIPO", MODE_PRIVATE)
                .getString("equipoId", null);

        if (equipoId == null) {
            Toast.makeText(this, "Equipo no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarEventos();
    }

    private void cargarEventos() {

        db.collection("eventos")
                .whereEqualTo("equipoId", equipoId)
                .whereEqualTo("finalizado", false)
                .orderBy("fechaHoraTimestamp")
                .addSnapshotListener((snap, e) -> {

                    if (e != null || snap == null) return;

                    eventos.clear();

                    for (DocumentSnapshot ds : snap) {
                        EventModel ev = ds.toObject(EventModel.class);
                        ev.setId(ds.getId());
                        eventos.add(ev);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
