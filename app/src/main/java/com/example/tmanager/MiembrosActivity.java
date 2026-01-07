package com.example.tmanager;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MiembrosActivity extends AppCompatActivity {

    private ImageView imgEscudoEquipo, imgEntrenador;
    private TextView txtNombreEquipo, txtCodigoEquipo, txtEntrenador;
    private RecyclerView recyclerJugadores;

    private FirebaseFirestore db;
    private String equipoId;

    private JugadoresAdapter adapter;
    private List<Map<String, Object>> jugadores = new ArrayList<>();

    private boolean esEntrenador = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miembros);

        imgEscudoEquipo = findViewById(R.id.imgEscudoEquipo);
        txtNombreEquipo = findViewById(R.id.txtNombreEquipo);
        txtCodigoEquipo = findViewById(R.id.txtCodigoEquipo);
        imgEntrenador = findViewById(R.id.imgEntrenador);
        txtEntrenador = findViewById(R.id.txtEntrenador);

        recyclerJugadores = findViewById(R.id.recyclerJugadores);
        recyclerJugadores.setLayoutManager(new LinearLayoutManager(this));



        db = FirebaseFirestore.getInstance();

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {

                    equipoId = userDoc.getString("equipoId");

                    if (equipoId == null) {
                        Toast.makeText(this, "No perteneces a ningún equipo", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String rol = userDoc.getString("rol");
                    esEntrenador = "entrenador".equals(rol);

                    adapter = new JugadoresAdapter(
                            this,
                            jugadores,
                            equipoId,
                            esEntrenador
                    );
                    recyclerJugadores.setAdapter(adapter);

                    cargarEquipo();
                    cargarMiembros();
                });

    }


    private void cargarEquipo() {

        db.collection("equipos").document(equipoId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    txtNombreEquipo.setText(doc.getString("nombre"));
                    txtCodigoEquipo.setText("Código: " + doc.getString("codigo"));

                    String logo = doc.getString("logoUrl");
                    if (logo != null && !logo.isEmpty()) {
                        Glide.with(this)
                                .load(logo)
                                .circleCrop()
                                .into(imgEscudoEquipo);
                    } else {
                        imgEscudoEquipo.setImageResource(R.drawable.circle_empty);
                    }
                });
    }


    private void cargarMiembros() {

        // Estado inicial visible
        txtEntrenador.setText("Entrenador");
        imgEntrenador.setImageResource(R.drawable.userlogo);

        jugadores.clear();

        db.collection("equipos")
                .document(equipoId)
                .collection("miembros")
                .addSnapshotListener((snap, e) -> {

                    if (e != null || snap == null) return;

                    jugadores.clear();

                    for (DocumentSnapshot d : snap.getDocuments()) {

                        String rol = d.getString("rol");

                        if ("entrenador".equals(rol)) {

                            // ===== ENTRENADOR =====
                            txtEntrenador.setText(d.getString("nombre"));

                            String foto = d.getString("fotoUrl");
                            if (foto != null && !foto.isEmpty()) {
                                Glide.with(this)
                                        .load(foto)
                                        .circleCrop()
                                        .into(imgEntrenador);
                            } else {
                                imgEntrenador.setImageResource(R.drawable.userlogo);
                            }

                        } else if ("jugador".equals(rol)) {

                            // ===== JUGADORES =====
                            jugadores.add(d.getData());
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
