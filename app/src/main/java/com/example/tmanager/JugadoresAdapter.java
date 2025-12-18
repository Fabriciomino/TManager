package com.example.tmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

public class JugadoresAdapter extends RecyclerView.Adapter<JugadoresAdapter.ViewHolder> {

    private Context context;
    private List<Map<String, Object>> jugadores;

    public JugadoresAdapter(Context context, List<Map<String, Object>> jugadores) {
        this.context = context;
        this.jugadores = jugadores;
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

        // Texto: Alias #dorsal (pos)
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
    }

    @Override
    public int getItemCount() {
        return jugadores.size();
    }

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
