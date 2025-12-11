package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {

    TextView txtBienvenido, txtNombreUsuario;
    LinearLayout btnCrearEquipo, btnUnirseEquipo;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        txtBienvenido = findViewById(R.id.txtBienvenido);
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario);
        btnCrearEquipo = findViewById(R.id.btnCrearEquipo);
        btnUnirseEquipo = findViewById(R.id.btnUnirseEquipo);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String nombre = user.getDisplayName();
            if (nombre == null || nombre.isEmpty()) {
                // si no tiene displayName, usamos el extra de registro
                String nombreIntent = getIntent().getStringExtra("nombre_usuario");
                nombre = (nombreIntent != null) ? nombreIntent : user.getEmail();
            }
            txtNombreUsuario.setText(nombre);
        }

        btnCrearEquipo.setOnClickListener(v -> startActivity(new Intent(this, CrearEquipoActivity.class)));
        btnUnirseEquipo.setOnClickListener(v -> startActivity(new Intent(this, UnirseEquipoActivity.class)));
    }
}
