package com.example.intrek.ui.main;

import android.content.Intent;
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
import com.example.intrek.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    private View fragmentView;
    private String uid;
    private ListView listView;
    private RecordingAdapter adapter;
    private DatabaseReference databaseRef;
    private MyFirebaseRecordingListener mFirebaseRecordingListener;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_history, container, false);

        uid = getActivity().getIntent().getExtras().getString(ProfileFragment.UID);
        listView = fragmentView.findViewById(R.id.history_list);
        adapter = new RecordingAdapter(getActivity(), R.layout.row_history);
        listView.setAdapter(adapter);

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

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
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


    private class RecordingAdapter extends ArrayAdapter<Recording> {

        private int row_layout;

        public RecordingAdapter(FragmentActivity activity, int row_layout) {
            super(activity, row_layout);
            this.row_layout = row_layout;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // reference to the row View
            View row = convertView;

            if (row == null) {
                // inflate it from layout
                row = LayoutInflater.from(getContext()).inflate(row_layout, parent, false);
            }

            ArrayList<RecordingData> statistics = getItem(position).getStatistics();
            String name = getItem(position).getName();
            String time = getItem(position).getStartingTime();
            String duration = getItem(position).getDuration();
            String distance = getItem(position).getDistance();
            String pace = statistics.get(0).getAverage();
            String elev = String.valueOf(statistics.get(3).getMaxY() - statistics.get(3).getMinY());

            ((TextView) row.findViewById(R.id.hike_name)).setText(name);
            ((TextView) row.findViewById(R.id.hike_time)).setText(time);
            ((TextView) row.findViewById(R.id.hike_duration)).setText(duration);
            ((TextView) row.findViewById(R.id.hike_distance)).setText(distance);
            ((TextView) row.findViewById(R.id.hike_pace)).setText(pace);
            ((TextView) row.findViewById(R.id.hike_elev_gain)).setText(elev);


            return row;
        }
    }

    private class MyFirebaseRecordingListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // otherwise it will add data every time we listens for events
            adapter.clear();

            for (final DataSnapshot rec : dataSnapshot.getChildren()) {

                GenericTypeIndicator<ArrayList<Long>> l = new GenericTypeIndicator<ArrayList<Long>>() {};
                GenericTypeIndicator<ArrayList<Double>> d = new GenericTypeIndicator<ArrayList<Double>>() {};
                GenericTypeIndicator<ArrayList<Integer>> i = new GenericTypeIndicator<ArrayList<Integer>>() {};

                final ArrayList<Long> distancesTimes = rec.child("distancesTimes").getValue(l);
                final ArrayList<Double> distances = rec.child("distances").getValue(d);
                final ArrayList<Long> speedsTimes = rec.child("speedsTimes").getValue(l);
                final ArrayList<Double> speeds = rec.child("speeds").getValue(d);
                final ArrayList<Double> altitudes = rec.child("altitudes").getValue(d);
                final ArrayList<Long> hrTimes = rec.child("hrTimes").getValue(l);
                final ArrayList<Integer> hrDataArrayList = rec.child("hrDataArrayList").getValue(i);
                final ArrayList<Double> temperaturesArray = rec.child("temperaturesArray").getValue(d);
                final ArrayList<Double> pressuresArray =  rec.child("pressuresArray").getValue(d);
                final ArrayList<Long> temperaturesTimesArray = rec.child("temperaturesTimesArray").getValue(l);
                final ArrayList<Long> pressuresTimesArray = rec.child("pressuresTimesArray").getValue(l);
                final Recording recording = new Recording("",distancesTimes, distances, speedsTimes, speeds, altitudes, hrTimes, hrDataArrayList, temperaturesTimesArray,temperaturesArray,pressuresTimesArray,pressuresArray);

                // Generic information about the hike
                String startingTime = rec.child("startingTime").getValue().toString();
                String name = rec.child("name").getValue().toString();
                String mapUrl = rec.child("mapUrl").getValue().toString();
                String duration = rec.child("duration").getValue().toString();
                recording.setGenericInformation(startingTime, name, mapUrl, duration);

                adapter.add(recording);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.v(TAG, databaseError.toString());
        }
    }
}
