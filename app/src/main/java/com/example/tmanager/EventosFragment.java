package com.example.tmanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.*;

public class EventosFragment extends Fragment {

    private RecyclerView recyclerEventos;
    private EventAdapter adapter;
    private List<EventModel> listaEventos = new ArrayList<>();

    private static final int PICK_IMAGE = 100;
    private Uri escudoUriSeleccionada = null;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String miEquipoNombre;
    private String miEquipoLogo;
    private String equipoId;

    EditText edtFechaDialog, edtHoraDialog;
    ImageView imgEscudoDialog;

    private ListenerRegistration eventosListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_eventos, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerEventos = vista.findViewById(R.id.recyclerEventos);
        recyclerEventos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(getContext(), listaEventos);
        recyclerEventos.setAdapter(adapter);

        adapter.setOnEventClickListener(this::mostrarOpcionesEvento);

        FloatingActionButton btnAgregar = vista.findViewById(R.id.btnAgregarEvento);
        btnAgregar.setOnClickListener(v -> mostrarDialogo(-1));

        cargarInfoEquipo();

        return vista;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (eventosListener != null) eventosListener.remove();
    }

    // ============================================================
    //                CARGAR EQUIPO + EVENTOS
    // ============================================================
    private void cargarInfoEquipo() {

        var prefs = requireActivity().getSharedPreferences("EQUIPO", getContext().MODE_PRIVATE);

        miEquipoNombre = prefs.getString("nombre", null);
        miEquipoLogo = prefs.getString("logoUrl", null);
        equipoId = prefs.getString("equipoId", null);

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            subscribeEventos(null);
            return;
        }

        if (miEquipoNombre != null && miEquipoLogo != null) {
            subscribeEventos(equipoId);
            return;
        }

        db.collection("usuarios").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        subscribeEventos(null);
                        return;
                    }

                    equipoId = doc.getString("equipoId");

                    if (equipoId == null) {
                        subscribeEventos(null);
                        return;
                    }

                    db.collection("equipos").document(equipoId)
                            .get()
                            .addOnSuccessListener(equipo -> {
                                miEquipoNombre = equipo.getString("nombre");
                                miEquipoLogo = equipo.getString("logoUrl");

                                prefs.edit()
                                        .putString("nombre", miEquipoNombre)
                                        .putString("logoUrl", miEquipoLogo)
                                        .putString("equipoId", equipoId)
                                        .apply();

                                subscribeEventos(equipoId);
                            });
                });
    }

    // ============================================================
    //               FIRESTORE EVENTOS EN TIEMPO REAL
    // ============================================================
    private void subscribeEventos(String equipoIdFilter) {

        Query query;

        if (equipoIdFilter != null) {
            query = db.collection("eventos")
                    .whereEqualTo("equipoId", equipoIdFilter)
                    .orderBy("fechaHoraTimestamp");
        } else {
            FirebaseUser u = auth.getCurrentUser();
            if (u == null) return;

            query = db.collection("eventos")
                    .whereEqualTo("creadorUid", u.getUid())
                    .orderBy("fechaHoraTimestamp");
        }

        eventosListener = query.addSnapshotListener((snap, err) -> {

            if (err != null) {
                err.printStackTrace();
                return;
            }

            listaEventos.clear();

            for (DocumentSnapshot ds : snap.getDocuments()) {

                EventModel ev = new EventModel();
                ev.setId(ds.getId());
                ev.setTitulo(ds.getString("titulo"));
                ev.setFecha(ds.getString("fecha"));
                ev.setHora(ds.getString("hora"));
                ev.setLugar(ds.getString("lugar"));
                ev.setEsPartido(Boolean.TRUE.equals(ds.getBoolean("esPartido")));
                ev.setMiEquipo(ds.getString("miEquipo"));
                ev.setMiEquipoLogo(ds.getString("miEquipoLogo"));
                ev.setRival(ds.getString("rival"));
                ev.setRivalEscudoUrl(ds.getString("rivalEscudoUrl"));
                ev.setEsLocal(Boolean.TRUE.equals(ds.getBoolean("esLocal")));

                List<String> conv = (List<String>) ds.get("convocados");
                ev.setConvocados(conv != null ? conv : new ArrayList<>());

                listaEventos.add(ev);
            }

            adapter.notifyDataSetChanged();
        });
    }

    // ============================================================
    //               MENÚ DE OPCIONES (EDITAR / BORRAR)
    // ============================================================
    private void mostrarOpcionesEvento(int pos) {

        EventModel ev = listaEventos.get(pos);

        new AlertDialog.Builder(getContext())
                .setTitle("Opciones del evento")
                .setItems(new String[]{"Editar", "Eliminar", "Cancelar"}, (d, w) -> {
                    if (w == 0) mostrarDialogo(pos);
                    else if (w == 1)
                        db.collection("eventos").document(ev.getId()).delete();
                })
                .show();
    }

    // ============================================================
    //              DIÁLOGO CREAR / EDITAR EVENTOS
    // ============================================================
    private void mostrarDialogo(int posEditing) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_evento, null);
        builder.setView(view);

        EditText edtTitulo = view.findViewById(R.id.edtTitulo);
        edtFechaDialog = view.findViewById(R.id.edtFecha);
        edtHoraDialog = view.findViewById(R.id.edtHora);
        EditText edtLugar = view.findViewById(R.id.edtLugar);
        EditText edtMiEquipo = view.findViewById(R.id.edtMiEquipo);
        EditText edtRival = view.findViewById(R.id.edtRival);

        Spinner spnTipo = view.findViewById(R.id.spinnerTipo);
        Spinner spnLocal = view.findViewById(R.id.spinnerLocalVisitante);
        LinearLayout layoutPartido = view.findViewById(R.id.layoutPartido);
        imgEscudoDialog = view.findViewById(R.id.imgEscudoRival);

        // ---- CONVOCATORIA ----
        Button btnConvocar = view.findViewById(R.id.btnConvocatoria);
        LinearLayout layoutConv = view.findViewById(R.id.layoutConvocatoria);
        LinearLayout container = view.findViewById(R.id.playersContainer);
        Button btnGuardarConv = view.findViewById(R.id.btnGuardarConvoc);

        edtMiEquipo.setText(miEquipoNombre);
        edtMiEquipo.setEnabled(false);

        spnTipo.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Evento normal", "Partido"}));

        spnLocal.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Local", "Visitante"}));

        EventModel editEvent = (posEditing != -1) ? listaEventos.get(posEditing) : null;

        List<String> convocadosPrevios = null;

        if (editEvent != null) {

            edtTitulo.setText(editEvent.getTitulo());
            edtFechaDialog.setText(editEvent.getFecha());
            edtHoraDialog.setText(editEvent.getHora());
            edtLugar.setText(editEvent.getLugar());

            if (editEvent.isEsPartido()) {
                spnTipo.setSelection(1);
                layoutPartido.setVisibility(View.VISIBLE);
                edtTitulo.setEnabled(false);
                edtRival.setText(editEvent.getRival());
                spnLocal.setSelection(editEvent.isEsLocal() ? 0 : 1);

                if (editEvent.getRivalEscudoUrl() != null) {
                    escudoUriSeleccionada = Uri.parse(editEvent.getRivalEscudoUrl());
                    Glide.with(getContext()).load(escudoUriSeleccionada).circleCrop().into(imgEscudoDialog);
                }
            }

            convocadosPrevios = new ArrayList<>(editEvent.getConvocados());
        }

        // Mostrar lista convocatoria
        List<String> finalConv = convocadosPrevios;
        btnConvocar.setOnClickListener(v -> {
            layoutConv.setVisibility(View.VISIBLE);
            llenarJugadoresConvocatoria(container, finalConv);
        });

        btnGuardarConv.setOnClickListener(v -> guardarConvocatoria(container, editEvent));

        // tipo evento
        spnTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                layoutPartido.setVisibility(pos == 1 ? View.VISIBLE : View.GONE);
                edtTitulo.setEnabled(pos == 0);
                if (pos == 1) edtTitulo.setText("Partido");
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        edtFechaDialog.setOnClickListener(v -> mostrarCalendario());
        edtHoraDialog.setOnClickListener(v -> mostrarReloj());

        imgEscudoDialog.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, PICK_IMAGE);
        });

        builder.setPositiveButton("Guardar", (d, w) -> guardarEvento(editEvent, edtTitulo, edtFechaDialog, edtHoraDialog, edtLugar, edtRival, spnTipo, spnLocal));
        builder.setNegativeButton("Cancelar", null);

        builder.show();
    }

    // ============================================================
    //                  GUARDAR EVENTO
    // ============================================================
    private void guardarEvento(EventModel editEvent,
                               EditText edtTitulo, EditText edtFecha, EditText edtHora, EditText edtLugar,
                               EditText edtRival, Spinner spnTipo, Spinner spnLocal) {

        String titulo = edtTitulo.getText().toString();
        String fecha = edtFecha.getText().toString();
        String hora = edtHora.getText().toString();
        String lugar = edtLugar.getText().toString();
        String rival = edtRival.getText().toString();

        boolean esPartido = spnTipo.getSelectedItemPosition() == 1;
        boolean esLocal = spnLocal.getSelectedItemPosition() == 0;

        Date fechaHora;
        try {
            fechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .parse(fecha + " " + hora);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Fecha/hora inválidas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (escudoUriSeleccionada != null && !escudoUriSeleccionada.toString().startsWith("http")) {
            uploadRivalLogoThenSave(escudoUriSeleccionada, titulo, fecha, hora, lugar,
                    esPartido, rival, esLocal, fechaHora, editEvent);
            return;
        }

        saveEventToFirestore(editEvent, titulo, fecha, hora, lugar,
                esPartido, rival, esLocal, fechaHora,
                editEvent != null ? editEvent.getRivalEscudoUrl() : null);
    }

    // ============================================================
    //               SUBIR FOTO RIVAL + GUARDAR EVENTO
    // ============================================================
    private void uploadRivalLogoThenSave(Uri uri, String titulo, String fecha, String hora, String lugar,
                                         boolean esPartido, String rival, boolean esLocal,
                                         Date fechaHora, @Nullable EventModel editEvent) {

        StorageReference ref =
                FirebaseStorage.getInstance().getReference("eventos/logos/" + UUID.randomUUID() + ".png");

        ref.putFile(uri)
                .addOnSuccessListener(a ->
                        ref.getDownloadUrl().addOnSuccessListener(url ->
                                saveEventToFirestore(editEvent, titulo, fecha, hora, lugar,
                                        esPartido, rival, esLocal, fechaHora, url.toString())))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error subiendo imagen", Toast.LENGTH_SHORT).show());
    }

    // ============================================================
    //                 GUARDAR EVENTO EN FIRESTORE
    // ============================================================
    private void saveEventToFirestore(EventModel editEvent, String titulo, String fecha, String hora,
                                      String lugar, boolean esPartido, String rival, boolean esLocal,
                                      Date fechaHora, String rivalEscudoUrl) {

        Map<String, Object> data = new HashMap<>();
        data.put("titulo", titulo);
        data.put("fecha", fecha);
        data.put("hora", hora);
        data.put("lugar", lugar);
        data.put("esPartido", esPartido);
        data.put("miEquipo", miEquipoNombre);
        data.put("miEquipoLogo", miEquipoLogo);
        data.put("rival", rival);
        data.put("rivalEscudoUrl", rivalEscudoUrl);
        data.put("esLocal", esLocal);
        data.put("equipoId", equipoId);
        data.put("creadorUid", auth.getCurrentUser().getUid());
        data.put("fechaHoraTimestamp", new Timestamp(fechaHora));

        if (editEvent == null) {
            db.collection("eventos")
                    .add(data)
                    .addOnSuccessListener(a ->
                            Toast.makeText(getContext(), "Evento creado", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("eventos").document(editEvent.getId())
                    .update(data)
                    .addOnSuccessListener(a ->
                            Toast.makeText(getContext(), "Evento actualizado", Toast.LENGTH_SHORT).show());
        }

        escudoUriSeleccionada = null;
    }

    // ============================================================
    //       CARGAR LISTA DE JUGADORES (alias + dorsal + posición)
    // ============================================================
    private void llenarJugadoresConvocatoria(LinearLayout container, @Nullable List<String> pre) {

        container.removeAllViews();

        if (equipoId == null) return;

        db.collection("equipos").document(equipoId)
                .get()
                .addOnSuccessListener(doc -> {

                    List<Map<String, Object>> jugadores = (List<Map<String, Object>>) doc.get("jugadores");
                    if (jugadores == null) return;

                    for (Map<String, Object> jug : jugadores) {

                        String uid = String.valueOf(jug.get("uid"));
                        String alias = (String) jug.get("alias");
                        String dorsal = String.valueOf(jug.get("dorsal"));
                        String pos = (String) jug.get("posicion");
                        String foto = (String) jug.get("fotoUrl");

                        String texto = alias + "  #" + dorsal + " (" + pos + ")";

                        View item = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_convocado, container, false);

                        TextView txt = item.findViewById(R.id.txtJugador);
                        CheckBox check = item.findViewById(R.id.checkJugador);
                        ImageView img = item.findViewById(R.id.imgJugador);

                        txt.setText(texto);
                        check.setTag(uid);

                        if (pre != null && pre.contains(uid)) check.setChecked(true);

                        if (foto != null)
                            Glide.with(getContext()).load(foto).circleCrop().into(img);

                        container.addView(item);
                    }
                });
    }

    // ============================================================
    //              GUARDAR CONVOCATORIA
    // ============================================================
    private void guardarConvocatoria(LinearLayout container, @Nullable EventModel ev) {

        if (ev == null) {
            Toast.makeText(getContext(), "Guarda el evento primero", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> lista = new ArrayList<>();

        for (int i = 0; i < container.getChildCount(); i++) {

            View item = container.getChildAt(i);
            CheckBox cb = item.findViewById(R.id.checkJugador);

            if (cb != null && cb.isChecked())
                lista.add(cb.getTag().toString());
        }

        db.collection("eventos").document(ev.getId())
                .update("convocados", lista)
                .addOnSuccessListener(a ->
                        Toast.makeText(getContext(), "Convocatoria guardada", Toast.LENGTH_SHORT).show());
    }

    // ============================================================
    //              PICKERS FECHA / HORA
    // ============================================================
    private void mostrarCalendario() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                getContext(),
                (v, y, m, d) ->
                        edtFechaDialog.setText(String.format("%02d/%02d/%04d", d, m + 1, y)),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void mostrarReloj() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(
                getContext(),
                (v, h, m) ->
                        edtHoraDialog.setText(String.format("%02d:%02d", h, m)),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }

    // ============================================================
    //              RESULTADO FOTO RIVAL
    // ============================================================
    @Override
    public void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if (req == PICK_IMAGE && data != null && data.getData() != null) {
            escudoUriSeleccionada = data.getData();
            Glide.with(getContext()).load(escudoUriSeleccionada).circleCrop().into(imgEscudoDialog);
        }
    }
}
