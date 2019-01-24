
 //creación del objeto "envio"


//Constantes y variables globales
const int Pin = 25;
bool puertaAbierta = false;

void configuracionPuerta(JsonObject& envio) {
  // put your setup code here, to run once:
 
  pinMode(Pin, INPUT_PULLUP);
   char texto[1000];
   if (digitalRead(Pin) == LOW) {
    
     envio["Puerta"] = "Principal";
    envio["Estado"] = "n";
    envio.printTo(texto);         //paso del objeto "envio" a texto para transmitirlo
    Serial.print("Enviando: ");
    Serial.println(texto);
        
        puertaAbierta = false;
        //udp.broadcastTo(texto, 1234); //se envía por el puerto 1234 el JSON como texto
        delay(200);
       
      
    }else if(digitalRead(Pin) == HIGH){
      
     envio["Puerta"] = "Principal";
    envio["Estado"] = "s";
    envio.printTo(texto); 
    Serial.print("Enviando: ");
    Serial.println(texto);
        puertaAbierta = true;
        //udp.broadcastTo(texto, 1234); //se envía por el puerto 1234 el JSON como texto
        delay(200);

      }
}

//PUERTA
bool lecturaPuerta(JsonObject& envio, char ( &texto )[500]){
  

  if (puertaAbierta) {
    if (digitalRead(Pin) == LOW) {
      delay(1000);
     envio["Puerta"] = "Principal";
    envio["Estado"] = "n";
    envio.printTo(texto);         //paso del objeto "envio" a texto para transmitirlo
    Serial.print("Enviando: ");
    //Serial.println(texto);
        
        puertaAbierta = false;
        //udp.broadcastTo(texto, 1234); //se envía por el puerto 1234 el JSON como texto
        delay(200);
       return true;
      
    }

  } else {
    if (digitalRead(Pin) == HIGH) {
      delay(1000);
     envio["Puerta"] = "Principal";
    envio["Estado"] = "s";
    envio.printTo(texto); 
    Serial.print("Enviando: ");
    //Serial.println(texto);
        puertaAbierta = true;
        //udp.broadcastTo(texto, 1234); //se envía por el puerto 1234 el JSON como texto
        delay(200);

        
      return true;

    }
  }
  return false;
}//lecturaPuerta()
