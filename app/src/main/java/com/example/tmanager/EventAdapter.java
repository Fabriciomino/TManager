package com.example.tmanager;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventModel> listaEventos;
    private Context context;

    public interface OnEventClickListener {
        void onEventClick(int position);
    }

    private OnEventClickListener listener;

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    public EventAdapter(Context context, List<EventModel> listaEventos) {
        this.context = context;
        this.listaEventos = listaEventos;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evento, parent, false);
        return new EventViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel evento = listaEventos.get(position);

        holder.fechaHora.setText(evento.getFecha() + " • " + evento.getHora());
        holder.lugar.setText(evento.getLugar());

        if (!evento.isEsPartido()) {
            holder.titulo.setText(evento.getTitulo());
            holder.imgEscudoLeft.setVisibility(View.GONE);
            holder.imgEscudoRight.setVisibility(View.GONE);
            return;
        }

        holder.imgEscudoLeft.setVisibility(View.VISIBLE);
        holder.imgEscudoRight.setVisibility(View.VISIBLE);

        String titulo;

        String miLogo = evento.getMiEquipoLogo(); // puede ser null
        String rivalLogo = evento.getRivalEscudoUrl(); // puede ser null

        if (evento.isEsLocal()) {
            titulo = evento.getMiEquipo() + " vs " + evento.getRival();

            // izquierda = mi equipo
            if (miLogo != null) {
                Glide.with(context).load(miLogo).circleCrop().into(holder.imgEscudoLeft);
            } else {
                holder.imgEscudoLeft.setImageResource(R.drawable.circle_empty);
            }

            // derecha = rival
            if (rivalLogo != null) {
                Glide.with(context).load(rivalLogo).circleCrop().into(holder.imgEscudoRight);
            } else {
                holder.imgEscudoRight.setImageResource(R.drawable.circle_empty);
            }

        } else {
            titulo = evento.getRival() + " vs " + evento.getMiEquipo();

            // izquierda = rival
            if (rivalLogo != null) {
                Glide.with(context).load(rivalLogo).circleCrop().into(holder.imgEscudoLeft);
            } else {
                holder.imgEscudoLeft.setImageResource(R.drawable.circle_empty);
            }

            // derecha = mi equipo
            if (miLogo != null) {
                Glide.with(context).load(miLogo).circleCrop().into(holder.imgEscudoRight);
            } else {
                holder.imgEscudoRight.setImageResource(R.drawable.circle_empty);
            }
        }

        holder.titulo.setText(titulo);
    }

    @Override
    public int getItemCount() {
        return listaEventos.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, fechaHora, lugar;
        ImageView imgEscudoLeft, imgEscudoRight;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            titulo = itemView.findViewById(R.id.txtTitulo);
            fechaHora = itemView.findViewById(R.id.txtFechaHora);
            lugar = itemView.findViewById(R.id.txtLugar);

            imgEscudoLeft = itemView.findViewById(R.id.imgEscudoLeft);
            imgEscudoRight = itemView.findViewById(R.id.imgEscudoRight);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(getAdapterPosition());
                }
            });
        }
    }
}
