package com.example.intrek.Managers;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class MicrocontrollerManager {

    // Fields coming from outside the class

    private static final int MIN_TILE = 40;
    private static final int MAX_TILE = 200;
    private static final int NUMBER_OF_POINTS = 50;
    private static final String TEMP_PLOT = "Temperature from sensorTile";
    private static final String PRESSURE_PLOT = "Pressure from sensorTile";
    private static final String TAG = "in MicroManager";
    private boolean hasPlot=false ;

    private AppCompatActivity activity ;
    TextView temperatureTextView ;
    TextView pressureTextView ;
    private ArrayList<Double> temperaturesArray ;
    private ArrayList<Double> pressuresArray ;
    private ArrayList<Long> temperaturesTimesArray ;
    private ArrayList<Long> pressuresTimesArray ;

    private XYPlot TemperaturePlot;
    private XYPlot PressurePlot;
    private XYPlotSeriesList xyPlotSeriesList;

    private BluetoothLeService mBluetoothLeService;
    private double mTemperature;
    private double mPressure;

    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private long initialTime = System.currentTimeMillis() ;

    // Fields created inside the class

    private MicrocontrollerBroadastReceiver broadastReceiver ;
    private boolean mConnected = false;

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
        // todo 3: create the Broadcast receiver -> OK
        broadastReceiver = new MicrocontrollerManager.MicrocontrollerBroadastReceiver();

        // todo 4: register it to the local broadast manager -> OK
        LocalBroadcastManager.getInstance(activity).registerReceiver(broadastReceiver,
                new IntentFilter(LiveRecordingActivity.ACTION_RECEIVE_TILE));


    }

    public void stopRecording() {
        // todo 5: unregister it from the manager
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(broadastReceiver);
    }


    private class MicrocontrollerBroadastReceiver extends BroadcastReceiver {

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

                if (hasPlot) {
                    // Plot the graph
                    xyPlotSeriesList.updateSeries(TEMP_PLOT, (int) mTemperature);
                    XYSeries hrWatchSeries = new SimpleXYSeries(xyPlotSeriesList.getSeriesFromList(TEMP_PLOT),
                            SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, TEMP_PLOT);
                    LineAndPointFormatter formatterPolar = xyPlotSeriesList.getFormatterFromList(TEMP_PLOT);
                    TemperaturePlot.clear();
                    TemperaturePlot.addSeries(hrWatchSeries, formatterPolar);
                    TemperaturePlot.redraw();
                    // And add HR value to HR ArrayList
                    temperaturesTimesArray.add(System.currentTimeMillis()-initialTime);
                    pressuresTimesArray.add(System.currentTimeMillis()-initialTime);
                    temperaturesArray.add(mTemperature);
                    pressuresArray.add(mPressure);
                }


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
                    Log.e(TAG, "Registering for Pressure and Temperature measurement");
                    mBluetoothLeService.setCharacteristicNotification(
                            gattCharacteristic, true);
                }
            }
        }
    }
}




