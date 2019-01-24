package com.GTI.Grupo1.IoT;

public class Sensores {
    private String nombre;
    private String medida;
    private TipoSensor tipo;


    public Sensores() {
    }

    public Sensores(String nombre, String medida, TipoSensor tipo){
        this.nombre = nombre;
        this.medida = medida;
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMedida() {
        return medida;
    }

    public void setMedida(String medida) {
        this.medida = medida;
    }

    public TipoSensor getTipo() {
        return tipo;
    }

    public void setTipo(TipoSensor tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Sensor{" +
                "Nombre='" + nombre + '\'' +
                "Medida='" + medida + '\'' +
                '}';
    }
}
