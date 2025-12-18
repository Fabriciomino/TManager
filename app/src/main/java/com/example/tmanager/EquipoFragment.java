package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

public class EquipoFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_equipo, container, false);

        // BOTÓN RESULTADOS
        RelativeLayout btnResultados = v.findViewById(R.id.btnResultados);
        btnResultados.setOnClickListener(view ->
                startActivity(new Intent(getContext(), ResultadosActivity.class))
        );

        // BOTÓN ESTADÍSTICAS
        RelativeLayout btnEstadisticas = v.findViewById(R.id.btnEstadisticas);
        btnEstadisticas.setOnClickListener(view ->
                startActivity(new Intent(getContext(), EstadisticasJugadoresActivity.class))
        );

        // BOTÓN REGISTRO DE ASISTENCIA
        RelativeLayout btnAsistencia = v.findViewById(R.id.btnAsistencia);
        btnAsistencia.setOnClickListener(view ->
                startActivity(new Intent(getContext(), RegistroAsistenciaActivity.class))
        );

        // BOTÓN REGISTRO DE ASISTENCIA
        RelativeLayout btnMiembros = v.findViewById(R.id.btnMiembros);
        btnMiembros.setOnClickListener(view ->
                startActivity(new Intent(getContext(), MiembrosActivity.class))
        );


        return v;
    }
}
