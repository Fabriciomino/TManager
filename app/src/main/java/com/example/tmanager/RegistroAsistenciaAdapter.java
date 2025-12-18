package com.example.tmanager;

import android.content.Context;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class RegistroAsistenciaAdapter
        extends RecyclerView.Adapter<RegistroAsistenciaAdapter.VH> {

    public interface OnEventoClick {
        void onClick(EventModel ev);
    }

    private Context ctx;
    private List<EventModel> lista;
    private OnEventoClick listener;

    public RegistroAsistenciaAdapter(Context ctx, List<EventModel> lista, OnEventoClick l) {
        this.ctx = ctx;
        this.lista = lista;
        this.listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(ctx)
                .inflate(R.layout.item_evento_asistencia, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {

        EventModel ev = lista.get(i);

        h.titulo.setText(ev.isEsPartido()
                ? "Partido"
                : ev.getTitulo());

        h.fecha.setText(ev.getFecha() + " · " + ev.getHora());

        int si = 0, no = 0, pend = 0;

        Map<String, String> asist = ev.getAsistencias();
        List<String> conv = ev.getConvocados();

        if (conv != null) {
            for (String uid : conv) {
                String estado = asist != null ? asist.get(uid) : null;
                if ("si".equals(estado)) si++;
                else if ("no".equals(estado)) no++;
                else pend++;
            }
        }

        h.contadores.setText("✔ " + si + "   ✖ " + no + "   ⏳ " + pend);

        h.itemView.setOnClickListener(v -> listener.onClick(ev));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView titulo, fecha, contadores;

        VH(View v) {
            super(v);
            titulo = v.findViewById(R.id.txtTitulo);
            fecha = v.findViewById(R.id.txtFecha);
            contadores = v.findViewById(R.id.txtContadores);
        }
    }
}
