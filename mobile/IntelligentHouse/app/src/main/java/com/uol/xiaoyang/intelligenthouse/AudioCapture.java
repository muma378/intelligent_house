package com.uol.xiaoyang.intelligenthouse;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.util.Log;
import android.widget.LinearLayout;
import android.util.Base64;

import java.io.IOException;
import java.io.File;
import java.net.URLEncoder;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AudioCapture extends ActionBarActivity {

    private final String TAG = "AudioCapture.java";
    private final String STATUS_WORKING = "Recording...";
    private final String STATUS_IDLE = "Tap to record";

    private Chronometer chronometer;
    private Button button_record;
    private MediaRecorder media_recorder;
    private LinearLayout linear_layout_record;
    private Button button_add;
    private Button button_cancel;

    private boolean recording = false;
    private static String audio_file = null;

    public AudioCapture() {
        audio_file = Environment.getExternalStorageDirectory().getAbsolutePath();
        audio_file += "/audiorecord.3gp";
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_capture);
        chronometer = (Chronometer)findViewById(R.id.chronometer);
        button_record = (Button)findViewById(R.id.btn_record);

        linear_layout_record = (LinearLayout) findViewById(R.id.layout_add_record);
        button_add = (Button) findViewById(R.id.btn_add);
        button_cancel = (Button) findViewById(R.id.btn_cancel);

    }

    public void onRecord(View view){
        if(recording == false){
            startRecord();
        }else {
            stopRecord();
        }

    }

    public void startRecord(){
        recording = true;
        //change text on button
        button_record.setText(STATUS_WORKING);
        //start timer
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        //start recording
        media_recorder = new MediaRecorder();
        media_recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        media_recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        media_recorder.setOutputFile(audio_file);
        media_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try{
            media_recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "prepare() failed " + e.toString());
        }

        media_recorder.start();
    }

    public void stopRecord(){
        recording = false;
        button_record.setText(STATUS_IDLE);
        //stop timer
        chronometer.stop();

        //stop recording
        media_recorder.stop();
        media_recorder.release();
        media_recorder = null;

        //play it
        MediaPlayer mPlayer = new MediaPlayer();

        try {
            mPlayer.setDataSource(audio_file);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        // change visibility
        button_record.setVisibility(View.GONE);
        linear_layout_record.setVisibility(View.VISIBLE);
    }

    //convert the record to base64, and sent it to the server
    public void onCommit(View view) {
        AsyncTaskPutRecord async_task = new AsyncTaskPutRecord();
        async_task.execute();

        onCancel(view);
    }
    public void onCancel(View view){
        button_record.setVisibility(View.VISIBLE);
        linear_layout_record.setVisibility(View.GONE);
    }

    // send record to server
    public class AsyncTaskPutRecord extends AsyncTask<String, String, String> {

        final String TAG = "AsyncTaskPutRecord.java";
        // set the url of the web service to call
        final String PutRecordSite = MainActivity.Host + "/record";
        String put_url;
        boolean success = false;

        @Override
        protected void onPreExecute(){
            String E ="UTF-8";
            try {
                File file = new File(audio_file);
                byte[] bytes = FileUtils.readFileToByteArray(file);
                String record = Base64.encodeToString(bytes, 0);
                Date name = new Date();

                String parameters = "/1";
                parameters += "?data=" + URLEncoder.encode(record, E);
                parameters += "&name=" + URLEncoder.encode(name.toString(), E);
                put_url = PutRecordSite + parameters;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error reading files " + e.toString());
            }
        }

        @Override
        protected String doInBackground(String... arg0) {
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
            if (success){
                //show dialog to alert sucsess
            }else{

            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_audio_capture, menu);
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
