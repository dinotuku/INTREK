package com.example.intrek.DataModel;

import java.util.ArrayList;

// This class is implemented to provide an array and its time value.
// It is used to pass them as reference values for the rows in list view of statistics
public class RecordingStatistic {
    private String name;
    private ArrayList<Long> times = new ArrayList<>();
    private ArrayList<Double> values = new ArrayList<>();

    public RecordingStatistic(String name, ArrayList<Long> times, ArrayList<Double> values) {
        this.name = name ;
        this.times = times;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Long> getTimes() {
        return times;
    }

    public ArrayList<Double> getValues() {
        return values;
    }
}
