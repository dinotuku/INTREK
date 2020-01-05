package com.example.intrek.ui.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.intrek.DataModel.Recording;
import com.example.intrek.DataModel.RecordingData;
import com.example.intrek.MainActivity;
import com.example.intrek.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

// Show activity history. It will fetch data on Firebase and show all the recordings in a list view.
public class HistoryFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    // Fields

    private String uid;
    private RecordingAdapter adapter;
    private DatabaseReference databaseRef;

    private ArrayList<Recording> recordings;

    // Constructors

    public HistoryFragment() {
        // Required empty public constructor
    }

    // Method which will be called by SectionsPagerAdapter

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    // Default methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_history, container, false);

        // Get user ID from intent
        uid = getActivity().getIntent().getExtras().getString(ProfileFragment.UID);

        // Set adapter for list view
        ListView listView = fragmentView.findViewById(R.id.history_list);
        adapter = new RecordingAdapter(getActivity(), R.layout.row_history);
        listView.setAdapter(adapter);

        // Handle onClick method for each list item
        // Open RecordingAnalysis when clicked
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Recording r = adapter.getItem(position);
                Intent i = new Intent(getActivity(), RecordingAnalysisActivity.class);
                i.putExtra("Recording", r);
                i.putExtra("isFromLiveRecording",false);
                startActivity(i);
            }
        });

        recordings = ProfileFragment.recordings;

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.clear();
        for (Recording rec : recordings) {
            adapter.add(rec);
        }
    }

    // Classes

    // Handle the contents of each row item
    private class RecordingAdapter extends ArrayAdapter<Recording> {

        // Fields

        private int row_layout;

        // Constructors

        public RecordingAdapter(FragmentActivity activity, int row_layout) {
            super(activity, row_layout);
            this.row_layout = row_layout;
        }

        // Default methods

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // reference to the row View
            View row = convertView;

            if (row == null) {
                // inflate it from layout
                row = LayoutInflater.from(getContext()).inflate(row_layout, parent, false);
            }

            // Get statistics
            ArrayList<RecordingData> statistics = getItem(position).getStatistics();
            String name = getItem(position).getName();
            String time = getItem(position).getStartingTime();
            String duration = getItem(position).getDuration();
            String distance = getItem(position).getDistance();
            String pace = statistics.get(0).getAverage();
            String elev = String.format(Locale.US, "%.2f [m]", getItem(position).getElevationGain());

            // Show statistics and map
            ((TextView) row.findViewById(R.id.hike_name)).setText(name);
            ((TextView) row.findViewById(R.id.hike_time)).setText(time);
            ((TextView) row.findViewById(R.id.hike_duration)).setText(duration);
            ((TextView) row.findViewById(R.id.hike_distance)).setText(distance);
            ((TextView) row.findViewById(R.id.hike_pace)).setText(pace);
            ((TextView) row.findViewById(R.id.hike_elev_gain)).setText(elev);
            new DownloadImageTask((ImageView) row.findViewById(R.id.mapImage)).execute(getItem(position).getMapUrl());

            return row;
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
