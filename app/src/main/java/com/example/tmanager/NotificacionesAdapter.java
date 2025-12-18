package com.example.tmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificacionesAdapter
        extends RecyclerView.Adapter<NotificacionesAdapter.ViewHolder> {

    private Context context;
    private List<NotificacionModel> lista;

    private OnNotificacionClickListener listener;

    public interface OnNotificacionClickListener {
        void onClick(NotificacionModel n);
    }

    public void setOnNotificacionClickListener(OnNotificacionClickListener l) {
        this.listener = l;
    }

    public NotificacionesAdapter(Context context, List<NotificacionModel> lista) {
        this.context = context;
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_notificacion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {

        NotificacionModel n = lista.get(position);

        h.txtTitulo.setText(n.titulo);
        h.txtMensaje.setText(n.mensaje);
        h.txtFecha.setText(formatearFecha(n.timestamp));

        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(n);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitulo, txtMensaje, txtFecha;

        ViewHolder(View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtMensaje = itemView.findViewById(R.id.txtMensaje);
            txtFecha = itemView.findViewById(R.id.txtFecha);
        }
    }

    // ----------------------------------------
    // FECHA RELATIVA
    // ----------------------------------------
    private String formatearFecha(Timestamp ts) {

        if (ts == null) return "";

        long diff = System.currentTimeMillis() - ts.toDate().getTime();

        long min = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (min < 1) return "Ahora";
        if (min < 60) return "Hace " + min + " min";

        long h = TimeUnit.MILLISECONDS.toHours(diff);
        if (h < 24) return "Hace " + h + " h";

        long d = TimeUnit.MILLISECONDS.toDays(diff);
        return "Hace " + d + " días";
    }
}
