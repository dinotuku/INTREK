package com.example.intrek.DataModel;

import java.util.ArrayList;

// This class is implemented to provide an array and its time value.
// It is used to pass them as reference values for the rows in list view of statistics
public class RecordingData {
    private String name;
    private ArrayList<Double> times = new ArrayList<>();
    private ArrayList<Double> values = new ArrayList<>();

    public RecordingData(String name, ArrayList<Double> times, ArrayList<Double> values) {
        this.name = name ;
        this.times = times;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Double> getTimes() {
        return times;
    }

    public ArrayList<Double> getValues() {
        return values;
    }

    // Returns the minimum value of the y chanel
    public double getMinY() {
        double min = values.get(0);
        for (double v: values) {
            if (v<min) {
                min = v ;
            }
        }
        return min;
    }

    // Returns the maximum value of the y chanel
    public double getMaxY() {
        double max = values.get(0);
        for (double v: values) {
            if (v>max) {
                max = v ;
            }
        }
        return max;
    }

    public int getNumberOfDataPoints() {
        return values.size();
    }




}
