package com.uol.xiaoyang.intelligenthouse;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.AdapterView;
import android.widget.ListView;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.util.Log;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
    public final static String Host = "http://178.62.91.44:8085";
    public final static String PAGE = "MainActivity";

    static final String KEY_ID = "id";  //song_id
    static final String KEY_NAME = "name";
    static final String KEY_DESCRIPTION = "description";   //artist - album
    static final String KEY_DURATION = "duration";
    static final String KEY_URL = "url";    //url not displayed
    static final String KEY_ARTIST = "artist";
    static final String KEY_ALBUM = "album";

    final String SEARCH_HINT = "Search songs here";
    ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> song_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        new AsyncTaskListSongs().execute();
        setContentView(R.layout.activity_main);
    }

    public class AsyncTaskListSongs extends AsyncTask<String, String, String> {

        final String TAG = "AsyncTaskListSongs.java";
        // set the url of the web service to call
        final String ListSongsSite = Host + "/songs/all";
        JSONArray dataJsonArr = null;

        @Override
        protected void onPreExecute(){
            songsList.clear();
        }

        @Override
        protected String doInBackground(String... arg0) {
            try {
                final String query_url = ListSongsSite;

                JsonParser jParser = new JsonParser();
                JSONObject json = jParser.getJSONFromUrl(query_url);
                dataJsonArr = json.getJSONArray("results");

                for (int i = 0; i < dataJsonArr.length(); i++) {
                    json = dataJsonArr.getJSONObject(i);
                    HashMap<String, String> map = new HashMap<String, String>();

                    map.put(KEY_ID, json.getString(KEY_ID));
                    map.put(KEY_NAME, json.getString(KEY_NAME));
                    map.put(KEY_ARTIST, json.getString(KEY_ARTIST));
                    map.put(KEY_ALBUM, json.getString(KEY_ALBUM));
                    map.put(KEY_DURATION, json.getString(KEY_DURATION));
                    map.put(KEY_URL, json.getString(KEY_URL));

                    songsList.add(map);
                }
            }catch (JSONException e){
                e.printStackTrace();
                Log.e(TAG, "Error read json " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg){
            ListView list = (ListView)findViewById(R.id.list_songs_list);

            // Getting adapter by passing xml data ArrayList
            QueryAdapter adapter=new QueryAdapter(MainActivity.this, songsList, PAGE);
            list.setAdapter(adapter);
            // Click event for single list row
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                }
            });
        }
    }

    public void appendSong(View view){
        QueryAdapter.ViewHolder viewHolder = (QueryAdapter.ViewHolder)view.getTag();
        song_info = viewHolder.data;
        AsyncTaskOperateSongs asyncTask = new AsyncTaskOperateSongs();
        asyncTask.operation = "POST";
        asyncTask.execute();
    }

    public class AsyncTaskOperateSongs extends AsyncTask<String, String, String>{
        final String TAG = "AsyncTaskAppendSongs.java";
        public final String url = Host + "/song/";
        public String parameter;
        public boolean success = false;
        public String operation;    //POST, DELETE

        @Override
        protected void onPreExecute(){
            parameter = song_info.get(KEY_ID);
        }

        @Override
        protected String doInBackground(String... params) {
            String request_url = url + parameter;

            JsonParser jParser = new JsonParser();
            JSONObject json;
            if (operation.equals("DELETE")) {
                json = jParser.getJSONFromDelete(request_url);
            }else {
                json = jParser.getJSONFromPost(request_url);
            }
            try {
                if (json.getInt("status")==200){
                    success = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg){
            onResume();

        }
    }

    public void removeSong(View view){
        QueryAdapter.ViewHolder viewHolder = (QueryAdapter.ViewHolder)view.getTag();
        song_info = viewHolder.data;
        AsyncTaskOperateSongs asyncTask = new AsyncTaskOperateSongs();
        asyncTask.operation = "DELETE";
        asyncTask.execute();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void addSong(View view){
        Intent intent = new Intent(this, NewSong.class);
        startActivity(intent);
    }

    public void addVoice(View view){
        Intent intent = new Intent(this, AudioCapture.class);
        startActivity(intent);

    }

    @Override
    public void onResume(){
        super.onResume();
        new AsyncTaskListSongs().execute();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
