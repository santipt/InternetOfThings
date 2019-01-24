package com.GTI.Grupo1.IoT;

import java.util.ArrayList;
import java.util.List;

public class SensoresVector implements SensoresInterface {

        protected List<Sensores> vectorLugares = ejemploSensores();

        public SensoresVector() {
            vectorLugares = ejemploSensores();
        }

        public Sensores elemento(int id) {
            return vectorLugares.get(id);
        }

        public void anyade(Sensores sensor) {
            vectorLugares.add(sensor);
        }

        public int nuevo() {
            Sensores sensor = new Sensores();
            vectorLugares.add(sensor);
            return vectorLugares.size()-1;
        }
        public void borrar(int id) {
            vectorLugares.remove(id);
        }

        public int tamanyo() {
            return vectorLugares.size();
        }

        public void actualiza(int id, Sensores sensor) {
            vectorLugares.set(id, sensor);
        }

        public static ArrayList<Sensores> ejemploSensores() {
            ArrayList<Sensores> sensor = new ArrayList<Sensores>();
            sensor.add(new Sensores("Iluminacion",
                    "Encendido", TipoSensor.ILUMINACION));
            sensor.add(new Sensores("Incendio",
                    "Tu casa no se está quemando", TipoSensor.FUEGO));
            sensor.add(new Sensores("Temperatura",
                    "22ºC", TipoSensor.TEMPERATURA));
            sensor.add(new Sensores("Humedad",
                    "45%", TipoSensor.HUMEDAD));
            sensor.add(new Sensores("Puerta",
                    "Cerrada", TipoSensor.PUERTA));
            sensor.add(new Sensores("Puerta",
                    "Abierta", TipoSensor.PUERTA));
            sensor.add(new Sensores("Personas",
                    "2", TipoSensor.PERSONAS));

            return sensor;
        }
}
