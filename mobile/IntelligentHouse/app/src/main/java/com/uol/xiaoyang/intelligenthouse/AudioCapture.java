package com.uol.xiaoyang.intelligenthouse;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
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

import java.io.IOException;

public class AudioCapture extends ActionBarActivity {

    private final String TAG = "AudioCapture.java";
    private final String STATUS_WORKING = "Recording...";
    private final String STATUS_IDLE = "Tap to record";

    private Chronometer chronometer;
    private Button button_record;
    private MediaRecorder media_recorder;

    private boolean recording = false;
    private static String audio_file = null;

    public AudioCapture() {
        audio_file = Environment.getExternalStorageDirectory().getAbsolutePath();
        audio_file += "/audiorecordtest.3gp";
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_capture);
        chronometer = (Chronometer)findViewById(R.id.chronometer);
        button_record = (Button)findViewById(R.id.btn_record);

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
