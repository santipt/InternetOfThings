package com.GTI.Grupo1.IoT;

public enum TipoSensor {
    // poner el resto de casos si esto funciona
    ILUMINACION ("Iluminacion", R.drawable.ic_bombilla),
    PUERTA ("Puerta", R.drawable.ic_lock),
    PUERTAABIERTA ("Puerta", R.drawable.unlock),
    FUEGO ("Fuego", R.drawable.ic_fuego),
    TEMPERATURA ("Temperatura \n Humedad", R.drawable.ic_temp),
    PERSONAS ("Personas en la casa", R.drawable.ic_personas),
    HUMEDAD ("Humedad", R.drawable.ic_humedad);

    private final String texto;
    private final int recurso;

    TipoSensor(String texto, int recurso) {
        this.texto = texto;
        this.recurso = recurso;
    }

    public String getTexto() {
        return texto;
    }

    public int getRecurso() {
        return recurso;
    }

    public static String[] getNombres() {
        String[] resultado = new String[TipoSensor.values().length];
        for (TipoSensor tipo : TipoSensor.values()) {
            resultado[tipo.ordinal()] = tipo.texto;
        }
        return resultado;
    }
}
