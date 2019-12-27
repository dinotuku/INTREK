package com.example.intrek.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.TextView;

import com.example.intrek.Interfaces.OnPositionUpdatedCallback;
import com.example.intrek.Managers.GPSManager;
import com.example.intrek.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class LiveMapActivity extends AppCompatActivity implements OnMapReadyCallback, OnPositionUpdatedCallback {

    private GoogleMap mMap;
    private Marker mapMarker;

    private TextView HRTextView ;
    // References to the textview to be used
    private TextView speedTextView;
    private TextView distanceTextView;
    private TextView altitudeTextView;
    private TextView dataPointsTextView;
    private Chronometer chronometer;

    // Variables of the clasd
    ArrayList<LatLng> locations;
    private GPSManager gpsManager;
    private Polyline polyline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.GoogleMap);
        mapFragment.getMapAsync(this);

        // Get the objects to be used later
        Intent intent = getIntent();
        locations = (ArrayList<LatLng>) intent.getExtras().get("Locations");
        HRTextView = findViewById(R.id.HRTextView);
        speedTextView = findViewById(R.id.speedTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        altitudeTextView = findViewById(R.id.altitudeTextView);
        dataPointsTextView = findViewById(R.id.dataPointsTextView);
        chronometer = findViewById(R.id.chrono);

        // Set the chronometer to correct
        Long timerValue = getIntent().getLongExtra("timerValue",0);
        Log.i("ABCDE",String.valueOf(timerValue));
        chronometer.setBase(SystemClock.elapsedRealtime() + timerValue);
        chronometer.start();

        // Create the services
        gpsManager = new GPSManager(this,speedTextView,distanceTextView,altitudeTextView,dataPointsTextView) ;
        gpsManager.setPositionCallback(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        gpsManager.startRecording();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gpsManager.stopRecording();
        chronometer.stop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Get the locations obtained
        LatLng currentLocation = locations.get(0);

        // Add a marker in the default location and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17));
        if(mapMarker!=null){ mapMarker.remove(); }

        // Draw the initial map.
        drawPath(locations);
    }

    private void drawPath(ArrayList<LatLng> locations) {
        polyline = mMap.addPolyline(new PolylineOptions().addAll(locations).width(5).color(Color.BLUE));
    }

    private void placeMarkers(ArrayList<LatLng> locations) {
        for (LatLng l: locations) {
            Marker newMarker = mMap.addMarker(new MarkerOptions().position(l).title("Point"));

        }
    }

    @Override
    public void newPointAvailable(LatLng point) {
        // This function is from our protocol ! Let's use it
        locations.add(point);
        polyline.setPoints(locations);

    }
}
