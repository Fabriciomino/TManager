package com.example.tmanager;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.*;

import java.util.*;

public class EstadisticasJugadoresActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private JugadorStatsAdapter adapter;
    private List<JugadorStats> lista = new ArrayList<>();

    private FirebaseFirestore db;
    private String equipoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas_jugadores);

        recycler = findViewById(R.id.recyclerStats);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new JugadorStatsAdapter(this, lista);
        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        equipoId = getSharedPreferences("EQUIPO", MODE_PRIVATE)
                .getString("equipoId", null);

        if (equipoId == null) {
            Toast.makeText(this, "Equipo no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarEstadisticas();
    }

    private void cargarEstadisticas() {

        db.collection("equipos").document(equipoId).get()
                .addOnSuccessListener(doc -> {

                    List<Map<String, Object>> jugadores =
                            (List<Map<String, Object>>) doc.get("jugadores");

                    if (jugadores == null) return;

                    Map<String, JugadorStats> map = new HashMap<>();

                    for (Map<String, Object> j : jugadores) {
                        JugadorStats js = new JugadorStats();
                        js.uid = String.valueOf(j.get("uid"));
                        js.alias = (String) j.get("alias");
                        js.dorsal = String.valueOf(j.get("dorsal"));
                        js.posicion = (String) j.get("posicion");
                        js.fotoUrl = (String) j.get("fotoUrl");
                        map.put(js.uid, js);
                    }

                    db.collection("eventos")
                            .whereEqualTo("equipoId", equipoId)
                            .whereEqualTo("esPartido", true)
                            .get()
                            .addOnSuccessListener(events -> {

                                for (DocumentSnapshot e : events) {

                                    Long gl = e.getLong("resultadoLocal");
                                    Long gr = e.getLong("resultadoRival");

                                    if (gl == null || gr == null) continue;

                                    List<String> conv =
                                            (List<String>) e.get("convocados");

                                    if (conv != null) {
                                        for (String uid : conv) {
                                            if (map.containsKey(uid)) {
                                                map.get(uid).partidos++;
                                            }
                                        }
                                    }

                                    List<Map<String, Object>> goles =
                                            (List<Map<String, Object>>) e.get("goles");

                                    if (goles != null) {
                                        for (Map<String, Object> g : goles) {
                                            String scorer = (String) g.get("scorerUid");
                                            String assist = (String) g.get("assistantUid");

                                            if (map.containsKey(scorer))
                                                map.get(scorer).goles++;

                                            if (assist != null && map.containsKey(assist))
                                                map.get(assist).asistencias++;
                                        }
                                    }

                                    List<Map<String, Object>> tarjetas =
                                            (List<Map<String, Object>>) e.get("tarjetas");

                                    if (tarjetas != null) {
                                        for (Map<String, Object> t : tarjetas) {
                                            String uid = (String) t.get("playerUid");
                                            String tipo = (String) t.get("tipo");

                                            if (!map.containsKey(uid)) continue;

                                            if ("amarilla".equals(tipo))
                                                map.get(uid).amarillas++;
                                            else if ("roja".equals(tipo))
                                                map.get(uid).rojas++;
                                        }
                                    }
                                }

                                lista.clear();
                                lista.addAll(map.values());

                                // Orden bonito
                                lista.sort((a, b) -> b.goles - a.goles);

                                adapter.notifyDataSetChanged();
                            });
                });
    }
}
