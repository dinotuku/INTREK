package com.example.intrek.Managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.intrek.DataModel.XYPlotSeriesList;
import com.example.intrek.R;
import com.example.intrek.ui.main.LiveRecordingActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;

// This class takes care of receiving and displaying the data from the watch.
public class HRManager {

    private static final int MIN_HR = 40;
    private static final int MAX_HR = 200;
    private static final int NUMBER_OF_POINTS = 50;
    private static final String HR_PLOT_WATCH = "HR from smart watch";

    private AppCompatActivity activity ;
    TextView hrTextView ;

    private HeartRateBroadcastReceiver heartRateBroadcastReceiver;
    private XYPlot heartRatePlot;
    private XYPlotSeriesList xyPlotSeriesList;
    private boolean hasPlot ;
    private ArrayList<Integer> hrDataArrayList ;
    private ArrayList<Long> hrTimes ;
    private long initialTime = System.currentTimeMillis() ;

    public HRManager(AppCompatActivity activity, TextView hrTextView) {
        this.hasPlot = false ;
        this.activity = activity;
        this.hrTextView = hrTextView;
    }

    public void setToPlot(XYPlot heartRatePlot, ArrayList<Integer> hrDataArrayList, ArrayList<Long> hrTimes  ) {
        this.hasPlot = true ;
        this.hrDataArrayList = hrDataArrayList ;
        this.hrTimes = hrTimes ;
        this.heartRatePlot = heartRatePlot ;
        configurePlot();
        setHRPlot();
    }

    public void startRecording() {
        //Get the HR data back from the watch
        heartRateBroadcastReceiver = new HeartRateBroadcastReceiver();
        LocalBroadcastManager.getInstance(activity).registerReceiver(heartRateBroadcastReceiver, new IntentFilter(LiveRecordingActivity.ACTION_RECEIVE_HEART_RATE));
    }

    public void stopRecording() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(heartRateBroadcastReceiver);
    }


    // region For the HR Plot
    private void configurePlot() {
        // Get background color from Theme
        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
        int backgroundColor = typedValue.data;
        // Set background colors
        heartRatePlot.setPlotMargins(0, 0, 0, 0);
        heartRatePlot.getBorderPaint().setColor(backgroundColor);
        heartRatePlot.getBackgroundPaint().setColor(backgroundColor);
        heartRatePlot.getGraph().getBackgroundPaint().setColor(backgroundColor);
        heartRatePlot.getGraph().getGridBackgroundPaint().setColor(backgroundColor);
        // Set the grid color
        heartRatePlot.getGraph().getRangeGridLinePaint().setColor(Color.DKGRAY);
        heartRatePlot.getGraph().getDomainGridLinePaint().setColor(Color.DKGRAY);
        // Set the origin axes colors
        heartRatePlot.getGraph().getRangeOriginLinePaint().setColor(Color.DKGRAY);
        heartRatePlot.getGraph().getDomainOriginLinePaint().setColor(Color.DKGRAY);
        // Set the XY axis boundaries and step values
        heartRatePlot.setRangeBoundaries(MIN_HR, MAX_HR, BoundaryMode.FIXED); heartRatePlot.setDomainBoundaries(0, NUMBER_OF_POINTS - 1, BoundaryMode.FIXED);
        heartRatePlot.setRangeStepValue(9); // 9 values 40 60 ... 200
        heartRatePlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("#"));
        // This line is to force the Axis to be integer
        heartRatePlot.setRangeLabel("Heart rate (bpm)");
    }

    private void setHRPlot() {
        xyPlotSeriesList = new XYPlotSeriesList();
        LineAndPointFormatter formatterWatch = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT, Color.TRANSPARENT, null);
        formatterWatch.getLinePaint().setStrokeWidth(8);
        xyPlotSeriesList.initializeSeriesAndAddToList(HR_PLOT_WATCH, MIN_HR, NUMBER_OF_POINTS, formatterWatch);
        XYSeries HRseries = new SimpleXYSeries(xyPlotSeriesList.getSeriesFromList(HR_PLOT_WATCH), SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, HR_PLOT_WATCH);
        heartRatePlot.clear();
        heartRatePlot.addSeries(HRseries, formatterWatch);
        heartRatePlot.redraw();
    }
    //endregion

    private class HeartRateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Show HR in a TextView
            int heartRateWatch = intent.getIntExtra(LiveRecordingActivity.HEART_RATE, -1);
            String s = String.valueOf(heartRateWatch) + " [BPM]" ;
            hrTextView.setText(s);

            if (hasPlot) {
                // Plot the graph
                xyPlotSeriesList.updateSeries(HR_PLOT_WATCH, heartRateWatch);
                XYSeries hrWatchSeries = new SimpleXYSeries(xyPlotSeriesList.getSeriesFromList(HR_PLOT_WATCH), SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, HR_PLOT_WATCH);
                LineAndPointFormatter formatterPolar = xyPlotSeriesList.getFormatterFromList(HR_PLOT_WATCH);
                heartRatePlot.clear();
                heartRatePlot.addSeries(hrWatchSeries, formatterPolar);
                heartRatePlot.redraw();
                // And add HR value to HR ArrayList
                hrDataArrayList.add(heartRateWatch);
                hrTimes.add(System.currentTimeMillis()-initialTime);
            }
        }

    }


}
