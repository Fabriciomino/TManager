package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔑 COMPROBAR SESIÓN ANTES DE MOSTRAR NADA
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // ✅ SESIÓN INICIADA → IR A EVENTOS
            irAMainEventos();
            return; // ⛔ no cargar layout
        }

        // ❌ NO HAY SESIÓN → MOSTRAR FIRST
        setContentView(R.layout.activity_first);

        TextView iniciar = findViewById(R.id.txtIniciar);
        TextView registrar = findViewById(R.id.txtRegistrar);

        iniciar.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

        registrar.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void irAMainEventos() {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("open", "eventos");
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
