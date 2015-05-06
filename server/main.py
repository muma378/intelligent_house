import tornado.httpserver
import tornado.ioloop
import tornado.web
import tornado.escape
import sqlite3
import sys
import time
import base64

from music import songRequestWrapper

_db = sqlite3.connect('intelligent_house.db')
_cursor = _db.cursor()
_song_requester = songRequestWrapper()

class queryRequestHandler(tornado.web.RequestHandler):
    def get(self):
        query = self.get_argument("s")
        offset = self.get_argument("offset")
        if query is None:
            self.write("Please specify the name to query")
	records = _song_requester.query(query, offset)
        self.write(tornado.escape.json_encode(records))

class songsRequestHandler(tornado.web.RequestHandler):
    def initialize(self):
        '''
        initialize the database
        '''
        self.songs_table = settings["songs_table"]
        self.list_table = settings["list_table"]
        _cursor.execute('''
            CREATE TABLE IF NOT EXISTS {0} (id INTEGER PRIMARY KEY AUTOINCREMENT,
            song_id REAL UNIQUE, name TEXT, artist TEXT, album TEXT, 
            duration INTEGER, url TEXT)
        '''.format(self.songs_table))

        _cursor.execute('''
            CREATE TABLE IF NOT EXISTS {0} (id INTEGER PRIMARY KEY AUTOINCREMENT,
            song INTEGER, update_time REAL, 
            FOREIGN KEY(song) REFERENCES {1}(id))
        '''.format(self.list_table, self.songs_table))

    def get(self, song_id=None):
        '''
        get the list of favourite songs if song_id is None
        ''' 
        if song_id is not None:
            raise NotImplementedError("Not support now")

        offset = self.get_argument("offset", default=0)
        sql_all = '''
            SELECT * FROM {lt}, {st} WHERE {lt}.song={st}.id ORDER BY {lt}.update_time DESC
            LIMIT 20 OFFSET {offset} 
        '''.format(lt=self.list_table, st=self.songs_table, offset=offset)
        _cursor.execute(sql_all)

        records = []
        for row in _cursor:
            records = records + [{'id':row[0], 'name':row[5], 'artist':row[6], 'album':row[7],
            'duration':row[8], 'url':row[9]}]
        print "Respond with " + str(len(records)) + " records" 
	records = {"results": records}
	self.write(tornado.escape.json_encode(records))

    def put(self, song_id):
        '''
        add the song the list
        '''
        args = self.get_argument
        sql_insert_song = '''
            INSERT INTO {songs_table} VALUES (NULL, {song_id}, '{name}', '{artist}', 
            '{album}', {duration}, '{url}')
        '''.format(songs_table=self.songs_table, song_id=song_id, name=args("name"), 
        artist=args("artist"), album=args("album"), duration=args("duration"), 
        url=args("url"))

        sql_query_id = '''
            SELECT id FROM {songs_table} WHERE song_id = {song_id}
        '''.format(songs_table=self.songs_table, song_id=song_id)

        _cursor.execute(sql_query_id)
        id = _cursor.fetchone()
        if id is None:      #if the song is already exist in the database
            _cursor.execute(sql_insert_song)
            _db.commit()
            _cursor.execute(sql_query_id)
            id = _cursor.fetchone()[0]
        else:
            id = id[0]
        
        sql_list = '''
            INSERT INTO {list_table} VALUES (NULL, {fk_id}, {update_time})
        '''.format(list_table=self.list_table, fk_id=id, update_time=time.time())
        _cursor.execute(sql_list)
        _db.commit()

	response = {"status":200, "message": "ok"}
	self.write(tornado.escape.json_encode(response))
        
    def delete(self, list_id):
        '''
        delete the song the favourite list
        '''
        sql_remove = '''
            DELETE FROM {list_table} WHERE id={list_id}
        '''.format(list_table=self.list_table, list_id=list_id)
        _cursor.execute(sql_remove)
        _db.commit()
	response = {"status":200, "message": "ok"}
	self.write(tornado.escape.json_encode(response))

    def post(self, list_id):
        '''
        update the time of list_id
        '''
        sql_update = '''
            UPDATE {list_table} SET update_time = {time} WHERE id = {list_id}
        '''.format(list_table=self.list_table, time=time.time(), list_id=list_id)
        _cursor.execute(sql_update)
        _db.commit()

	response = {"status":200, "message": "ok"}
	self.write(tornado.escape.json_encode(response))

class uploadRecordsHandler(tornado.web.RequestHandler):
    def initialize(self):
	self.static_path = settings["static_path"]
	self.server = settings["host"] + ":" + str(settings["port"])

        self.songs_table = settings["songs_table"]
	self.list_table = settings["list_table"]

    def put(self, record_id):
	record = self.get_argument("data")
	name = self.get_argument("name", default=time.time())
    	
	if not name.endswith(settings["record_format"]):
	    name += settings["record_format"]    
	file_path = self.static_path + '/' + name
	try:
	    record = base64.b64decode(record)
	    with open(file_path, 'wb') as out:
                out.write(record)
        except IOError as e:
	    print e
            response = {"status": 404, "message": "commit failed"}
	    return response
	
	file_url = self.server + '/'  + file_path
	print file_url
        record_id = int(divmod(time.time(), 1000000)[1])
	try:
	    self.insert_records(record_id, name, file_url, self.get_argument("duration"))
	except IOError as e:
	    print e
        response = {"status": 200, "message": "ok"}
	self.write(tornado.escape.json_encode(response))

    def insert_records(self, record_id, name, file_url, duration):
	sql_insert_songs = '''
            INSERT INTO {songs_table} VALUES (NULL, {record_id}, '{name}', 'User', 
            'Records', {duration}, '{url}')
        '''.format(songs_table=self.songs_table, record_id=record_id, name=name, 
         duration=long(duration), url=file_url)
	_cursor.execute(sql_insert_songs)
	_db.commit()
	
	sql_query_id ='''
            SELECT id FROM {songs_table} WHERE song_id = {record_id}
        '''.format(songs_table=self.songs_table, record_id=record_id)

        _cursor.execute(sql_query_id)
        id = _cursor.fetchone()[0]

    	sql_insert_list = '''
            INSERT INTO {list_table} VALUES (NULL, {fk_id}, {update_time})
        '''.format(list_table=self.list_table, fk_id=id, update_time=time.time())
        _cursor.execute(sql_insert_list)
        _db.commit()
	return True	

settings = {
    "static_path": "static/records",
    "port": 8085,
    "host": "http://178.62.91.44",
    "record_format": ".3gp",
    "list_table": "list",
    "songs_table": "songs",
}

application = tornado.web.Application([
    (r'/songs/q', queryRequestHandler),
    (r'/songs/all', songsRequestHandler),
    (r'/song/([\d]+)', songsRequestHandler),
    (r'/record/([\d]+)', uploadRecordsHandler),
    (r'/static/(.*)', tornado.web.StaticFileHandler, {'path':'static'}),
])

if __name__ == '__main__':
    #http_server = tornado.httpserver.HTTPServer(application, ssl_options={
    #    "certfile": "keys/cert.pem",
    #    "keyfile": "keys/key.pem",
    #})
    http_server = tornado.httpserver.HTTPServer(application)
    http_server.listen(settings["port"])
    tornado.ioloop.IOLoop.instance().start()
