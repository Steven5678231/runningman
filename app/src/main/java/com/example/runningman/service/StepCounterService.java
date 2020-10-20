package com.example.runningman.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class StepCounterService extends Service implements SensorEventListener {

    public static final String STEP_COUNTER_BROADCAST = "success";
    private double MagnitudePrevious;
    private int stepCount;
    private float x_acceleration;
    private float y_acceleration;
    private float z_acceleration;
    private double Magnitude;
    private double MagnitudeDelta;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Intent intent;


    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        stepCount = 0;
        MagnitudePrevious = 0;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        intent = new Intent(StepCounterService.STEP_COUNTER_BROADCAST);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent != null) {
            x_acceleration = sensorEvent.values[0];
            y_acceleration = sensorEvent.values[1];
            z_acceleration = sensorEvent.values[2];

            Magnitude = Math.sqrt(x_acceleration * x_acceleration + y_acceleration * y_acceleration + z_acceleration * z_acceleration);
            MagnitudeDelta = Magnitude - MagnitudePrevious;
            MagnitudePrevious = Magnitude;

            if (MagnitudeDelta > 3) {
                stepCount++;
                intent.putExtra("steps", stepCount);
            }
            sendBroadcast(intent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onDestroy() {
        //Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        stepCount = 0;
        MagnitudePrevious = 0;
        Magnitude = 0;
        sensorManager.unregisterListener(this);
    }

}
