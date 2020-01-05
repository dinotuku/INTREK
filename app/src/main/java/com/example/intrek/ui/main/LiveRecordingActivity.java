package com.example.intrek.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.intrek.BuildConfig;
import com.example.intrek.DataModel.Recording;
import com.example.intrek.Managers.GPSManager;
import com.example.intrek.Managers.HRManager;
import com.example.intrek.Managers.MicrocontrollerManager;
import com.example.intrek.R;
import com.example.intrek.SensorTile.BluetoothLeService;
import com.example.intrek.SensorTile.SampleGattAttributes;
import com.example.intrek.WearService;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Show statistics and heart rate plot when user starts an activity.
public class LiveRecordingActivity extends AppCompatActivity {

    public static final String ACTION_RECEIVE_HEART_RATE = "ACTION_RECEIVE_HEART_RATE";
    public static final String HEART_RATE = "HeartRate";
    private static final String TAG = "In LiveRecording";

    public static final String ACTION_RECEIVE_TILE = "ACTION_RECEIVE_TILE ";

    private Chronometer timerTextView;
    private Button pauseButton;
    private XYPlot heartRatePlot;
    private TextView speedTextView;
    private TextView distanceTextView;
    private TextView pressureTextView;
    private TextView temperatureTextView;
    private TextView avePaceTextView;
    private TextView dataPointsTextView;
    private TextView altitudeTextView;

    private long timerValueWhenPaused = 0;
    private boolean isPaused = false;
    // https://stackoverflow.com/questions/5369682/how-to-get-current-time-and-date-in-android
    private String startingTime ;
    private GPSManager gpsManager;
    private HRManager hrManager ;
    private MicrocontrollerManager microcontrollerManager ;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private BluetoothLeService mBluetoothLeService;
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected = false;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";


    // For the location
    private int i = 0 ;

    // Arrays for the location and for the distance (same time vector)
    private ArrayList<Long> locationsTimes = new ArrayList<>();
    private ArrayList<LatLng> locations = new ArrayList<>();
    private ArrayList<LatLng> averagedLocations = new ArrayList<>();
    private ArrayList<Long> distanceTimes = new ArrayList<>();
    private ArrayList<Double> distances = new ArrayList<>();

    // Arrays for the speed
    private ArrayList<Double> speeds = new ArrayList<>();
    private ArrayList<Long> speedsTimes = new ArrayList<>();

    // Arrays for the HR
    private ArrayList<Integer> hrDataArrayList = new ArrayList<>();
    private ArrayList<Long> hrTimes = new ArrayList<>();
    private ArrayList<Double> altitudes = new ArrayList<>();

