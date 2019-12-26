package com.example.intrek.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
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
import com.example.intrek.DataModel.Recording;
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
    private TextView speedTextView;
    private TextView distanceTextView;
    private TextView pressureTextView;

    private long timerValueWhenPaused = 0;
    private boolean isPaused = false;
    private HeartRateBroadcastReceiver heartRateBroadcastReceiver;
    private int heartRateWatch = 0;
    private XYPlotSeriesList xyPlotSeriesList;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // For the location
    private int i = 0 ;
    private long initialTime = 0 ;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_recording);

        // 0. Get the elements of the UI
        timerTextView = findViewById(R.id.timerTextView);
        speedTextView = findViewById(R.id.SpeedTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        pressureTextView = findViewById(R.id.pressureTextView);
        timerTextView.start();
        pauseButton = findViewById(R.id.PauseButton);
        heartRatePlot = findViewById(R.id.HRPlot);


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

        // 3. Perform the required initialisation
        initialTime = System.currentTimeMillis();
    }

    // region Overridden function of activity
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
    //endregion

    //region Pause functions

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
    //endregion


    public void mapButtonTapped(View view) {
        // Open a new activity which will show the map
        Intent startMapIntent = new Intent(LiveRecordingActivity.this, LiveMapActivity.class);
        startMapIntent.putExtra("Locations",this.locations);
        startActivity(startMapIntent);
    }

    // Open the recording manager to view analysis of the hike
    public void finishRecordingButtonTapped(View view) {
        pause();
        // 1. Create the recording
        Recording r = new Recording(distanceTimes,distances,speedsTimes,speeds,hrTimes,hrDataArrayList);

        // 2. Send it to the new activity
        Intent i = new Intent(LiveRecordingActivity.this, RecordingAnalysisActivity.class);
        i.putExtra("Recording", r);
        startActivity(i);

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

        int N_pos = 5 ;
        i ++ ;
        int numberOfPoints = locations.size();
        if (numberOfPoints<=N_pos) {
            // 1. Initialisation of the data
            // Just add the values without any computation
            locationsTimes.add(System.currentTimeMillis()-initialTime);
            locations.add(new LatLng(location.getLatitude(),location.getLongitude()));
        } else {
            // 2. Average the location over N_pos last values
            locationsTimes.add(System.currentTimeMillis()-initialTime);
            locations.add(new LatLng(location.getLatitude(),location.getLongitude()));
            double lats = 0.0 ;
            double longs = 0.0 ;
            for (int j=0; j < N_pos; j++) {
                LatLng l = locations.get(numberOfPoints-1-j);
                lats += l.latitude ;
                longs += l.longitude ;
            }
            LatLng averagedValue = new LatLng(lats/N_pos, longs/N_pos);
            averagedLocations.add(averagedValue);
            distanceTimes.add(System.currentTimeMillis()-initialTime);

            // 3. Do the computation for the distance for instance
            double dist = SphericalUtil.computeLength(averagedLocations);
            distances.add(dist);
            distanceTextView.setText("Travelled distance: " + dist);
        }

        // 4. Save the speed
        double speed = location.getSpeed();
        speeds.add(speed);
        speedsTimes.add(System.currentTimeMillis()-initialTime);
        speedTextView.setText("Current speed:" + speed);
        pressureTextView.setText(String.valueOf(i));

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
            hrTimes.add(System.currentTimeMillis()-initialTime);
        }

    }
}

