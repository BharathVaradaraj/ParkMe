import serial
from firebase import firebase
firebase = firebase.FirebaseApplication("Your database url")

def update_status(id,status):
    x = firebase.get("sensor","")
    status = str(status)
    for i in x:
        print(x[i]["SensorID"])
        if x[i]["SensorID"] == id:
            firebase.put('/sensor/' + i ,"status" ,status )


s = serial.Serial("COM5", 9600)

while True:
    x = s.readline()
    sensor_id = str(x)[10:13]
    status = str(x)[14]
    print("sensor_id =",sensor_id)
    print(status)
    update_status(sensor_id,status)


