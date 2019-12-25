package com.example.intrek.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.intrek.DataModel.XYPlotSeriesList;
import com.example.intrek.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.example.intrek.R.id.HRPlot;

public class LiveRecordingActivity extends AppCompatActivity {

    public static final String ACTION_RECEIVE_HEART_RATE = "ACTION_RECEIVE_HEART_RATE";
    public static final String HEART_RATE = "HeartRate";
    private static final int MIN_HR = 40;
    private static final int MAX_HR = 200;
    private static final int NUMBER_OF_POINTS = 50;
    private static final String HR_PLOT_WATCH = "HR from smart watch";

    private Chronometer timerTextView;
    private Button pauseButton ;
    private XYPlot heartRatePlot;

    private long timerValueWhenPaused = 0 ;
    private boolean isPaused = false ;
    private HeartRateBroadcastReceiver heartRateBroadcastReceiver;
    private int heartRateWatch = 0;
    private ArrayList<Integer> hrDataArrayList = new ArrayList<>();
    private XYPlotSeriesList xyPlotSeriesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_recording);

        // 0. Get the elements of the UI
        timerTextView = findViewById(R.id.timerTextView);
        timerTextView.start();
        pauseButton = findViewById(R.id.PauseButton);
        heartRatePlot = findViewById(HRPlot);
        configurePlot();
        xyPlotSeriesList = new XYPlotSeriesList();
        LineAndPointFormatter formatterWatch = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT, Color.TRANSPARENT, null);
        formatterWatch.getLinePaint().setStrokeWidth(8);
        xyPlotSeriesList.initializeSeriesAndAddToList(HR_PLOT_WATCH, MIN_HR, NUMBER_OF_POINTS, formatterWatch);
        XYSeries HRseries = new SimpleXYSeries(xyPlotSeriesList.getSeriesFromList(HR_PLOT_WATCH), SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, HR_PLOT_WATCH);
        heartRatePlot.clear();
        heartRatePlot.addSeries(HRseries, formatterWatch);
        heartRatePlot.redraw();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Get the HR data back from the watch
        heartRateBroadcastReceiver = new HeartRateBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(heartRateBroadcastReceiver, new IntentFilter(ACTION_RECEIVE_HEART_RATE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(heartRateBroadcastReceiver);
    }


    public void pauseButtonTapped(View view) {
        if (isPaused) {
            timerTextView.setBase(SystemClock.elapsedRealtime() + timerValueWhenPaused);
            timerTextView.start();
            pauseButton.setText("Pause");
            isPaused = false ;
        } else {
            timerValueWhenPaused = timerTextView.getBase() - SystemClock.elapsedRealtime();
            timerTextView.stop();
            pauseButton.setText("Resume");
            isPaused = true ;
        }
    }

    // MARK: - Private methods

    private void configurePlot() {
        // Get background color from Theme
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
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


    // MARK: - Inner class to listen the watch data

    private class HeartRateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Show HR in a TextView
            heartRateWatch = intent.getIntExtra(HEART_RATE, -1);
            TextView hrTextView = findViewById(R.id.HRTextView);
            hrTextView.setText(String.valueOf(heartRateWatch));
            // Plot the graph
            xyPlotSeriesList.updateSeries(HR_PLOT_WATCH, heartRateWatch);
            XYSeries hrWatchSeries = new SimpleXYSeries(xyPlotSeriesList.getSeriesFromList(HR_PLOT_WATCH), SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, HR_PLOT_WATCH);
            LineAndPointFormatter formatterPolar = xyPlotSeriesList.getFormatterFromList(HR_PLOT_WATCH);
            heartRatePlot.clear();
            heartRatePlot.addSeries(hrWatchSeries, formatterPolar);
            heartRatePlot.redraw();
            // And add HR value to HR ArrayList
            hrDataArrayList.add(heartRateWatch);
        }

    }
}

