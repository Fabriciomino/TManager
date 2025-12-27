package com.example.tmanager;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MensajesAdapter extends RecyclerView.Adapter<MensajesAdapter.VH> {

    Context ctx;
    List<MensajeModel> lista;

    public MensajesAdapter(Context c, List<MensajeModel> l) {
        ctx = c;
        lista = l;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup p, int v) {
        return new VH(LayoutInflater.from(ctx)
                .inflate(R.layout.item_mensaje, p, false));
    }

    @Override
    public void onBindViewHolder(VH h, int pos) {
        MensajeModel m = lista.get(pos);

        h.txtNombre.setText(m.nombre);
        h.txtMensaje.setText(m.texto);
        h.txtHora.setText(formatear(m.timestamp));

        if (m.fotoUrl != null)
            Glide.with(ctx).load(m.fotoUrl).circleCrop().into(h.img);
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView txtNombre, txtMensaje, txtHora;
        VH(View v) {
            super(v);
            img = v.findViewById(R.id.imgUsuario);
            txtNombre = v.findViewById(R.id.txtNombre);
            txtMensaje = v.findViewById(R.id.txtMensaje);
            txtHora = v.findViewById(R.id.txtHora);
        }
    }

    private String formatear(Timestamp t) {
        if (t == null) return "";
        long min = TimeUnit.MILLISECONDS.toMinutes(
                System.currentTimeMillis() - t.toDate().getTime());
        if (min < 1) return "Ahora";
        if (min < 60) return min + " min";
        return TimeUnit.MILLISECONDS.toHours(min * 60000) + " h";
    }
}
