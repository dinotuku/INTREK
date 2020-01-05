package com.example.intrek.DataModel;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

// This class is implemented to provide an array and its time value.
// It is used to pass them as reference values for the rows in list view of statistics
// One element of this class can be plotted on a plot of the analysis activity
public class RecordingData {

    private String name;
    private String unit;
    private ArrayList<Double> times = new ArrayList<>();
    private ArrayList<Double> values = new ArrayList<>();
    private ArrayList<Float> valuesfloat = new ArrayList<>();

    public RecordingData(String name, ArrayList<Double> times, ArrayList<Double> values, String unit) {
        this.name = name ;
        this.times = times;
        this.values = values;
        this.unit = unit ;
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

    public String getAverage() {
        // 1. Compute the average
        double tmp = 0.0 ;
        for (double v: values) {
            tmp += v ;
        }
        tmp = tmp / values.size() ;

        // 2. construct the right string
        NumberFormat nf = new DecimalFormat("##.##");
        return nf.format(tmp) + " " + unit ;
    }

    public int getNumberOfDataPoints() {
        return values.size();
    }




}
