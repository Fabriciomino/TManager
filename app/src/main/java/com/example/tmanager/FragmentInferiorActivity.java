package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentInferiorActivity extends Fragment {

    private LinearLayout btnEventos, btnEquipo, btnPerfil;
    private ImageView iconEventos, iconEquipo, iconPerfil;
    private TextView txtEventos, txtEquipo, txtPerfil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_inferior, container, false);

        // Vincular botones e iconos
        btnEventos = view.findViewById(R.id.btnEventos);
        btnEquipo = view.findViewById(R.id.btnEquipo);
        btnPerfil = view.findViewById(R.id.btnPerfil);

        iconEventos = view.findViewById(R.id.iconEventos);
        iconEquipo = view.findViewById(R.id.iconEquipo);
        iconPerfil = view.findViewById(R.id.iconPerfil);

        txtEventos = btnEventos.findViewById(R.id.txtEventos);
        txtEquipo = btnEquipo.findViewById(R.id.txtEquipo);
        txtPerfil = btnPerfil.findViewById(R.id.txtPerfil);

        // Detectar actividad actual para marcar botón seleccionado
        String currentActivity = getActivity().getClass().getSimpleName();

        resetButtons(); // primero poner todos deseleccionados

        switch (currentActivity) {
            case "EventosActivity":
                selectButton(btnEventos, iconEventos, txtEventos);
                break;
            case "EquipoActivity":
                selectButton(btnEquipo, iconEquipo, txtEquipo);
                break;
            case "MiPerfilActivity":
                selectButton(btnPerfil, iconPerfil, txtPerfil);
                break;
        }

        // Click listeners
        btnEventos.setOnClickListener(v -> {
            if (!currentActivity.equals("EventosActivity")) {
                startActivity(new Intent(getActivity(), EventosFragment.class));
                getActivity().finish();
            }
        });

        btnEquipo.setOnClickListener(v -> {
            if (!currentActivity.equals("EquipoActivity")) {
                startActivity(new Intent(getActivity(), EquipoFragment.class));
                getActivity().finish();
            }
        });

        btnPerfil.setOnClickListener(v -> {
            if (!currentActivity.equals("MiPerfilActivity")) {
                startActivity(new Intent(getActivity(), MiPerfilFragment.class));
                getActivity().finish();
            }
        });

        return view;
    }

    // Función para resetear colores a no seleccionados
    private void resetButtons() {
        // Colores por defecto
        int colorDefault = 0xFF0D0D0D; // negro oscuro

        txtEventos.setTextColor(colorDefault);
        txtEquipo.setTextColor(colorDefault);
        txtPerfil.setTextColor(colorDefault);

        iconEventos.setAlpha(0.5f);
        iconEquipo.setAlpha(0.5f);
        iconPerfil.setAlpha(0.5f);
    }

    // Función para seleccionar un botón
    private void selectButton(LinearLayout btn, ImageView icon, TextView txt) {
        txt.setTextColor(0xFF3F51B5); // color azul
        icon.setAlpha(1f);
    }
}
