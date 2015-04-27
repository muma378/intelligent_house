import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BCM)
PIR_PIN = 4
GPIO.setup(PIR_PIN, GPIO.IN)

def motion(PIR_PIN):
    print "Motion detected!"

try:
    GPIO.add_event_detect(PIR_PIN, GPIO.RISING, callback=motion)
    while True:
        time.sleep(100)
except KeybordInterrupt:
    print "Quit"
    GPIO.cleanup()
