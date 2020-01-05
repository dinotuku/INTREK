package com.example.intrek;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MainActivity extends WearableActivity implements SensorEventListener {

    public static final String ACTION_RECEIVE_PACE = "ACTION_RECEIVE_PACE";
    public static final String ACTION_RECEIVE_DIST = "ACTION_RECEIVE_DIST";
    NumberFormat nf = new DecimalFormat("##.##");

    private ConstraintLayout mLayout;
    TextView textViewData;
    TextView textViewName;

    // Index of what is displayed on the screen
    // 0 = HR
    // 1 = pace
    private int selectedIndexDisplayed = 0;
    private final int INDEX_HR = 0 ;
    private final int INDEX_PACE = 1 ;
    private final int INDEX_DISTANCE = 2 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.recordingContainer);

        textViewData = findViewById(R.id.HeartRateWearTextView);
        textViewName = findViewById(R.id.textViewName);

        // Enables Always-on
        setAmbientEnabled();

        // Activate sensor reading
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission("android.permission.BODY_SENSORS") == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{"android.permission.BODY_SENSORS"}, 0);
        }

        // Register sensor services
        SensorManager sensorManager = (SensorManager) getSystemService(MainActivity.SENSOR_SERVICE);
        Sensor hr_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sensorManager.registerListener(this, hr_sensor, SensorManager.SENSOR_DELAY_UI);

        // Register receiver for the GPS data which can be sent
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Double pace = intent.getDoubleExtra(WearService.PACE,-2) ;
                if (selectedIndexDisplayed == INDEX_PACE) {
                    String s = nf.format(pace) + " [min/km]" ;
                    textViewData.setText(s);
                }
            }
        }, new IntentFilter(ACTION_RECEIVE_PACE));

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Double dist = intent.getDoubleExtra(WearService.DISTANCE,-3) ;
                if (selectedIndexDisplayed == INDEX_DISTANCE) {
                    String s = nf.format(dist) + " [km]" ;
                    textViewData.setText(s);
                }
            }
        }, new IntentFilter(ACTION_RECEIVE_DIST));


    }

    //// Method for handling the screen touching

    public void screenTapped(View view) {
        if (selectedIndexDisplayed == 0) {
            selectedIndexDisplayed = 1 ;
            textViewName.setText("Pace");
            textViewData.setText("");
        } else if (selectedIndexDisplayed == 1) {
            selectedIndexDisplayed = 2 ;
            textViewName.setText("Distance");
            textViewData.setText("");
        } else {
            selectedIndexDisplayed = 0 ;
            textViewName.setText("Heart Rate");
            textViewData.setText("");
        }
    }


    //// Methods for SensorEventListener

    @Override
    public void onSensorChanged(SensorEvent event) {
        float data = event.values[0];

        // 1. Display the data on the watch
        if (selectedIndexDisplayed == INDEX_HR) {
            textViewData.setText(String.valueOf(data));
        }

        // 2. Send it to the phone
        Intent intent = new Intent(MainActivity.this, WearService.class);
        intent.setAction(WearService.ACTION_SEND.HEART_RATE.name());
        intent.putExtra(WearService.HEART_RATE, (int) data);
        startService(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //// Methods for the ambient mode
    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        updateDisplay();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mLayout.setBackgroundColor(getResources().getColor(android.R.color.black, getTheme()));
        } else {
            mLayout.setBackgroundColor(getResources().getColor(android.R.color.white, getTheme()));
        }
    }


}
