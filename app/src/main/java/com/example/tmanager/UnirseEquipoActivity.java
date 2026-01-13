package com.example.tmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.*;

public class UnirseEquipoActivity extends AppCompatActivity {

    EditText edtCodigo, edtAlias, edtDorsal;
    Spinner spinnerPosicion;
    ImageView imgFotoJugador;
    Button btnUnirse;

    Uri fotoUri = null;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    private static final int PICK_IMAGE = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unirse_equipo);

        edtCodigo = findViewById(R.id.edtCodigo);
        edtAlias = findViewById(R.id.edtAlias);
        edtDorsal = findViewById(R.id.edtDorsal);
        spinnerPosicion = findViewById(R.id.spinnerPosicion);
        imgFotoJugador = findViewById(R.id.imgFotoJugador);
        btnUnirse = findViewById(R.id.btnUnirse);

        String[] posiciones = {"Delantero", "Mediocentro", "Defensa", "Portero"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                posiciones
        ) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                // TEXTO SELECCIONADO (BLANCO)
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(android.graphics.Color.WHITE);
                tv.setTextSize(16);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                // OPCIONES (GRIS)
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(android.graphics.Color.parseColor("#1A0026")); // gris elegante
                tv.setBackgroundColor(android.graphics.Color.parseColor("#F2F2F2"));
                tv.setPadding(20, 20, 20, 20);
                return tv;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPosicion.setAdapter(adapter);



        imgFotoJugador.setOnClickListener(v -> seleccionarFoto());
        btnUnirse.setOnClickListener(v -> intentarUnirse());
    }

    private void seleccionarFoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            fotoUri = data.getData();
            Glide.with(this).load(fotoUri).circleCrop().into(imgFotoJugador);
        }
    }

    private void intentarUnirse() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String codigo = edtCodigo.getText().toString().trim();
        if (codigo.isEmpty()) {
            Toast.makeText(this, "Introduce el código del equipo", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("equipos")
                .whereEqualTo("codigo", codigo)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        Toast.makeText(this, "Código incorrecto", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot equipoDoc = query.getDocuments().get(0);
                    String equipoId = equipoDoc.getId();
                    String uid = user.getUid();

                    // Primero obtener nombre del usuario
                    db.collection("usuarios").document(uid)
                            .get()
                            .addOnSuccessListener(uDoc -> {

                                String nombreJugador = uDoc.getString("nombre");
                                if (nombreJugador == null) nombreJugador = "Jugador";

                                // Ahora subir foto si existe
                                if (fotoUri != null)
                                    subirFotoYUnir(equipoDoc, equipoId, uid, nombreJugador);
                                else
                                    unirJugador(equipoDoc, equipoId, uid, nombreJugador, null);

                            });
                });
    }

    private void subirFotoYUnir(DocumentSnapshot equipoDoc, String equipoId, String uid,
                                String nombreJugador) {

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("jugadores/fotos/" + UUID.randomUUID() + ".png");

        ref.putFile(fotoUri).addOnSuccessListener(task ->
                ref.getDownloadUrl().addOnSuccessListener(url ->
                        unirJugador(equipoDoc, equipoId, uid, nombreJugador, url.toString())
                )
        );
    }

    private void unirJugador(DocumentSnapshot equipoDoc, String equipoId, String uid,
                             String nombreJugador, String fotoUrl) {


        String alias = edtAlias.getText().toString().trim();
        String dorsal = edtDorsal.getText().toString().trim();
        String posicion = spinnerPosicion.getSelectedItem().toString();

        String nombreMostrar = (alias != null && !alias.isEmpty())
                ? alias
                : nombreJugador;

        if (dorsal.isEmpty()) dorsal = "-";

        Map<String, Object> jugadorData = new HashMap<>();
        jugadorData.put("uid", uid);
        jugadorData.put("nombre", nombreJugador);
        jugadorData.put("alias", alias);
        jugadorData.put("posicion", posicion);
        jugadorData.put("dorsal", dorsal);
        jugadorData.put("fotoUrl", fotoUrl);

        db.collection("equipos").document(equipoId)
                .update("jugadores", FieldValue.arrayUnion(jugadorData))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al unirse al equipo", Toast.LENGTH_SHORT).show()
                );

        Map<String, Object> miembro = new HashMap<>();
        miembro.put("uid", uid);
        miembro.put("nombre", nombreJugador);
        miembro.put("alias", alias);
        miembro.put("posicion", posicion);
        miembro.put("dorsal", dorsal);
        miembro.put("fotoUrl", fotoUrl);
        miembro.put("rol", "jugador");

        db.collection("equipos")
                .document(equipoId)
                .collection("miembros")
                .document(uid)
                .set(miembro)

                .addOnSuccessListener(a -> {

                    Map<String, Object> updateUser = new HashMap<>();
                    updateUser.put("rol", "jugador");
                    updateUser.put("equipoId", equipoId);
                    if (fotoUrl != null) {
                        updateUser.put("fotoUrl", fotoUrl);
                    }

                    db.collection("usuarios").document(uid)
                            .update(updateUser)
                            .addOnSuccessListener(v -> {

                                //  NOTIFICACIONES

                                String entrenadorUid = equipoDoc.getString("entrenadorUid");

                                if (entrenadorUid != null && !entrenadorUid.equals(uid)) {
                                    NotificacionUtil.crear(
                                            entrenadorUid,
                                            equipoId,
                                            "union_equipo",
                                            "Nuevo jugador",
                                            nombreMostrar + " se ha unido al equipo",
                                            null
                                    );
                                }

                                NotificacionUtil.crear(
                                        uid,
                                        equipoId,
                                        "union_ok",
                                        "Bienvenido",
                                        "Te has unido correctamente al equipo",
                                        null
                                );


                                // GUARDAR PREFS Y NAVEGAR

                                getSharedPreferences("EQUIPO", MODE_PRIVATE)
                                        .edit()
                                        .putString("nombre", equipoDoc.getString("nombre"))
                                        .putString("logoUrl", equipoDoc.getString("logoUrl"))
                                        .putString("equipoId", equipoId)
                                        .apply();

                                Toast.makeText(this,
                                        "Te has unido al equipo correctamente",
                                        Toast.LENGTH_SHORT).show();

                                Intent i = new Intent(this, MainActivity.class);
                                i.putExtra("open", "eventos");
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                finish();
                            });
                });
    }

}
