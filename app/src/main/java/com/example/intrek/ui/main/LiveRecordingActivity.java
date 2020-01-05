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
        Log.e(TAG,"Address : " + mDeviceAddress);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

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
                pressureTextView,temperaturesTimesArray,temperaturesArray,pressuresTimesArray,pressuresArray);

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
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.e(TAG, "Connect request result=" + result);
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
        microcontrollerManager.startRecording();
        isPaused = true ;
        unregisterReceiver(mGattUpdateReceiver);
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

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.e(TAG, "Inside OnServiceConnected");
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private class MicrocontrollerBroadastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // todo 1: get the pressure and the temperature, and the time at which we obtained them

            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                registerTileService(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Double mTemperature = intent.getDoubleExtra(BluetoothLeService.TEMPERATURE, -1) /10.0;
                Double mPressure = intent.getDoubleExtra(BluetoothLeService.PRESSURE, -1)/100.0;
                String s = String.valueOf(mTemperature) + " [C°]" ;
                temperatureTextView.setText(s);
                s = String.valueOf(mPressure) + " [mPa]" ;
                temperatureTextView.setText(s);
                Log.e("In Manager","Temp: "+ mTemperature);
                Log.e("In Manager","Press: "+ mPressure);



            }

            // (see in HRManager !)
            // todo 2: set them to the textviews (fields of this class)
        }

    }

    private void registerTileService(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic
                    gattCharacteristic : gattCharacteristics) {
                uuid = gattCharacteristic.getUuid().toString();
                Log.e(TAG, "Value : " + gattCharacteristic.getValue());
                // Find heart rate measurement (0x2A37)
                if (SampleGattAttributes.lookup(uuid, "unknown")
                        .equals("Pressure + Temperature ")) {
                    Log.e(TAG, "Registering for Pressure and Temperature measurement");
                    mBluetoothLeService.setCharacteristicNotification(
                            gattCharacteristic, true);
                }
            }
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                registerTileService(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Double mTemperature = intent.getDoubleExtra(BluetoothLeService.TEMPERATURE, -1) /10.0;
                Double mPressure = intent.getDoubleExtra(BluetoothLeService.PRESSURE, -1)/100.0;
                String s = String.valueOf(mTemperature) + " [C°]" ;
                temperatureTextView.setText(s);
                s = String.valueOf(mPressure) + " [mPa]" ;
                temperatureTextView.setText(s);
                Log.e("In Manager","Temp: "+ mTemperature);
                Log.e("In Manager","Press: "+ mPressure);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }


    //endregion

}

