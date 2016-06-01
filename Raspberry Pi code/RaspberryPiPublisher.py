

from pi_switch import RCSwitchReceiver

import paho.mqtt.client as mqtt
import json
import time

receiver = RCSwitchReceiver()
receiver.enableReceive(2)

num = 0
prevTemp=0
prevHum=0

def onConnect(client, userdata, flags, rc):
        print("Connected with result code "+str(rc))


mqttClient = mqtt.Client("c3")
mqttClient.on_connect = onConnect

#mqttClient.connect("54.187.15.61",1883)
mqttClient.connect("54.67.96.20",1883)

message={}

while True:
    if receiver.available():
        received_value = receiver.getReceivedValue()

        if received_value:
            num += 1
            print("Received[%s]:" % num)
            print(received_value)
            print("%s / %s bit" % (received_value, receiver.getReceivedBitlength()))
            print("Protocol: %s" % receiver.getReceivedProtocol())
            print("")
            lst = [int(i) for i in str(received_value)]
            print(lst[1])
            try:
            	checkSum=lst[0]+lst[1]+lst[2]+lst[3]+lst[4]
            	checkSum=checkSum*lst[4]
            except:
            	print("exception: less than 7 digits")
            print("checksum")
            print(checkSum)
            vb=lst[5:]
            print(vb)
	    vb1=''.join(map(str,vb))
	    print(vb1)
            if checkSum!=int(vb1):
                print("checksum wrong: data currupt")
                continue
            if lst[1]==1:
                currentTemp=''.join(map(str,lst[2:4]))
                print(currentTemp)
                if prevTemp!=currentTemp and prevHum!=0:
                    #if prevHum!=0:
                      print("publish temperature")
                      message["team"]= lst[0]
                      message["temp"]= currentTemp
                      message["humidity"]= prevHum
                      mqttClient.publish("Message",json.dumps(message))
                      print("published temperature successfully")
                      mqttClient.loop(2)
                      print message
                prevTemp=currentTemp

            elif lst[1]==2:
                currentHum=''.join(map(str,lst[2:4]))
                print(currentHum)
                if prevHum!=currentHum and prevTemp!=0:
                   # if prevTemp!=0:
                      print("publish humidity")
                      message["team"]=lst[0]
                      message["temp"]=prevTemp
                      message["humidity"]=currentHum
                      mqttClient.publish("Message",json.dumps(message))
                      print("published humidity successfully")
                      mqttClient.loop(2)
                      print message
                prevHum=currentHum
            else: 
                print("data type is not temp or humidity : data type courrupt")
        #time.sleep(60)
        receiver.resetAvailable()
