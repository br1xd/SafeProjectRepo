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

    private String lat;

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    private String log;
    private Boolean denunciado;
    private String image_url;

    public Reportes(String id, String tipo, String fecha, String autor, String lat, String log, Boolean denunciado, String imageUrl) {
        this.id = id;
        this.tipo = tipo;
        this.fecha = fecha;
        this.autor = autor;
        this.lat = lat;
        this.log = log;
        this.denunciado = denunciado;
        this.image_url = imageUrl;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public Boolean getDenunciado() {
        return denunciado;
    }

    public void setDenunciado(Boolean denunciado) {
        this.denunciado = denunciado;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
