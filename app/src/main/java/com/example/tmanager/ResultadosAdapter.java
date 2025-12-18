package com.example.tmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultadosAdapter extends RecyclerView.Adapter<ResultadosAdapter.Holder> {

    private Context context;
    private List<EventModel> lista;

    // posición actualmente expandida
    private int expandedPosition = -1;

    // mapa uid -> nombre jugador
    private Map<String, String> uidToName = new HashMap<>();

    public ResultadosAdapter(Context context, List<EventModel> lista) {
        this.context = context;
        this.lista = lista;
    }

    // se llama desde ResultadosActivity
    public void setUidToName(Map<String, String> map) {
        if (map != null) this.uidToName = map;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_evento_resultado, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {

        EventModel e = lista.get(pos);

        // =====================================================
        //                     CABECERA
        // =====================================================

        // Título
        String titulo = e.isEsLocal()
                ? e.getMiEquipo() + " vs " + e.getRival()
                : e.getRival() + " vs " + e.getMiEquipo();
        h.txtTitulo.setText(titulo);

        // Marcador
        if (e.getResultadoLocal() != null && e.getResultadoRival() != null) {
            h.txtMarcador.setText(e.getResultadoLocal() + " - " + e.getResultadoRival());
        } else {
            h.txtMarcador.setText("–");
        }

        // Fecha
        h.txtFecha.setText(e.getFecha() + " · " + e.getHora());

        // Escudos
        if (e.isEsLocal()) {
            loadImage(h.imgLeft, e.getMiEquipoLogo());
            loadImage(h.imgRight, e.getRivalEscudoUrl());
        } else {
            loadImage(h.imgLeft, e.getRivalEscudoUrl());
            loadImage(h.imgRight, e.getMiEquipoLogo());
        }

        // =====================================================
        //                  EXPAND / COLLAPSE
        // =====================================================

        boolean isExpanded = pos == expandedPosition;
        h.layoutDetalle.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        h.itemView.setOnClickListener(v -> {
            expandedPosition = isExpanded ? -1 : pos;
            notifyDataSetChanged();
        });

        // =====================================================
        //                  DETALLE PARTIDO
        // =====================================================

        if (isExpanded) {

            // ---------- GOLES ----------
            h.containerGoles.removeAllViews();

            if (e.getGoles() != null) {
                for (Map<String, Object> g : e.getGoles()) {

                    View row = LayoutInflater.from(context)
                            .inflate(R.layout.item_gol, h.containerGoles, false);

                    TextView txt = row.findViewById(R.id.txtGol);

                    String goleadorUid = (String) g.get("scorerUid");
                    String asistUid = (String) g.get("assistantUid");

                    String texto = getNombre(goleadorUid);

                    if (asistUid != null) {
                        texto += " (Asist. " + getNombre(asistUid) + ")";
                    }

                    txt.setText(texto);
                    h.containerGoles.addView(row);
                }
            }

            // ---------- TARJETAS ----------
            h.containerTarjetas.removeAllViews();

            if (e.getTarjetas() != null) {
                for (Map<String, Object> t : e.getTarjetas()) {

                    String tipo = (String) t.get("tipo");

                    int layout = "roja".equalsIgnoreCase(tipo)
                            ? R.layout.item_tarjeta_roja
                            : R.layout.item_tarjeta_amarilla;

                    View row = LayoutInflater.from(context)
                            .inflate(layout, h.containerTarjetas, false);

                    TextView txt = row.findViewById(R.id.txtTarjeta);
                    txt.setText(getNombre((String) t.get("playerUid")));

                    h.containerTarjetas.addView(row);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    // =====================================================
    //                    HELPERS
    // =====================================================

    private void loadImage(ImageView img, String url) {
        if (url != null) {
            Glide.with(context).load(url).circleCrop().into(img);
        } else {
            img.setImageResource(R.drawable.circle_empty);
        }
    }

    private String getNombre(String uid) {
        if (uid == null) return "";
        return uidToName.containsKey(uid) ? uidToName.get(uid) : uid;
    }

    // =====================================================
    //                    VIEW HOLDER
    // =====================================================

    static class Holder extends RecyclerView.ViewHolder {

        ImageView imgLeft, imgRight;
        TextView txtTitulo, txtMarcador, txtFecha;

        LinearLayout layoutDetalle;
        LinearLayout containerGoles;
        LinearLayout containerTarjetas;

        Holder(View v) {
            super(v);

            imgLeft = v.findViewById(R.id.imgEscudoLeft);
            imgRight = v.findViewById(R.id.imgEscudoRight);
            txtTitulo = v.findViewById(R.id.txtTitulo);
            txtMarcador = v.findViewById(R.id.txtMarcador);
            txtFecha = v.findViewById(R.id.txtFechaHora);

            layoutDetalle = v.findViewById(R.id.layoutDetalle);
            containerGoles = v.findViewById(R.id.containerGoles);
            containerTarjetas = v.findViewById(R.id.containerTarjetas);
        }
    }
}
