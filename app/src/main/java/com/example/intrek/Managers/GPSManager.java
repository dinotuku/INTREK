package com.example.intrek.Managers;

import android.app.Activity;
import android.location.Location;
import android.os.Looper;
import android.widget.TextView;

import com.example.intrek.Interfaces.OnPositionUpdatedCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

// This class is used to handle the GPS over an activity.
// It will handle all the receiving of new data by the GPS values and the updating of the appropriate TextView
// This class has 3 features:
// 1. It displays on the activity the collected data.
// 2. (Optional) It saves the data onto arrays to be post analysed.
// 3. (Optional) call a function whem position is updated with the new position
public class GPSManager {

    private Activity activity;
    private OnPositionUpdatedCallback callback;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isCollectingData;
    private boolean hasMapCallback ;


    // All arrays that are used and need to be updated
    /*
    Note for the coder:
    some array are only here to save the data, but not all of them.
    The arrays locations and average locations are important to compute the distance as well !
     */
    private long initialTime = System.currentTimeMillis() ;
    private int i = 0 ;
    private ArrayList<Long> locationsTimes ;
    private ArrayList<LatLng> locations = new ArrayList<>() ;
    private ArrayList<LatLng> averagedLocations = new ArrayList<>() ;
    private ArrayList<Long> distanceTimes ;
    private ArrayList<Double> distances ;
    private ArrayList<Long> speedsTimes ;
    private ArrayList<Double> speeds ;
    private ArrayList<Double> altitudes;

    // References to the textview to be used
    private TextView speedTextView;
    private TextView distanceTextView;
    private TextView altitudeTextView;
    private TextView dataPointsTextView;

    // Set the GPS manager to the given activity.
    // If the function 'setArraysToCollectData' is not called, then it will not collect the data.
    public GPSManager(Activity activity, TextView speedTextView, TextView distanceTextView, TextView altitudeTextView, TextView dataPointsTextView) {
        this.activity = activity;
        this.isCollectingData = false;
        this.hasMapCallback = false;
        this.speedTextView = speedTextView ;
        this.distanceTextView = distanceTextView ;
        this.altitudeTextView = altitudeTextView ;
        this.dataPointsTextView = dataPointsTextView ;
        fusedLocationClient = new FusedLocationProviderClient(activity);
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

    public void setArraysToCollectData(ArrayList<Long> locationsTimes, ArrayList<LatLng> locations, ArrayList<LatLng> averagedLocations, ArrayList<Long> distanceTimes, ArrayList<Double> distances, ArrayList<Long> speedsTimes, ArrayList<Double> speeds, ArrayList<Double> altitudes) {
        this.isCollectingData = true ;
        this.locationsTimes = locationsTimes;
        this.locations = locations;
        this.averagedLocations = averagedLocations;
        this.distanceTimes = distanceTimes;
        this.distances = distances;
        this.speedsTimes = speedsTimes;
        this.speeds = speeds;
        this.altitudes = altitudes;
    }

    public void setPositionCallback(OnPositionUpdatedCallback callback) {
        this.hasMapCallback = true ;
        this.callback = callback;
    }

    public void startRecording() {
        LocationRequest locationRequest = new LocationRequest().setInterval(5).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void stopRecording() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // This method is called everytime a new location has been obtained.
    private void onLocationChanged(Location location) {
        int N_pos = 5 ; i ++ ;

        // 1. Add the data to be process / save (optional)
        if (isCollectingData) {
            locationsTimes.add(System.currentTimeMillis()-initialTime);
        }
        LatLng newPoint = new LatLng(location.getLatitude(),location.getLongitude());
        locations.add(newPoint);
        int numberOfPoints = locations.size();
        if (hasMapCallback) {
            this.callback.newPointAvailable(newPoint);
        }


        if (numberOfPoints>N_pos) {
            // 2. Average the location over N_pos last values and compute the distance
            double lats = 0.0 ; double longs = 0.0 ;
            for (int j=0; j < N_pos; j++) {
                LatLng l = locations.get(numberOfPoints-1-j);
                lats += l.latitude ;
                longs += l.longitude ;
            }
            LatLng averagedValue = new LatLng(lats/N_pos, longs/N_pos);
            averagedLocations.add(averagedValue);
            double dist = SphericalUtil.computeLength(averagedLocations);
            displayDistance(dist);

            if (isCollectingData) {
                distanceTimes.add(System.currentTimeMillis()-initialTime);
                distances.add(dist);
            }
        }

        // 3. Get the speed and the altitude
        double speed = location.getSpeed();
        double altitude = location.getAltitude();
        displaySpeed(speed);
        displayAltitude(altitude);
        displayDataPoints();
        if (isCollectingData) {
            speeds.add(speed);
            altitudes.add(altitude);
            speedsTimes.add(System.currentTimeMillis()-initialTime);
        }

        // 4. Callback for position
    }

    private void displayDistance(Double dist) {
        Double inKm = dist / 1000 ;
        NumberFormat nf = new DecimalFormat("##.##");
        String s = nf.format(inKm) + " km";
        distanceTextView.setText(s);
    }

    private void displaySpeed(Double speed) {
        if (speed > 0) {
            Double inKmH = speed * 3.6 ;
            Double pace = 60 / inKmH ;
            if (pace > 20) {
                pace = 0.0 ;
            }
            NumberFormat nf = new DecimalFormat("##.##");
            String s = nf.format(speed) + " [km/h] - " + nf.format(pace) + " [min/km]" ;
            speedTextView.setText(s);
        }
    }

    private void displayAltitude(Double alt) {
        NumberFormat nf = new DecimalFormat("##.##");
        String s = nf.format(alt) + " m";
        altitudeTextView.setText(s);
    }

    private void displayDataPoints() {
        String s = String.valueOf(i) + " GPS datapoints" ;
        dataPointsTextView.setText(s);
    }


}
