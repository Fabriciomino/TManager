package com.example.tmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.UUID;

public class DetallesEquipoActivity extends AppCompatActivity {

    ImageView imgLogo;
    EditText edtNombre;
    Uri logoUri;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String equipoId;

    private static final int PICK_IMAGE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_equipo);

        imgLogo = findViewById(R.id.imgLogoEquipo);
        edtNombre = findViewById(R.id.edtNombreEquipo);

        equipoId = getSharedPreferences("EQUIPO", MODE_PRIVATE).getString("equipoId", null);

        cargarEquipo();

        findViewById(R.id.btnCambiarLogo).setOnClickListener(v -> seleccionarLogo());
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());
        findViewById(R.id.btnEliminarEquipo).setOnClickListener(v -> eliminarEquipo());
    }

    private void cargarEquipo() {
        db.collection("equipos").document(equipoId).get().addOnSuccessListener(d -> {
            edtNombre.setText(d.getString("nombre"));
            Glide.with(this).load(d.getString("logoUrl")).circleCrop().into(imgLogo);
        });
    }

    private void seleccionarLogo() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int r, int c, Intent d) {
        super.onActivityResult(r, c, d);
        if (r == PICK_IMAGE && d != null) {
            logoUri = d.getData();
            Glide.with(this).load(logoUri).circleCrop().into(imgLogo);
        }
    }

    private void guardar() {
        if (logoUri != null) {
            FirebaseStorage.getInstance().getReference("equipos/logos/" + UUID.randomUUID())
                    .putFile(logoUri)
                    .addOnSuccessListener(t ->
                            t.getStorage().getDownloadUrl().addOnSuccessListener(url ->
                                    db.collection("equipos").document(equipoId)
                                            .update("nombre", edtNombre.getText().toString(),
                                                    "logoUrl", url.toString())
                            ));
        } else {
            db.collection("equipos").document(equipoId)
                    .update("nombre", edtNombre.getText().toString());
        }
    }

    private void eliminarEquipo() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar equipo")
                .setMessage("Se eliminará el equipo completo")
                .setPositiveButton("Eliminar", (d, w) ->
                        db.collection("equipos").document(equipoId).delete())
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
