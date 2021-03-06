package com.example.intrek.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.Toast;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.intrek.DataModel.Recording;
import com.example.intrek.DataModel.RecordingData;
import com.example.intrek.DataModel.XYPlotSeriesList;
import com.example.intrek.MainActivity;
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
    private int selectedType ;
    Bitmap imageBitmap ;
    private ListView listView;
    private boolean isFromLiveRecording;

    NumberFormat nf = new DecimalFormat("##.##");

    private EditText hikeNameEditText ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_analysis);
        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.bringToFront();
        Button shareButton = findViewById(R.id.mShareButton);
        shareButton.bringToFront();

        // 1. Obtain the recording
        recording = (Recording) getIntent().getSerializableExtra("Recording");
        uid = getIntent().getStringExtra(ProfileFragment.UID);
        selectedType = getIntent().getIntExtra(NewRecordingFragment.SELECTED_INDEX,-1) ;

        // 2. Set the list view
        setListView();

        // 3. Set the view properly
        isFromLiveRecording = getIntent().getBooleanExtra("isFromLiveRecording", true) ;
        if (!isFromLiveRecording) {
            // Disable the save button
            Button saveButton = findViewById(R.id.SaveButton);
            saveButton.setVisibility(View.INVISIBLE);
            saveButton.setEnabled(false);
            cancelButton.setVisibility(View.INVISIBLE);
            cancelButton.setEnabled(false);
            shareButton.setVisibility(View.INVISIBLE);
            shareButton.setEnabled(false);


        }

        // 4. Set the sharing action
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareButtonTap();
            }
        });

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
            Toast.makeText(this, "Name required.", Toast.LENGTH_LONG).show();
        } else {
            // set name
            recording.setName(text);
            recording.setActivityType(NewRecordingFragment.getActivityType(selectedType));
            // save to Firebase
            recording.saveToFirebase(uid);
            // Come back to first page
            Intent i = new Intent(RecordingAnalysisActivity.this, MainActivity.class);        // Specify any activity here e.g. home or splash or login etc
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("EXIT", true);
            i.putExtra(ProfileFragment.UID,uid);
            startActivity(i);
            finish();
        }
    }

    public void cancelButtonTapped(View view) {
        Intent i = new Intent(RecordingAnalysisActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("EXIT", true);
        i.putExtra(ProfileFragment.UID,uid);
        startActivity(i);
        finish();
    }

    public void shareButtonTap() {

        // Activate sensor reading
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 0);
        }

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"recipient@example.com"});

        //// Construct the body of the message
        // 1. get the name of the hike
        String hikeName = hikeNameEditText.getText().toString();
        if (hikeName.isEmpty()) {
            hikeName = "no_name" ;
        }
        String title = "My hike: " + hikeName ;

        // 2. Get some statistics about the hike
        String body = "" ;
        body += "On the " + recording.getStartingTime() + ", I did a great hike " + hikeName + ". \n\n" ;
        body += "During this hike, here are my performances: \n" ;
        body += "- Activity type: " + NewRecordingFragment.getActivityType(selectedType) + "\n";
        body += "- Distance: " + recording.getDistance() +  " \n";
        body += "- Average pace: " + nf.format(recording.getAvePace()) + " min/km\n";
        body += "- Elevation gain: " + nf.format(recording.getElevationGain()) + " m\n" ;

        body += "\n\nYou can find attached on this mail the path I did ! \n" ;
        body += "The recording was done with INTREK, a  cool app designed for a super cool EPFL's course: EE-490G\n\n";

        body += "Keep exercising, \n\n Best";


        String path = MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap,"title", null);
        Uri screenshotUri = Uri.parse(path);
        i.putExtra(Intent.EXTRA_SUBJECT, title);
        i.putExtra(Intent.EXTRA_TEXT   , body);
        i.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(RecordingAnalysisActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
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
                hikeNameEditText.setCursorVisible(false);
                hikeNameEditText.setLongClickable(false);
                hikeNameEditText.setClickable(false);
                hikeNameEditText.setFocusable(false);
                hikeNameEditText.setSelected(false);
                hikeNameEditText.setKeyListener(null);
                hikeNameEditText.setBackgroundResource(android.R.color.transparent);
                hikeNameEditText.setTextColor(Color.BLACK);
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
            imageBitmap = result ;
            bmImage.setImageBitmap(result);
        }
    }
}
