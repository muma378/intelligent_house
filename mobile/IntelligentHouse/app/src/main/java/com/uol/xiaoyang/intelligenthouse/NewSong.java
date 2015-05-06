package com.uol.xiaoyang.intelligenthouse;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;


public class NewSong extends ActionBarActivity {

    final String TAG = "NewSong.java";

    // XML node keys
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
        setContentView(R.layout.activity_new_song);
    }

    public void removeText(View view){
        TextView tv_search = (TextView) findViewById(R.id.txt_query);
        if(tv_search.getText().toString().equals(SEARCH_HINT)){
            tv_search.setText("");
        }
    }

    public void addList(View view){
        QueryAdapter.ViewHolder viewHolder = (QueryAdapter.ViewHolder) view.getTag();
        song_info = viewHolder.data;
        AsyncTaskAddList asyncAddList = new AsyncTaskAddList();
        asyncAddList.target = (ImageView) view;
        asyncAddList.execute();
    }

    public class AsyncTaskAddList extends AsyncTask<String, String, String>{
        final String TAG = "AsyncTaskAddList.java";
        final String PutSite = MainActivity.Host + "/song/";
        String put_url;
        boolean success = false;
        public ImageView target;

        @Override
        protected void onPreExecute(){
            String parameters = song_info.get(KEY_ID);
            String E ="UTF-8";
        try{
            parameters += "?name=" + URLEncoder.encode(song_info.get(KEY_NAME), E);
            parameters += "&artist=" + URLEncoder.encode(song_info.get(KEY_ARTIST), E);
            parameters += "&album=" + URLEncoder.encode(song_info.get(KEY_ALBUM), E);
            parameters += "&duration=" + URLEncoder.encode(song_info.get(KEY_DURATION), E);
            parameters += "&url=" + URLEncoder.encode(song_info.get(KEY_URL), E);
            put_url = PutSite + parameters;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e(TAG, "Error encoding data " + e.toString());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            JsonParser jParser = new JsonParser();
            JSONObject json = jParser.getJSONFromPut(put_url);
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
            if(success){
                target.setImageResource(R.drawable.ok);
            }
        }
    }

    public void queryName(View view){
        new AsyncTaskQuerySongs().execute();
    }

    public class AsyncTaskQuerySongs extends AsyncTask<String, String, String> {

        final String TAG = "AsyncTaskQuerySongs.java";
        // set the url of the web service to call
        final String QuerySite = MainActivity.Host + "/songs/q";
        JSONArray dataJsonArr = null;

        @Override
        protected void onPreExecute(){
            songsList.clear();
        }

        @Override
        protected String doInBackground(String... arg0) {
            try {
                TextView tv_search = (TextView) findViewById(R.id.txt_query);
                String query_string = tv_search.getText().toString();
                final String query_url = QuerySite + "?offset=0&s=" + URLEncoder.encode(query_string, "UTF-8");

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
            }catch(UnsupportedEncodingException e){
                e.printStackTrace();
                Log.e(TAG, "Error encoding data " + e.toString());
            }catch (JSONException e){
                e.printStackTrace();
                Log.e(TAG, "Error read json " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg){
            ListView list = (ListView)findViewById(R.id.list_result);

            // Getting adapter by passing xml data ArrayList
            QueryAdapter adapter=new QueryAdapter(NewSong.this, songsList);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_song, menu);
        return true;
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
