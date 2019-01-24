const int sensorPin = 26;
 bool fuego = false;

void LlamaSetup()
{
 
   pinMode(sensorPin, INPUT);
}
 
bool medirFuego(JsonObject& envio, char ( &texto )[500])
{
   if (fuego) {
    if (digitalRead(sensorPin) == HIGH) {
      delay(1000);
 
    envio["Incendio"] = "n";
    envio.printTo(texto);         //paso del objeto "envio" a texto para transmitirlo
    Serial.print("Enviando: ");
    //Serial.println(texto);
        
        fuego = false;
        //udp.broadcastTo(texto, 1234); //se envía por el puerto 1234 el JSON como texto
        delay(200);
       return true;
      
    }

  } else {
    if (digitalRead(sensorPin) == LOW) {
      delay(1000);
     
    envio["Incendio"] = "s";
    envio.printTo(texto); 
    Serial.print("Enviando: ");
    //Serial.println(texto);
       fuego = true;
        //udp.broadcastTo(texto, 1234); //se envía por el puerto 1234 el JSON como texto
        delay(200);

        return true;
      

    }
  }
  return false;
}
