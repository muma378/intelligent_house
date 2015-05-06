import sqlite3
import logging
import logging.config

# about preferences
gpio_pin = 4  # sensor's pin for GPIO
occlusion_time = 3 # time for occlusion detected
play_length = -1  # minutes to play
stationarity_length = 5  # minutes for house to keep stationarity
leaps = 5  #seconds for timer to check bounds every leaps

# about websites
host = "http://178.62.91.44:8085"
list_url = "/songs/all"

# about databases
db_name = "ih_local.db"
history_table = "history"

_db = sqlite3.connect(db_name)

# about logging
logging.config.fileConfig('../config/logging.conf')
logger = logging.getLogger('fileLogger')
