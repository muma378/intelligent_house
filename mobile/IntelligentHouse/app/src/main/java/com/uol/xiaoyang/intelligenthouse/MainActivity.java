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
import android.widget.ListView;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.Date;

import android.util.Log;

public class MainActivity extends ActionBarActivity {
    public final static String Host = "http://178.62.91.44:8085";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AsyncTaskParseJson().execute();
        setContentView(R.layout.activity_main);
    }

    public class AsyncTaskParseJson extends AsyncTask<String, String, String> {

        final String TAG = "AsyncTaskParseJson.java";
        // set the url of the web service to call
        final String SongsListSite = Host + "/songs/all";
        ArrayList<String> items = new ArrayList<String>();
        JSONArray dataJsonArr = null;

        @Override
        protected void onPreExecute(){}

        @Override
        protected String doInBackground(String... arg0) {
            try{
                JsonParser jParser = new JsonParser();
                JSONObject json = jParser.getJSONFromUrl(SongsListSite);

                dataJsonArr = json.getJSONArray("results");

                for (int i = 0; i < dataJsonArr.length(); i++) {
                    json = dataJsonArr.getJSONObject(i);
                    String songItem = Integer.toString(i+1) + ". " + json.getString("name") + "\n" + json.getString("artist") + " - " +json.getString("album");

                    items.add(songItem);
                    Log.d(songItem, "Output");
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg){
            ListView list = (ListView)findViewById(R.id.lst_songlist);
            ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, items);
            list.setAdapter(mArrayAdapter);
        }

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
//        ListView list = (ListView)findViewById(R.id.lst_songlist);
        new AsyncTaskParseJson().execute();
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
