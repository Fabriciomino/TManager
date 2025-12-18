package com.example.tmanager;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

        // Cargar fragment superior (si lo usas)
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentSuperior, new FragmentSuperiorActivity())
                .commit();

        // Bottom nav
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        String open = getIntent().getStringExtra("open");

        if ("eventos".equals(open)) {
            bottomNavigationView.setSelectedItemId(R.id.btnEventos);
            loadFragment(new EventosFragment());
        } else {
            bottomNavigationView.setSelectedItemId(R.id.btnEventos);
            loadFragment(new EventosFragment());
        }


        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            if (item.getItemId() == R.id.btnEventos) fragment = new EventosFragment();
            else if (item.getItemId() == R.id.btnEquipo) fragment = new EquipoFragment();
            else if (item.getItemId() == R.id.btnPerfil) fragment = new MiPerfilFragment();

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });

    }

    private void loadFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }


}

