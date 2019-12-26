package com.example.intrek.DataModel;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;
import java.util.ArrayList;

public class Recording {

    // MARK: - Fields

    ///// Generic information about the hike
    
    private Time startingTime ;
    private Time endingTime ;
    private int grade; // Out of 5
    private String name;

    //// Collected data on the hike to be analysed

    // Arrays for the location and the distance
    private ArrayList<Long> locationsTimes = new ArrayList<>();
    private ArrayList<LatLng> locations = new ArrayList<>();
    private ArrayList<Double> distances = new ArrayList<>();

    // Arrays for the speed
    private ArrayList<Double> speeds = new ArrayList<>();
    private ArrayList<Long> speedsTimes = new ArrayList<>();

    // Arrays for the HR
    private ArrayList<Integer> hrDataArrayList = new ArrayList<>();
    private ArrayList<Long> hrTimes = new ArrayList<>();

    // MARK: - Public methods

    public Recording(ArrayList<Long> locationsTimes, ArrayList<LatLng> locations, ArrayList<Double> distances, ArrayList<Double> speeds, ArrayList<Long> speedsTimes, ArrayList<Integer> hrDataArrayList, ArrayList<Long> hrTimes) {
        this.locationsTimes = locationsTimes;
        this.locations = locations;
        this.distances = distances;
        this.speeds = speeds;
        this.speedsTimes = speedsTimes;
        this.hrDataArrayList = hrDataArrayList;
        this.hrTimes = hrTimes;
    }

    public Recording() {
        // todo
    }

    // This constructor is to build the statistics
    public void saveToFirebase() {
        // todo
    }




    // MARK: - Private methods


}
