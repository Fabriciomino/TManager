package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText edtNombre, edtEmail, edtPassword, edtPassword2;
    ImageView btnGoogle, btnVerPass1, btnVerPass2;
    Button btnRegistrar;
    TextView txtIrLogin;

    TextView rule1, rule2, rule3, rule4, rule5;

    boolean passVisible1 = false;
    boolean passVisible2 = false;

    FirebaseAuth auth;
    FirebaseFirestore db;
    GoogleSignInClient googleClient;

    private static final int RC_GOOGLE = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // UI
        edtNombre = findViewById(R.id.edtNombre);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtPassword2 = findViewById(R.id.edtPassword2);

        btnVerPass1 = findViewById(R.id.btnVerPassword1);
        btnVerPass2 = findViewById(R.id.btnVerPassword2);

        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnGoogle = findViewById(R.id.btnGoogle);
        txtIrLogin = findViewById(R.id.txtIrLogin);

        rule1 = findViewById(R.id.rule1);
        rule2 = findViewById(R.id.rule2);
        rule3 = findViewById(R.id.rule3);
        rule4 = findViewById(R.id.rule4);
        rule5 = findViewById(R.id.rule5);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        configurarGoogle();
        configurarPasswordListeners();

        btnRegistrar.setOnClickListener(v -> registrarManual());
        btnGoogle.setOnClickListener(v -> iniciarGoogle());
        txtIrLogin.setOnClickListener(v -> irALogin());

        btnVerPass1.setOnClickListener(v -> togglePassword(edtPassword, btnVerPass1, true));
        btnVerPass2.setOnClickListener(v -> togglePassword(edtPassword2, btnVerPass2, false));
    }

    // ------------------------------------------------------------
    // VALIDACIÓN VISUAL DE CONTRASEÑA
    // ------------------------------------------------------------
    private void configurarPasswordListeners() {
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) { validarPassword(s.toString()); }
            @Override public void afterTextChanged(Editable editable) {}
        });
    }

    private void validarPassword(String pass) {
        rule1.setTextColor(pass.length() >= 8 ? 0xFF00E676 : 0xFFFFFFFF);
        rule2.setTextColor(pass.matches(".*[A-Z].*") ? 0xFF00E676 : 0xFFFFFFFF);
        rule3.setTextColor(pass.matches(".*[a-z].*") ? 0xFF00E676 : 0xFFFFFFFF);
        rule4.setTextColor(pass.matches(".*[0-9].*") ? 0xFF00E676 : 0xFFFFFFFF);
        rule5.setTextColor(pass.matches(".*[@#_$%^&+=!¿?.-].*") ? 0xFF00E676 : 0xFFFFFFFF);
    }

    // ------------------------------------------------------------
    // MOSTRAR / OCULTAR CONTRASEÑA
    // ------------------------------------------------------------
    private void togglePassword(EditText editText, ImageView icon, boolean first) {
        boolean visible = first ? passVisible1 : passVisible2;

        if (visible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            icon.setImageResource(R.drawable.ic_eye_closed);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            icon.setImageResource(R.drawable.ic_eye_open);
        }

        editText.setSelection(editText.getText().length());

        if (first) passVisible1 = !passVisible1;
        else passVisible2 = !passVisible2;
    }

    // ------------------------------------------------------------
    // GOOGLE SIGN IN
    // ------------------------------------------------------------
    private void configurarGoogle() {
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        googleClient = GoogleSignIn.getClient(this, gso);
    }

    private void iniciarGoogle() {
        Intent intent = googleClient.getSignInIntent();
        startActivityForResult(intent, RC_GOOGLE);
    }

    // ------------------------------------------------------------
    // REGISTRO MANUAL
    // ------------------------------------------------------------
    private void registrarManual() {
        String nombre = edtNombre.getText().toString().trim();
        String correo = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();
        String pass2 = edtPassword2.getText().toString().trim();

        if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty() || pass2.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(pass2)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(correo, pass)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) guardarUsuarioEnFirestore(user.getUid(), nombre, correo);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error registro: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ------------------------------------------------------------
    // GOOGLE RESULT
    // ------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account == null) return;

                AuthCredential credential =
                        GoogleAuthProvider.getCredential(account.getIdToken(), null);

                auth.signInWithCredential(credential)
                        .addOnSuccessListener(r -> {
                            FirebaseUser user = auth.getCurrentUser();
                            guardarUsuarioEnFirestore(
                                    user.getUid(),
                                    user.getDisplayName(),
                                    user.getEmail()
                            );
                        });

            } catch (Exception e) {
                Toast.makeText(this, "Error Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ------------------------------------------------------------
    // GUARDAR EN FIRESTORE
    // ------------------------------------------------------------
    private void guardarUsuarioEnFirestore(String uid, String nombre, String correo) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("nombre", nombre);
        user.put("email", correo);
        user.put("rol", "none");
        user.put("equipoId", null);
        user.put("creado", new Date());

        db.collection("usuarios").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Intent i = new Intent(this, WelcomeActivity.class);
                    i.putExtra("nombre_usuario", nombre);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error guardando usuario", Toast.LENGTH_SHORT).show());
    }

    private void irALogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
