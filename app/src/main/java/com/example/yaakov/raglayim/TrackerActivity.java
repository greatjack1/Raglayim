package com.example.yaakov.raglayim;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.net.Socket;

public class TrackerActivity extends AppCompatActivity {
    //the referance to the button
    private Button mButtonStart;
    private SeekBar mSensBar;
    private TextView mSensLabel;
    private int Sensitivity =13;
    private boolean sensBarEnabled = true;
    public static boolean TrackerRunning = false;

    public synchronized static boolean isTrackerRunning() {
        return TrackerRunning;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tracker);
        mButtonStart = (Button) findViewById(R.id.start_button);
        mSensBar = (SeekBar) findViewById(R.id.seekBar);
        mSensLabel = (TextView) findViewById(R.id.sens_label);
        //retreive the bundle and get the TrackerRunning Value if it is not null and configure the button to show that value
        if (savedInstanceState != null) {
            TrackerRunning = savedInstanceState.getBoolean("tracker_running");
           sensBarEnabled = savedInstanceState.getBoolean("sens_bar_enabled");
            mSensBar.setEnabled(sensBarEnabled);
            if (isTrackerRunning() == true) {
                mButtonStart.setText("Stop Raglayim");
            }
        }
mSensBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Sensitivity = progress;
        mSensLabel.setText(String.valueOf(Sensitivity));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
});

        //Assign a Event Handeler That starts keeping track of Acceleromiter and sending a socket to play or pause the movie based on
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Button Has Been Clicked", "click");
                if (mButtonStart.getText().equals("Stop Raglayim")) {
                    //change the button text to start raglayim and enable the slide bar
                    sensBarEnabled = true;
                    mSensBar.setEnabled(sensBarEnabled);
                    //stop the tracker and then kill the thread
                    TrackerRunning = false;
                    //use the public method on the class to stop the thread
                    SensorThread.StopThread();
                    mButtonStart.setText(R.string.start_button);

                } else {
                    //since we are starting the thread disable the SensBar
                    sensBarEnabled=false;
                    mSensBar.setEnabled(sensBarEnabled);
                    //change the button text to stop
                    mButtonStart.setText("Stop Raglayim");
                    TrackerRunning = true;
                    //get the sensor manaagert context and pass it to the sensor thread
                    final SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
///create a thread to run in the background that handles the sensor events
                   final Thread th = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Started Thread", "sensor thread is about to starrt");
                            SensorThread st = new SensorThread(sm, TrackerActivity.this,Sensitivity);
                        }
                    });
                    th.start();

                }

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i("Tracker Activiy", "onSaveInstanceState");
        savedInstanceState.putBoolean("tracker_running", TrackerRunning);
savedInstanceState.putBoolean("sens_bar_enabled",sensBarEnabled);
    }

}