    // Arrays for the microcontroller
    private ArrayList<Double> temperaturesArray = new ArrayList<>();
    private ArrayList<Double> pressuresArray = new ArrayList<>();
    private ArrayList<Long> temperaturesTimesArray = new ArrayList<>();
    private ArrayList<Long> pressuresTimesArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_recording);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // 1. Get the elements of the UI
        timerTextView = findViewById(R.id.timerTextView);
        speedTextView = findViewById(R.id.SpeedTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        pressureTextView = findViewById(R.id.pressureTextView);
        dataPointsTextView = findViewById(R.id.dataPointTextView);
        altitudeTextView = findViewById(R.id.altitudeTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        avePaceTextView = findViewById(R.id.avePaceTextView);
        pauseButton = findViewById(R.id.PauseButton);
        heartRatePlot = findViewById(R.id.HRPlot);


        // 2. Add location manager to retrieve all the positions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission("android" + "" + ".permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED || checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED || checkSelfPermission("android" + "" + ".permission.INTERNET") == PackageManager.PERMISSION_DENIED)) {
            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android" + ".permission.ACCESS_COARSE_LOCATION", "android.permission.INTERNET"}, 0);
        }
        gpsManager = new GPSManager(this, speedTextView,distanceTextView,altitudeTextView,dataPointsTextView);
        gpsManager.setArraysToCollectData(locationsTimes,locations,averagedLocations,distanceTimes,distances,speedsTimes,speeds,altitudes);
        gpsManager.setAveragePactextView(avePaceTextView);

        // 3. Add the HR manager
        /*
        startRecordingOnWatch();
        TextView hrTextView = findViewById(R.id.HRTextView);
        hrManager = new HRManager(this, hrTextView) ;
        hrManager.setToPlot(heartRatePlot,hrDataArrayList,hrTimes);*/

        // 4. Add the microcontroller manager
        microcontrollerManager = new MicrocontrollerManager(this, temperatureTextView,
                pressureTextView,temperaturesTimesArray,temperaturesArray,pressuresTimesArray,pressuresArray,mDeviceAddress);

        // 5. Start all recordings !
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        startingTime = sdf.format(new Date());
        resume();
    }

    private void startRecordingOnWatch() {
        // Send an intent to open watch
        Intent intentStartRec = new Intent(this, WearService.class);
        intentStartRec.setAction(WearService.ACTION_SEND.START_ACTIVITY.name());
        intentStartRec.putExtra(WearService.START_ACTIVITY_KEY, BuildConfig.W_start_activity);
        startService(intentStartRec);
    }

    //region Pause functions
    private void resume() {
        // START AGAIN
        timerTextView.setBase(SystemClock.elapsedRealtime() + timerValueWhenPaused);
        timerTextView.start();
        pauseButton.setText("Pause");
        gpsManager.startRecording();
        //hrManager.startRecording();
        //Try to start the SensorTile only if there is a name
        if (mDeviceName!=null){
            microcontrollerManager.startRecording();
        }
        isPaused = false ;
    }

    private void pause() {
        // PAUSE
        timerValueWhenPaused = timerTextView.getBase() - SystemClock.elapsedRealtime();
        timerTextView.stop();
        pauseButton.setText("Resume");
        gpsManager.stopRecording();
        //hrManager.stopRecording();
        if (mDeviceName!=null) {
            microcontrollerManager.stopRecording();
        }
        isPaused = true ;

    }
    //endregion

    //region Function for handling the buttons actions
    public void pauseButtonTapped(View view) {
        if (isPaused) {
            resume();
        } else {
            pause();
        }
    }

    public void mapButtonTapped(View view) {
        // Open a new activity which will show the map
        Intent startMapIntent = new Intent(LiveRecordingActivity.this, LiveMapActivity.class);
        // Put all the extras for the mapActivity to work
        startMapIntent.putExtra("Locations",this.locations);
        timerValueWhenPaused = timerTextView.getBase() - SystemClock.elapsedRealtime();
        startMapIntent.putExtra("timerValue",timerValueWhenPaused);
        startMapIntent.putExtra("distanceOffset",gpsManager.getDistance()) ;
        startActivity(startMapIntent);
    }

    // Open the recording manager to view analysis of the hike
    public void finishRecordingButtonTapped(View view) {
        pause();

        // 1. Create the recording
        String duration = (String) this.timerTextView.getText();
        Recording r = new Recording(duration,distanceTimes,distances,speedsTimes,speeds,altitudes,hrTimes,hrDataArrayList,temperaturesTimesArray, temperaturesArray, pressuresTimesArray, pressuresArray);
        r.constructURLFromLocations(averagedLocations);
        r.setStartingTime(startingTime);
        r.setElevationGain(gpsManager.getElevationGain());


        // 2. Send it to the new activity
        String uid = getIntent().getStringExtra(ProfileFragment.UID);
        Intent i = new Intent(LiveRecordingActivity.this, RecordingAnalysisActivity.class);
        i.putExtra("Recording", r);
        i.putExtra(ProfileFragment.UID,uid);
        startActivity(i);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDeviceName!=null) {
            microcontrollerManager.destroy();
        }
    }


    //endregion

}

