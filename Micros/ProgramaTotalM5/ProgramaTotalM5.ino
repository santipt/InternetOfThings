/*
 * Programa que recibe los datos, en formato texto, por UDP, via wifi, del ESP32
 * Los convierte los datos a json
 * Guarda la hora del momento en que recibe cada dato y lo almacena junto a el
 * Transmite los datos por la UART a la RPI
 * También publica alertas con MQTT
 * Implementado: Wifi, UDP, Tiempo autonomo, conexion UART, MQTT, Formato JSON.
 */
#define BLANCO 0XFFFF
#define NEGRO 0
#define ROJO 0xF800
#define VERDE 0x07E0
#define AZUL 0x001F
#include <SPI.h>
#include <MFRC522.h>
#include <M5Stack.h>
#include "WiFi.h"
#include <MQTT.h>
#include "AsyncUDP.h"
#include "time.h"
#include <ArduinoJson.h>
#include <string> 
#define RST_PIN 2 //Pin 9 para el reset del RC522 no es necesario conctarlo
#define SS_PIN 21 //Pin 10 para el SS (SDA) del RC522
MFRC522 mfrc522(SS_PIN, RST_PIN); ///Creamos el objeto para el RC522
MFRC522::StatusCode status; //variable to get card status
const int EchoPin = 2;
const int TriggerPin = 16;
const int EchoPin1 = 5;
const int TriggerPin1 = 26;

const int MQ_PIN = 36;

 const char * ssid = "Grupo1";
const char * password = "123456789";
const char* ntpServer = "pool.ntp.org";
const long  gmtOffset_sec = 3600;
const int   daylightOffset_sec = 3600;
const char broker[] = "iot.eclipse.org";

int hora;
boolean rec = 0;

const char* puerta;
const char* movimiento;
const char* incendio;
const char* luz;
const char* temperatura;
const char* humedad;

bool Luces=false;
char texto[500];
int altura;
int peso;

AsyncUDP udp;
WiFiClient net;
MQTTClient client;
unsigned long lastMillis = 0;
struct tm timeinfo;

void connect() {
 
 while (WiFi.status() != WL_CONNECTED) {
 
 delay(1000);
 }
 

 while (!client.connect("4b64416e74a44de9b6da0cd9bdbd2cc9", "try", "try")) {
 
 delay(1000);
 }
 
 client.subscribe("Grupo1/practica/POWER");
 //client.unsubscribe("<usuario>/practica/#");
}
void messageReceived(String &topic, String &payload) {
if(&payload[0]=="O"&& &payload[1]=="F"&& &payload[2]=="F"){
  Luces= false;
  }else{
    Luces= true;
    }
}


void setup() {
  
  M5.begin();
  
  M5.Lcd.setTextSize(2); //Tamaño del texto
  
   Serial.begin(115200);
   
   SPI.begin(); //Iniciamos el Bus SPI
mfrc522.PCD_Init(); // Iniciamos el MFRC522

M5.Lcd.setCursor(30, 10);
M5.Lcd.setTextColor(BLANCO);
M5.Lcd.println("PASE MEDICAMENTO");
   pinMode(TriggerPin, OUTPUT);
   pinMode(EchoPin, INPUT);
   pinMode(TriggerPin1, OUTPUT);
   pinMode(EchoPin1, INPUT);
   pinMode(17, OUTPUT);

    WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password); //Inicializamos la conexión
  while (WiFi.status() != WL_CONNECTED) {
    
    delay(500);
    
  }
  

  
  if (udp.listen(1234)) {
    
    udp.onPacket([](AsyncUDPPacket packet) {

      int i = 200;
      while (i--) {
        *(texto + i) = *(packet.data() + i);
      }
      rec = 1;      //indica mensaje recibido

    });
  }
  client.begin(broker, net);
 client.onMessage(messageReceived);
 connect();
   delay(2000);
}

 int contadorPersonas=0;
 bool haPasadoAlguien=false;
 bool luces=false;
 bool sensor1=false;
 bool sensor2=false;
 bool acabaDeContar=false;
 bool acabaDeDescontar=false;
 
 byte ActualUID[7]; //almacenará el código del Tag leído
