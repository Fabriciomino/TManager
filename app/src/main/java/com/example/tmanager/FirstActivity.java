package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        TextView iniciar = findViewById(R.id.txtIniciar);
        TextView registrar = findViewById(R.id.txtRegistrar);

        iniciar.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        registrar.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}

