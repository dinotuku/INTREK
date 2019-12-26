package com.example.intrek.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.intrek.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class LiveMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker mapMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.GoogleMap);
        mapFragment.getMapAsync(this);



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Get the locations obtained
        Intent intent = getIntent();
        ArrayList<LatLng> locations = (ArrayList<LatLng>) intent.getExtras().get("Locations");
        LatLng currentLocation = locations.get(0);

        // Add a marker in the default location and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        if(mapMarker!=null){ mapMarker.remove(); }
        mapMarker = mMap.addMarker(new MarkerOptions().position(currentLocation) .title("Current Location"));

        placeMarkers(locations);

    }

    private void placeMarkers(ArrayList<LatLng> locations) {
        for (LatLng l: locations) {
            Marker newMarker = mMap.addMarker(new MarkerOptions().position(l).title("Point"));
        }
    }
}
