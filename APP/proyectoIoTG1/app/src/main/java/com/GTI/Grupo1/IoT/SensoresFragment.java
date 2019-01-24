package com.GTI.Grupo1.IoT;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;


public class SensoresFragment extends Fragment {

    private String peso;
    private String altura;
    private boolean puerta;
    private String estadoPuerta;
    private String temperatura;
    private String humedad;
    private boolean luces;
    private String estadoLuces;
    private String personas;
    private FirebaseUser user = MainActivity.user;

    /*private RecyclerView recyclerView;
    public SensoresAdaptador adaptador;
    private RecyclerView.LayoutManager layoutManager;
    public static SensoresInterface sensoresInterface = new SensoresVector();*/

    public SensoresFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensores_layout, container, false);

        // Inflate the layout for this fragment
        /*recyclerView = rootView.findViewById(R.id.recycler_view);
        adaptador = new SensoresAdaptador(getActivity(), sensoresInterface);
        recyclerView.setAdapter(adaptador);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);*/

        final TextView textoPuerta = view.findViewById(R.id.medida3);
        final TextView textoTemp = view.findViewById(R.id.medida);
        final TextView textoHum = view.findViewById(R.id.medida1);
        final TextView textoLuces = view.findViewById(R.id.medida2);
        final TextView textoPersonas = view.findViewById(R.id.medida4);

        final ImageView tipoLuces = view.findViewById(R.id.tipo2);
        final ImageView tipoPuerta = view.findViewById(R.id.tipo3);
        final ImageView tipoTemp = view.findViewById(R.id.tipo);
        final ImageView tipoHum = view.findViewById(R.id.tipo1);
        final ImageView tipoPersonas = view.findViewById(R.id.tipo4);

        FirebaseFirestore db2 = FirebaseFirestore.getInstance();
        db2.collection("CASAS").document("Casa1.123456789").get()
                .addOnCompleteListener(
                        new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {

                                    //Recoger los valores de la bd
                                    puerta = task.getResult().getBoolean("PuertaAbierta");
                                    temperatura = task.getResult().getDouble("Temperatura").toString();
                                    humedad = task.getResult().getDouble("Humedad").toString();
                                    luces = task.getResult().getBoolean("Luces");
                                    personas = task.getResult().getDouble("Personas").toString();

                                    estadoPuerta = "Abierta";

                                    if (!puerta) {
                                        estadoPuerta = "Cerrada";
                                    }

                                    //Añadir el texto de la bd en el layout
                                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                    if (pref.getString("temperatura", "0").equals("1")) {
                                        textoTemp.setText(cambioMedidaTemperatura(Float.parseFloat(temperatura)) + "ºF");
                                    } else if (pref.getString("temperatura", "0").equals("2")) {
                                        textoTemp.setText(cambioMedidaTemperatura(Float.parseFloat(temperatura)) + "ºK");
                                    } else {
                                        textoTemp.setText(cambioMedidaTemperatura(Float.parseFloat(temperatura)) + "ºC");
                                    }

                                    textoPuerta.setText(estadoPuerta);
                                    textoHum.setText(humedad + " % ");
                                    textoPersonas.setText("Hay " + personas);
                                    tipoHum.setImageResource(R.drawable.ic_humedad);
                                    tipoLuces.setImageResource(R.drawable.ic_bombilla);
                                    tipoPersonas.setImageResource(R.drawable.ic_personas);
                                    tipoTemp.setImageResource(R.drawable.ic_temp);

                                    if (textoPuerta.equals("Abierta")){
                                        tipoPuerta.setImageResource(R.drawable.unlock);
                                    } else {
                                        tipoPuerta.setImageResource(R.drawable.ic_lock);
                                    }

                                }
                            }
                        });
        return view;
    }

    // funcion cambio de temperatura
    public float cambioMedidaTemperatura (float temperaturaACambiar) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (pref.getString("temperatura", "0").equals("1")) {
            float res;
            res = temperaturaACambiar * 1.8f; //fahrenheit
            res = res + 32;
            return res;
        } else if (pref.getString("temperatura", "0").equals("2")) {
            float res;
            res = temperaturaACambiar + 273.15f;
            return res;
        } else {
            return temperaturaACambiar;
        }
    }



}