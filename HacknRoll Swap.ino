#include <ESP8266WiFi.h>

int fsrAnalogPin = A0; // FSR is connected to analog 0
int LEDpin = 15;      // connect Red LED to pin 11 (PWM pin)
int fsrReading;      // the analog reading from the FSR resistor divider
int LEDbrightness;
int previousstate=0;
int currentstate=0;
int counter=0;
int trigger=0;
int* triggerpt = &trigger;
int value = 0;
unsigned long uploadtime = millis();
const char* host = "ericteo50dot001xc7e.appspot.com";
const char* streamId   = "....................";
const char* privateKey = "....................";
const char* ssid     = "LTF (2)";
const char* password = "tengfoonglam";
int toolong = 0;
int* toolongpt = &toolong;
long starttime = 0;
long* starttimept = &starttime;
long toolongtimelimit = 3000;

//const char* ssid     = "HacknRoll Legacy";
//const char* password = "hacknroll";

void checktoolong(int *len, long *start,int state, long timelimit){
  if (state==1){
  
    if ((millis()-*start)>timelimit){
      *len = 1;
    }

    return;
  }else{
    *start = millis();
    *len = 0;
  }
}

int a2d(int reading, int threshold){
  if (reading < threshold){
    return 0;
  }else{
    return 1;
  }
}

int stepmeasure(int prev, int current, int* trig){
  if (prev == current){
    return 0;
  }
  else if (prev > current){ // leg raised up
    *trig = 1;
    return 0;
  }
  else if (prev < current && *trig == 1){ //leg raised down
    *trig = 0;
    return 1;
  }else{
    *trig = 0;
    return 0;
  }
}

void setup(void) {
  Serial.begin(9600);   // We'll send debugging information via the Serial monitor
  pinMode(LEDpin, OUTPUT);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
  delay(500);
  Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");  
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void loop(void) {
  fsrReading = analogRead(fsrAnalogPin);  
//  Serial.print("Analog reading = ");
//  Serial.println(fsrReading);

  currentstate = a2d(fsrReading, 800);
  int oldcounter;
  oldcounter = counter;
  counter += stepmeasure(previousstate, currentstate, triggerpt);
  previousstate = currentstate;
  if(oldcounter != counter){
//     erial.println(counter);
  }

//  Serial.print("outside");
//  Serial.println(millis());
//  Serial.println(timeout);
  
  if ((millis() - uploadtime) > 5000) {
//      Serial.print("inside");
//      Serial.println(millis());

      WiFiClient client;
      const int httpPort = 80;
      if (!client.connect(host, httpPort)) {
          Serial.println("connection failed");
          
            WiFi.begin(ssid, password);
          
            Serial.println("");
            Serial.println("WiFi connected");  
            Serial.println("IP address: ");
            Serial.println(WiFi.localIP());
            return;
            }
       // We now create a URI for the request
//      String url = "/input/";
//      url += streamId;
//      url += "?private_key=";
//      url += privateKey;
//      url += "&value=";
//      url += value;
      
//        Serial.print("Requesting URL...");
//      Serial.println(url);
      
      // This will send the request to the server
      client.print(String("GET") + " /?steps="+String(counter)+"&too_long="+String(toolong)+" HTTP/1.1\r\n" +
"Host: ericteo50dot001xc7e.appspot.com\r\n" +
"Connection: keep-alive\r\n\r\n");
      unsigned long timeout = millis();
      while (client.available() == 0) {
        if ((millis() - timeout) > 7000) {
//          Serial.println(">>> Client Timeout !");
          client.stop();
          return;
        }
      }
      
      // Read all the lines of the reply from server and print them to Serial
      while(client.available()){
        String line = client.readStringUntil('\r');
        line.trim();
//        Serial.print(line);
        char charBuf[50];
        line.toCharArray(charBuf, 50);
//        Serial.println(line=="1");
        if (line == "1"){
          counter = 0;
        }
      }
      
//      Serial.println();
//      Serial.println("closing connection");
      uploadtime = millis();
      }
  
//  // we'll need to change the range from the analog reading (0-1023) down to the range
//  // used by analogWrite (0-255) with map!
//  LEDbrightness = map(fsrReading, 0, 1023, 0, 255);
//  // LED gets brighter the harder you press
//  analogWrite(LEDpin, LEDbrightness);
  checktoolong(toolongpt, starttimept ,currentstate, toolongtimelimit);
  if(toolong == 1){
    digitalWrite(LEDpin, HIGH);
  }else{
    digitalWrite(LEDpin, LOW);
  }
  delay(50);
}
