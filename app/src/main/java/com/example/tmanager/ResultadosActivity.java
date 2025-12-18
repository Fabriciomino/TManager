package com.example.tmanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultadosActivity extends AppCompatActivity {

    private TextView txtPartidos, txtVictorias, txtEmpates, txtDerrotas;
    private RecyclerView recycler;

    private FirebaseFirestore db;

    private ResultadosAdapter adapter;
    private List<EventModel> partidos = new ArrayList<>();

    private String equipoId; // 👈 CLAVE
    private Map<String, String> uidToAlias = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados);

        txtPartidos = findViewById(R.id.txtPartidos);
        txtVictorias = findViewById(R.id.txtVictorias);
        txtEmpates = findViewById(R.id.txtEmpates);
        txtDerrotas = findViewById(R.id.txtDerrotas);
        recycler = findViewById(R.id.recyclerResultados);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResultadosAdapter(this, partidos);
        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // 🔥 EXACTAMENTE IGUAL QUE EN EVENTOSFRAGMENT
        var prefs = getSharedPreferences("EQUIPO", MODE_PRIVATE);
        equipoId = prefs.getString("equipoId", null);

        cargarAliasJugadores();
        cargarResultados();
    }

    // =====================================================
    //      CARGAR ALIAS (IGUAL QUE EVENTOSFRAGMENT)
    // =====================================================
    private void cargarAliasJugadores() {

        if (equipoId == null) return;

        db.collection("equipos").document(equipoId)
                .get()
                .addOnSuccessListener(doc -> {

                    List<Map<String, Object>> jugadores =
                            (List<Map<String, Object>>) doc.get("jugadores");

                    if (jugadores == null) return;

                    uidToAlias.clear();

                    for (Map<String, Object> j : jugadores) {
                        String uid = String.valueOf(j.get("uid"));
                        String alias = (String) j.get("alias");
                        if (uid != null && alias != null) {
                            uidToAlias.put(uid, alias);
                        }
                    }

                    adapter.setUidToName(uidToAlias);
                    adapter.notifyDataSetChanged();
                });
    }

    // =====================================================
    //                CARGAR RESULTADOS
    // =====================================================
    private void cargarResultados() {

        if (equipoId == null) return;

        db.collection("eventos")
                .whereEqualTo("equipoId", equipoId)
                .whereEqualTo("esPartido", true)
                .addSnapshotListener((snap, err) -> {

                    if (err != null || snap == null) return;

                    partidos.clear();

                    int jugados = 0, victorias = 0, empates = 0, derrotas = 0;

                    for (DocumentSnapshot ds : snap.getDocuments()) {

                        Integer gl = ds.getLong("resultadoLocal") != null
                                ? ds.getLong("resultadoLocal").intValue()
                                : null;
                        Integer gr = ds.getLong("resultadoRival") != null
                                ? ds.getLong("resultadoRival").intValue()
                                : null;

                        if (gl == null || gr == null) continue;

                        EventModel ev = ds.toObject(EventModel.class);
                        ev.setId(ds.getId());
                        ev.setResultadoLocal(gl);
                        ev.setResultadoRival(gr);

                        partidos.add(ev);
                        jugados++;

                        if (gl > gr) victorias++;
                        else if (gl.equals(gr)) empates++;
                        else derrotas++;
                    }

                    txtPartidos.setText(String.valueOf(jugados));
                    txtVictorias.setText(String.valueOf(victorias));
                    txtEmpates.setText(String.valueOf(empates));
                    txtDerrotas.setText(String.valueOf(derrotas));

                    adapter.notifyDataSetChanged();
                });
    }
}
