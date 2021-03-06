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
import com.example.intrek.Managers.HRManager;
import com.example.intrek.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

// Show a live map with the current location and some key statistics when user starts an activity.
public class LiveMapActivity extends AppCompatActivity implements OnMapReadyCallback, OnPositionUpdatedCallback {

    private GoogleMap mMap;
    private Marker mapMarker;

    private TextView HRTextView ;
    private TextView speedTextView;
    private TextView distanceTextView;
    private TextView altitudeTextView;
    private TextView dataPointsTextView;
    private Chronometer chronometer;

    // Variables of the class
    ArrayList<LatLng> locations;
    private GPSManager gpsManager;
    private HRManager hrManager;
    private Polyline polyline;
    private Circle circle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.GoogleMap);
        mapFragment.getMapAsync(this);

        // Get the objects to be used later
        Intent intent = getIntent();
        locations = (ArrayList<LatLng>) intent.getExtras().get("Locations");
        Double distanceOffset = intent.getDoubleExtra("distanceOffset", 0.0) ;
        HRTextView = findViewById(R.id.HRTextView);
        speedTextView = findViewById(R.id.speedTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        altitudeTextView = findViewById(R.id.altitudeTextView);
        dataPointsTextView = findViewById(R.id.dataPointsTextView);
        chronometer = findViewById(R.id.chrono);

        // Set the chronometer to correct time
        Long timerValue = getIntent().getLongExtra("timerValue",0);
        chronometer.setBase(SystemClock.elapsedRealtime() + timerValue);
        chronometer.start();

        // Create the GPS manager
        gpsManager = new GPSManager(this,speedTextView,distanceTextView,altitudeTextView,dataPointsTextView) ;
        gpsManager.setPositionCallback(this);
        gpsManager.setDistanceOffset(distanceOffset);

        // Create the HR manager
        hrManager = new HRManager(this,HRTextView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        gpsManager.startRecording();
        hrManager.startRecording();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gpsManager.stopRecording();
        hrManager.stopRecording();
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
        polyline = mMap.addPolyline(new PolylineOptions().addAll(locations).width(12).color(Color.BLUE));
        //circle = mMap.addCircle(new CircleOptions().center(locations.get(locations.size()-1)).radius(1).fillColor(Color.BLUE).strokeWidth(10f).strokeColor(Color.WHITE));
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
        // todo: center point on the map isn't really good
        // circle.setCenter(point);
    }
}
