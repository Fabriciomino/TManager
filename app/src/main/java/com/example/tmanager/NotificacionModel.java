package com.example.tmanager;

import com.google.firebase.Timestamp;

public class NotificacionModel {

    public String id;
    public String tipo;
    public String titulo;
    public String mensaje;
    public String eventoId;   // 👈 CLAVE

    public Timestamp timestamp;
    public boolean leida;

    public NotificacionModel() {}
}
