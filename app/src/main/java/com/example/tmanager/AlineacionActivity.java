package com.example.tmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;


import java.util.*;

public class AlineacionActivity extends AppCompatActivity {

    TextView txtRival, txtFecha;
    Button btnGuardarAlineacion;

    LinearLayout posPortero,
            posDef1, posDef2, posDef3, posDef4,
            posMed1, posMed2, posMed3,
            posDel1, posDel2, posDel3;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String equipoId, partidoId, rolUsuario;

    List<Jugador> jugadoresConvocados = new ArrayList<>();
    Set<String> jugadoresUsados = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alineacion);

        txtRival = findViewById(R.id.txtRival);
        txtFecha = findViewById(R.id.txtFecha);
        btnGuardarAlineacion = findViewById(R.id.btnGuardarAlineacion);
        btnGuardarAlineacion.setVisibility(View.GONE);

        posPortero = findViewById(R.id.posPortero);
        posDef1 = findViewById(R.id.posDef1);
        posDef2 = findViewById(R.id.posDef2);
        posDef3 = findViewById(R.id.posDef3);
        posDef4 = findViewById(R.id.posDef4);
        posMed1 = findViewById(R.id.posMed1);
        posMed2 = findViewById(R.id.posMed2);
        posMed3 = findViewById(R.id.posMed3);
        posDel1 = findViewById(R.id.posDel1);
        posDel2 = findViewById(R.id.posDel2);
        posDel3 = findViewById(R.id.posDel3);

        equipoId = getSharedPreferences("EQUIPO", MODE_PRIVATE)
                .getString("equipoId", null);

        obtenerRolUsuario();
        cargarSiguientePartido();
    }


    private void obtenerRolUsuario() {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {

                    rolUsuario = doc.getString("rol");

                    if ("entrenador".equals(rolUsuario)) {
                        btnGuardarAlineacion.setVisibility(View.VISIBLE);
                        activarClicks();
                    }
                });
    }


    private void cargarSiguientePartido() {

        if (equipoId == null) return;

        db.collection("eventos")
                .whereEqualTo("equipoId", equipoId)
                .whereEqualTo("esPartido", true)
                .whereEqualTo("finalizado", false)
                .get()
                .addOnSuccessListener(snap -> {

                    DocumentSnapshot partido = null;

                    for (DocumentSnapshot d : snap.getDocuments()) {

                        Boolean eliminado = d.getBoolean("resultadoEliminado");
                        if (Boolean.TRUE.equals(eliminado)) continue;

                        Timestamp ts = d.getTimestamp("fechaHoraTimestamp");
                        if (ts == null) continue;

                        if (partido == null ||
                                ts.compareTo(partido.getTimestamp("fechaHoraTimestamp")) < 0) {
                            partido = d;
                        }
                    }

                    if (partido == null) {
                        txtRival.setText("No hay partidos disponibles");
                        txtFecha.setText("");
                        return;
                    }

                    partidoId = partido.getId();
                    txtRival.setText("vs " + partido.getString("rival"));
                    txtFecha.setText(
                            partido.getString("fecha") + " · " + partido.getString("hora")
                    );

                    cargarConvocados((List<String>) partido.get("convocados"));
                });
    }


    private void cargarAlineacionGuardada() {

        db.collection("alineaciones").document(partidoId).get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    colocarDesdeDB(posPortero, doc.getString("portero"));

                    colocarLista(posDef1, posDef2, posDef3, posDef4,
                            (List<String>) doc.get("defensas"));

                    colocarLista(posMed1, posMed2, posMed3,
                            (List<String>) doc.get("medios"));

                    colocarLista(posDel1, posDel2, posDel3,
                            (List<String>) doc.get("delanteros"));
                });
    }

    private void colocarLista(LinearLayout a, LinearLayout b, LinearLayout c, LinearLayout d, List<String> uids) {
        if (uids == null) return;
        LinearLayout[] arr = {a, b, c, d};
        for (int i = 0; i < arr.length && i < uids.size(); i++) {
            colocarDesdeDB(arr[i], uids.get(i));
        }
    }

    private void colocarLista(LinearLayout a, LinearLayout b, LinearLayout c, List<String> uids) {
        if (uids == null) return;
        LinearLayout[] arr = {a, b, c};
        for (int i = 0; i < arr.length && i < uids.size(); i++) {
            colocarDesdeDB(arr[i], uids.get(i));
        }
    }

    private void colocarDesdeDB(LinearLayout pos, String uid) {
        if (uid == null) return;

        for (Jugador j : jugadoresConvocados) {
            if (uid.equals(j.uid)) {
                colocarJugador(pos, j);
                jugadoresUsados.add(j.uid);
                break;
            }
        }
    }
    private void cargarConvocados(List<String> uids) {

        if (uids == null || uids.isEmpty()) return;

        db.collection("equipos").document(equipoId).get()
                .addOnSuccessListener(doc -> {

                    List<Map<String, Object>> jugadores =
                            (List<Map<String, Object>>) doc.get("jugadores");

                    if (jugadores == null) return;

                    jugadoresConvocados.clear();

                    for (Map<String, Object> j : jugadores) {
                        if (!uids.contains(j.get("uid"))) continue;

                        Jugador ju = new Jugador();
                        ju.uid = (String) j.get("uid");
                        ju.nombre = (String) j.get("alias");
                        ju.posicion = (String) j.get("posicion");
                        ju.fotoUrl = (String) j.get("fotoUrl");

                        jugadoresConvocados.add(ju);
                    }
                    cargarAlineacionGuardada();

                });
    }

    private void activarClicks() {

        configurarPosicion(posPortero, "Portero");
        configurarPosicion(posDef1, "Defensa");
        configurarPosicion(posDef2, "Defensa");
        configurarPosicion(posDef3, "Defensa");
        configurarPosicion(posDef4, "Defensa");
        configurarPosicion(posMed1, "Mediocentro");
        configurarPosicion(posMed2, "Mediocentro");
        configurarPosicion(posMed3, "Mediocentro");
        configurarPosicion(posDel1, "Delantero");
        configurarPosicion(posDel2, "Delantero");
        configurarPosicion(posDel3, "Delantero");

        btnGuardarAlineacion.setOnClickListener(v -> guardarAlineacion());
    }

    private void configurarPosicion(LinearLayout pos, String rol) {

        pos.setOnClickListener(v -> mostrarSelector(pos, rol));

        pos.setOnLongClickListener(v -> {
            String uid = (String) pos.getTag();
            if (uid != null) jugadoresUsados.remove(uid);

            limpiarPosicion(pos);
            return true;
        });
    }

    private void mostrarSelector(LinearLayout posView, String posicion) {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_elegir_jugador, null);
        dialog.setContentView(v);

        RecyclerView recycler = v.findViewById(R.id.recyclerJugadores);
        recycler.setLayoutManager(new GridLayoutManager(this, 3));

        List<Jugador> filtrados = new ArrayList<>();
        for (Jugador j : jugadoresConvocados) {
            if (posicion.equals(j.posicion) && !jugadoresUsados.contains(j.uid)) {
                filtrados.add(j);
            }
        }

        recycler.setAdapter(new JugadoresAlineacionAdapter(
                this,
                filtrados,
                jugador -> {
                    colocarJugador(posView, jugador);
                    jugadoresUsados.add(jugador.uid);
                    dialog.dismiss();
                }
        ));

        dialog.show();
    }

    private void colocarJugador(LinearLayout posView, Jugador jugador) {

        ImageView img = posView.findViewById(R.id.imgJugador);
        TextView txt = posView.findViewById(R.id.txtJugador);

        Glide.with(this)
                .load(jugador.fotoUrl != null ? jugador.fotoUrl : R.drawable.userlogo)
                .circleCrop()
                .into(img);

        txt.setText(jugador.nombre);
        posView.setTag(jugador.uid);
    }

    private void limpiarPosicion(LinearLayout posView) {

        ImageView img = posView.findViewById(R.id.imgJugador);
        TextView txt = posView.findViewById(R.id.txtJugador);

        img.setImageResource(R.drawable.circle_empty);
        txt.setText("");
        posView.setTag(null);
    }

    private void guardarAlineacion() {

        Map<String, Object> data = new HashMap<>();

        data.put("portero", posPortero.getTag());
        data.put("defensas", Arrays.asList(
                posDef1.getTag(), posDef2.getTag(),
                posDef3.getTag(), posDef4.getTag()
        ));
        data.put("medios", Arrays.asList(
                posMed1.getTag(), posMed2.getTag(), posMed3.getTag()
        ));
        data.put("delanteros", Arrays.asList(
                posDel1.getTag(), posDel2.getTag(), posDel3.getTag()
        ));

        db.collection("alineaciones")
                .document(partidoId)
                .set(data);

        Toast.makeText(this, "Alineación guardada", Toast.LENGTH_SHORT).show();
    }
}
