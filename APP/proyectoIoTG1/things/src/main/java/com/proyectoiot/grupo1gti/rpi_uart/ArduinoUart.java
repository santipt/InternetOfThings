package com.proyectoiot.grupo1gti.rpi_uart;

import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ArduinoUart {

    public UartDevice uartPrivada;

    public ArduinoUart(String nombre, int baudios) {
        try {
            uartPrivada = PeripheralManager.getInstance().openUartDevice(nombre);
            uartPrivada.setBaudrate(baudios);
            uartPrivada.setDataSize(8);
            uartPrivada.setParity(UartDevice.PARITY_NONE);
            uartPrivada.setStopBits(1);
        } catch (IOException e) {
            Log.w(TAG, "Error iniciando UART", e);
        }
    }
    public void escribir(String s) {
        try {
            int escritos = uartPrivada.write(s.getBytes(), s.length());
            Log.d(TAG, escritos+" bytes escritos en UART");
        } catch (IOException e) {
            Log.w(TAG, "Error al escribir en UART", e);
        }
    }
    public String leer() {
        String s = "";
        int len;
        final int maxCount = 8; // Máximo de datos leídos cada vez
        byte[] buffer = new byte[maxCount];
        try {
            do {
                len = uartPrivada.read(buffer, buffer.length);
                for (int i=0; i<len; i++) {
                    s += (char)buffer[i];
                }
            } while(len>0);
        } catch (IOException e) {
            Log.w(TAG, "Error al leer de UART", e);
        }
        return s;
    }
    public void cerrar() {
        if (uartPrivada != null) {
            try {
                uartPrivada.close();
                uartPrivada = null;
            } catch (IOException e) {
                Log.w(TAG, "Error cerrando UART", e);
            }
        }
    }
    static public List<String> disponibles() {
        return PeripheralManager.getInstance().getUartDeviceList();
    }

    public UartDevice getUartPrivada() {
        return uartPrivada;
    }

}
