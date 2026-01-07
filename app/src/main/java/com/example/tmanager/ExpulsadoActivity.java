package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ExpulsadoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expulsado);

        Button btnUnirse = findViewById(R.id.btnUnirse);
        Button btnOtraCuenta = findViewById(R.id.btnOtraCuenta);

        btnUnirse.setOnClickListener(v -> {
            startActivity(new Intent(this, UnirseEquipoActivity.class));
            finish();
        });

        btnOtraCuenta.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }

    // ❌ No permitir volver atrás
    @Override
    public void onBackPressed() {
        // nada
    }
}
