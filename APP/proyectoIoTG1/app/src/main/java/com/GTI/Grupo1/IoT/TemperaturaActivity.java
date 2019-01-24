package com.GTI.Grupo1.IoT;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;


public class TemperaturaActivity extends Activity {

    public static int numero = 20;
    public static String temperatura ="0";
    public static String humedad ="0";

    static TextView t;
    static TextView textoTemp;
    static TextView textoHum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temperatura);

        t = findViewById(R.id.cambiarValor);
        t.setText(String.valueOf(numero));

        textoTemp = findViewById(R.id.textotempActual);
        textoHum = findViewById(R.id.textoHumActual);

    }
    public void Sumar (View view){
        TextView t = findViewById(R.id.cambiarValor);
        numero++;
        t.setText(String.valueOf(numero));
    }
    public void Restar (View view){
        TextView t = findViewById(R.id.cambiarValor);
        numero--;
        t.setText(String.valueOf(numero));
    }

    public static void refresh(){
        if(temperatura!=null&&textoTemp!=null) {
            textoTemp.setText(temperatura);
        }else if(humedad!=null&&textoHum!=null) {
            textoHum.setText(humedad);
        }
    }
}//()
