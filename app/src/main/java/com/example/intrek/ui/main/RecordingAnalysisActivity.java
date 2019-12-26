package com.example.intrek.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.intrek.DataModel.RecordingStatistic;
import com.example.intrek.R;

public class RecordingAnalysisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_analysis);

        setListView();

    }

    // This method is here to set the statistics list view
    private void setListView(){
        ListView listView = findViewById(R.id.statisticsListView);
        // Set the adapter to the created adapter
        StatisticListViewAdapter adapter = new StatisticListViewAdapter(RecordingAnalysisActivity.this,R.layout.row_time_statistics);
        listView.setAdapter(adapter);
    }

    private class StatisticListViewAdapter extends ArrayAdapter<RecordingStatistic> {

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
                row = LayoutInflater.from(getContext()).inflate(R.layout.row_activity_type, parent, false);
            }

            // And set the row with the data
            RecordingStatistic stat = getItem(position);

            // Set the text
            ((TextView) row.findViewById(R.id.statisticNameTextView)).setText(stat.getName());

            // Set the plot view



            return row ;
        }
    }
}
