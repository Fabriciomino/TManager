package com.example.tmanager;

public class Jugador {

    public String uid;
    public String nombre;
    public String posicion;
    public String fotoUrl;

    public Jugador() {
    }

    public Jugador(String uid, String nombre, String posicion, String fotoUrl) {
        this.uid = uid;
        this.nombre = nombre;
        this.posicion = posicion;
        this.fotoUrl = fotoUrl;
    }
}
