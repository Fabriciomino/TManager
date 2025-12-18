package com.example.tmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.*;

import java.util.*;

public class DetalleAsistenciaEventoActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private LinearLayout laySi, layNo, layPend;
    private TextView txtTitulo, txtFecha;

    private String eventoId;
    private String equipoId;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_detalle_asistencia);

        txtTitulo = findViewById(R.id.txtTituloEvento);
        txtFecha = findViewById(R.id.txtFechaEvento);

        laySi = findViewById(R.id.layoutSi);
        layNo = findViewById(R.id.layoutNo);
        layPend = findViewById(R.id.layoutPendiente);

        eventoId = getIntent().getStringExtra("eventoId");

        equipoId = getSharedPreferences("EQUIPO", MODE_PRIVATE)
                .getString("equipoId", null);

        db = FirebaseFirestore.getInstance();

        cargarDetalle();
    }

    private void cargarDetalle() {

        db.collection("eventos").document(eventoId).get()
                .addOnSuccessListener(evDoc -> {

                    EventModel ev = evDoc.toObject(EventModel.class);

                    txtTitulo.setText(ev.isEsPartido() ? "Partido" : ev.getTitulo());
                    txtFecha.setText(ev.getFecha() + " · " + ev.getHora());

                    Map<String, String> asist = ev.getAsistencias();
                    List<String> conv = ev.getConvocados();

                    db.collection("equipos").document(equipoId).get()
                            .addOnSuccessListener(eq -> {

                                List<Map<String, Object>> jugadores =
                                        (List<Map<String, Object>>) eq.get("jugadores");

                                for (Map<String, Object> j : jugadores) {

                                    String uid = String.valueOf(j.get("uid"));
                                    if (conv == null || !conv.contains(uid)) continue;

                                    String estado = asist != null ? asist.get(uid) : null;

                                    View item = crearItemJugador(j);

                                    if ("si".equals(estado)) laySi.addView(item);
                                    else if ("no".equals(estado)) layNo.addView(item);
                                    else layPend.addView(item);
                                }
                            });
                });
    }

    private View crearItemJugador(Map<String, Object> j) {

        View v = getLayoutInflater().inflate(R.layout.item_jugador, null);

        TextView txt = v.findViewById(R.id.txtJugador);
        ImageView img = v.findViewById(R.id.imgJugador);

        String nombre = j.get("alias") + " #" + j.get("dorsal");
        txt.setText(nombre);

        String foto = (String) j.get("fotoUrl");
        if (foto != null)
            Glide.with(this).load(foto).circleCrop().into(img);

        return v;
    }
}
