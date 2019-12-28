package com.example.intrek.ui.main;

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
import com.example.intrek.SensorTile.BlePopUp;
import com.example.intrek.SensorTile.DeviceScanActivity;
import com.example.intrek.WearService;

import java.util.ArrayList;
import java.util.Arrays;

public class NewRecordingFragment extends Fragment {

    // MARK: - Public variables

    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String NEW_INDEX = "newIndex";
    public static final ArrayList<String> ACTIVITY_TYPES = new ArrayList<String>(Arrays.asList("Running","Mountain Hiking","City Hiking"));
    public static final String SELECTED_INDEX = "SelectedIndex";

    // MARK: - Private variables

    private static final int TYPE_REQUEST = 1;
    private static final int TYPE_REQUEST_TILE = 2;
    private View fragmentView;
    private int selectedType = 0 ;
    private Button typeButton;


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

        ImageButton testButton = fragmentView.findViewById(R.id.startButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startExercise();
            }
        });

        typeButton = fragmentView.findViewById(R.id.activityTypeButton);
        typeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentTypePickerDialog();
            }
        });

        Button tileButton = fragmentView.findViewById(R.id.buttonTile);
        tileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DeviceScanActivity.class);
                startActivityForResult(intent,TYPE_REQUEST_TILE);
            }
        });

        updateTypeButtonText();

        return fragmentView;
    }

    // MARK: - overiden functions

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TYPE_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            selectedType = data.getIntExtra(TypePickerPopUp.NEW_INDEX,0);
            updateTypeButtonText();
            Log.e("Test","Type");
        }
        if (requestCode == TYPE_REQUEST_TILE && resultCode == AppCompatActivity.RESULT_OK) {
            String deviceName = data.getStringExtra(DeviceScanActivity.DEVICE_NAME);
            Log.e("Test","Device: "+ deviceName);
        }
    }


    // MARK: - Functions for the logic of the class

    // Called when the user taps 'start' to lunch the activity screen.
    // Must open a new activity which will present the activity.
    private void startExercise() {
        // If the watch isn't connected, don't send anything to it...

        // Start the recording activity on the watch
        openWatchActivity();

        // Open the live activity
        Intent intent = new Intent(getActivity(), LiveRecordingActivity.class);
        startActivity(intent);

    }




    // This function will call the watch and start the recording
    private void openWatchActivity() {
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
        intent.putExtra(SELECTED_INDEX,selectedType);
        startActivityForResult(intent,TYPE_REQUEST);
    }

    // This function will update the text displayed on the type button
    private void updateTypeButtonText() {
        typeButton.setText(ACTIVITY_TYPES.get(this.selectedType));
    }
}
