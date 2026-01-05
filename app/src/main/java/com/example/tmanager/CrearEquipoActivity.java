package com.example.tmanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CrearEquipoActivity extends AppCompatActivity {

    EditText edtNombreEquipo;
    ImageView imgEscudo;
    Spinner spinnerDeporte;
    Button btnCrearEquipo;

    Uri escudoUri = null;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    // Seleccionar imagen desde galería
    ActivityResultLauncher<Intent> seleccionarImagen =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    escudoUri = result.getData().getData();
                    Glide.with(this)
                            .load(escudoUri)
                            .centerCrop()
                            .into(imgEscudo);

                }
            });

    // Pedir permiso
    ActivityResultLauncher<String> pedirPermiso =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) abrirGaleria();
                else Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_equipo);

        edtNombreEquipo = findViewById(R.id.edtNombreEquipo);
        imgEscudo = findViewById(R.id.imgEscudo);
        btnCrearEquipo = findViewById(R.id.btnCrearEquipo);


        imgEscudo.setOnClickListener(v -> verificarPermisos());
        btnCrearEquipo.setOnClickListener(v -> crearEquipo());
    }

    private void verificarPermisos() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                pedirPermiso.launch(Manifest.permission.READ_MEDIA_IMAGES);
            } else abrirGaleria();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                pedirPermiso.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else abrirGaleria();
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        seleccionarImagen.launch(intent);
    }

    private void crearEquipo() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión para crear un equipo", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = edtNombreEquipo.getText().toString().trim();
        String uidEntrenador = user.getUid();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Pon un nombre al equipo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (escudoUri == null) {
            Toast.makeText(this, "Selecciona un escudo", Toast.LENGTH_SHORT).show();
            return;
        }

        String codigo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("equipos/" + uidEntrenador + "_" + System.currentTimeMillis() + ".png");

        // SUBIR LOGO
        ref.putFile(escudoUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {

                            // Datos del equipo
                            HashMap<String, Object> equipo = new HashMap<>();
                            equipo.put("nombre", nombre);
                            equipo.put("logoUrl", uri.toString());
                            equipo.put("codigo", codigo);
                            equipo.put("entrenadorUid", uidEntrenador);
                            equipo.put("jugadores", new ArrayList<String>());
                            equipo.put("fechaCreacion", Timestamp.now());

                            // Guardar equipo
                            db.collection("equipos")
                                    .add(equipo)
                                    .addOnSuccessListener(documentRef -> {

                                        String equipoId = documentRef.getId();

                                        HashMap<String, Object> datosUsuario = new HashMap<>();
                                        datosUsuario.put("rol", "entrenador");
                                        datosUsuario.put("equipoId", equipoId);

                                        db.collection("usuarios").document(uidEntrenador)
                                                .update(datosUsuario);

                                        Toast.makeText(this, "Equipo creado correctamente", Toast.LENGTH_SHORT).show();
                                        getSharedPreferences("EQUIPO", MODE_PRIVATE)
                                                .edit()
                                                .clear()
                                                .apply();
                                        Intent intent = new Intent(CrearEquipoActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    });

                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error subiendo el logo: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

}
