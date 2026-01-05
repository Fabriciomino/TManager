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

public class JugadoresAlineacionAdapter
        extends RecyclerView.Adapter<JugadoresAlineacionAdapter.VH> {

    public interface OnJugadorClickListener {
        void onJugadorClick(Jugador jugador);
    }

    private Context context;
    private List<Jugador> jugadores;
    private OnJugadorClickListener listener;

    public JugadoresAlineacionAdapter(Context context,
                                      List<Jugador> jugadores,
                                      OnJugadorClickListener listener) {
        this.context = context;
        this.jugadores = jugadores;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_jugador, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        Jugador j = jugadores.get(position);

        h.txtJugador.setText(j.nombre);

        if (j.fotoUrl != null && !j.fotoUrl.isEmpty()) {
            Glide.with(context)
                    .load(j.fotoUrl)
                    .circleCrop()
                    .into(h.imgJugador);
        } else {
            h.imgJugador.setImageResource(R.drawable.userlogo);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onJugadorClick(j);
        });
    }

    @Override
    public int getItemCount() {
        return jugadores.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        ImageView imgJugador;
        TextView txtJugador;

        VH(View v) {
            super(v);
            imgJugador = v.findViewById(R.id.imgJugador);
            txtJugador = v.findViewById(R.id.txtJugador);
        }
    }
}
