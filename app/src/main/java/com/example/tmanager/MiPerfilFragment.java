package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MiPerfilFragment extends Fragment {

    RelativeLayout btnMiInformacion, btnDetallesEquipo, btnCentroAyuda;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_mi_perfil, container, false);

        // 🔗 VINCULAR VISTAS
        btnMiInformacion = v.findViewById(R.id.btnMiInformacion);
        btnDetallesEquipo = v.findViewById(R.id.btnDetallesEquipo);
        btnCentroAyuda = v.findViewById(R.id.btnCentroAyuda);

        // 🔐 COMPROBAR ROL DEL USUARIO
        comprobarRolUsuario();

        // 👉 CLICK MI INFORMACIÓN
        btnMiInformacion.setOnClickListener(view ->
                startActivity(new Intent(getContext(), MiInformacionActivity.class))
        );

        // 👉 CLICK DETALLES DE EQUIPO (solo entrenador)
        btnDetallesEquipo.setOnClickListener(view ->
                startActivity(new Intent(getContext(), DetallesEquipoActivity.class))
        );

        return v;
    }

    // =========================================================
    //   COMPROBAR ROL Y OCULTAR BOTÓN SI ES JUGADOR
    // =========================================================
    private void comprobarRolUsuario() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    String rol = doc.getString("rol");

                    // 👤 JUGADOR → NO VE DETALLES DE EQUIPO
                    if ("jugador".equals(rol)) {
                        btnDetallesEquipo.setVisibility(View.GONE);
                    }
                });
    }
}
