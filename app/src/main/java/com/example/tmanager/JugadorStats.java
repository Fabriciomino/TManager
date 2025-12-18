package com.example.tmanager;

public class JugadorStats {

    public String uid;
    public String alias;
    public String dorsal;
    public String posicion;
    public String fotoUrl;

    public int goles = 0;
    public int asistencias = 0;
    public int amarillas = 0;
    public int rojas = 0;
    public int partidos = 0;

    public double getGp() {
        if (partidos == 0) return 0;
        return (double) goles / partidos;
    }
}
