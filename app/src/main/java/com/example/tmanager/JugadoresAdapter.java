package com.example.tmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class JugadoresAdapter extends RecyclerView.Adapter<JugadoresAdapter.ViewHolder> {

    private Context context;
    private List<Map<String, Object>> jugadores;

    // 🔥 NUEVO (no rompe nada)
    private boolean esEntrenador = false;
    private String equipoId;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public JugadoresAdapter(Context context, List<Map<String, Object>> jugadores) {
        this.context = context;
        this.jugadores = jugadores;
    }

    public JugadoresAdapter(Context context,
                            List<Map<String, Object>> jugadores,
                            String equipoId,
                            boolean esEntrenador) {
        this.context = context;
        this.jugadores = jugadores;
        this.equipoId = equipoId;
        this.esEntrenador = esEntrenador;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_jugador, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {

        Map<String, Object> j = jugadores.get(position);

        String alias = (String) j.get("alias");
        String dorsal = j.get("dorsal") != null ? String.valueOf(j.get("dorsal")) : "";
        String posicion = (String) j.get("posicion");
        String foto = (String) j.get("fotoUrl");

        // Alias #dorsal (pos)
        String texto = alias != null ? alias : "Jugador";

        if (!dorsal.isEmpty())
            texto += " #" + dorsal;

        if (posicion != null)
            texto += " (" + posicion + ")";

        h.txtJugador.setText(texto);

        if (foto != null && !foto.isEmpty()) {
            Glide.with(context)
                    .load(foto)
                    .circleCrop()
                    .into(h.imgJugador);
        } else {
            h.imgJugador.setImageResource(android.R.drawable.ic_menu_camera);
        }

        // ELIMINAR JUGADOR (SOLO ENTRENADOR)
        if (esEntrenador && equipoId != null) {
            h.itemView.setOnClickListener(v ->
                    confirmarEliminar(j)
            );
        }
    }

    @Override
    public int getItemCount() {
        return jugadores.size();
    }


    private void confirmarEliminar(Map<String, Object> jugador) {

        String uidJugador = (String) jugador.get("uid");
        String alias = (String) jugador.get("alias");

        if (uidJugador == null) return;

        new AlertDialog.Builder(context)
                .setTitle("Eliminar jugador")
                .setMessage("¿Seguro que quieres eliminar a " +
                        (alias != null ? alias : "este jugador") +
                        " del equipo?")
                .setPositiveButton("Eliminar", (d, w) ->
                        eliminarJugador(uidJugador)
                )
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void eliminarJugador(String uidJugador) {

        // 1️⃣ BORRAR DE MIEMBROS
        db.collection("equipos")
                .document(equipoId)
                .collection("miembros")
                .document(uidJugador)
                .delete()
                .addOnSuccessListener(a -> {

                    // 2️⃣ LIMPIAR USUARIO
                    db.collection("usuarios")
                            .document(uidJugador)
                            .update(
                                    "equipoId", null,
                                    "rol", "none"
                            )
                            .addOnSuccessListener(b ->
                                    Toast.makeText(context,
                                            "Jugador eliminado del equipo",
                                            Toast.LENGTH_SHORT).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(context,
                                "Error eliminando jugador",
                                Toast.LENGTH_SHORT).show()
                );
    }


    // =====================================================
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgJugador;
        TextView txtJugador;

        ViewHolder(View itemView) {
            super(itemView);
            imgJugador = itemView.findViewById(R.id.imgJugador);
            txtJugador = itemView.findViewById(R.id.txtJugador);
        }
    }
}
