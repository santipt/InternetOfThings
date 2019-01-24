const int LEDPin = 5;        // pin para el LED
const int PIRPin = 16;         // pin de entrada (para PIR sensor)
 
int pirState = LOW;           // de inicio no hay movimiento
int val = 0;                  // estado del pin

 //creación del objeto "envio"
 
void configuracionSensorMovimiento() 
{
   pinMode(LEDPin, OUTPUT); 
   pinMode(PIRPin, INPUT);
}
 
bool lecturaMovimiento(JsonObject& envio, char (&texto)[500])
{
   
   val = digitalRead(PIRPin);
  
   if (val == HIGH)   //si está activado
   { 
      digitalWrite(LEDPin, HIGH);  //LED ON
      if (pirState == LOW)  //si previamente estaba apagado
      {
         envio["Habitación"] = "Comedor";
        envio["EstadoM"] = "s";
        envio.printTo(texto);         //paso del objeto "envio" a texto para transmitirlo
        //udp.broadcastTo(texto, 1234); //se envía por el puerto 1234 el JSON como texto
        Serial.print("Enviando: ");
       // Serial.println(texto);
        pirState = HIGH; //Hay alguien
      return true;
      }
   } 
   else   //si esta desactivado
   {
      digitalWrite(LEDPin, LOW); // LED OFF
      if (pirState == HIGH)  //si previamente estaba encendido
      {
        envio["Habitación"] = "Comedor";
        envio["EstadoM"] = "n";
        envio.printTo(texto);         //paso del objeto "envio" a texto para transmitirlo
        //udp.broadcastTo(texto, 1234); //se envía por el puerto 1234 el JSON como texto
        Serial.print("Enviando: ");
       // Serial.println(texto);
        pirState = LOW; //No hay nadie
       return true;
      }
   }
   return false;
}
