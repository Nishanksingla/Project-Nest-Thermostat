import pywapi
import paho.mqtt.client as mqtt
import json
import urllib2, urllib,time

baseurl = "https://query.yahooapis.com/v1/public/yql?"
yql_query = "select * from weather.forecast where woeid=2488042"
yql_url = baseurl + urllib.urlencode({'q':yql_query}) + "&format=json"

logfile = open("logfile3.txt","w")


def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    client.subscribe("Message")
    print "Subscription successful"

def on_message(client, userdata, msg):
    print "got mesasge"
    print(msg.topic+" "+str(msg.payload))
    jsonmsg = json.loads(msg.payload)
    ##result = urllib2.urlopen(yql_url).read()
    ##data = json.loads(result)
    ##temp = int(data['query']['results']['channel']['item']['condition']['temp'])
    ##outTemp = (temp - 32) * 5/9
    weather_com_result = pywapi.get_weather_from_weather_com('95122')
    outTemp = weather_com_result['current_conditions']['temperature']
    print "outTemp: " +str(outTemp)
    logfile.write(str(jsonmsg["team"]) + "," + str(jsonmsg["temp"]) + "," + str(jsonmsg["humidity"]) + "," + str(outTemp) +"\n")
    logfile.flush()

client = mqtt.Client()
client.on_connect = on_connect
print "connected vaibhav"
client.on_message = on_message
print "completed on message action"

client.connect("54.67.96.20", 1883, 60)

client.loop_forever()
logfile.close()
