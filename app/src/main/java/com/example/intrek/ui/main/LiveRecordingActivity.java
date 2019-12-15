package com.example.intrek.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.example.intrek.R;

public class LiveRecordingActivity extends AppCompatActivity {

    public static final String ACTION_RECEIVE_HEART_RATE = "ACTION_RECEIVE_HEART_RATE";
    public static final String HEART_RATE = "HeartRate";

    private Chronometer timerTextView;
    private Button pauseButton ;

    private long timerValueWhenPaused = 0 ;

    private boolean isPaused = false ;
    private HeartRateBroadcastReceiver heartRateBroadcastReceiver;
    private int heartRateWatch = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_recording);

        // 0. Get the elements of the UI
        timerTextView = findViewById(R.id.timerTextView);
        timerTextView.start();
        pauseButton = findViewById(R.id.PauseButton);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Get the HR data back from the watch
        heartRateBroadcastReceiver = new HeartRateBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(heartRateBroadcastReceiver, new IntentFilter(ACTION_RECEIVE_HEART_RATE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(heartRateBroadcastReceiver);
    }


    public void pauseButtonTapped(View view) {
        if (isPaused) {
            timerTextView.setBase(SystemClock.elapsedRealtime() + timerValueWhenPaused);
            timerTextView.start();
            pauseButton.setText("Pause");
            isPaused = false ;
        } else {
            timerValueWhenPaused = timerTextView.getBase() - SystemClock.elapsedRealtime();
            timerTextView.stop();
            pauseButton.setText("Resume");
            isPaused = true ;
        }
    }


    // MARK: - Inner class to listen the watch data

    private class HeartRateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Show HR in a TextView
            heartRateWatch = intent.getIntExtra(HEART_RATE, -1);
            TextView hrTextView = findViewById(R.id.HRTextView);
            hrTextView.setText(String.valueOf(heartRateWatch));
        }

    }
}

