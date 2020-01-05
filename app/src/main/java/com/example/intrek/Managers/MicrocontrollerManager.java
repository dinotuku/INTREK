package com.example.intrek.Managers;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.intrek.DataModel.XYPlotSeriesList;
import com.example.intrek.SensorTile.BluetoothLeService;
import com.example.intrek.SensorTile.SampleGattAttributes;
import com.example.intrek.ui.main.LiveRecordingActivity;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

public class MicrocontrollerManager {

    // Fields coming from outside the class
    private static final String TEMP_PLOT = "Temperature from sensorTile";
    private static final String TAG = "in MicroManager";

    private AppCompatActivity activity ;
    TextView temperatureTextView ;
    TextView pressureTextView ;
    private ArrayList<Double> temperaturesArray ;
    private ArrayList<Double> pressuresArray ;
    private ArrayList<Long> temperaturesTimesArray ;
    private ArrayList<Long> pressuresTimesArray ;


    private double mTemperature;
    private double mPressure;
    private String mDeviceAddress;
    private boolean mConnected = false;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothLeService mBluetoothLeService;
    private long initialTime = System.currentTimeMillis() ;

    // Fields created inside the class

    // private MicrocontrollerBroadcastReceiver broadastReceiver ;


    // Call this method in the onCreate of the activities which need to receive this data
    public MicrocontrollerManager(AppCompatActivity activity, TextView temperatureTextView,
                                  TextView pressureTextView, ArrayList<Long> temperaturesTimesArray,
                                  ArrayList<Double> temperaturesArray, ArrayList<Long> pressuresTimesArray,
                                  ArrayList<Double> pressuresArray, String mDeviceAddress) {
        this.activity = activity;
        this.temperatureTextView = temperatureTextView;
        this.pressureTextView = pressureTextView;
        this.temperaturesTimesArray = temperaturesTimesArray ;
        this.temperaturesArray = temperaturesArray;
        this.pressuresTimesArray = pressuresTimesArray ;
        this.pressuresArray = pressuresArray;
        this.mDeviceAddress = mDeviceAddress;
    }

    // Start recording data
    public void startRecording(){
        Intent gattServiceIntent = new Intent(activity, BluetoothLeService.class);
        activity.bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        activity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.e(TAG, "Connect request result=" + result);
        }
    }

    // Stop Recording data
    public void stopRecording(){
        activity.unregisterReceiver(mGattUpdateReceiver);
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.e(TAG, "Inside OnServiceConnected");
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };




    /*
    // Broadcast receiver for the sensorTile
    private class MicrocontrollerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // todo 1: get the pressure and the temperature, and the time at which we obtained them

            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                activity.invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                activity.invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                registerTileService(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                mTemperature = intent.getDoubleExtra(BluetoothLeService.TEMPERATURE, -1)/10.0;
                mPressure = intent.getDoubleExtra(BluetoothLeService.PRESSURE, -1)/100.0;
                Log.e("In Manager","Temp: "+ mTemperature);
                Log.e("In Manager","Press: "+ mPressure);
                String s = String.valueOf(mTemperature) + " [C°]" ;
                temperatureTextView.setText(s);
                s = String.valueOf(mPressure) + " [mPa]" ;
                temperatureTextView.setText(s);

                // And add HR value to HR ArrayList
                temperaturesTimesArray.add(System.currentTimeMillis()-initialTime);
                pressuresTimesArray.add(System.currentTimeMillis()-initialTime);
                temperaturesArray.add(mTemperature);
                pressuresArray.add(mPressure);



            }

            // (see in HRManager !)
            // todo 2: set them to the textviews (fields of this class)
        }

    }

     */

    // Allow the characteristic with the temperature and the pressure
    private void registerTileService(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic
                    gattCharacteristic : gattCharacteristics) {
                uuid = gattCharacteristic.getUuid().toString();
                // Find heart rate measurement (0x2A37)
                if (SampleGattAttributes.lookup(uuid, "unknown")
                        .equals("Pressure + Temperature ")) {
                    final int charaProp = gattCharacteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        // If there is an active notification on a characteristic, clear
                        // it first so it doesn't update the data field on the user interface.
                        if (mNotifyCharacteristic != null) {
                            mBluetoothLeService.setCharacteristicNotification(
                                    mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }
                        mBluetoothLeService.readCharacteristic(gattCharacteristic);
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        Log.i(TAG, "Registering for Temperature and pressure measurement");
                        mNotifyCharacteristic = gattCharacteristic;
                        mBluetoothLeService.setCharacteristicNotification(
                                gattCharacteristic, true);
                    }
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
                activity.invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                activity.invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                registerTileService(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Double mTemperature = intent.getDoubleExtra(BluetoothLeService.TEMPERATURE, -1) /10.0;
                Double mPressure = intent.getDoubleExtra(BluetoothLeService.PRESSURE, -1)/100.0;

                // Treat the data here

                temperaturesTimesArray.add(System.currentTimeMillis()-initialTime);
                pressuresTimesArray.add(System.currentTimeMillis()-initialTime);
                temperaturesArray.add(mTemperature);
                pressuresArray.add(mPressure);

                // And display the data here

                String s = String.valueOf(mTemperature) + " [C°]" ;
                temperatureTextView.setText(s);
                s = String.valueOf(mPressure) + " [mPa]" ;
                pressureTextView.setText(s);

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

    public void destroy(){
        activity.unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
}




