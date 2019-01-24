const long A = 1000;     //Resistencia en oscuridad en KΩ
const int B = 15;        //Resistencia a la luz (10 Lux) en KΩ
const int Rc = 10;       //Resistencia calibracion en KΩ
const int LEDPin1 = 4;   //Pin del LED
 const int APin=34;
int V;
int ilum;
 bool aux=false;
 bool sePuedeEncender= false;
void iniciarLDR() 
{
  pinMode(LEDPin1, OUTPUT);
  
}
 
bool calcularLuminosidad(JsonObject& envio, char ( &texto )[500])
{
   V = analogRead(APin);         
 
   //ilum = ((long)(1024-V)*A*10)/((long)B*Rc*V);  //usar si LDR entre GND y A0 
   
   ilum = -((long)V*A*10)/((long)B*Rc*(1024-V));    //usar si LDR entre A0 y Vcc (como en el esquema anterior)
   if(ilum<0){
    ilum=-ilum;
    }
  
 if(aux){
  if(ilum <= 140){
    digitalWrite(LEDPin1, HIGH);
    envio["Luces"] = "s";
     envio.printTo(texto); 
    Serial.print("Enviando: ");
   // Serial.println(texto);
     // Serial.print(ilum);
        //udp.broadcastTo(texto, 1234); //se envía por el puerto 1234 el JSON como texto
        //delay(200);
    aux=false;
    sePuedeEncender = true;
    return true;
    }
  }else{
      if(ilum > 140){
   digitalWrite(LEDPin1, LOW);
    envio["Luces"] = "n";
     envio.printTo(texto); 
    Serial.print("Enviando: ");
   // Serial.println(texto);
      
        //udp.broadcastTo(texto, 1234); //se envía por el puerto 1234 el JSON como texto
        //delay(100);
    aux=true;
    sePuedeEncender = false;
    return true;
    }
  }
  return false;
}

 
