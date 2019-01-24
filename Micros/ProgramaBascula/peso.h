#include "soc/rtc.h"; 
#include "HX711.h"
#include <M5Stack.h>

//#define DOUT 5
#define DOUT  16
#define CLK  2

HX711 balanza(DOUT, CLK);

void configuracionPeso() {
  
  rtc_clk_cpu_freq_set(RTC_CPU_FREQ_80M); //bajo la frecuencia a 80MHz
  /*Serial.print("Lectura del valor del ADC:  ");
  Serial.println(balanza.read());
  Serial.println("No ponga ningun  objeto sobre la balanza");
  Serial.println("Destarando...");*/
  int dato = 50600 / 2;
  balanza.set_scale(dato); //La escala por defecto es 1
  balanza.tare(20);  //El peso actual es considerado Tara.
  //Serial.println("Coloque un peso conocido:");
  
}


float peso() {
  
  //Serial.print("Valor de lectura:  ");
  float medida = balanza.get_units(20);
  //Serial.print(medida);
  if(medida <= 0 && medida<=0.6){
    medida=0;
  }
  //Serial.println(medida,3);
  //delay(100);
  return medida;
}
