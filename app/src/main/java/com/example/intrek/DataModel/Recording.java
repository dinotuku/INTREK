package com.example.intrek.DataModel;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;

public class Recording implements Serializable {

    // MARK: - Fields

    ///// Generic information about the hike
    // todo
    private Time startingTime ;
    private Time endingTime ;
    private int grade; // Out of 5
    private String name;

    //// Collected data on the hike to be analysed

    // Arrays for the location and the distance
    private ArrayList<Long> distancesTimes = new ArrayList<>();
    private ArrayList<Double> distances = new ArrayList<>();

    // Arrays for the speed
    private ArrayList<Long> speedsTimes = new ArrayList<>();
    private ArrayList<Double> speeds = new ArrayList<>();

    // Arrays for the HR
    private ArrayList<Long> hrTimes = new ArrayList<>();
    private ArrayList<Integer> hrDataArrayList = new ArrayList<>();

    // MARK: - Public methods

    public Recording() {
        // todo
    }

    public Recording(ArrayList<Long> distancesTimes, ArrayList<Double> distances, ArrayList<Long> speedsTimes, ArrayList<Double> speeds, ArrayList<Long> hrTimes, ArrayList<Integer> hrDataArrayList) {
        this.distancesTimes = distancesTimes;
        this.distances = distances;
        this.speedsTimes = speedsTimes;
        this.speeds = speeds;
        this.hrTimes = hrTimes;
        this.hrDataArrayList = hrDataArrayList;
    }

    // This constructor is to build the statistics
    public void saveToFirebase() {
        // todo
    }

    public ArrayList<RecordingStatistic> getStatistics() {

        // Construct the stats and return them
        RecordingStatistic s1 = new RecordingStatistic("Distance",distancesTimes,distances) ;
        RecordingStatistic s2 = new RecordingStatistic("Velocity",speedsTimes,speeds) ;
        ArrayList<RecordingStatistic> statistics = new ArrayList<>();
        statistics.add(s1);
        statistics.add(s2);
        return statistics;

    }
    // MARK: - Private methods


}
