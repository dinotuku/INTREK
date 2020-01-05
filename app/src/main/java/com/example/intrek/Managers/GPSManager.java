package com.example.intrek.Managers;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.widget.TextView;

import com.example.intrek.BuildConfig;
import com.example.intrek.Interfaces.OnPositionUpdatedCallback;
import com.example.intrek.WearService;
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
    private boolean hasDistanceOffset ;
    private boolean hasAvePace;
    private boolean isSendingNoticeToWatch ;
    private Double distanceOffsetInMeter  ;

    // All arrays that are used and need to be updated
    /*
    Note for the coder:
    some array are only here to save the data, but not all of them.
    The arrays locations and average locations are important to compute the distance as well !
     */
    private long initialTime = System.currentTimeMillis() ;
    NumberFormat nf = new DecimalFormat("##.##");
    private int i = 0 ;
    private double currentGain = 0.0  ;

    // This array is only used here and
    private ArrayList<Double> averagedAltitudes = new ArrayList<>();

    // Arrays to be saved later
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
    private TextView avePaceTextView;

    // Set the GPS manager to the given activity.
    // If the function 'setArraysToCollectData' is not called, then it will not collect the data.
    public GPSManager(Activity activity, TextView speedTextView, TextView distanceTextView, TextView altitudeTextView, TextView dataPointsTextView) {
        this.activity = activity;
        this.isCollectingData = false;
        this.hasMapCallback = false;
        this.hasDistanceOffset = false ;
        this.hasAvePace = false  ;
        this.isSendingNoticeToWatch = false ;
        this.distanceOffsetInMeter = 0.0 ;
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

    // Set the distance offset. If this function is called, every distance outèut will be moved by a special offset in meters.
    public void setDistanceOffset(Double offset) {
        this.hasDistanceOffset = true ;
        this.distanceOffsetInMeter = offset ;
        displayDistance(0.0);
    }

    public void setAveragePacextView(TextView avePaceTextView) {
        this.avePaceTextView = avePaceTextView ;
        this.hasAvePace = true ;
    }

    // If this method is called, then the gps will send the data to the watch as well.
    public void setSendingNoticeToWatch() {
        this.isSendingNoticeToWatch = true ;
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
        int N_pos = 5 ;

        // 1. Add the data to be process / save (optional)
        LatLng newPoint = new LatLng(location.getLatitude(),location.getLongitude());
        double speed = location.getSpeed() * 3.6;
        double altitude = location.getAltitude();

        locations.add(newPoint);
        if (isCollectingData) {
            locationsTimes.add(System.currentTimeMillis()-initialTime);
            speeds.add(speed);
            altitudes.add(altitude);
            speedsTimes.add(System.currentTimeMillis()-initialTime);
        }

        if (hasMapCallback) {
            this.callback.newPointAvailable(newPoint);
        }

        displaySpeed(speed);
        displayAltitude(altitude);
        displayDataPoints();
        if (hasAvePace) {
            displayAvePace();
        }


        // 2. Average the location over N_pos last values and compute the distance

        if (locations.size()>N_pos) {
            double lats = 0.0 ; double longs = 0.0 ; double alts = 0.0 ;
            for (int j=0; j < N_pos; j++) {
                LatLng l = locations.get(locations.size()-1-j);
                lats += l.latitude ;
                longs += l.longitude ;
                if (isCollectingData) {
                    alts += altitudes.get(altitudes.size()-1-j) ;
                }
            }
            averagedLocations.add(new LatLng(lats/N_pos, longs/N_pos));
            if (isCollectingData) {
                averagedAltitudes.add(alts/N_pos) ;
                updateElevationGain();
            }

            double dist = SphericalUtil.computeLength(averagedLocations);
            displayDistance(dist);
            if (isCollectingData) {
                distanceTimes.add(System.currentTimeMillis()-initialTime);
                distances.add(dist);
            }

        }

    }

    private void sendPaceToWatch(Double data) {
        // Send an intent to open watch
        Intent intentStartRec = new Intent(this.activity, WearService.class);
        intentStartRec.setAction(WearService.ACTION_SEND.PACE.name());
        intentStartRec.putExtra(WearService.PACE, data);
        activity.startService(intentStartRec);
    }

    private void sendDistanceToWatch(Double data) {
        // Send an intent to open watch
        Intent intentStartRec = new Intent(this.activity, WearService.class);
        intentStartRec.setAction(WearService.ACTION_SEND.DISTANCE.name());
        intentStartRec.putExtra(WearService.DISTANCE, data);
        activity.startService(intentStartRec);
    }

    //// GETTERS for the class

    public Double getDistance() {
        return averagedLocations.size() > 0 ? SphericalUtil.computeLength(averagedLocations) : 0.0 ;
    }

    public Double getElevationGain() {
        return currentGain;
    }

    // Updates the value of the elevation gain using the last 2 values of the average altitudes array
    private void updateElevationGain() {
        if (averagedAltitudes.size()>1) {
            int i = averagedAltitudes.size()-1;
            double deltaZ = averagedAltitudes.get(i) - averagedAltitudes.get(i-1) ;
            if (deltaZ>0)
                currentGain += deltaZ ;
        }
    }

    private void displayDistance(Double dist) {
        Double inKm = (dist + (hasDistanceOffset ? distanceOffsetInMeter : 0.0)) / 1000 ;
        String s = nf.format(inKm) + " [km]";
        if (isSendingNoticeToWatch) {
            sendDistanceToWatch(inKm);
        }
        distanceTextView.setText(s);
    }

    private void displaySpeed(Double speed) {
        if (speed > 0) {
            Double inKmH = speed ;
            Double pace = 60 / inKmH ;
            if (pace > 20) {
                pace = 0.0 ;
            }
            if (isSendingNoticeToWatch) {
                sendPaceToWatch(pace);
            }
            String s = nf.format(pace) + " [min/km]" ;
            speedTextView.setText(s);
        }
    }

    private void displayAvePace() {
        // 1. Compute the average speed
        Double aveSpeed = 0.0 ;
        for (Double s: speeds) {
            aveSpeed += s ;
        }
        aveSpeed = aveSpeed / speeds.size() ;
        // 2. display the average speed
        Double avePace = 60 / aveSpeed ;
        if (avePace > 20) {
            avePace = 0.0 ;
        }
        String s = nf.format(avePace) + " [min/km]" ;
        avePaceTextView.setText(s);
    }

    private void displayAltitude(Double alt) {
        String s = nf.format(currentGain) + " [m]";
        altitudeTextView.setText(s);
    }

    private void displayDataPoints() {
        String s = String.valueOf(i) + " GPS datapoints" ;
        dataPointsTextView.setText(s);
    }


}
