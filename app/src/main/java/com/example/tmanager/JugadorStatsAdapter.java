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
import java.util.Locale;

public class JugadorStatsAdapter extends RecyclerView.Adapter<JugadorStatsAdapter.VH> {

    private Context ctx;
    private List<JugadorStats> lista;

    public JugadorStatsAdapter(Context ctx, List<JugadorStats> lista) {
        this.ctx = ctx;
        this.lista = lista;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(ctx)
                .inflate(R.layout.item_jugador_stats, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {

        JugadorStats j = lista.get(i);

        h.nombre.setText(j.alias + " #" + j.dorsal + " (" + j.posicion + ")");
        h.goles.setText(String.valueOf(j.goles));
        h.asist.setText(String.valueOf(j.asistencias));
        h.amar.setText(String.valueOf(j.amarillas));
        h.roja.setText(String.valueOf(j.rojas));
        h.pg.setText(String.valueOf(j.partidos));
        h.gp.setText(String.format(Locale.getDefault(), "%.2f", j.getGp()));

        if (j.fotoUrl != null)
            Glide.with(ctx).load(j.fotoUrl).circleCrop().into(h.img);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        ImageView img;
        TextView nombre, goles, asist, amar, roja, pg, gp;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.imgJugador);
            nombre = v.findViewById(R.id.txtNombre);
            goles = v.findViewById(R.id.txtGoles);
            asist = v.findViewById(R.id.txtAsist);
            amar = v.findViewById(R.id.txtAmar);
            roja = v.findViewById(R.id.txtRoja);
            pg = v.findViewById(R.id.txtPg);
            gp = v.findViewById(R.id.txtGp);
        }
    }
}
