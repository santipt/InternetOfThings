package com.proyectoiot.grupo1gti.rpi_uart;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
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
import static android.os.SystemClock.sleep;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SERVICE_ID = "com.GTI.Grupo1.IoT";
    private static String nameNearby = "DomoHouse.zx45b";
    private String usuario = null;

    private ArduinoUart uart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Lista de UART disponibles: " + ArduinoUart.disponibles());
        uart = new ArduinoUart("MINIUART", 115200);

        sleep(2000);

        startAdvertising();
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
//                case "fecha":
//                    map.put(entry[0].trim(), fecha);
//
//                    break;
                default:
                    Log.d("Prueba Uart", "En el default del switch");
                    break;
            }
            //Log.d("Prueba UArt 1", entry[0] + entry[1]);
            //map.put(entry[0].trim(), Float.parseFloat(entry[1].trim()));          //add them to the hashmap and trim whitespaces
            //Log.d("Prueba Uart 2", map.get(entry[0]).toString());
        }
        map.put("fecha", fecha);
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
        stopAdvertising();
    }

    public void sendToFirestore (Map datos){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.i("Nearby", "Datos del usuario: " + usuario);

        if(usuario != null) {
//            db.collection("USUARIOS").document("n5Mt1LUqQNWsRH2Ny21YZbia1Dh2")
//                    .collection("Bascula").document().set(datos);
            db.collection("USUARIOS").document(usuario)
                    .collection("Bascula").document().set(datos);
            usuario = null;
        }

        System.out.println("Datos añadidos a bd" + datos);

    }

    //----------------------------------------------------------------------------------------------
    // Nearby connections
    //----------------------------------------------------------------------------------------------

    private void startAdvertising() {
        Nearby.getConnectionsClient(this).startAdvertising(
                nameNearby, SERVICE_ID, mConnectionLifecycleCallback,
                new AdvertisingOptions(Strategy.P2P_STAR))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override public void onSuccess(Void unusedResult) {
                        Log.i(TAG, "Estamos en modo anunciante!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "Error al comenzar el modo anunciante", e);
                    }
                });
    }

    private void stopAdvertising() {
        Nearby.getConnectionsClient(this).stopAdvertising();
        Log.i(TAG, "Detenido el modo anunciante!");
    }

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override public void onConnectionInitiated(
                        String endpointId, ConnectionInfo connectionInfo) {
                    // Aceptamos la conexión automáticamente en ambos lados.
                    Nearby.getConnectionsClient(getApplicationContext())
                            .acceptConnection(endpointId, mPayloadCallback);
                    Log.i(TAG, "Aceptando conexión entrante sin autenticación");
                }
                @Override public void onConnectionResult(String endpointId,
                                                         ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            Log.i(TAG, "Estamos conectados!");
                            stopAdvertising();
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            Log.i(TAG, "Conexión rechazada por uno o ambos lados");
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            Log.i(TAG, "Conexión perdida antes de ser aceptada");
                            break;
                    }
                }
                @Override
                public void onDisconnected(String endpointId) {
                    Log.i(TAG, "Desconexión del endpoint, no se pueden " +
                            "intercambiar más datos.");

                }
            };

    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override public void onPayloadReceived(String endpointId,
                                                Payload payload) {
            usuario = new String(payload.asBytes());
            Log.i(TAG, "Se ha recibido una transferencia desde (" +
                    endpointId + ") con el siguiente contenido: " + usuario);
            disconnect(endpointId);
        }
        @Override public void onPayloadTransferUpdate(String endpointId,
                                                      PayloadTransferUpdate update) {
            // Actualizaciones sobre el proceso de transferencia
        }
    };

    protected void disconnect(String endpointId) {
        Nearby.getConnectionsClient(this)
                .disconnectFromEndpoint(endpointId);
        Log.i(TAG, "Desconectado del endpoint (" + endpointId + ").");
        startAdvertising();
    }

}
