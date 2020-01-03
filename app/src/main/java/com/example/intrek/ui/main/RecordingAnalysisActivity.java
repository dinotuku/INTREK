package com.example.intrek.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

// Show a summary of a completed hike.
// The summary will contain statistics and plots of sensor values.
public class RecordingAnalysisActivity extends AppCompatActivity {

    private Recording recording ;
    private String uid ;
    private ListView listView;
    private boolean isFromLiveRecording;

    NumberFormat nf = new DecimalFormat("##.##");

    private EditText hikeNameEditText ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_analysis);

        // 1. Obtain the recording
        recording = (Recording) getIntent().getSerializableExtra("Recording");
        uid = getIntent().getStringExtra(ProfileFragment.UID);

        // 2. Set the list view
        setListView();

        // 3. Set the view properly
        isFromLiveRecording = getIntent().getBooleanExtra("isFromLiveRecording", true) ;
        if (!isFromLiveRecording) {
            // Disable the save button
            Button saveButton = findViewById(R.id.SaveButton);
            saveButton.setVisibility(View.INVISIBLE);
            saveButton.setEnabled(false);
        }


    }



    @Override
    protected void onStart() {
        super.onStart();

    }

    public void saveButtonTapped(View view) {
        // 1. obtain the name
        String text = hikeNameEditText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            hikeNameEditText.setError("Name required.");
        } else {
            // set name
            recording.setName(text);
            // save to Firebase
            recording.saveToFirebase(uid);
        }
    }

    // This method is here to set the list view.
    // The list view will present all the information about the hike
    // It has 2 different layout types
    private void setListView(){
        listView = findViewById(R.id.statisticsListView);
        int[] resources = new int[] {R.layout.row_first_statistic, R.layout.row_time_statistics} ;
        ArrayList<Object> list = new ArrayList<>() ;
        list.add("nothing") ;
        list.addAll(recording.getStatistics());
        AnalysisListAdapter myCustomAdapter = new AnalysisListAdapter(RecordingAnalysisActivity.this,resources,list);
        listView.setAdapter(myCustomAdapter);
    }


    // This inner class is the custom ArrayAdapter used to display a ListView with 2 differnet layout types.
    private class AnalysisListAdapter<T> extends ArrayAdapter<T> {

        private LayoutInflater mInflater;


        public AnalysisListAdapter(Context context, int[] resources, List<T> objects) {
            super(context, resources[1], objects);
            mInflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getViewTypeCount() {
            return 2 ;
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? 0 : 1 ;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;

            if (position == 0) {
                if (row == null) {
                    // Inflate it from layout
                    row = mInflater.inflate(R.layout.row_first_statistic, parent,false);
                }

                // And set the row with the proper information
                configureFirstRow(row);

            } else {
                // It is not the first row...
                if (row == null) {
                    // Inflate it from layout
                    row = mInflater.inflate(R.layout.row_time_statistics,parent,false);
                }

                // get the data by force casting it to what we know it is
                RecordingData data = (RecordingData) getItem(position);

                // Set the text
                TextView tx = row.findViewById(R.id.statisticNameTextView);
                tx.setText(data.getName());

                // Set the plot view
                XYPlot plot = row.findViewById(R.id.statisticGrap);
                configurePlot(plot, data.getMaxY());
                setPlot(plot,data,8);

                // Set the average value of
                TextView aveTv = row.findViewById(R.id.averageValueTextView) ;
                aveTv.setText(data.getAverage());
            }
            return row ;
        }

        private void configureFirstRow(View row) {
            // The row has 4 textviews to be set and the map
            // The data for this row is contained in the class field 'Recording'

            // Obtain the text views
            TextView distanceTV = row.findViewById(R.id.distanceTextView);
            TextView avePaceTV = row.findViewById(R.id.avePaceTextView);
            TextView timeTV = row.findViewById(R.id.timeTextView);
            TextView elevationTV = row.findViewById(R.id.elevationTextView);

            // Set their text
            distanceTV.setText(recording.getDistance());
            avePaceTV.setText(nf.format(recording.getAvePace()) + " [min/km]");
            timeTV.setText(recording.getDuration());
            elevationTV.setText(nf.format(recording.getElevationGain()) + " [m]");

            // Set eventually the name
            hikeNameEditText = row.findViewById(R.id.HikeNameEditText) ;
            if (!isFromLiveRecording) {
                // Set the text of the editText
                hikeNameEditText.setText(recording.getName(),TextView.BufferType.EDITABLE);
                hikeNameEditText.setEnabled(false);
            }

            // plot the map's data
            String url = "https://maps.googleapis.com/maps/api/staticmap?center=40.714%2c%20-73.998&zoom=12&size=400x400&key=AIzaSyCjDSiAyIqt1YApD1rCTgUTAFeO6Udcixs" ;
            new DownloadImageTask((ImageView) row.findViewById(R.id.mapImageView)).execute(recording.getMapUrl());
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

    // This inner class is used to download an image from Internet
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
