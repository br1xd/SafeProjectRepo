package com.example.testmapboxkotlin.model;

public class Comunas {
    public Comunas(String idComuna, Integer añoComuna, Integer tasaCrimen) {
        this.idComuna = idComuna;
        this.añoComuna = añoComuna;
        this.tasaCrimen = tasaCrimen;
    }

    public String getIdComuna() {
        return idComuna;
    }

    public void setIdComuna(String idComuna) {
        this.idComuna = idComuna;
    }

    public Integer getAñoComuna() {
        return añoComuna;
    }

    public void setAñoComuna(Integer añoComuna) {
        this.añoComuna = añoComuna;
    }

    public Integer getTasaCrimen() {
        return tasaCrimen;
    }

    public void setTasaCrimen(Integer tasaCrimen) {
        this.tasaCrimen = tasaCrimen;
    }

    private String idComuna;
    private Integer añoComuna;
    private Integer tasaCrimen;
}
