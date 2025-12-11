package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnEntrar;
    ImageView btnGoogle, btnVerPassword;
    TextView txtIrRegistro;

    FirebaseAuth auth;
    GoogleSignInClient googleClient;
    private static final int RC_GOOGLE = 3000;

    boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnGoogle = findViewById(R.id.btnGoogleLogin);
        btnVerPassword = findViewById(R.id.btnVerPassword);
        txtIrRegistro = findViewById(R.id.txtIrRegistro);

        auth = FirebaseAuth.getInstance();

        configurarGoogle();

        btnEntrar.setOnClickListener(v -> loginManual());
        btnGoogle.setOnClickListener(v -> iniciarGoogle());
        btnVerPassword.setOnClickListener(v -> togglePassword());
        txtIrRegistro.setOnClickListener(v -> irARegistro());
    }

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

    private void loginManual() {
        String mail = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (mail.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(mail, pass)
                .addOnSuccessListener(authResult -> {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error login: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void togglePassword() {
        if (passwordVisible) {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnVerPassword.setImageResource(R.drawable.ic_eye_closed);
        } else {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnVerPassword.setImageResource(R.drawable.ic_eye_open);
        }

        edtPassword.setSelection(edtPassword.getText().length());
        passwordVisible = !passwordVisible;
    }

    private void irARegistro() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account == null) {
                    Toast.makeText(this, "Google cancelado", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                auth.signInWithCredential(credential)
                        .addOnSuccessListener(authResult -> {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error auth Google: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );

            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
