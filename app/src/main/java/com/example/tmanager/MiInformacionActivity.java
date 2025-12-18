package com.example.tmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class MiInformacionActivity extends AppCompatActivity {

    ImageView imgPerfil;
    EditText edtNombre;
    TextView txtEmail, btnCambiarFoto;
    Button btnCambiarPassword, btnCerrarSesion, btnEliminarCuenta;

    Uri fotoUri;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final int PICK_IMAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_informacion);

        imgPerfil = findViewById(R.id.imgPerfil);
        edtNombre = findViewById(R.id.edtNombre);
        txtEmail = findViewById(R.id.txtEmail);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);
        btnCambiarPassword = findViewById(R.id.btnCambiarPassword);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);

        cargarDatos();

        btnCambiarFoto.setOnClickListener(v -> seleccionarFoto());

        btnCambiarPassword.setOnClickListener(v ->
                auth.sendPasswordResetEmail(txtEmail.getText().toString())
        );

        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        btnEliminarCuenta.setOnClickListener(v -> confirmarEliminarCuenta());
    }

    // 🔄 Al volver de cambiar foto, refresca
    @Override
    protected void onResume() {
        super.onResume();
        cargarFotoPerfil();
    }

    // --------------------------------------------------
    // CARGAR DATOS USUARIO
    // --------------------------------------------------
    private void cargarDatos() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        txtEmail.setText(user.getEmail());

        db.collection("usuarios").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    edtNombre.setText(doc.getString("nombre"));
                });

        cargarFotoPerfil();
    }

    // --------------------------------------------------
    // FOTO PERFIL (MISMA LÓGICA QUE EL FRAGMENT)
    // --------------------------------------------------
    private void cargarFotoPerfil() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("usuarios").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {

                    String fotoUrl = doc.getString("fotoUrl");

                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        Glide.with(this)
                                .load(fotoUrl)
                                .circleCrop()
                                .into(imgPerfil);
                    } else if (user.getPhotoUrl() != null) {
                        Glide.with(this)
                                .load(user.getPhotoUrl())
                                .circleCrop()
                                .into(imgPerfil);
                    } else {
                        imgPerfil.setImageResource(R.drawable.userlogo);
                    }
                });
    }

    // --------------------------------------------------
    // SELECCIONAR FOTO
    // --------------------------------------------------
    private void seleccionarFoto() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_IMAGE);
    }

    // --------------------------------------------------
    // SUBIR FOTO Y GUARDAR EN FIRESTORE
    // --------------------------------------------------
    @Override
    protected void onActivityResult(int r, int c, Intent d) {
        super.onActivityResult(r, c, d);

        if (r == PICK_IMAGE && c == RESULT_OK && d != null) {

            fotoUri = d.getData();

            Glide.with(this)
                    .load(fotoUri)
                    .circleCrop()
                    .into(imgPerfil);

            FirebaseUser user = auth.getCurrentUser();
            if (user == null) return;

            StorageReference ref = FirebaseStorage.getInstance()
                    .getReference("usuarios/fotos/" + UUID.randomUUID() + ".jpg");

            ref.putFile(fotoUri)
                    .addOnSuccessListener(task ->
                            ref.getDownloadUrl().addOnSuccessListener(url -> {

                                // ✅ GUARDAR EN FIRESTORE (CLAVE)
                                db.collection("usuarios")
                                        .document(user.getUid())
                                        .update("fotoUrl", url.toString());
                            })
                    );
        }
    }

    // --------------------------------------------------
    // CERRAR SESIÓN
    // --------------------------------------------------
    private void cerrarSesion() {
        auth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity();
    }

    // --------------------------------------------------
    // ELIMINAR CUENTA
    // --------------------------------------------------
    private void confirmarEliminarCuenta() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("Esta acción no se puede deshacer")
                .setPositiveButton("Eliminar", (d, w) -> {

                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) return;

                    db.collection("usuarios").document(user.getUid()).delete();
                    user.delete();

                    cerrarSesion();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
