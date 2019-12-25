package com.example.intrek.ui.main;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.intrek.DataModel.XYPlotSeriesList;
import com.example.intrek.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.example.intrek.R.id.HRPlot;

public class LiveRecordingActivity extends AppCompatActivity {

    public static final String ACTION_RECEIVE_HEART_RATE = "ACTION_RECEIVE_HEART_RATE";
    public static final String HEART_RATE = "HeartRate";
    private static final int MIN_HR = 40;
    private static final int MAX_HR = 200;
    private static final int NUMBER_OF_POINTS = 50;
    private static final String HR_PLOT_WATCH = "HR from smart watch";

    private Chronometer timerTextView;
    private Button pauseButton;
    private XYPlot heartRatePlot;

    private long timerValueWhenPaused = 0;
    private boolean isPaused = false;
    private HeartRateBroadcastReceiver heartRateBroadcastReceiver;
    private int heartRateWatch = 0;
    private ArrayList<Integer> hrDataArrayList = new ArrayList<>();
    private XYPlotSeriesList xyPlotSeriesList;
    private TextView speedTextView;
    private TextView distanceTextView;

    // For the location
    private int i = 0 ;
    private long initialTime = 0 ;
    // Arrays used to compute the speed and distances
    private ArrayList<Long> times = new ArrayList<>();
    private ArrayList<LatLng> locations = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_recording);

        // 0. Get the elements of the UI
        timerTextView = findViewById(R.id.timerTextView);
        speedTextView = findViewById(R.id.SpeedTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        timerTextView.start();
        pauseButton = findViewById(R.id.PauseButton);
        heartRatePlot = findViewById(HRPlot);
        configurePlot();
        setHRPlot();

        // 2. Add location manager to retrieve all the positions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission("android" + "" + ".permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED || checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED || checkSelfPermission("android" + "" + ".permission.INTERNET") == PackageManager.PERMISSION_DENIED)) {
            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android" + ".permission.ACCESS_COARSE_LOCATION", "android.permission.INTERNET"}, 0);
        }
        initialTime = System.currentTimeMillis();
        fusedLocationClient = new FusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) { return; }
                for (Location location : locationResult.getLocations()) {
                    onLocationChanged(location);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        resume();
        //startLocationRecording();
        //Get the HR data back from the watch
        //heartRateBroadcastReceiver = new HeartRateBroadcastReceiver();
        //LocalBroadcastManager.getInstance(this).registerReceiver(heartRateBroadcastReceiver, new IntentFilter(ACTION_RECEIVE_HEART_RATE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
        //stopLocationUpdates();
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(heartRateBroadcastReceiver);
    }

    public void mapButtonTapped(View view) {
        // Open a new activity which will show the map
        Intent startMapIntent = new Intent(LiveRecordingActivity.this, LiveMapActivity.class);
        startActivity(startMapIntent);
    }

    public void pauseButtonTapped(View view) {
        if (isPaused) {
            resume();
        } else {
            pause();
        }
    }

    private void resume() {
        // START AGAIN
        timerTextView.setBase(SystemClock.elapsedRealtime() + timerValueWhenPaused);
        timerTextView.start();
        pauseButton.setText("Pause");
        startLocationRecording();
        //Get the HR data back from the watch
        heartRateBroadcastReceiver = new HeartRateBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(heartRateBroadcastReceiver, new IntentFilter(ACTION_RECEIVE_HEART_RATE));
        isPaused = false ;
    }

    private void pause() {
        // PAUSE
        timerValueWhenPaused = timerTextView.getBase() - SystemClock.elapsedRealtime();
        timerTextView.stop();
        pauseButton.setText("Resume");
        stopLocationUpdates();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(heartRateBroadcastReceiver);
        isPaused = true ;
    }

    // MARK: - Private methods

    private void configurePlot() {
        // Get background color from Theme
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
        int backgroundColor = typedValue.data;
        // Set background colors
        heartRatePlot.setPlotMargins(0, 0, 0, 0);
        heartRatePlot.getBorderPaint().setColor(backgroundColor);
        heartRatePlot.getBackgroundPaint().setColor(backgroundColor);
        heartRatePlot.getGraph().getBackgroundPaint().setColor(backgroundColor);
        heartRatePlot.getGraph().getGridBackgroundPaint().setColor(backgroundColor);
        // Set the grid color
        heartRatePlot.getGraph().getRangeGridLinePaint().setColor(Color.DKGRAY);
        heartRatePlot.getGraph().getDomainGridLinePaint().setColor(Color.DKGRAY);
        // Set the origin axes colors
        heartRatePlot.getGraph().getRangeOriginLinePaint().setColor(Color.DKGRAY);
        heartRatePlot.getGraph().getDomainOriginLinePaint().setColor(Color.DKGRAY);
        // Set the XY axis boundaries and step values
        heartRatePlot.setRangeBoundaries(MIN_HR, MAX_HR, BoundaryMode.FIXED); heartRatePlot.setDomainBoundaries(0, NUMBER_OF_POINTS - 1, BoundaryMode.FIXED);
        heartRatePlot.setRangeStepValue(9); // 9 values 40 60 ... 200
        heartRatePlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("#"));
        // This line is to force the Axis to be integer
        heartRatePlot.setRangeLabel("Heart rate (bpm)");
    }

    private void setHRPlot() {
        xyPlotSeriesList = new XYPlotSeriesList();
        LineAndPointFormatter formatterWatch = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT, Color.TRANSPARENT, null);
        formatterWatch.getLinePaint().setStrokeWidth(8);
        xyPlotSeriesList.initializeSeriesAndAddToList(HR_PLOT_WATCH, MIN_HR, NUMBER_OF_POINTS, formatterWatch);
        XYSeries HRseries = new SimpleXYSeries(xyPlotSeriesList.getSeriesFromList(HR_PLOT_WATCH), SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, HR_PLOT_WATCH);
        heartRatePlot.clear();
        heartRatePlot.addSeries(HRseries, formatterWatch);
        heartRatePlot.redraw();
    }

    // MARK: - methods used for location computation

    private void startLocationRecording(){
        LocationRequest locationRequest = new LocationRequest().setInterval(5).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // This method is called everytime a new location has been obtained.
    private void onLocationChanged(Location location) {
        if (locations.size()==0) {
            initialTime = System.currentTimeMillis() ;
            times.add(System.currentTimeMillis()-initialTime);
            locations.add(new LatLng(location.getLatitude(),location.getLongitude()));
            // Do some mathematics here
            double dist = SphericalUtil.computeLength(locations);
            distanceTextView.setText("Travelled distance: " + dist);
            speedTextView.setText("Current speed:" + location.getSpeed());
        }
        // Noise filter: check against small radius
        double noiseRadius = 1 ; // meter
        double speedLowerLimit = 0.5 ; // km/h
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        double lastDistance = SphericalUtil.computeDistanceBetween(locations.get(locations.size()-1),latLng) ;
        if (lastDistance > noiseRadius && location.getSpeed() > speedLowerLimit) {
            // Then it means we accept the new value
            times.add(System.currentTimeMillis()-initialTime);
            locations.add(latLng);
            // Do some mathematics here
            double dist = SphericalUtil.computeLength(locations);
            distanceTextView.setText("Travelled distance: " + dist);
            speedTextView.setText("Current speed:" + location.getSpeed());
        }
    }

    //get the speed from the given location updates
    private double computeSpeed() {
        return 0.0;
    }

    // MARK: - Inner class to listen the watch data

    private class HeartRateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Show HR in a TextView
            heartRateWatch = intent.getIntExtra(HEART_RATE, -1);
            TextView hrTextView = findViewById(R.id.HRTextView);
            hrTextView.setText(String.valueOf(heartRateWatch));
            // Plot the graph
            xyPlotSeriesList.updateSeries(HR_PLOT_WATCH, heartRateWatch);
            XYSeries hrWatchSeries = new SimpleXYSeries(xyPlotSeriesList.getSeriesFromList(HR_PLOT_WATCH), SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, HR_PLOT_WATCH);
            LineAndPointFormatter formatterPolar = xyPlotSeriesList.getFormatterFromList(HR_PLOT_WATCH);
            heartRatePlot.clear();
            heartRatePlot.addSeries(hrWatchSeries, formatterPolar);
            heartRatePlot.redraw();
            // And add HR value to HR ArrayList
            hrDataArrayList.add(heartRateWatch);
        }

    }
}

