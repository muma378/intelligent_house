import threading
import subprocess
import time
import thread

import settings
from errors import MultiObjectsError

logger = settings.logger


class ThreadPlayer(threading.Thread):
	'''
	This threading ought to play songs which in the playlist via the command "mplayer url" 
	As an optional parameter, mins represents the time to play, -1 means no limit about the time
	However the flag _play_next deceides whether next songs should be play
	'''
	_instance = None
	def __new__(cls, *args, **kwargs):
		if cls._instance:
			raise MultiObjectsError("Only one player thread can exist at the same time")
		cls._instance = super(ThreadPlayer, cls).__new__(cls, *args, **kwargs)
		return cls._instance

	def __init__(self, playlist, mins=-1):
		super(ThreadPlayer, self).__init__()
		self._play_next = True
		if playlist is []:
			raise IOError("Playlist is empty")
		self.playlist = playlist
		self.length = self.get_length(mins)

	def __del__(self):
		super(ThreadPlayer, self).__del__()
		# ThreadPlayer._instance = None
		logger.info("Player exit")

	def run(self):
		song_index = 0
		while song_index < self.length and self._play_next:
			song = self.playlist[song_index]
			logger.info("Begin to play " + song["name"] + " by " + song["artist"])
			cmd = "mplayer \"" + song["url"] + "\""
			subprocess.call(cmd, shell=True)  # call the command mplayer url in the shell
			song_index += 1
		logger.info("Play to end.")
		ThreadPlayer._instance = None
		# thread.exit()
		return None

	def stop_play(self):
		self._play_next = False
	
	def get_length(self, mins):
		'''
		get the number of songs will be play according to the minutes
		'''
		length = 0
		if mins == -1:
			length = len(self.playlist)
		else:
			ms = mins * 60 * 1000  # convert its unit to microsecond
			duration = 0
			for song in self.playlist:
				duration += song["duration"]
				length += 1
				if duration >= ms:
					break
		logger.info("There are " + str(length) + " songs will be played")
		return length


class ThreadTimer(threading.Thread):
	'''
	This thread ought to time for the house in stationarity
	Every time a occlusion detected, the timer was reset.
	'''
	_instance = None
	def __new__(cls, *args, **kwargs):
		if cls._instance:
			raise MultiObjectsError("Only one timer thread can exist at the same time")
		cls._instance = super(ThreadTimer, cls).__new__(cls, *args, **kwargs)
		return cls._instance

	def __init__(self, player, bound=15):
		super(ThreadTimer, self).__init__()
		logger.info("Timer initialized")
		self.player = player
		self.bound = bound * 60  # cpnvert seconds to minutes
		self.gap = settings.leaps
		self.elapse = 0
		self.over = False

	def __del__(self):
		super(ThreadTimer, self).__init__()
		# ThreadTimer._instance = None
		logger.info("Timer exit")

	def run(self):
		while self.elapse < self.bound:
			time.sleep(self.gap)
			self.elapse += self.gap
		logger.info("Time up")
		self.player.stop_play()
		self.over = True
		ThreadTimer._instance = None
		return None

	def reset(self):
		if self.over:
			raise AttributeError("Time up")
		else:
			self.elapse = 0
			logger.info("Timer is reset")
