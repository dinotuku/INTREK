package com.example.intrek.ui.main;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.intrek.DataModel.Recording;
import com.example.intrek.DataModel.RecordingData;
import com.example.intrek.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// Show activity history. It will fetch data on Firebase and show all the recordings in a list view.
public class HistoryFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    // Fields

    private String uid;
    private RecordingAdapter adapter;
    private DatabaseReference databaseRef;
    private MyFirebaseRecordingListener mFirebaseRecordingListener;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_history, container, false);

        // Get user ID from intent
        uid = getActivity().getIntent().getExtras().getString(ProfileFragment.UID);

        // Set adapter for list view
        ListView listView = fragmentView.findViewById(R.id.history_list);
        adapter = new RecordingAdapter(getActivity(), R.layout.row_history);
        listView.setAdapter(adapter);

        // TODO: open the corresponding recording analysis
        // Handle onClick method for each list item
        // Open RecordingAnalysis when clicked
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getContext(), "Exercise: "
                                + ((TextView) view.findViewById(R.id.hike_name)).getText().toString()
                                + " on " + ((TextView) view.findViewById(R.id.hike_time)).getText().toString()
                        , Toast.LENGTH_SHORT).show();
            }
        });

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // register the listener
        databaseRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseRecordingListener = new MyFirebaseRecordingListener();
        databaseRef.child("profiles").child(uid).child("recordings").addValueEventListener(mFirebaseRecordingListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregister the listener to avoid memory leaks
        databaseRef.child("profiles").child(uid).child("recording").removeEventListener(mFirebaseRecordingListener);
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
            //String endingTime = getItem(position).getEndingTime();
            String duration = getItem(position).getDuration();
            String distance = getItem(position).getDistance();
            String pace = statistics.get(0).getAverage();
            String elev = String.valueOf(statistics.get(3).getMaxY() - statistics.get(3).getMinY());

            // Show statistics
            ((TextView) row.findViewById(R.id.hike_name)).setText(name);
            //((TextView) row.findViewById(R.id.hike_time)).setText(endingTime);
            ((TextView) row.findViewById(R.id.hike_duration)).setText(duration);
            ((TextView) row.findViewById(R.id.hike_distance)).setText(distance);
            ((TextView) row.findViewById(R.id.hike_pace)).setText(pace);
            ((TextView) row.findViewById(R.id.hike_elev_gain)).setText(elev);

            // TODO: update the small map image (have to save it to firebase first)

            return row;
        }
    }

    // Fetch data from Firebase
    private class MyFirebaseRecordingListener implements ValueEventListener {

        // Default methods

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // Otherwise it will add data every time we listens for events
            adapter.clear();

            // Loop over every recording
            for (final DataSnapshot rec : dataSnapshot.getChildren()) {
                // For simply getting arrays using getValue
                GenericTypeIndicator<ArrayList<Long>> l = new GenericTypeIndicator<ArrayList<Long>>() {
                };
                GenericTypeIndicator<ArrayList<Double>> d = new GenericTypeIndicator<ArrayList<Double>>() {
                };
                GenericTypeIndicator<ArrayList<Integer>> i = new GenericTypeIndicator<ArrayList<Integer>>() {
                };

                // Get array data
                final ArrayList<Long> distancesTimes = rec.child("distancesTimes").getValue(l);
                final ArrayList<Double> distances = rec.child("distances").getValue(d);
                final ArrayList<Long> speedsTimes = rec.child("speedsTimes").getValue(l);
                final ArrayList<Double> speeds = rec.child("speeds").getValue(d);
                final ArrayList<Double> altitudes = rec.child("altitudes").getValue(d);
                final ArrayList<Long> hrTimes = rec.child("hrTimes").getValue(l);
                final ArrayList<Integer> hrDataArrayList = rec.child("hrDataArrayList").getValue(i);

                // todo: get duration
                // Create a Recording object
                final Recording recording = new Recording("", distancesTimes, distances, speedsTimes, speeds, altitudes, hrTimes, hrDataArrayList);

                // Get generic information about the hike
                String startingTime = rec.child("startingTime").getValue().toString();
                String endingTime = rec.child("endingTime").getValue().toString();
                int grade = rec.child("grade").getValue(int.class);
                String name = rec.child("name").getValue().toString();

                // Save generic information to the Recording object
                recording.setGenericInformation(startingTime, endingTime, grade, name);

                adapter.add(recording);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.v(TAG, databaseError.toString());
        }
    }
}
