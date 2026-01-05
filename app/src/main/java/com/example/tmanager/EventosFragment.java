package com.example.tmanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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

    private String rolUsuario = "admin";
    private String uidActual;

    EditText edtFechaDialog, edtHoraDialog;
    ImageView imgEscudoDialog;
    private FloatingActionButton btnAgregarEvento;


    private ListenerRegistration eventosListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_eventos, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        btnAgregarEvento = vista.findViewById(R.id.btnAgregarEvento);

        uidActual = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("usuarios").document(uidActual)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {
                        rolUsuario = doc.getString("rol");
                    }

                    if ("entrenador".equals(rolUsuario)) {
                        btnAgregarEvento.setVisibility(View.VISIBLE);
                    }


                    cargarInfoEquipo();
                });



        recyclerEventos = vista.findViewById(R.id.recyclerEventos);
        recyclerEventos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(getContext(), listaEventos);
        recyclerEventos.setAdapter(adapter);

        adapter.setOnEventClickListener(this::mostrarOpcionesEvento);

        btnAgregarEvento = vista.findViewById(R.id.btnAgregarEvento);
        btnAgregarEvento.setOnClickListener(v -> mostrarDialogo(-1));



        return vista;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (eventosListener != null) eventosListener.remove();
    }

    //            CARGAR EQUIPO + EVENTOS

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

                Boolean finalizado = ds.getBoolean("finalizado");
                Boolean resultadoEliminado = ds.getBoolean("resultadoEliminado");

                if (Boolean.TRUE.equals(finalizado)) continue;
                if (Boolean.TRUE.equals(resultadoEliminado)) continue;


                List<String> conv = (List<String>) ds.get("convocados");

                if ("jugador".equals(rolUsuario)) {
                    if (conv == null) continue;
                    if (!conv.contains(uidActual)) continue;
                }


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

                ev.setFinalizado(Boolean.TRUE.equals(finalizado));

                ev.setConvocados(conv != null ? conv : new ArrayList<>());
                Map<String, Object> asistencias =
                        (Map<String, Object>) ds.get("asistencias");

                if (asistencias != null && asistencias.containsKey(uidActual)) {
                    ev.setMiAsistencia((String) asistencias.get(uidActual));
                } else {
                    ev.setMiAsistencia(null);
                }


                listaEventos.add(ev);
            }



            adapter.notifyDataSetChanged();
        });
    }


    //         MENÚ DE OPCIONES

    private void mostrarOpcionesEvento(int pos) {

        EventModel ev = listaEventos.get(pos);

        if ("jugador".equals(rolUsuario)) {
            mostrarDialogoAsistencia(ev);
            return;
        }
        List<String> opciones = new ArrayList<>();
        opciones.add("Editar");
        opciones.add("Eliminar");

        if (ev.isEsPartido()) opciones.add("Resultado");

        opciones.add("Cancelar");

        new AlertDialog.Builder(getContext())
                .setTitle("Opciones del evento")
                .setItems(opciones.toArray(new String[0]), (d, w) -> {

                    String opcion = opciones.get(w);

                    switch (opcion) {
                        case "Editar":
                            mostrarDialogo(pos);
                            break;

                        case "Eliminar":
                            db.collection("eventos").document(ev.getId()).delete();
                            break;

                        case "Resultado":
                            mostrarDialogoResultado(ev);
                            break;

                        case "Cancelar":
                        default:
                            break;
                    }
                })
                .show();
    }

    //        DIÁLOGO CREAR

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

        // CONVOCATORIA
        Button btnConvocar = view.findViewById(R.id.btnConvocatoria);
        LinearLayout layoutConv = view.findViewById(R.id.layoutConvocatoria);
        LinearLayout container = view.findViewById(R.id.playersContainer);

        edtMiEquipo.setText(miEquipoNombre);
        edtMiEquipo.setEnabled(false);

        spnTipo.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Evento normal", "Partido"}));

        spnLocal.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Local", "Visitante"}));

        EventModel editEvent = (posEditing != -1) ? listaEventos.get(posEditing) : null;
        if (editEvent == null) {
            spnTipo.setSelection(0); // Evento normal
            layoutPartido.setVisibility(View.GONE);
            edtTitulo.setEnabled(true);
            edtRival.setText("");
            escudoUriSeleccionada = null;
            imgEscudoDialog.setImageDrawable(null);
        }


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

        List<String> finalConv = convocadosPrevios;
        btnConvocar.setOnClickListener(v -> {
            layoutConv.setVisibility(View.VISIBLE);
            llenarJugadoresConvocatoria(container, finalConv);
        });

        spnTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                layoutPartido.setVisibility(pos == 1 ? View.VISIBLE : View.GONE);
                edtTitulo.setEnabled(pos == 0);
                if (pos == 1) edtTitulo.setText("Partido");
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        edtFechaDialog.setOnClickListener(v -> mostrarCalendario());
        edtHoraDialog.setOnClickListener(v -> mostrarReloj());

        imgEscudoDialog.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, PICK_IMAGE);
        });

        builder.setPositiveButton("Guardar", (d, w) -> guardarEvento(editEvent, edtTitulo, edtFechaDialog, edtHoraDialog, edtLugar, edtRival, spnTipo, spnLocal, container));
        builder.setNegativeButton("Cancelar", null);

        builder.show();
    }

    //             GUARDAR EVENTO
    private void guardarEvento(EventModel editEvent,
                               EditText edtTitulo, EditText edtFecha, EditText edtHora, EditText edtLugar,
                               EditText edtRival, Spinner spnTipo, Spinner spnLocal,
                               LinearLayout containerConvocados) {

        String titulo = edtTitulo.getText().toString().trim();
        String fecha = edtFecha.getText().toString().trim();
        String hora = edtHora.getText().toString().trim();
        String lugar = edtLugar.getText().toString().trim();
        String rival = edtRival.getText().toString().trim();

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


        // TOMAR CONVOCADOS ANTES DE GUARDAR

        List<String> convocados = new ArrayList<>();
        for (int i = 0; i < containerConvocados.getChildCount(); i++) {
            View item = containerConvocados.getChildAt(i);
            CheckBox cb = item.findViewById(R.id.checkJugador);
            if (cb != null && cb.isChecked()) {
                convocados.add(cb.getTag().toString());
            }
        }

        if (escudoUriSeleccionada != null && !escudoUriSeleccionada.toString().startsWith("http")) {
            uploadRivalLogoThenSave(fechaHora, editEvent, titulo, fecha, hora, lugar,
                    esPartido, rival, esLocal, convocados);
            return;
        }

        saveEventToFirestore(fechaHora, editEvent, titulo, fecha, hora, lugar,
                esPartido, rival, esLocal,
                editEvent != null ? editEvent.getRivalEscudoUrl() : null,
                convocados);
    }

    //      SUBIR FOTO RIVAL Y LUEGO GUARDAR EVENTO
    private void uploadRivalLogoThenSave(Date fechaHora, EventModel editEvent,
                                         String titulo, String fecha, String hora, String lugar,
                                         boolean esPartido, String rival, boolean esLocal,
                                         List<String> convocados) {

        StorageReference ref =
                FirebaseStorage.getInstance().getReference("eventos/logos/" + UUID.randomUUID() + ".png");

        ref.putFile(escudoUriSeleccionada)
                .addOnSuccessListener(a ->
                        ref.getDownloadUrl().addOnSuccessListener(url ->
                                saveEventToFirestore(fechaHora, editEvent, titulo, fecha, hora, lugar,
                                        esPartido, rival, esLocal, url.toString(), convocados)))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error subiendo imagen", Toast.LENGTH_SHORT).show());
    }


    //          GUARDAR EVENTO + CONVOCATORIA EN FIRESTORE
    private void saveEventToFirestore(Date fechaHora, EventModel editEvent,
                                      String titulo, String fecha, String hora, String lugar,
                                      boolean esPartido, String rival, boolean esLocal,
                                      String rivalEscudoUrl, List<String> convocados) {

        Map<String, Object> data = new HashMap<>();
        data.put("titulo", titulo);
        data.put("fecha", fecha);
        data.put("hora", hora);
        data.put("lugar", lugar);
        data.put("esPartido", esPartido);

        if (esPartido) {
            data.put("miEquipo", miEquipoNombre);
            data.put("miEquipoLogo", miEquipoLogo);
            data.put("rival", rival);
            data.put("rivalEscudoUrl", rivalEscudoUrl);
            data.put("esLocal", esLocal);
        } else {
            // LIMPIAR CAMPOS DE PARTIDO
            data.put("miEquipo", null);
            data.put("miEquipoLogo", null);
            data.put("rival", null);
            data.put("rivalEscudoUrl", null);
            data.put("esLocal", null);
        }

        data.put("equipoId", equipoId);
        data.put("creadorUid", auth.getCurrentUser().getUid());
        data.put("fechaHoraTimestamp", new Timestamp(fechaHora));

        // 🎯 CONVOCATORIA SE GUARDA AQUÍ
        data.put("convocados", convocados);
        data.put("finalizado", false);

        if (editEvent == null) {

            db.collection("eventos")
                    .add(data)
                    .addOnSuccessListener(docRef -> {

                        Toast.makeText(getContext(), "Evento creado", Toast.LENGTH_SHORT).show();


                        //  NOTIFICAR CONVOCADOS

                        if (convocados != null && !convocados.isEmpty()) {

                            String tituloNotif = esPartido
                                    ? "Convocado a partido"
                                    : "Convocado a evento";

                            String mensajeNotif = esPartido
                                    ? "Has sido convocado a un nuevo partido"
                                    : "Has sido convocado al evento: " + titulo;

                            String tipoNotif = esPartido
                                    ? "convocatoria_partido"
                                    : "convocatoria_evento";


                            for (String uidJugador : convocados) {
                                NotificacionUtil.crear(
                                        uidJugador,
                                        equipoId,
                                        tipoNotif,
                                        tituloNotif,
                                        mensajeNotif,
                                        docRef.getId()
                                );
                            }


                        }
                    });

        } else {

            db.collection("eventos").document(editEvent.getId())
                    .update(data)
                    .addOnSuccessListener(a ->
                            Toast.makeText(getContext(), "Evento actualizado", Toast.LENGTH_SHORT).show());
        }


        escudoUriSeleccionada = null;
    }


    //              CONVOCATORIA — CARGAR LISTA

    private void llenarJugadoresConvocatoria(LinearLayout container, @Nullable List<String> pre) {

        container.removeAllViews();

        if (equipoId == null) return;

        db.collection("equipos").document(equipoId)
                .get()
                .addOnSuccessListener(doc -> {

                    List<Map<String, Object>> jugadores = (List<Map<String, Object>>) doc.get("jugadores");
                    if (jugadores == null) jugadores = new ArrayList<>();

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
    //             FECHA Y HORA

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


    //              DIALOGO RESULTADO
    private void mostrarDialogoResultado(EventModel ev) {

        if (ev == null) return;
        if (!ev.isEsPartido()) return;

        db.collection("equipos").document(equipoId)
                .get()
                .addOnSuccessListener(doc -> {

                    List<Map<String, Object>> jugadores = (List<Map<String, Object>>) doc.get("jugadores");
                    if (jugadores == null) jugadores = new ArrayList<>();

                    Map<String, String> uidToAlias = new HashMap<>();
                    for (Map<String, Object> j : jugadores) {
                        String uid = String.valueOf(j.get("uid"));
                        uidToAlias.put(uid, (String) j.get("alias"));
                    }

                    List<String> convUids = new ArrayList<>();
                    List<String> convNames = new ArrayList<>();
                    if (ev.getConvocados() != null) {
                        for (String u : ev.getConvocados()) {
                            if (uidToAlias.containsKey(u)) {
                                convUids.add(u);
                                convNames.add(uidToAlias.get(u));
                            }
                        }
                    }

                    if (convUids.isEmpty()) {
                        Toast.makeText(getContext(), "No hay convocados", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_resultado, null);
                    builder.setView(view);

                    EditText edtGolesLocal = view.findViewById(R.id.edtGolesLocal);
                    EditText edtGolesRival = view.findViewById(R.id.edtGolesRival);
                    LinearLayout contGoles = view.findViewById(R.id.containerGoles);
                    LinearLayout contTarjetas = view.findViewById(R.id.containerTarjetas);
                    Button btnAddGol = view.findViewById(R.id.btnAddGol);
                    Button btnAddTarjeta = view.findViewById(R.id.btnAddTarjeta);
                    Button guardar = view.findViewById(R.id.btnGuardarResultado);

                    // PREFILL
                    if (ev.getResultadoLocal() != null)
                        edtGolesLocal.setText(String.valueOf(ev.getResultadoLocal()));
                    if (ev.getResultadoRival() != null)
                        edtGolesRival.setText(String.valueOf(ev.getResultadoRival()));

                    edtGolesLocal.addTextChangedListener(new TextWatcher() {
                        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                        @Override public void afterTextChanged(Editable s) {
                            int max = 0;
                            try { max = Integer.parseInt(s.toString()); } catch (Exception ignored) {}
                            while (contGoles.getChildCount() > max) {
                                contGoles.removeViewAt(contGoles.getChildCount() - 1);
                            }
                        }
                    });

                    btnAddGol.setOnClickListener(v1 -> {
                        int max = 0;
                        try { max = Integer.parseInt(edtGolesLocal.getText().toString()); } catch (Exception ignored) {}
                        if (contGoles.getChildCount() >= max) {
                            Toast.makeText(getContext(), "No más goles", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        contGoles.addView(createGolItemView(convUids, convNames));
                    });

                    if (ev.getGoles() != null) {
                        for (Map<String, Object> g : ev.getGoles()) {

                            String scorer = (String) g.get("scorerUid");
                            String assist = (String) g.get("assistantUid");

                            View row = createGolItemView(convUids, convNames);

                            Spinner spnScorer = row.findViewWithTag("spnScorer");
                            Spinner spnAssist = row.findViewWithTag("spnAssist");

                            if (scorer != null && convUids.contains(scorer))
                                spnScorer.setSelection(convUids.indexOf(scorer));

                            if (assist == null) spnAssist.setSelection(0);
                            else if (convUids.contains(assist))
                                spnAssist.setSelection(convUids.indexOf(assist) + 1);

                            contGoles.addView(row);
                        }
                    }

                    btnAddTarjeta.setOnClickListener(v2 ->
                            contTarjetas.addView(createTarjetaItemView(convUids, convNames))
                    );

                    if (ev.getTarjetas() != null) {
                        for (Map<String, Object> t : ev.getTarjetas()) {
                            String uid = (String) t.get("playerUid");
                            String tipo = (String) t.get("tipo");

                            View row = createTarjetaItemView(convUids, convNames);
                            Spinner spnPlayer = row.findViewWithTag("spnPlayerTarjeta");
                            Spinner spnTipo = row.findViewWithTag("spnTipoTarjeta");

                            if (uid != null && convUids.contains(uid))
                                spnPlayer.setSelection(convUids.indexOf(uid));

                            if ("roja".equalsIgnoreCase(tipo))
                                spnTipo.setSelection(1);

                            contTarjetas.addView(row);
                        }
                    }

                    AlertDialog dialog = builder.create();

                    guardar.setOnClickListener(v3 -> {
                        Integer golesLocal = edtGolesLocal.getText().toString().isEmpty()
                                ? null : Integer.parseInt(edtGolesLocal.getText().toString());

                        Integer golesRival = edtGolesRival.getText().toString().isEmpty()
                                ? null : Integer.parseInt(edtGolesRival.getText().toString());

                        List<Map<String, Object>> golesList = new ArrayList<>();
                        for (int i = 0; i < contGoles.getChildCount(); i++) {
                            View row = contGoles.getChildAt(i);

                            Spinner spnScorer = row.findViewWithTag("spnScorer");
                            Spinner spnAssist = row.findViewWithTag("spnAssist");

                            String scorerUid = convUids.get(spnScorer.getSelectedItemPosition());
                            String assistUid = null;

                            int p = spnAssist.getSelectedItemPosition();
                            if (p > 0) assistUid = convUids.get(p - 1);

                            Map<String, Object> gmap = new HashMap<>();
                            gmap.put("scorerUid", scorerUid);
                            gmap.put("assistantUid", assistUid);

                            golesList.add(gmap);
                        }

                        List<Map<String, Object>> tarjetasList = new ArrayList<>();
                        for (int i = 0; i < contTarjetas.getChildCount(); i++) {
                            View row = contTarjetas.getChildAt(i);

                            Spinner spnPlayer = row.findViewWithTag("spnPlayerTarjeta");
                            Spinner spnTipo = row.findViewWithTag("spnTipoTarjeta");

                            String uid = convUids.get(spnPlayer.getSelectedItemPosition());
                            String tipo = spnTipo.getSelectedItemPosition() == 0 ? "amarilla" : "roja";

                            Map<String, Object> tmap = new HashMap<>();
                            tmap.put("playerUid", uid);
                            tmap.put("tipo", tipo);

                            tarjetasList.add(tmap);
                        }

                        saveResultadoEnFirestore(ev, golesLocal, golesRival, golesList, tarjetasList, dialog);
                    });

                    dialog.show();
                });
    }

    //       FILA DE GOL
    private ArrayAdapter<String> smallAdapter(List<String> items) {
        ArrayAdapter<String> ad = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, items);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return ad;
    }

    private View createGolItemView(List<String> convUids, List<String> convNames) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        Spinner spnScorer = new Spinner(getContext());
        spnScorer.setAdapter(smallAdapter(convNames));
        spnScorer.setTag("spnScorer");
        row.addView(spnScorer, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        List<String> assist = new ArrayList<>();
        assist.add("Ninguno");
        assist.addAll(convNames);

        Spinner spnAssist = new Spinner(getContext());
        spnAssist.setAdapter(smallAdapter(assist));
        spnAssist.setTag("spnAssist");

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMarginStart(8);
        row.addView(spnAssist, lp);

        ImageButton del = new ImageButton(getContext());
        del.setImageResource(android.R.drawable.ic_delete);
        del.setBackground(null);
        del.setOnClickListener(v -> ((ViewGroup) row.getParent()).removeView(row));

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.setMarginStart(8);
        row.addView(del, lp2);

        return row;
    }

    //        FILA TARJETA
    private View createTarjetaItemView(List<String> convUids, List<String> convNames) {

        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        Spinner spnPlayer = new Spinner(getContext());
        spnPlayer.setAdapter(smallAdapter(convNames));
        spnPlayer.setTag("spnPlayerTarjeta");
        row.addView(spnPlayer, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Spinner spnTipo = new Spinner(getContext());
        spnTipo.setAdapter(smallAdapter(Arrays.asList("Amarilla", "Roja")));
        spnTipo.setTag("spnTipoTarjeta");

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMarginStart(8);
        row.addView(spnTipo, lp);

        ImageButton del = new ImageButton(getContext());
        del.setImageResource(android.R.drawable.ic_delete);
        del.setBackground(null);
        del.setOnClickListener(v ->
                ((ViewGroup) row.getParent()).removeView(row));

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.setMarginStart(8);
        row.addView(del, lp2);

        return row;
    }


    //       GUARDAR RESULTADO EN FIRESTORE
    private void saveResultadoEnFirestore(EventModel ev,
                                          Integer golesLocal,
                                          Integer golesRival,
                                          List<Map<String, Object>> goles,
                                          List<Map<String, Object>> tarjetas,
                                          AlertDialog dialog) {

        Map<String, Object> data = new HashMap<>();
        data.put("resultadoLocal", golesLocal);
        data.put("resultadoRival", golesRival);
        data.put("goles", goles);
        data.put("tarjetas", tarjetas);
        data.put("finalizado", true);
        data.put("resultadoEliminado", false);



        db.collection("eventos").document(ev.getId())
                .update(data)
                .addOnSuccessListener(a -> {
                    Toast.makeText(getContext(), "Resultado guardado", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error guardando resultado", Toast.LENGTH_SHORT).show());
    }

    // FOTO RIVAL

    @Override
    public void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if (req == PICK_IMAGE && data != null && data.getData() != null) {
            escudoUriSeleccionada = data.getData();
            if (imgEscudoDialog != null)
                Glide.with(getContext()).load(escudoUriSeleccionada).circleCrop().into(imgEscudoDialog);
        }
    }
    private void mostrarDialogoAsistencia(EventModel ev) {

        String estadoActual = ev.getMiAsistencia();
        List<String> opciones = new ArrayList<>();

        if (estadoActual == null) {
            opciones.add("Asistiré");
            opciones.add("No asistiré");
        } else if ("si".equals(estadoActual)) {
            opciones.add("No asistiré");
        } else if ("no".equals(estadoActual)) {
            opciones.add("Asistiré");
        }

        opciones.add("Cancelar");

        new AlertDialog.Builder(getContext())
                .setTitle("Confirmar asistencia")
                .setItems(opciones.toArray(new String[0]), (d, w) -> {

                    String seleccion = opciones.get(w);

                    if ("Cancelar".equals(seleccion)) return;

                    String nuevoEstado = "si";
                    if ("No asistiré".equals(seleccion)) nuevoEstado = "no";

                    db.collection("eventos")
                            .document(ev.getId())
                            .update("asistencias." + uidActual, nuevoEstado);

                    // Obtener entrenadorUid del equipo
                    db.collection("equipos").document(equipoId)
                            .get()
                            .addOnSuccessListener(doc -> {

                                String entrenadorUid = doc.getString("entrenadorUid");
                                if (entrenadorUid == null) return;

                                db.collection("usuarios").document(uidActual)
                                        .get()
                                        .addOnSuccessListener(userDoc -> {

                                            String nombreJugador = userDoc.getString("nombre");
                                            if (nombreJugador == null)
                                                nombreJugador = "Un jugador";

                                            NotificacionUtil.crear(
                                                    entrenadorUid,
                                                    equipoId,
                                                    "asistencia",
                                                    "Asistencia confirmada",
                                                    nombreJugador + " ha confirmado su asistencia",
                                                    null
                                            );
                                        });
                            });

                })
                .show();
    }


}