byte Medicamento1[7]= {0x04, 0x33, 0xC1, 0x5A, 0x51, 0x59, 0x80} ; //código del usuario 1
byte Medicamento2[7]= {0x04, 0x2A, 0xC1, 0x5A, 0x51, 0x59, 0x80} ; //código del usuario 2
byte Medicamento3[7]= {0x04, 0x21, 0xC1, 0x5A, 0x51, 0x59, 0x80} ; //código del usuario 3
byte Medicamento4[7]= {0x04, 0x19, 0xC1, 0x5A, 0x51, 0x59, 0x80} ; //código del usuario 4


int reloj=0;

void loop() {

  //MQTT
   client.loop();
   client.onMessage(messageReceived);
 delay(10); // <- Esperamos a que WiFi sea estable
 if (!client.connected()) {
  
 connect();
 }

//Contador de personas
   int cm = ping(TriggerPin, EchoPin);
   
   //delay(500);
   int cm1 = ping(TriggerPin1, EchoPin1);
 
   if(cm < 100){
    sensor1=true;
    }else{
      sensor1=false;
      }
       if(cm1 < 100){
    sensor2=true;
    }else{
      sensor2=false;
      }
      if(sensor1==true && sensor2==true){
      
        haPasadoAlguien=true;
       
        }
        if(sensor1==true && sensor2==false){
          if(haPasadoAlguien==true){
          
            haPasadoAlguien=false;
            if(!acabaDeDescontar){
             contadorPersonas--;
             acabaDeDescontar=true;
            }
          }
        }
          if(sensor1==false && sensor2==true){
            if(haPasadoAlguien==true){
              haPasadoAlguien=false;
               if(!acabaDeContar){
        contadorPersonas++;
        acabaDeContar=true;
      }
              if(contadorPersonas != 0){
                if(luces==false){
                  luces=true;
                  }
                }
              }
            }
            if(contadorPersonas==0){
              if(luces==true){
                luces=false;
                }
              }
              if(sensor1==false && sensor2==false){
                haPasadoAlguien=false;
                acabaDeContar=false;
                acabaDeDescontar=false;
                }
                if(contadorPersonas<=0){
                  contadorPersonas=0;
                  }
              if(luces){
                digitalWrite(17, HIGH);
               }else{
                  digitalWrite(17, LOW);
                  }

                  
                  //-----------------------GAS--------------------------------------------

  int raw_adc = analogRead(MQ_PIN);
  float value_adc = raw_adc * (5.0 / 1023.0);
  if (value_adc > 4)
  {
    client.publish("Grupo1/practica/alertas/gas", "¡Atención! Posible fuga de gas");
  }
                  
                  //------------------------medicamentos--------------------------------
                  // Revisamos si hay nuevas tarjetas presentes
if ( mfrc522.PICC_IsNewCardPresent())
{
//Seleccionamos una tarjeta
if ( mfrc522.PICC_ReadCardSerial())
{
// Enviamos serialemente su UID

M5.Lcd.setCursor(0, 30);
M5.Lcd.fillScreen(NEGRO);
M5.Lcd.setTextColor(AZUL);
M5.Lcd.print(F("MEDICAMENTO:"));
for (byte i = 0; i < mfrc522.uid.size; i++) {

M5.Lcd.print(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " ");

M5.Lcd.print(mfrc522.uid.uidByte[i], HEX);
ActualUID[i]=mfrc522.uid.uidByte[i];
}

M5.Lcd.print(" "); 
//comparamos los UID para determinar si es uno de nuestros usuarios
if(compareArray(ActualUID,Medicamento1,7))
{
M5.Lcd.setCursor(0, 60);
M5.Lcd.setTextColor(VERDE);
M5.Lcd.println("Medicamento 1");
lectura_datos();
si();
client.publish("Grupo1/practica/medicamentos", "Este medicamento está en su lista");
}
else if(compareArray(ActualUID,Medicamento2,7))
{
M5.Lcd.setCursor(0, 60);
M5.Lcd.setTextColor(VERDE);
M5.Lcd.println("Medicamento 2");
lectura_datos();
si();
client.publish("Grupo1/practica/medicamentos", "Este medicamento está en su lista");
}
else if(compareArray(ActualUID,Medicamento3,7))
{
M5.Lcd.setCursor(0, 60);
M5.Lcd.setTextColor(VERDE);
M5.Lcd.println("Medicamento 3");
lectura_datos();
si();
client.publish("Grupo1/practica/medicamentos", "Este medicamento está en su lista");
}
else if(compareArray(ActualUID,Medicamento2,7))
{
M5.Lcd.setCursor(0, 60);
M5.Lcd.setTextColor(VERDE);
M5.Lcd.println("Medicamento 4");
lectura_datos();
si();
client.publish("Grupo1/practica/medicamentos", "Este medicamento está en su lista");
}
else
{
M5.Lcd.setCursor(0, 60);
M5.Lcd.setTextColor(ROJO);
M5.Lcd.println("?????");
no();
client.publish("Grupo1/practica/medicamentos", "Este medicamento NO está en su lista, no lo consuma");
}
// Terminamos la lectura de la tarjeta tarjeta actual
mfrc522.PICC_HaltA();
M5.Lcd.setCursor(30, 140);
M5.Lcd.setTextColor(BLANCO);
M5.Lcd.println("PASE OTRO MEDICAMENTO");
}
}

//UDP
   if (rec) {
    //Send broadcast
    rec = 0;            //mensaje procesado

    //udp.broadcastTo("Recibido", 1234);  //envia confirmacion
    //udp.broadcastTo(texto, 1234);       //y dato recibido
    //hora = atol(texto);                 //paso de texto a entero
   

    StaticJsonBuffer<500> jsonBufferRecv; //definición del buffer para almacenar el objero JSON, 200 máximo
    JsonObject& recibo = jsonBufferRecv.parseObject(texto); //paso de texto a formato JSON
    //recibo.printTo(Serial);       //envio por el puerto serie el objeto "recibido"
//if(recibo["Altura"] != NULL){
    altura = recibo["Altura"];
    Serial.print(altura);
//}
//if(recibo["Peso"] != NULL){
    peso = recibo["Peso"];
     Serial.print(peso);
//}
//if(recibo["Estado"] != NULL){
    puerta = recibo["Estado"];
     Serial.print(puerta);
//}
//if(recibo["EstadoM"] != NULL){
    movimiento = recibo["EstadoM"];
     Serial.print(movimiento);
//}
//if(recibo["Incendio"] != NULL){
    incendio = recibo["Incendio"];
     Serial.print(incendio);
//}
//if(recibo["Luces"] != NULL){
    luz = recibo["Luces"];
     Serial.print(luz);
//}
//if(recibo["Temperatura"] != NULL){
    temperatura = recibo["Temperatura"];
     Serial.print(temperatura);
//}
//if(recibo["Humedad"] != NULL){
    humedad = recibo["Humedad"];
     Serial.print(humedad);
//}
    
    if((int)peso > 2){
      char texto2[100];
       StaticJsonBuffer<100> jsonBufferRecv2; //definición del buffer para almacenar el objero JSON, 200 máximo
    JsonObject& recibo2 = jsonBufferRecv2.createObject(); //paso de texto a formato JSON
    time_t now;
    
String Altura= String(altura);
String Peso=String(peso);

time(&now);

String Now = String(now);
//Serial.print(Peso);
recibo2["peso"]=Peso.toFloat();
recibo2["altura"]=Altura.toInt();
recibo2["fecha"]=Now.toInt();
recibo2.printTo(texto2);
Serial.write(texto2);
    
    }
    String Temperatura = String(temperatura);
  

 client.publish("Grupo1/practica/temperatura", Temperatura+"℃");
   String Humedad = String(humedad);
  

 client.publish("Grupo1/practica/humedad", Humedad+"%");
 String Incendio = String(incendio);
String Presencia = String(movimiento);

if (Incendio=="s") {

 client.publish("Grupo1/practica/alertas/incendio", "¡Atención! Hay un incendio");
 }

  if (contadorPersonas==0&&Presencia=="s") { //Hay movimiento sin que haya habido nadie en la casa

 client.publish("Grupo1/practica/alertas/intruso", "¡Atención! Podría haber un intruso en la casa");
 }
  String Luz = String(luz);
  if (contadorPersonas!=0&&Luz=="s") {  //Hay alguien  en la casa y el LDR indica que hace falta luz
Luces=true;
 client.publish("Grupo1/practica/cmnd/POWER", "ON");
 }
  if (Luces&&Luz=="n"&&contadorPersonas!=0) { //Las luces están encendidas pero el LDR no capta luminosidad

 client.publish("Grupo1/practica/alertas/apagón", "Es posible que haya habido un apagón");
 }
    String Puerta = String(puerta);
  if (contadorPersonas==0&&Puerta=="s") { //La puerta está abierta sin que haya nadie

 client.publish("Grupo1/practica/alertas/puerta", "¡Alguien se ha dejado la puerta abierta sin que haya nadie!");
 }

   if (Puerta=="s") { 

 client.publish("Grupo1/practica/puerta", "Abierta");
 }else{
  client.publish("Grupo1/practica/puerta", "Cerrada");
  }

if(Presencia=="s"||contadorPersonas==0||Luces==false){ //Se reinicia el contador de letarguidad cada vez que la casa se vacía, hay movimiento
  reloj=0;
  }
if(reloj==100&&Luces){
  
  client.publish("Grupo1/practica/alertas/letargo", "Alguien no se ha movido en un tiempo con las luces encendidas, podría estar inconsciente");
  }

                  }//recibe datos
                  
                   if (acabaDeContar||acabaDeDescontar) {
String myString = String(contadorPersonas);
 client.publish("Grupo1/practica/personas", "Hay "+myString+" personas en la casa");
   if(contadorPersonas==0){
 
  client.publish("Grupo1/practica/cmnd/POWER", "OFF");
   Luces=false;
  }
 }
 
     
 reloj++;
                  delay(100);
}










