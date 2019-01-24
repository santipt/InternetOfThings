package com.proyectoiot.grupo1gti.rpi_uart;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ArduinoUart uart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Lista de UART disponibles: " + ArduinoUart.disponibles());
        uart = new ArduinoUart("UART0", 115200);

    }

    public void readUartBuffer(UartDevice uart) throws IOException {
//        // Maximum amount of data to read at one time
//        final int maxCount =8;
//        byte[] buffer = new byte[maxCount];
//
//        int count;
//        while ((count = uart.read(buffer, buffer.length)) > 0) {
//            Log.d(TAG, "Read " + count + " bytes from peripheral");
//        }
        String datos;
        try {
            datos = this.uart.leer();
            Log.d("Pruebas Uart", datos);
            datos = datos.substring(1, datos.length() - 1);           //remove curly brackets
        }catch (Exception e){
            Log.e("Uart", "Error en uart" + e);
            return;
        }

        String[] keyValuePairs = datos.split(",");              //split the string to creat key-value pairs
        Map<String,Object> map = new HashMap<>();
        Date date = new Date();
        Timestamp fecha = new Timestamp(date);

        for(String pair : keyValuePairs)                        //iterate over the pairs
        {
            String[] entry = pair.split(":");                   //split the pairs to get key and value
            entry[0] = entry[0].substring(1, entry[0].length()-1);
            switch (entry[0]){
                case "peso":
                    map.put(entry[0].trim(), Float.parseFloat(entry[1].trim()));
                    break;
                case "altura":
                    map.put(entry[0].trim(), Integer.parseInt(entry[1].trim()));
                    break;
                case "fecha":
                    map.put(entry[0].trim(), fecha);

                    break;
                default:
                    Log.d("Prueba Uart", "En el default del switch");
                    break;
            }
            //Log.d("Prueba UArt 1", entry[0] + entry[1]);
            //map.put(entry[0].trim(), Float.parseFloat(entry[1].trim()));          //add them to the hashmap and trim whitespaces
            //Log.d("Prueba Uart 2", map.get(entry[0]).toString());
        }

        //Log.d("Prueba Uart 3", map.get("peso").toString());
        sendToFirestore(map);
        Log.d("Test BD", "Despues de la funcion senToFirestore");
    }

    private UartDeviceCallback mUartCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            // Read available data from the UART device
            try {
                readUartBuffer(uart);
            } catch (IOException e) {
                Log.w(TAG, "Unable to access UART device", e);
            }

            // Continue listening for more interrupts
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };

    @Override
    protected void onStart(){
        super.onStart();

        try {
            uart.uartPrivada.registerUartDeviceCallback(mUartCallback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Interrupt events no longer necessary
        uart.uartPrivada.unregisterUartDeviceCallback(mUartCallback);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void sendToFirestore (Map datos){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("USUARIOS").document("n5Mt1LUqQNWsRH2Ny21YZbia1Dh2")
                .collection("Bascula").document().set(datos);

        System.out.println("Datos añadidos a bd");

    }



}
