package com.example.tmanager;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventModel {

    // ================= IDENTIDAD =================
    private String id;

    // ================= INFO BÁSICA =================
    private String titulo;
    private String fecha;
    private String hora;
    private String lugar;

    // ================= TIPO EVENTO =================
    private Boolean esPartido;
    private Boolean esLocal;
    private Boolean finalizado = false;

    // ================= EQUIPOS =================
    private String miEquipo;
    private String miEquipoLogo;
    private String rival;
    private String rivalEscudoUrl;

    // ================= RESULTADO =================
    private Integer resultadoLocal;
    private Integer resultadoRival;

    // ================= LISTAS =================
    private List<String> convocados = new ArrayList<>();
    private List<Map<String, Object>> goles = new ArrayList<>();
    private List<Map<String, Object>> tarjetas = new ArrayList<>();

    // ================= ASISTENCIAS =================
    // uid → "si" | "no" | "pendiente"
    private Map<String, String> asistencias = new HashMap<>();

    // Asistencia del usuario actual (NO se guarda en Firestore)
    @Exclude
    private String miAsistencia;

    // ================= CONSTRUCTOR VACÍO (OBLIGATORIO FIRESTORE) =================
    public EventModel() {
    }

    // ================= GETTERS / SETTERS =================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    // ===== Boolean Firestore =====

    public Boolean getEsPartido() {
        return esPartido;
    }

    public void setEsPartido(Boolean esPartido) {
        this.esPartido = esPartido;
    }

    public Boolean getEsLocal() {
        return esLocal;
    }

    public void setEsLocal(Boolean esLocal) {
        this.esLocal = esLocal;
    }

    public Boolean getFinalizado() {
        return finalizado;
    }

    public void setFinalizado(Boolean finalizado) {
        this.finalizado = finalizado;
    }

    // ===== Helpers LÓGICOS (NO Firestore) =====

    @Exclude
    public boolean isEsPartido() {
        return Boolean.TRUE.equals(esPartido);
    }

    @Exclude
    public boolean isEsLocal() {
        return Boolean.TRUE.equals(esLocal);
    }

    // ================= EQUIPOS =================

    public String getMiEquipo() {
        return miEquipo;
    }

    public void setMiEquipo(String miEquipo) {
        this.miEquipo = miEquipo;
    }

    public String getMiEquipoLogo() {
        return miEquipoLogo;
    }

    public void setMiEquipoLogo(String miEquipoLogo) {
        this.miEquipoLogo = miEquipoLogo;
    }

    public String getRival() {
        return rival;
    }

    public void setRival(String rival) {
        this.rival = rival;
    }

    public String getRivalEscudoUrl() {
        return rivalEscudoUrl;
    }

    public void setRivalEscudoUrl(String rivalEscudoUrl) {
        this.rivalEscudoUrl = rivalEscudoUrl;
    }

    // ================= RESULTADO =================

    public Integer getResultadoLocal() {
        return resultadoLocal;
    }

    public void setResultadoLocal(Integer resultadoLocal) {
        this.resultadoLocal = resultadoLocal;
    }

    public Integer getResultadoRival() {
        return resultadoRival;
    }

    public void setResultadoRival(Integer resultadoRival) {
        this.resultadoRival = resultadoRival;
    }

    // ================= LISTAS =================

    public List<String> getConvocados() {
        return convocados;
    }

    public void setConvocados(List<String> convocados) {
        this.convocados = convocados != null ? convocados : new ArrayList<>();
    }

    public List<Map<String, Object>> getGoles() {
        return goles;
    }

    public void setGoles(List<Map<String, Object>> goles) {
        this.goles = goles != null ? goles : new ArrayList<>();
    }

    public List<Map<String, Object>> getTarjetas() {
        return tarjetas;
    }

    public void setTarjetas(List<Map<String, Object>> tarjetas) {
        this.tarjetas = tarjetas != null ? tarjetas : new ArrayList<>();
    }

    // ================= ASISTENCIAS =================

    public Map<String, String> getAsistencias() {
        return asistencias;
    }

    public void setAsistencias(Map<String, String> asistencias) {
        this.asistencias = asistencias != null ? asistencias : new HashMap<>();
    }

    // ================= ASISTENCIA DEL USUARIO =================

    @Exclude
    public String getMiAsistencia() {
        return miAsistencia;
    }

    @Exclude
    public void setMiAsistencia(String miAsistencia) {
        this.miAsistencia = miAsistencia;
    }
}
