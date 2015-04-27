import RPi.GPIO as GPIO
import time
import requests
import sqlite3
import json
import subprocess

GPIO.setmode(GPIO.BCM)
PIR_PIN = 4
GPIO.setup(PIR_PIN, GPIO.IN)
SERVER = "http://178.62.91.44:8085"
LIST_URL = "/songs/all"
LIST_TABLE = "list"

SONGS_INFO = []

COUNTER = 0
PLAY = False

_db = sqlite3.connect('local_list.db')
_cursor = _db.cursor()

def initialize():
    sql_init = '''
	CREATE TABLE IF NOT EXISTS {0} (id INTERGER PRIMARY KEY, name TEXT, url TEXT)
    '''.format(LIST_TABLE)
    _cursor.execute(sql_init)

def play_music(PIR_PIN):
    if COUNTER < 15:
        print(COUNTER)
	COUNTER += 1
    else:
	COUNTER = 0
        PLAY = True
        GPIO.add_event_detect(PIR_PIN, GPIO.RISING, callback=stop_music)
	subprocess.call(compose_cmd(), shell=True)

def stop_music():
    PLAY = False
    print "stop music"
    

def compose_cmd():
    cmd = "mplayer "
    for song in SONGS_INFO:
        cmd = cmd + song['url'] + ' '
    cmd += '-loop 2'
    return cmd 

def refresh_database():
    results = requests.get(SERVER+LIST_URL, verify=False)
    SONGS_INFO = json.loads(results.text)	
    print SONGS_INFO



if __name__ == "__main__":
    initialize()
    refresh_database()
    try:
        GPIO.add_event_detect(PIR_PIN, GPIO.RISING, callback=play_music)
        while True:
            time.sleep(100)
    except KeybordInterrupt:
        print "Quit"
        GPIO.cleanup()
