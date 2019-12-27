package com.example.intrek.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.intrek.DataModel.Recording;
import com.example.intrek.DataModel.RecordingData;
import com.example.intrek.DataModel.XYPlotSeriesList;
import com.example.intrek.R;

import java.text.DecimalFormat;

public class RecordingAnalysisActivity extends AppCompatActivity {

    private Recording recording ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_analysis);

        // 1. Obtain the recording
        recording = (Recording) getIntent().getSerializableExtra("Recording");

        // 2. Set the list view
        setListView();

        // 3. Set the top of the screen
        TextView durationTextView = findViewById(R.id.durationTextView);
        TextView distanceTextView = findViewById(R.id.distanceTextView);
        durationTextView.setText(recording.getDuration());
        distanceTextView.setText(recording.getDistance());


    }

    // This method is here to set the statistics list view
    private void setListView(){
        ListView listView = findViewById(R.id.statisticsListView);
        // Set the adapter to the created adapter
        StatisticListViewAdapter adapter = new StatisticListViewAdapter(RecordingAnalysisActivity.this,R.layout.row_time_statistics);
        listView.setAdapter(adapter);
        // Add the model to the cells
        for (RecordingData rd: recording.getStatistics()) {
            adapter.add(rd);
        }
    }

    private class StatisticListViewAdapter extends ArrayAdapter<RecordingData> {

        public StatisticListViewAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Reference to the row View
            View row = convertView;

            if (row == null) {
                // Inflate it from layout
                row = LayoutInflater.from(getContext()).inflate(R.layout.row_time_statistics, parent, false);
            }


            // And set the row with the data
            RecordingData data = getItem(position);

            // Set the text
            TextView tx = row.findViewById(R.id.statisticNameTextView);
            tx.setText(data.getName());

            // Set the plot view
            XYPlot plot = row.findViewById(R.id.statisticGrap);
            configurePlot(plot, data.getMaxY());
            setPlot(plot,data,8);


            return row ;
        }

        // This function set the plot for the given inputs.
        private void configurePlot(XYPlot plot, double maxY) {
            //// Set the colors
            // Get background color from Theme
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
            int backgroundColor = typedValue.data;
            // Set background colors
            plot.setPlotMargins(0, 0, 0, 0);
            plot.getBorderPaint().setColor(backgroundColor);
            plot.getBackgroundPaint().setColor(backgroundColor);
            plot.getGraph().getBackgroundPaint().setColor(backgroundColor);
            plot.getGraph().getGridBackgroundPaint().setColor(backgroundColor);
            // Set the grid color
            plot.getGraph().getRangeGridLinePaint().setColor(Color.DKGRAY);
            plot.getGraph().getDomainGridLinePaint().setColor(Color.DKGRAY);
            // Set the origin axes colors
            plot.getGraph().getRangeOriginLinePaint().setColor(Color.DKGRAY);
            plot.getGraph().getDomainOriginLinePaint().setColor(Color.DKGRAY);

            //// Last settings of the plot
            // Set the XY axis boundaries and step values
            plot.getGraph().setLineLabelEdges(XYGraphWidget.Edge.BOTTOM, XYGraphWidget.Edge.LEFT);
            plot.getLegend().setVisible(false);
            plot.setRangeBoundaries(0,1.3*maxY, BoundaryMode.FIXED);
        }

        private void setPlot(XYPlot plot, RecordingData data, int strokeWidth) {
            LineAndPointFormatter formatter = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT, Color.TRANSPARENT, null);
            formatter.getLinePaint().setStrokeWidth(strokeWidth);
            SimpleXYSeries series = new SimpleXYSeries(data.getTimes(),data.getValues(),data.getName());
            plot.clear();
            plot.addSeries(series,formatter);
            plot.redraw();
        }
    }
}
