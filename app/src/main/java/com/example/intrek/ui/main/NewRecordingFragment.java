package com.example.intrek.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.intrek.BuildConfig;
import com.example.intrek.R;
import com.example.intrek.WearService;

public class NewRecordingFragment extends Fragment {

    private View fragmentView;

    public NewRecordingFragment() {
        // Required empty public constructor
    }

    public static NewRecordingFragment newInstance() {
        NewRecordingFragment fragment = new NewRecordingFragment();
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
        fragmentView = inflater.inflate(R.layout.fragment_new_recording, container, false);


        Button testButton = fragmentView.findViewById(R.id.testButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testButtonTapped();
            }
        });

        return fragmentView;
    }

    // MARK: - Functions for the logic of the class

    private void testButtonTapped() {
        Log.i("ABC","Is sending something to the watch");
        // send an intent to the watch
        Intent intentStartRec = new Intent(getActivity(), WearService.class);
        intentStartRec.setAction(WearService.ACTION_SEND.START_ACTIVITY.name());
        intentStartRec.putExtra(WearService.START_ACTIVITY_KEY, BuildConfig.W_start_activity);
        getActivity().startService(intentStartRec);
    }

}
