package com.example.tmanager;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class MensajesActivity extends AppCompatActivity {

    RecyclerView recycler;
    EditText edtMensaje;
    ImageButton btnEnviar;

    ImageView imgLogoEquipo;
    TextView txtNombreEquipo;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String equipoId, uid;

    List<MensajeModel> lista = new ArrayList<>();
    MensajesAdapter adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mensajes);

        recycler = findViewById(R.id.recyclerMensajes);
        edtMensaje = findViewById(R.id.edtMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);

        imgLogoEquipo = findViewById(R.id.imgLogoEquipo);
        txtNombreEquipo = findViewById(R.id.txtNombreEquipo);

        uid = FirebaseAuth.getInstance().getUid();
        equipoId = getSharedPreferences("EQUIPO", MODE_PRIVATE)
                .getString("equipoId", null);

        adapter = new MensajesAdapter(this, lista);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        cargarDatosEquipo();
        escucharMensajes();

        btnEnviar.setOnClickListener(v -> enviarMensaje());
    }

    // =====================================================
    // HEADER EQUIPO
    // =====================================================
    private void cargarDatosEquipo() {

        var prefs = getSharedPreferences("EQUIPO", MODE_PRIVATE);

        String nombreLocal = prefs.getString("nombre", null);
        String logoLocal = prefs.getString("logoUrl", null);

        if (nombreLocal != null) {
            txtNombreEquipo.setText(nombreLocal);
        }

        if (logoLocal != null) {
            Glide.with(this)
                    .load(logoLocal)
                    .circleCrop()
                    .into(imgLogoEquipo);
        }

        if (equipoId == null) return;

        db.collection("equipos").document(equipoId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    String nombre = doc.getString("nombre");
                    String logo = doc.getString("logoUrl");

                    if (nombre != null) {
                        if (nombre.length() > 15) {
                            nombre = nombre.substring(0, 15) + "…";
                        }
                        txtNombreEquipo.setText(nombre);
                    }

                    if (logo != null) {
                        Glide.with(this)
                                .load(logo)
                                .circleCrop()
                                .into(imgLogoEquipo);
                    }
                });
    }

    // =====================================================
    // MENSAJES
    // =====================================================
    private void escucharMensajes() {

        if (equipoId == null) return;

        db.collection("mensajes")
                .document(equipoId)
                .collection("chat")
                .orderBy("timestamp")
                .addSnapshotListener((snap, e) -> {

                    if (e != null || snap == null) return;

                    lista.clear();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        MensajeModel m = d.toObject(MensajeModel.class);
                        if (m != null) lista.add(m);
                    }

                    adapter.notifyDataSetChanged();

                    if (!lista.isEmpty()) {
                        recycler.scrollToPosition(lista.size() - 1);
                    }
                });
    }

    private void enviarMensaje() {

        String texto = edtMensaje.getText().toString().trim();
        if (texto.isEmpty()) return;

        edtMensaje.setText("");

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(u -> {

                    MensajeModel m = new MensajeModel();
                    m.userUid = uid;
                    m.nombre = u.getString("nombre");
                    m.fotoUrl = u.getString("fotoUrl");
                    m.texto = texto;
                    m.timestamp = new Timestamp(new Date());

                    db.collection("mensajes")
                            .document(equipoId)
                            .collection("chat")
                            .add(m);

                    notificarEquipo(m.nombre);
                });
    }

    private void notificarEquipo(String nombre) {

        db.collection("equipos").document(equipoId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    List<Map<String, Object>> jugadores =
                            (List<Map<String, Object>>) doc.get("jugadores");

                    if (jugadores != null) {
                        for (Map<String, Object> j : jugadores) {
                            String uidJugador = (String) j.get("uid");
                            if (uidJugador == null || uidJugador.equals(uid)) continue;

                            NotificacionUtil.crear(
                                    uidJugador,
                                    equipoId,
                                    "mensaje",
                                    "Nuevo mensaje",
                                    nombre + " ha enviado un mensaje",
                                    null
                            );
                        }
                    }

                    String entrenadorUid = doc.getString("entrenadorUid");
                    if (entrenadorUid != null && !entrenadorUid.equals(uid)) {
                        NotificacionUtil.crear(
                                entrenadorUid,
                                equipoId,
                                "mensaje",
                                "Nuevo mensaje",
                                nombre + " ha enviado un mensaje",
                                null
                        );
                    }
                });
    }
}
