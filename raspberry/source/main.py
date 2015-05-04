import RPi.GPIO as GPIO
import time
import requests
import json
import subprocess
import logging
import logging.config

import settings
from thread_utils import ThreadPlayer, ThreadTimer
from errors import MultiObjectsError

logger = settings.logger

PIR_PIN = settings.gpio_pin
GPIO.setmode(GPIO.BCM)
GPIO.setup(PIR_PIN, GPIO.IN)

_db = settings._db
_cursor = _db.cursor()

def initialize():
    sql_init = '''
	CREATE TABLE IF NOT EXISTS {0} (id INTERGER PRIMARY KEY, name TEXT, url TEXT)
    '''.format(settings.history_table)
    _cursor.execute(sql_init)


class Controller:
	'''
	This is a media controller according to the occlusion detected

	'''
	def __init__(self):
		self.occlusion = 0
		self.is_playing = False
		self.__threshold = settings.occlusion_time

	def control(self, PIR_PIN):
		if self.is_playing:
			self.timeit()
		else:
			if self.occlusion < self.__threshold:
				# print(self.occlusion)
				self.occlusion += 1
			else:
				self.occlusion = 0
				self.play_music()

	def timeit(self):
		try:
			self.timer.reset()
		except AttributeError:	
			self.is_playing = False
			# self.player = None
			# self.timer = None

	def play_music(self):
		try:	
			self.player = ThreadPlayer(self.request_playlist(), settings.play_length)
			self.player.start()
			self.is_playing = True

			self.timer = ThreadTimer(self.player, settings.stationarity_length)
			self.timer.start()
		except MultiObjectsError as e:
			logger.info(e)
			return None
		except IOError as e:
			logger.error(e)

	def request_playlist(self):
		results = requests.get(settings.host+settings.list_url, verify=False)
		songs_info = json.loads(results.text)
		return songs_info["results"]

if __name__ == "__main__":
	initialize()
	c = Controller()
	try:
		GPIO.add_event_detect(PIR_PIN, GPIO.RISING, callback=c.control)
		while True:
			time.sleep(100)
	except KeybordInterrupt:
		print "Quit"
		GPIO.cleanup()
