package com.GTI.Grupo1.IoT;


import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.DecimalFormat;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static santi.example.rpi_uart.comun.Mqtt.TAG;
import static santi.example.rpi_uart.comun.Mqtt.qos;
import static santi.example.rpi_uart.comun.Mqtt.topicRoot;


public class InicioFragment extends Fragment {

    public static String peso;
    private String altura;
    private boolean puerta;
    public static String estadoPuerta;
    public static String temperatura;
    public static String humedad;
    private boolean luces;
    public static String estadoLuces;
    public static String personas;
    public static String medicamentos;

    private FirebaseUser user = MainActivity.user;

    public static  View view;

    public InicioFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        view = inflater.inflate(R.layout.inicio, container, false);
        refresh();
        view.findViewById(R.id.imageView2).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                refresh();
            }
        });
        getActivity().startService(new Intent(getActivity(),
                IntentServiceOperacion.class));

        FloatingActionButton pesarme = view.findViewById(R.id.pesarme);
        pesarme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), PesoNow.class);
                getActivity().startActivity(i);
            }
        });

//----------------------------- DATOS DE BASCULA Y ALTURA ---------------------------------------------------------
        final TextView textoPeso = view.findViewById(R.id.peso);
        final TextView textoAltura = view.findViewById(R.id.altura);
        final TextView textoPuerta = view.findViewById(R.id.puerta);
        final TextView textoTemp = view.findViewById(R.id.temp);
        final TextView textoHum = view.findViewById(R.id.hum);
        final TextView textoLuces = view.findViewById(R.id.luces);
        final TextView textoPersonas = view.findViewById(R.id.personas);
        final TextView textoMedicamentos = view.findViewById(R.id.medicamentos);
        view.findViewById(R.id.botonLuz).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //Apagar encender luz

                IntentServiceOperacion.botonLuz(view);
            }
        });
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("USUARIOS")
                .document(user.getUid())
                .collection("Bascula").orderBy("fecha",Query.Direction.DESCENDING).limit(1).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int i = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
//
                            //Recoger los valores de la bd
                            peso = document.getData().get("peso").toString();
                            altura = document.getData().get("altura").toString();
                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            //Añadir el texto de la bd en el layout
                            if (pref.getString("masa", "0").equals("1")) {
                                textoPeso.setText(cambioMedidaPeso(Float.parseFloat(peso)) + " st");
                            } else if (pref.getString("masa", "0").equals("2")) {
                                textoPeso.setText(cambioMedidaPeso(Float.parseFloat(peso)) + " lb");
                            } else {
                                textoPeso.setText(cambioMedidaPeso(Float.parseFloat(peso)) + " kg");
                            }
                            if (pref.getString("altura", "0").equals("1")) {
                                textoAltura.setText(cambioMedidaAltura(Float.parseFloat(altura)) + " ft");
                            } else if (pref.getString("altura", "0").equals("2")) {
                                textoAltura.setText(cambioMedidaAltura(Float.parseFloat(altura)) + " in");
                            } else {
                                textoAltura.setText(cambioMedidaAltura(Float.parseFloat(altura)) + " cm");
                            }


                            i++;
                        }
                    }
                });
//----------------------------- DATOS DE SENSORES ---------------------------------------------------------

        //Añadir el texto en el layout
        temperatura = "22";
        textoLuces.setText("OFF");
        textoPuerta.setText(getString(R.string.cerrada));

        textoHum.setText("5 %");
        textoPersonas.setText(getString(R.string.personasEnLaCasa));
        textoMedicamentos.setText(getString(R.string.medicamentosTexto));

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (pref.getString("temperatura", "0").equals("1")) {
            textoTemp.setText(cambioMedidaTemperatura(Float.parseFloat(temperatura)) + "ºF");
        } else if (pref.getString("temperatura", "0").equals("2")) {
            textoTemp.setText(cambioMedidaTemperatura(Float.parseFloat(temperatura)) + "ºK");
        } else {
            textoTemp.setText(cambioMedidaTemperatura(Float.parseFloat(temperatura)) + "ºC");
        }

        return view;

    }//onCreate()
    public static void refresh(){

        final TextView textoPuerta = view.findViewById(R.id.puerta);
        final TextView textoTemp = view.findViewById(R.id.temp);
        final TextView textoHum = view.findViewById(R.id.hum);
        final TextView textoLuces = view.findViewById(R.id.luces);
        final TextView textoPersonas = view.findViewById(R.id.personas);
        final TextView textoMedic = view.findViewById(R.id.medicamentos);


        textoPuerta.setText(estadoPuerta);
        textoTemp.setText(temperatura);
        if(temperatura!=null) {
            TemperaturaActivity.temperatura = temperatura;
            TemperaturaActivity.refresh();
        }if(humedad!=null) {
            TemperaturaActivity.humedad = humedad;
            TemperaturaActivity.refresh();
        }
        textoHum.setText(humedad);
        textoPersonas.setText(personas);
        textoLuces.setText(estadoLuces);
        textoMedic.setText(medicamentos);

    }
    //funcion de cambio de peso
    public float cambioMedidaPeso (float pesoACambiar) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        DecimalFormat formato = new DecimalFormat("#.#");

        if (pref.getString("masa", "0").equals("1")) {
            float res;
            res = pesoACambiar * 0.157473f; //stones
            return Float.parseFloat(formato.format(res).replaceAll(",", "."));
        } else if (pref.getString("masa", "0").equals("2")) {
            float res;
            res = pesoACambiar * 2.20462f; //libras
            return Float.parseFloat(formato.format(res).replaceAll(",", "."));
        } else {
            return Float.parseFloat(formato.format(pesoACambiar).replaceAll(",", ".")); //kg
        }
    }

    // funcion cambio de altura
    public float cambioMedidaAltura (float alturaACambiar) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        DecimalFormat formato = new DecimalFormat("#.#");

        if (pref.getString("altura", "0").equals("1")) {
            float res;
            res = alturaACambiar * 0.0328084f; //stones
            return Float.parseFloat(formato.format(res).replaceAll(",", "."));
        } else if (pref.getString("altura", "0").equals("2")) {
            float res;
            res = alturaACambiar * 0.393701f;
            return Float.parseFloat(formato.format(res).replaceAll(",", "."));
        } else {
            return Float.parseFloat(formato.format(alturaACambiar).replaceAll(",", "."));
        }
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


}//()

