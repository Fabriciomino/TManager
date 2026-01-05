package com.example.tmanager;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;

    private static final int REQ_NOTIF = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pedirPermisoNotificaciones();

        //  GUARDAR TOKEN FCM
        FCMUtil.guardarToken();

        fragmentManager = getSupportFragmentManager();

        // Fragment superior
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentSuperior, new FragmentSuperiorActivity())
                .commit();

        // Bottom navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setSelectedItemId(R.id.btnEventos);
        loadFragment(new EventosFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            if (item.getItemId() == R.id.btnEventos)
                fragment = new EventosFragment();
            else if (item.getItemId() == R.id.btnEquipo)
                fragment = new EquipoFragment();
            else if (item.getItemId() == R.id.btnPerfil)
                fragment = new MiPerfilFragment();

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

    // PERMISO NOTIFICACIONES ANDROID
    private void pedirPermisoNotificaciones() {

        if (Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIF
                );
            }
        }
    }
}
