package com.GTI.Grupo1.IoT;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class SensoresAdaptador extends RecyclerView.Adapter<SensoresAdaptador.ViewHolder> {

    protected SensoresInterface sensores; // Lista de lugares a mostrar
    protected LayoutInflater inflador; // Crea Layouts a partir del XML
    protected Context contexto; // Lo necesitamos para el inflador

    public SensoresAdaptador(Context contexto, SensoresInterface sensores) {
        this.contexto = contexto;
        this.sensores = sensores;
        inflador = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //Creamos nuestro ViewHolder, con los tipos de elementos a modificar
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nombre, medida;
        public ImageView tipo;
        public ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombre);
            medida = itemView.findViewById(R.id.medida);
            tipo = itemView.findViewById(R.id.tipo);
        }
    }

    // Creamos el ViewHolder con la vista de un elemento sin personalizar
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflamos la vista desde el xml
        View v = inflador.inflate(R.layout.elemento_lista_sensores, parent, false);
        return new ViewHolder(v);
    }

    // Usando como base el ViewHolder y lo personalizamos
    @Override
    public void onBindViewHolder(ViewHolder holder, int posicion) {
        Sensores sensor = sensores.elemento(posicion);
        personalizaVista(holder, sensor);
    }

    // sustituir el tipo por nuestro tipo de sensor y añadir iconos personalizados
    public void personalizaVista(ViewHolder holder, Sensores sensor) {
        holder.nombre.setText(sensor.getNombre());
        holder.medida.setText(sensor.getMedida());
        int id = R.drawable.ic_calendario;
        switch(sensor.getTipo()) {
            //if (sensor.getTipo() == TipoSensor.PUERTA && sensor.getMedida() == "cerrada"){ }
            case PUERTA:
                if (sensor.getMedida().trim().equals("Cerrada")) {id = R.drawable.ic_lock; break;}
                id = R.drawable.unlock; break;
            case ILUMINACION:id = R.drawable.ic_bombilla; break;
            case FUEGO:id = R.drawable.ic_fuego; break;
            case PERSONAS:id = R.drawable.ic_personas; break;
            case TEMPERATURA:id = R.drawable.ic_temp; break;
            case HUMEDAD:id = R.drawable.ic_humedad; break;
        }
        holder.tipo.setImageResource(id);
        holder.tipo.setScaleType(ImageView.ScaleType.FIT_END);
    }

    // Indicamos el número de elementos de la lista
    @Override public int getItemCount() {
        return sensores.tamanyo();
    }
}