//Función para comparar dos vectores
boolean compareArray(byte array1[],byte array2[], int n_byte)
{
for (int i=0; i<n_byte; i++)
{
if(array1[i] != array2[i])return(false);
}
return(true);
}
void si ()
{M5.Lcd.setTextSize(4);
M5.Lcd.setCursor(150, 90);
M5.Lcd.setTextColor(VERDE);
M5.Lcd.println("SI");
M5.Lcd.setTextSize(2);
}
void no ()
{M5.Lcd.setTextSize(4);
M5.Lcd.setCursor(150, 90);
M5.Lcd.setTextColor(ROJO);
M5.Lcd.println("NO");
M5.Lcd.setTextSize(2);
}
void lectura_datos()
{
byte buffer_1[18]; //buffer intermedio para leer 16 bytes
byte buffer[66]; //data transfer buffer (64+2 bytes data+CRC)
byte tam = sizeof(buffer);
byte tam1= sizeof(buffer_1);
uint8_t pageAddr = 0x06; //In this example we will write/read 50 bytes (page 6,7,8 hasta la 18).
//Ultraligth mem = 16 pages. 4 bytes per page.
//Pages 0 to 4 are for special functions.
// Read data ***************************************************
//En esta función los datos se leen de 16 bytes en 16 y se almacenan en buffer_1 (de 16+2 bytes)
//para despues transferirlos a buffer que tiene un tamaño mayor
//Serial.println(F("Reading data ... "));
for (int i=0; i<(tam-2)/16; i++)
{
//data in 4 block is readed at once 4 bloques de 4 bytes total 16 bytes en cada lectura.
status = (MFRC522::StatusCode) mfrc522.MIFARE_Read(pageAddr+i*4, buffer_1, &tam1);
// if (status != MFRC522::STATUS_OK) {

// return;
// }
//copio los datos leidos en buffer_1 a la posición correspondiente del buffer
for (int j=0; j<16; j++)
{
buffer[j+i*16]=buffer_1[j];
}
}
//Presentacion de los datos ledidos por el puerto serie y por el M5Stack
//Serial.print(F("Readed data: "));
//Dump a byte array to Serial
for (byte i = 0; i < (tam-2); i++) {
//Serial.write(buffer[i]);
}
M5.Lcd.setTextSize(2);
M5.Lcd.setCursor(0, 160);
M5.Lcd.setTextColor(VERDE);
for (byte i = 0; i < (tam-2); i++) {
M5.Lcd.print((char)buffer[i]);
}
                  //---------------------------------------------------------------------------
               
     
  }
                  
   

 
int ping(int TriggerPin2, int EchoPin2) {
   long duration, distanceCm;
   
   digitalWrite(TriggerPin2, LOW);  //para generar un pulso limpio ponemos a LOW 4us
   delayMicroseconds(4);
   digitalWrite(TriggerPin2, HIGH);  //generamos Trigger (disparo) de 10us
   delayMicroseconds(10);
   digitalWrite(TriggerPin2, LOW);
   
   duration = pulseIn(EchoPin2, HIGH);  //medimos el tiempo entre pulsos, en microsegundos
   
   distanceCm = duration * 10 / 292/ 2;   //convertimos a distancia, en cm
   return distanceCm;
}
