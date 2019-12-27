package com.example.intrek.Interfaces;

import com.google.android.gms.maps.model.LatLng;

public interface OnPositionUpdatedCallback {
    // Call this method when a new point was computed in order to update the path on the map.
    void newPointAvailable(LatLng point) ;
}
