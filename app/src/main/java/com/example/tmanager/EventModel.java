package com.example.tmanager;

import java.util.List;
import java.util.Map;

public class EventModel {

    private String id; // id del documento en Firestore
    private String titulo;
    private String fecha;
    private String hora;
    private String lugar;

    private boolean esPartido;
    private String miEquipo;
    private String miEquipoLogo;      // URL del logo de mi equipo
    private String rival;
    private String rivalEscudoUrl;    // URL del logo rival
    private boolean esLocal;

    // 🔥 NUEVO: lista de convocados
    private List<String> convocados;

    // 🔥 NUEVO: resultado del partido
    private Integer resultadoLocal;   // puede ser null
    private Integer resultadoRival;   // puede ser null

    // 🔥 NUEVO: goles y tarjetas
    private List<Map<String, Object>> goles;
    private List<Map<String, Object>> tarjetas;

    public EventModel() {}

    public EventModel(String id, String titulo, String fecha, String hora, String lugar,
                      boolean esPartido, String miEquipo, String miEquipoLogo,
                      String rival, String rivalEscudoUrl, boolean esLocal,
                      List<String> convocados,
                      Integer resultadoLocal, Integer resultadoRival,
                      List<Map<String, Object>> goles,
                      List<Map<String, Object>> tarjetas) {

        this.id = id;
        this.titulo = titulo;
        this.fecha = fecha;
        this.hora = hora;
        this.lugar = lugar;
        this.esPartido = esPartido;
        this.miEquipo = miEquipo;
        this.miEquipoLogo = miEquipoLogo;
        this.rival = rival;
        this.rivalEscudoUrl = rivalEscudoUrl;
        this.esLocal = esLocal;
        this.convocados = convocados;
        this.resultadoLocal = resultadoLocal;
        this.resultadoRival = resultadoRival;
        this.goles = goles;
        this.tarjetas = tarjetas;
    }

    // --- id ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public boolean isEsPartido() { return esPartido; }
    public void setEsPartido(boolean esPartido) { this.esPartido = esPartido; }

    public String getMiEquipo() { return miEquipo; }
    public void setMiEquipo(String miEquipo) { this.miEquipo = miEquipo; }

    public String getMiEquipoLogo() { return miEquipoLogo; }
    public void setMiEquipoLogo(String miEquipoLogo) { this.miEquipoLogo = miEquipoLogo; }

    public String getRival() { return rival; }
    public void setRival(String rival) { this.rival = rival; }

    public String getRivalEscudoUrl() { return rivalEscudoUrl; }
    public void setRivalEscudoUrl(String rivalEscudoUrl) { this.rivalEscudoUrl = rivalEscudoUrl; }

    public boolean isEsLocal() { return esLocal; }
    public void setEsLocal(boolean esLocal) { this.esLocal = esLocal; }

    // --- Convocados ---
    public List<String> getConvocados() { return convocados; }
    public void setConvocados(List<String> convocados) { this.convocados = convocados; }

    @Override
    public String toString() {
        return titulo + " " + fecha + " " + hora;
    }
}
