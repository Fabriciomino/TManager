package com.example.tmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class MiInformacionActivity extends AppCompatActivity {

    ImageView imgPerfil;
    EditText edtNombre, edtPassActual, edtPassNueva, edtPassConfirmar;
    ImageView btnSaveNombre, btnSavePassword;
    ImageView eyeActual, eyeNueva, eyeConfirmar;
    TextView txtEmail;
    Button btnCerrarSesion, btnEliminarCuenta;

    TextView rule1, rule2, rule3, rule4, rule5;
    LinearLayout layoutPassword;
    EditText edtAlias;
    ImageView btnSaveAlias;
    View layoutAlias;


    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Uri fotoUri;
    boolean esGoogle = false;

    private static final int PICK_IMAGE = 101;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mi_informacion);

        imgPerfil = findViewById(R.id.imgPerfil);
        edtNombre = findViewById(R.id.edtNombre);
        txtEmail = findViewById(R.id.txtEmail);

        edtAlias = findViewById(R.id.edtAlias);
        btnSaveAlias = findViewById(R.id.btnSaveAlias);
        layoutAlias = findViewById(R.id.layoutAlias);


        edtPassActual = findViewById(R.id.edtPasswordActual);
        edtPassNueva = findViewById(R.id.edtPasswordNueva);
        edtPassConfirmar = findViewById(R.id.edtPasswordConfirmar);

        btnSaveNombre = findViewById(R.id.btnSaveNombre);
        btnSavePassword = findViewById(R.id.btnSavePassword);

        eyeActual = findViewById(R.id.btnEyeActual);
        eyeNueva = findViewById(R.id.btnEyeNueva);
        eyeConfirmar = findViewById(R.id.btnEyeConfirmar);

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);

        layoutPassword = findViewById(R.id.layoutPassword);

        rule1 = findViewById(R.id.rule1);
        rule2 = findViewById(R.id.rule2);
        rule3 = findViewById(R.id.rule3);
        rule4 = findViewById(R.id.rule4);
        rule5 = findViewById(R.id.rule5);

        detectarProveedor();
        cargarDatos();
        configurarOjos();
        configurarValidacionPassword();
        cargarFotoPerfil();


        imgPerfil.setOnClickListener(v -> seleccionarFoto());
        btnSaveNombre.setOnClickListener(v -> confirmarGuardarNombre());
        btnSaveAlias.setOnClickListener(v -> confirmarGuardarAlias());
        btnSavePassword.setOnClickListener(v -> confirmarGuardarPassword());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
        btnEliminarCuenta.setOnClickListener(v -> confirmarEliminarCuenta());
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void seleccionarFoto() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {

            fotoUri = data.getData();
            if (fotoUri == null) return;

            // PREVIEW INMEDIATA
            Glide.with(this)
                    .load(fotoUri)
                    .circleCrop()
                    .into(imgPerfil);

            FirebaseUser user = auth.getCurrentUser();
            if (user == null) return;

            // SUBIR A STORAGE
            StorageReference ref = FirebaseStorage.getInstance()
                    .getReference("usuarios/fotos/" + user.getUid() + ".jpg");

            ref.putFile(fotoUri)
                    .addOnSuccessListener(task ->
                                    ref.getDownloadUrl().addOnSuccessListener(url -> {

                                        String fotoNueva = url.toString();

                                        //  GUARDAR EN FIRESTORE
                                        db.collection("usuarios")
                                                .document(user.getUid())
                                                .update("fotoUrl", fotoNueva);

                                        // ACTUALIZAR FIREBASE AUTH
                                        UserProfileChangeRequest profileUpdates =
                                                new UserProfileChangeRequest.Builder()
                                                        .setPhotoUri(Uri.parse(fotoNueva))
                                                        .build();

                                        user.updateProfile(profileUpdates)
                                                .addOnSuccessListener(a ->
                                                        Toast.makeText(this,
                                                                "Foto de perfil actualizada",
                                                                Toast.LENGTH_SHORT).show()
                                                );

                                    })
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Error subiendo imagen",
                                    Toast.LENGTH_SHORT).show()
                    );
        }
    }


    private void confirmarGuardarNombre() {
        String nombre = edtNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            toast("El nombre no puede estar vacío");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Cambiar nombre")
                .setMessage("¿Deseas guardar este nombre?")
                .setPositiveButton("Guardar", (d, w) ->
                        db.collection("usuarios")
                                .document(auth.getUid())
                                .update("nombre", nombre))
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void confirmarGuardarPassword() {

        String actual = edtPassActual.getText().toString();
        String nueva = edtPassNueva.getText().toString();
        String confirmar = edtPassConfirmar.getText().toString();

        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            toast("Completa todos los campos");
            return;
        }

        if (!passwordValida(nueva)) {
            toast("La contraseña no cumple los requisitos");
            return;
        }

        if (!nueva.equals(confirmar)) {
            toast("Las contraseñas no coinciden");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Cambiar contraseña")
                .setMessage("¿Deseas cambiar tu contraseña?")
                .setPositiveButton("Cambiar", (d, w) -> cambiarPassword(actual, nueva))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cambiarPassword(String actual, String nueva) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        AuthCredential cred =
                EmailAuthProvider.getCredential(user.getEmail(), actual);

        user.reauthenticate(cred)
                .addOnSuccessListener(a ->
                        user.updatePassword(nueva)
                                .addOnSuccessListener(b ->
                                        toast("Contraseña actualizada")))
                .addOnFailureListener(e ->
                        toast("Contraseña actual incorrecta"));
    }


    private void configurarOjos() {
        toggleEye(eyeActual, edtPassActual);
        toggleEye(eyeNueva, edtPassNueva);
        toggleEye(eyeConfirmar, edtPassConfirmar);
    }

    private void toggleEye(ImageView eye, EditText edt) {
        eye.setOnClickListener(v -> {
            boolean visible = edt.getInputType()
                    == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

            edt.setInputType(visible
                    ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

            eye.setImageResource(visible
                    ? R.drawable.ic_eye_closed_dark
                    : R.drawable.ic_eye_open_dark);

            edt.setSelection(edt.getText().length());
        });
    }

    private void configurarValidacionPassword() {
        edtPassNueva.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                validarReglas(s.toString());
            }
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable e) {}
        });
    }

    private boolean passwordValida(String p) {
        return p.length() >= 8 &&
                p.matches(".*[A-Z].*") &&
                p.matches(".*[a-z].*") &&
                p.matches(".*[0-9].*") &&
                p.matches(".*[@#_$%^&+=!¿?.-].*");
    }

    private void validarReglas(String p) {
        rule1.setTextColor(p.length() >= 8 ? 0xFF00E676 : 0xFFFFFFFF);
        rule2.setTextColor(p.matches(".*[A-Z].*") ? 0xFF00E676 : 0xFFFFFFFF);
        rule3.setTextColor(p.matches(".*[a-z].*") ? 0xFF00E676 : 0xFFFFFFFF);
        rule4.setTextColor(p.matches(".*[0-9].*") ? 0xFF00E676 : 0xFFFFFFFF);
        rule5.setTextColor(p.matches(".*[@#_$%^&+=!¿?.-].*") ? 0xFF00E676 : 0xFFFFFFFF);
    }

    private void detectarProveedor() {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) return;
        for (UserInfo i : u.getProviderData())
            if ("google.com".equals(i.getProviderId())) {
                esGoogle = true;
                layoutPassword.setVisibility(View.GONE);
            }
    }

    private void cargarDatos() {

        FirebaseUser u = auth.getCurrentUser();
        if (u == null) return;

        txtEmail.setText(u.getEmail());

        db.collection("usuarios")
                .document(u.getUid())
                .get()
                .addOnSuccessListener(d -> {

                    edtNombre.setText(d.getString("nombre"));

                    String rol = d.getString("rol");
                    String equipoId = d.getString("equipoId");

                    if (!"jugador".equals(rol) || equipoId == null) {
                        layoutAlias.setVisibility(View.GONE);
                        return;
                    }

                    layoutAlias.setVisibility(View.VISIBLE);

                    // 🔥 LEER ALIAS DESDE MIEMBROS
                    db.collection("equipos")
                            .document(equipoId)
                            .collection("miembros")
                            .document(u.getUid())
                            .get()
                            .addOnSuccessListener(mDoc -> {
                                edtAlias.setText(mDoc.getString("alias"));
                            });
                });
    }



    private void cerrarSesion() {
        auth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity();
    }

    private void confirmarEliminarCuenta() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("Esta acción no se puede deshacer")
                .setPositiveButton("Eliminar", (d, w) -> {
                    FirebaseUser u = auth.getCurrentUser();
                    if (u == null) return;
                    db.collection("usuarios").document(u.getUid()).delete();
                    u.delete();
                    cerrarSesion();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void toast(String t) {
        Toast.makeText(this, t, Toast.LENGTH_SHORT).show();
    }
    private void cargarFotoPerfil() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("usuarios").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {

                    String fotoFirestore = doc.getString("fotoUrl");

                    // 1️⃣ FOTO SUBIDA POR USUARIO
                    if (fotoFirestore != null && !fotoFirestore.isEmpty()) {
                        Glide.with(this)
                                .load(fotoFirestore)
                                .circleCrop()
                                .into(imgPerfil);
                        return;
                    }

                    // 2️⃣ FOTO GOOGLE
                    if (user.getPhotoUrl() != null) {
                        Glide.with(this)
                                .load(user.getPhotoUrl())
                                .circleCrop()
                                .into(imgPerfil);
                        return;
                    }

                    // 3️⃣ DEFAULT
                    imgPerfil.setImageResource(R.drawable.userlogo);
                })
                .addOnFailureListener(e ->
                        imgPerfil.setImageResource(R.drawable.userlogo)
                );
    }
    private void confirmarGuardarAlias() {

        String alias = edtAlias.getText().toString().trim();
        if (alias.isEmpty()) {
            toast("El alias no puede estar vacío");
            return;
        }

        String uid = auth.getUid();
        if (uid == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Cambiar alias")
                .setMessage("¿Deseas guardar este alias?")
                .setPositiveButton("Guardar", (d, w) -> {

                    db.collection("usuarios")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(uDoc -> {

                                String equipoId = uDoc.getString("equipoId");
                                if (equipoId == null) return;

                                // 🔥 GUARDAR DONDE REALMENTE SE USA
                                db.collection("equipos")
                                        .document(equipoId)
                                        .collection("miembros")
                                        .document(uid)
                                        .update("alias", alias)
                                        .addOnSuccessListener(a ->
                                                toast("Alias actualizado correctamente")
                                        );
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }




}