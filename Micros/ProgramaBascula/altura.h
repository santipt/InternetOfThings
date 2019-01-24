#include <M5Stack.h>
#include "WiFi.h"
#include "AsyncUDP.h"


const int EchoPin = 15;
const int TriggerPin = 13;
//const int botonAuxiliar = 4;

void configuracionAltura()
{
  pinMode(TriggerPin, OUTPUT);
  pinMode(EchoPin, INPUT);
  //pinMode(botonAuxiliar, INPUT_PULLUP);
}

//------------------------------------------------------------------------------------------------
//  Funciones
//------------------------------------------------------------------------------------------------

int distancia () {
  long duracion, distanciaCM;
  digitalWrite(TriggerPin, LOW);
  delayMicroseconds(4);
  digitalWrite(TriggerPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(TriggerPin, LOW);
  duracion = pulseIn(EchoPin, HIGH) * 3;
  distanciaCM = (((duracion * 10) / 292) / 2); //medicion en cm //mínimo de 4 cm a máximo de 3 m
  distanciaCM = 206 - distanciaCM;
  return distanciaCM;
}

