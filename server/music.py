#!/usr/bin/env python
#encoding: UTF-8
import locale  
import requests  
import json  
import sys  
import subprocess

class songRequestWrapper():

    def __init__(self):
        locale.setlocale(locale.LC_ALL, "")
   
        self.header = {
            'Accept': '*/*',
            'Accept-Encoding': 'gzip,deflate,sdch',
            'Accept-Language': 'zh-CN,zh;q=0.8,gl;q=0.6,zh-TW;q=0.4',
            'Connection': 'keep-alive',
            'Content-Type': 'application/x-www-form-urlencoded',
            'DNT':1,
            'Host': 'music.163.com',
            'Origin': 'http://music.163.com',
            'Referer': 'http://music.163.com/search/',
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36'
        }

        self.cookies = {
            'appver': '1.5.2'
            }
        self.limit = 20
        self.search_site = "http://music.163.com/api/search/pc"
        reload(sys)
        sys.setdefaultencoding('UTF-8')

    def query(self, q, offset=0):
        song_name = "".join(q.split())
        if song_name is None:
            print "no characters in q"

        data = {
            'hlpretag': '<span class="s-fc7">',
            'hlposttag': '</span>',
            's': song_name,
            'type': 1,
            'offset': offset,
            'total' : 'true',
            'limit': self.limit
        }

        song = requests.post(
            self.search_site,
            data=data,
            cookies=self.cookies,
            headers=self.header
        )

        song.encoding = 'UTF-8'
        song = json.loads(song.text)
        query_result = self.compose_song_info(song)
        return query_result


    def compose_song_info(self, data):
        songs = []
        for i in range(0, self.limit):
            song = data['result']['songs'][i]
            song_info = {
               'id': song['id'],
               'name': song['name'],
               'duration': song['duration'],
               'artist': song['artists'][0]['name'],
               'album': song['album']['name'],
               'url': song['mp3Url'],
               }
            songs.append(song_info)
        return songs
