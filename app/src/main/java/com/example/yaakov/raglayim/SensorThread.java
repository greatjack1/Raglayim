package com.example.yaakov.raglayim;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by yaakov on 2/26/17.
 */

public class SensorThread implements SensorEventListener {
    private int mPort = 23456;
    //will set it to my macs ip address to test
    private String mOpAddr = "192.168.1.7";
    private boolean playing = true;
    private int Sensitivity;
    private double[] accelVals;
    private boolean mShouldRun = true;
    private Context cx;
    private static Thread th;
    public SensorThread(SensorManager sm, Context context,int Sensitivity) {
        //set th to the current thread that is running so that it can be interuppted through the public method in this class
        th = Thread.currentThread();
        Sensitivity = (Sensitivity==0)?1:Sensitivity;
        this.Sensitivity = Sensitivity;
        accelVals = new double[Sensitivity];
        for(int i =0;i<accelVals.length;i++){
         accelVals[i] = 0.0;
        }
        cx = context;
        if (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            Sensor accelSen = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(SensorThread.this, accelSen, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d("Sensor running", "Sensor has registered a listener");

        } else {
            Log.d("Doesnt have a sensor", "no sensor");

        }
    }

    /**
     * This method is called when a sensor event is called and process
     * whether the user is running or not and based on that plays or pauses
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (TrackerActivity.isTrackerRunning() == false) {

            //exit the sub since the stop button is pressed
            return;
        }
        Log.d("Event Value", "Value Is " + event.values[1]);

        //1st shift all of the previous values to one place below

        for (int i = 0; i < Sensitivity -1; i++) {
            accelVals[i] = accelVals[i + 1];
        }

        accelVals[Sensitivity -1] = event.values[1];
        //in this loop unnegate any values
        for (int i = 0; i < Sensitivity; i++) {
            if (accelVals[i] < 0) {
                accelVals[i] = -accelVals[i];
            }
        }

        //check if the person is running by comparing the values
        boolean isRunning = false;
        //loop through the values testing if the kid is running
        for (int i = 0; i < Sensitivity -1; i++) {
            if (((accelVals[i] - accelVals[i + 1]) > 1)) {

                isRunning = true;

            }
        }
        Log.d("Is RUnning check", "IS running = " + isRunning);


        if (isRunning) {
            if (!playing) {

                // a class that holds the task to send a socket, it is used to prevent the network on main thread error
                class SendSocketTask extends AsyncTask<Void, Void, Void> {

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Socket sc = new Socket(mOpAddr, mPort);
                            PrintWriter pw = new PrintWriter(sc.getOutputStream(), true);
                            pw.println("play");
                            sc.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }
                //execute the task
                new SendSocketTask().execute();
                playing = true;
            }
        } else {
            if (playing) {
                // a class that holds the task to send a socket, it is used to prevent the network on main thread error
                class SendSocketTask extends AsyncTask<Void, Void, Void> {

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Socket sc = new Socket(mOpAddr, mPort);
                            PrintWriter pw = new PrintWriter(sc.getOutputStream(), true);
                            pw.println("pause");
                            sc.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }
                //execute the task
                new SendSocketTask().execute();

                playing = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    //this method is used to stop the thread when raglayim is stopped
    public static synchronized void StopThread(){
        //interrupt the thread to stop it
        th.interrupt();
    }
}
