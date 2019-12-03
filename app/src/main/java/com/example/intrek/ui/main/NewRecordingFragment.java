package com.example.intrek.ui.main;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.intrek.BuildConfig;
import com.example.intrek.R;
import com.example.intrek.WearService;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class NewRecordingFragment extends Fragment {


    public static final ArrayList<String> activityTypes = new ArrayList<String>(Arrays.asList("Running","Mountain Hiking","City Hiking"));

    private static final int TYPE_REQUEST = 1;
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

        ImageButton testButton = fragmentView.findViewById(R.id.testButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testButtonTapped();
            }
        });

        Button typeButton = fragmentView.findViewById(R.id.activityTypeButton);
        typeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentTypePickerDialog();
            }
        });

        return fragmentView;
    }

    // MARK: - overriden functions

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TYPE_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            Log.i("ABC","Message received");
        }
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

    // Presents a dialog with a list view which will ask for the type
    private void presentTypePickerDialog() {
        Intent intent = new Intent(getActivity(), TypePickerPopUp.class);
        startActivityForResult(intent,TYPE_REQUEST);
    }
}
