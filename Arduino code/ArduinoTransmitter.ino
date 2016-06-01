

#include <dht.h>


#include <RCSwitch.h>

#define dht_dpin A0 //no ; here. Set equal to channel sensor is on

RCSwitch mySwitch = RCSwitch();

dht DHT;

int prevTemp=0;
int prevHumidity=0;

void setup(){
  Serial.begin(9600);
  delay(300);//Let system settle
  Serial.println("Humidity and temperature\n\n");
  delay(700);//Wait rest of 1000ms recommended delay before
  //accessing sensor

  
}//end "setup()"

void loop(){

   mySwitch.enableTransmit(10);
  
  //This is the "heart" of the program.
  DHT.read11(dht_dpin);

  int randNumber;
 // int prevTemp=0;
 // int prevHumidity=0;
  int currentTemp;
  int currentHumidity;

    Serial.print("Current humidity = ");
    currentHumidity = DHT.humidity;
    Serial.print(currentHumidity);
    Serial.print("%  ");
    Serial.print("temperature = ");
    currentTemp = DHT.temperature;
    Serial.print(currentTemp); 
    Serial.println("C  ");

    // below is tranmitting logic

      Serial.println("previous values");
      Serial.println(prevTemp);
      Serial.println(currentTemp);
      
      // for sending temperature 
       if(prevTemp!=currentTemp)
       {     

         randNumber = random(1,10);

        for(int i=0; i<5;i++){

      Serial.println(randNumber);
      char result[400];

      strcpy(result,"51");
     // Serial.println(result);
      char bufferTemp[10];
      itoa(currentTemp,bufferTemp,10);          // integer to string

      strcat(result,bufferTemp);                    // appending in string
      strcat(result,itoa(randNumber,bufferTemp,10));

      // for checksum purpose
      int checkSum = 5+1+(currentTemp/10)+(currentTemp%10)+randNumber;
      checkSum =checkSum*randNumber;

      Serial.println(checkSum);
      strcat(result,itoa(checkSum,bufferTemp,10));
      
      Serial.println(strtol(result,NULL,10));   // string to integer
      
      mySwitch.send(strtol(result,NULL,10),32);
      Serial.println("Transmitted temperature");

    // tranmitting logic ends
  delay(800);//Don't try to access too frequently... in theory
  //should be once per two seconds, fastest,
  //but seems to work after 0.8 second.

        }
      
       prevTemp=currentTemp;
       } 
  //sending temperature ends

  

  // for sending Humidity


         if(prevHumidity!=currentHumidity)
       {     

        randNumber = random(1,10);

        for(int i=0; i<5;i++){

      Serial.println(randNumber);
      char resultH[400];

      strcpy(resultH,"52");
     // Serial.println(result);
      char bufferHum[10];
      itoa(currentHumidity,bufferHum,10);          // integer to string

      strcat(resultH,bufferHum);                    // appending in string
      strcat(resultH,itoa(randNumber,bufferHum,10));

      // for checksum purpose
      int checkSumH = 5+2+(currentHumidity/10)+(currentHumidity%10)+randNumber;
      checkSumH =checkSumH*randNumber;

      Serial.println(checkSumH);
      strcat(resultH,itoa(checkSumH,bufferHum,10));
      
      Serial.println(strtol(resultH,NULL,10));   // string to integer
      
      mySwitch.send(strtol(resultH,NULL,10),32);
      Serial.println("Transmitted Humidity");

    // tranmitting logic ends
  delay(800);//Don't try to access too frequently... in theory
  //should be once per two seconds, fastest,
  //but seems to work after 0.8 second.

        }
      
       prevHumidity=currentHumidity;
       } 

  // sending Humidity ends

 // delay(100000);
}// end loop()

