package com.example.testmapboxkotlin.model;

public class Reportes {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    private String id;
    private String tipo;
    private String fecha;

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    private String autor;

    public Reportes(String id, String tipo, String fecha, String autor) {
        this.id = id;
        this.tipo = tipo;
        this.fecha = fecha;
        this.autor = autor;
    }

}
