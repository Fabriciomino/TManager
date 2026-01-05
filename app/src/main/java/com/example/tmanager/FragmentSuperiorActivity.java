package com.example.tmanager;

import android.content.Intent;
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
import com.google.firebase.firestore.ListenerRegistration;

public class FragmentSuperiorActivity extends Fragment {

    TextView txtNombreEquipo;
    ImageView imgLogo, imgPerfil;
    ImageView imgNotificaciones;
    TextView txtBadge;

    ListenerRegistration notifListener;


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
        imgNotificaciones = view.findViewById(R.id.imgNotificaciones);
        txtBadge = view.findViewById(R.id.txtBadge);

        cargarFotoUsuario();
        cargarDatosEquipo();

        imgNotificaciones.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), NotificacionesActivity.class));
        });

        return view;
    }


    // FOTO DEL PERFIL
    private void cargarFotoUsuario() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {
                        String fotoUrl = doc.getString("fotoUrl");

                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(fotoUrl)
                                    .circleCrop()
                                    .into(imgPerfil);
                            return;
                        }
                    }

                    if (user.getPhotoUrl() != null) {
                        Glide.with(requireContext())
                                .load(user.getPhotoUrl())
                                .circleCrop()
                                .into(imgPerfil);
                    } else {
                        imgPerfil.setImageResource(R.drawable.userlogo);
                    }
                })
                .addOnFailureListener(e ->
                        imgPerfil.setImageResource(R.drawable.userlogo)
                );
    }

    // CARGAR NOMBRE Y LOGO DEL EQUIPO
    private void cargarDatosEquipo() {

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

    private void escucharNotificaciones() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        notifListener = db.collection("notificaciones")
                .whereEqualTo("userUid", user.getUid())
                .whereEqualTo("leida", false)
                .addSnapshotListener((snap, e) -> {

                    if (snap == null) return;

                    int count = snap.size();

                    if (count > 0) {
                        txtBadge.setText(String.valueOf(count));
                        txtBadge.setVisibility(View.VISIBLE);
                    } else {
                        txtBadge.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarFotoUsuario();
        escucharNotificaciones();
    }
}
