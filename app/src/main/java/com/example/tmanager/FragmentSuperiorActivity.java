package com.example.tmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FragmentSuperiorActivity extends Fragment {

    TextView txtNombreEquipo;
    ImageView imgLogo, imgPerfil;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_superior, container, false);

        txtNombreEquipo = view.findViewById(R.id.txtNombreEquipo);
        imgLogo = view.findViewById(R.id.imgLogo);
        imgPerfil = view.findViewById(R.id.imgPerfil);

        cargarFotoUsuario();
        cargarDatosEquipo();

        return view;
    }

    // --------------------------------------------------------
    // FOTO DEL PERFIL (Google o icono por defecto)
    // --------------------------------------------------------
    private void cargarFotoUsuario() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.getPhotoUrl() != null) {
            // Usuario de Google → cargar su foto
            Glide.with(requireContext())
                    .load(user.getPhotoUrl())
                    .circleCrop()
                    .into(imgPerfil);
        } else {
            // Si no hay foto → icono por defecto
            imgPerfil.setImageResource(R.drawable.userlogo);
        }
    }

    // --------------------------------------------------------
    // CARGAR NOMBRE Y LOGO DEL EQUIPO
    // --------------------------------------------------------
    private void cargarDatosEquipo() {

        // 1️⃣ Comprobar SharedPreferences (más rápido)
        String nombreLocal = requireActivity().getSharedPreferences("EQUIPO", getContext().MODE_PRIVATE)
                .getString("nombre", null);
        String logoLocal = requireActivity().getSharedPreferences("EQUIPO", getContext().MODE_PRIVATE)
                .getString("logoUrl", null);

        if (nombreLocal != null) {
            txtNombreEquipo.setText(nombreLocal);
        }

        if (logoLocal != null) {
            Glide.with(requireContext())
                    .load(logoLocal)
                    .circleCrop()
                    .into(imgLogo);
        }

        // 2️⃣ Comprobar Firestore si no está en preferencias
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) return;

        db.collection("usuarios").document(uid).get().addOnSuccessListener(doc -> {

            if (!doc.exists()) return;

            String equipoId = doc.getString("equipoId");
            if (equipoId == null) return;

            db.collection("equipos").document(equipoId).get().addOnSuccessListener(equipo -> {

                if (!equipo.exists()) return;

                String nombre = equipo.getString("nombre");
                String logo = equipo.getString("logoUrl");

                if (nombre != null) {
                    // Limitar el nombre a máximo 15 caracteres
                    if (nombre.length() > 15) {
                        nombre = nombre.substring(0, 15) + "…";
                    }
                    txtNombreEquipo.setText(nombre);
                }

                if (logo != null) {
                    Glide.with(requireContext())
                            .load(logo)
                            .circleCrop()
                            .into(imgLogo);
                }

            });
        });
    }
}
