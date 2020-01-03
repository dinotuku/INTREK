package com.example.intrek.Managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.intrek.ui.main.LiveRecordingActivity;

import java.util.ArrayList;

public class MicrocontrollerManager {

    // Fields coming from outside the class

    private AppCompatActivity activity ;
    TextView temperatureTextView ;
    TextView pressureTextView ;
    private ArrayList<Double> temperaturesArray ;
    private ArrayList<Double> pressuresArray ;
    private ArrayList<Long> temperaturesTimesArray ;
    private ArrayList<Long> pressuresTimesArray ;

    // Fields created inside the class

    private MicrocontrollerBroadastReceiver broadastReceiver ;

    // Required init

    // Call this method in the onCreate of the activities which need to receive this data
    public MicrocontrollerManager(AppCompatActivity activity, TextView temperatureTextView, TextView pressureTextView, ArrayList<Long> temperaturesTimesArray, ArrayList<Double> temperaturesArray, ArrayList<Long> pressuresTimesArray, ArrayList<Double> pressuresArray) {
        this.activity = activity;
        this.temperatureTextView = temperatureTextView;
        this.pressureTextView = pressureTextView;
        this.temperaturesTimesArray = temperaturesTimesArray ;
        this.temperaturesArray = temperaturesArray;
        this.pressuresTimesArray = pressuresTimesArray ;
        this.pressuresArray = pressuresArray;
    }

    // Methods to handle the class

    public void startRecording() {
        //Get the HR data back from the watch
        // todo 3: create the Broadcast receiver
        // todo 4: register it to the local broadast manager
    }

    public void stopRecording() {
        // todo 5: unregister it from the manager
    }


    private class MicrocontrollerBroadastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // todo 1: get the pressure and the temperature, and the time at which we obtained them
            // (see in HRManager !)
            // todo 2: set them to the textviews (fields of this class)
        }
    }


}




