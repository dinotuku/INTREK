package com.example.intrek.DataModel;
import com.androidplot.xy.LineAndPointFormatter;

import java.util.ArrayList;
import java.util.List;

// This class is used to save all the data which needs to be plot by our graph. The class can be used to plot different series.
public class XYPlotSeriesList {

    // List of series of coordinates
    private ArrayList<Integer[]> xList = new ArrayList<>();
    private ArrayList<Integer[]> yList = new ArrayList<>();

    // Same as before but using doubles
    private ArrayList<Double[]> xListDouble = new ArrayList<>();
    private ArrayList<Double[]> yListDouble = new ArrayList<>();


    // List of the arrays (x,y) of the series
    private ArrayList<List<Number>> xyList = new ArrayList<>();

    // Contains a unique tag for each serie
    private ArrayList<String> xyTagList = new ArrayList<>();

    // Contains the formater to use
    private ArrayList<LineAndPointFormatter> xyFormatterList = new ArrayList<>();

    // Will add an initial (x,y) series of length NUMBER_OF_POINTS to the plot. Every y points is set to the constant
    public void initializeSeriesAndAddToList(String xyTag, int CONSTANT, int NUMBER_OF_POINTS, LineAndPointFormatter xyFormatter) {
        Integer[] x = new Integer[NUMBER_OF_POINTS];
        Integer[] y = new Integer[NUMBER_OF_POINTS];
        List<Number> xy = new ArrayList<>();
        for (int i = 0; i < y.length; i += 1) {
            x[i] = i;
            y[i] = CONSTANT;
            xy.add(x[i]);
            xy.add(y[i]);
        }
        xList.add(x);
        yList.add(y);
        xyList.add(xy);
        xyTagList.add(xyTag);
        xyFormatterList.add(xyFormatter);
    }

    // This method takes as input a list and will create a serie using this list as y values.
    public void initializeWithDoubleSerie(String xyTag,ArrayList<Double> xValues, ArrayList<Double> yValues, LineAndPointFormatter xyFormatter) {
        xyTagList.add(xyTag);
        xyFormatterList.add(xyFormatter);
        int NUMBER_OF_POINTS = yValues.size();
        Double[] x = new Double[NUMBER_OF_POINTS];
        Double[] y = new Double[NUMBER_OF_POINTS];
        List<Number> xy = new ArrayList<>();
        for (int i = 0; i < y.length; i += 1) {
            x[i] = xValues.get(i);
            y[i] = yValues.get(i);
            xy.add(x[i]);
            xy.add(y[i]);
        }
        xListDouble.add(x);
        yListDouble.add(y);
        xyList.add(xy);
    }

    // Appends a new data point to an existing series with the tag xyTag in a XYplotSeriesList instance.
    // Since the series has a fixed size, all its previous values need to be shifted first and the new value added to the end of it.
    public void updateSeries(String xyTag, int data) {
        // Get the current arrays of coordinates
        List<Number> xy = xyList.get(xyTagList.indexOf(xyTag));
        Integer[] x = xList.get(xyTagList.indexOf(xyTag));
        Integer[] y = yList.get(xyTagList.indexOf(xyTag));

        // Recreate the the xy array.
        xy.clear();
        for (int i = 0; i < y.length - 1; i += 1) {
            y[i] = y[i + 1];
            xy.add(x[i]);
            xy.add(y[i]);
        }

        // Add the last element
        y[y.length - 1] = data;
        xy.add(x[y.length-1]);
        xy.add(y[y.length-1]);

        // Save the new solution for this serie
        xyList.set(xyTagList.indexOf(xyTag), xy);
        xList.set(xyTagList.indexOf(xyTag), x);
        yList.set(xyTagList.indexOf(xyTag), y);
    }

    public List<Number> getSeriesFromList(String xyTag) {
        return xyList.get(xyTagList.indexOf(xyTag));
    }

    public LineAndPointFormatter getFormatterFromList(String xyTag) {
        return xyFormatterList.get(xyTagList.indexOf(xyTag));
    }

}