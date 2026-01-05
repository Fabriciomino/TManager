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
import com.google.firebase.firestore.*;

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

        adapter = new JugadoresAdapter(this, jugadores);
        recyclerJugadores.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        finish();
                        return;
                    }

                    equipoId = doc.getString("equipoId");

                    if (equipoId == null) {
                        Toast.makeText(this, "No perteneces a ningún equipo", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    cargarEquipoYEntrenador();
                    cargarJugadores();
                });
    }

    //          EQUIPO + ENTRENADOR
    private void cargarEquipoYEntrenador() {

        db.collection("equipos").document(equipoId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    // EQUIPO
                    txtNombreEquipo.setText(doc.getString("nombre"));
                    txtCodigoEquipo.setText("Código: " + doc.getString("codigo"));

                    String logo = doc.getString("logoUrl");
                    if (logo != null && !logo.isEmpty()) {
                        Glide.with(this).load(logo).circleCrop().into(imgEscudoEquipo);
                    }

                    // ENTRENADOR
                    String entrenadorUid = doc.getString("entrenadorUid");
                    if (entrenadorUid == null) return;

                    db.collection("usuarios").document(entrenadorUid)
                            .get()
                            .addOnSuccessListener(user -> {

                                if (!user.exists()) return;

                                txtEntrenador.setText(user.getString("nombre"));

                                String foto = user.getString("fotoUrl");
                                if (foto != null && !foto.isEmpty()) {
                                    Glide.with(this)
                                            .load(foto)
                                            .circleCrop()
                                            .into(imgEntrenador);
                                }
                            });
                });
    }


    private void cargarJugadores() {

        db.collection("equipos").document(equipoId)
                .addSnapshotListener((doc, e) -> {

                    if (e != null || doc == null || !doc.exists()) return;

                    List<Map<String, Object>> list =
                            (List<Map<String, Object>>) doc.get("jugadores");

                    jugadores.clear();

                    if (list != null)
                        jugadores.addAll(list);

                    adapter.notifyDataSetChanged();
                });
    }
}
