
/*
====================================================================
 PROGRAMA UNICO - DHT11 + USB (JAVA + BD) + BLUETOOTH HC-05 (APP)
====================================================================
 
 O QUE ESTE PROGRAMA FAZ?
 - Le a temperatura (Celsius) e a umidade do ar com o DHT11.
 - Envia cada leitura pela USB para o programa Java (Java + BD).
 - Envia a mesma leitura pelo Bluetooth HC-05 para o app mobile.
 
 LIGACOES (iguais aos seus programas 01 e 02, que ja funcionam):
 - DHT11 VCC  -> 5V
 - DHT11 DATA -> pino digital 10
 - DHT11 GND  -> GND
 - HC-05 TXD  -> pino 4 do Arduino (RX do SoftwareSerial)
 - HC-05 RXD  <- pino 5 do Arduino (TX do SoftwareSerial),
                 com divisor resistivo (5 V -> ~3,3 V)
 - HC-05 GND  -> GND
 - HC-05 VCC  -> alimentacao conforme a sua placa HC-05
 
 FORMATO DAS MENSAGENS (uma linha por leitura, a cada 2 segundos):
 - USB (Java):        25.30;60.00
                      temperatura;umidade
 - Bluetooth (app):   25.30;60.00;0
                      temperatura;umidade;alerta
   alerta = 1 quando a temperatura chega ao LIMITE_CRITICO, senao 0.
 - Se o sensor falhar, as duas saidas enviam a linha: ERRO
 
 IMPORTANTE:
 - Feche o Monitor Serial da IDE do Arduino antes de abrir o
   programa Java: so um programa por vez usa a porta USB.
 - Baud rate: 9600 na USB e no HC-05.
====================================================================
*/
 
#include "DHT.h"            // Biblioteca do sensor DHT11
#include <SoftwareSerial.h> // Permite usar os pinos 4 e 5 como serial
 
// ===== CONFIGURACOES =====
const float LIMITE_CRITICO = 35.0; // Acima ou igual a isso o app recebe alerta = 1
 
float u, t; // u guarda a umidade; t guarda a temperatura.
 
// Sensor DHT11 no pino 10 (igual ao seu programa 01).
DHT DHT_sens(10, DHT11);
 
// HC-05: RX = pino 4 (Arduino recebe); TX = pino 5 (Arduino envia).
SoftwareSerial HC05(4, 5);
 
// setup() executa UMA VEZ quando o Arduino liga ou reinicia.
void setup() {
  DHT_sens.begin();   // Prepara o sensor.
  Serial.begin(9600); // USB -> Java.
  HC05.begin(9600);   // HC-05 -> app.
}
 
// loop() executa REPETIDAMENTE enquanto o Arduino estiver ligado.
void loop() {
 
  // ===== ETAPA 1: LER O SENSOR =====
  u = DHT_sens.readHumidity();
  t = DHT_sens.readTemperature();
 
  // ===== ETAPA 2: VERIFICAR SE A LEITURA DEU CERTO =====
  if (isnan(u) || isnan(t)) {
    // Mensagem curta, para os programas (Java/app) ignorarem a linha.
    Serial.println("ERRO");
    HC05.println("ERRO");
    delay(2000);
    return;
  }
 
  // ===== ETAPA 3: ENVIAR PARA O JAVA (USB) =====
  // Formato: temperatura;umidade   (ex.: 25.30;60.00)
  Serial.print(t, 2);
  Serial.print(";");
  Serial.println(u, 2);
 
  // ===== ETAPA 4: ENVIAR PARA O APP (BLUETOOTH) =====
  // Formato: temperatura;umidade;alerta   (ex.: 25.30;60.00;0)
  int alerta = (t >= LIMITE_CRITICO) ? 1 : 0;
  HC05.print(t, 2);
  HC05.print(";");
  HC05.print(u, 2);
  HC05.print(";");
  HC05.println(alerta);
 
  delay(2000); // Espera 2 segundos antes da proxima leitura.
}
 