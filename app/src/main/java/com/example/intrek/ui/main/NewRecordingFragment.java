package com.example.intrek.ui.main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Binder;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.intrek.BuildConfig;
import com.example.intrek.R;
import com.example.intrek.SensorTile.BluetoothLeService;
import com.example.intrek.SensorTile.DeviceScanActivity;
import com.example.intrek.SensorTile.NumberConversion;
import com.example.intrek.WearService;

import java.util.ArrayList;
import java.util.Arrays;

import static android.content.Context.BIND_AUTO_CREATE;

public class NewRecordingFragment extends Fragment {

    // MARK: - Public variables

    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String NEW_INDEX = "newIndex";
    public static final ArrayList<String> ACTIVITY_TYPES = new ArrayList<String>(Arrays.asList("Running","Mountain Hiking","City Hiking"));
    public static final String SELECTED_INDEX = "SelectedIndex";
    public static final String DEVICE_NOT_SUPPORTED = "Device not supported";
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;

    // MARK: - Private variables

    private static final int TYPE_REQUEST = 1;
    private static final int TYPE_REQUEST_TILE = 2;
    private View fragmentView;
    private int selectedType = 0 ;
    private Button typeButton,tileButton;
    private Switch switchTile;


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

        tileButton = fragmentView.findViewById(R.id.buttonTile);
        tileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DeviceScanActivity.class);
                startActivityForResult(intent,TYPE_REQUEST_TILE);
            }
        });

        switchTile = fragmentView.findViewById(R.id.TileSwitch);
        switchTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchTile.isChecked()){
                    connexionTile();
                    Log.e("AddressDevice",mDeviceAddress);
                    Intent gattServiceIntent = new Intent(getActivity(), BluetoothLeService.class);
                    mBluetoothLeService.connect(mDeviceAddress);
                    switchTile.setText("Connected");
                    mConnected = true;

                }else{
                    mBluetoothLeService.disconnect();
                    switchTile.setText("Disconnected");
                    mConnected = false;

                }

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
        }
        if (requestCode == TYPE_REQUEST_TILE && resultCode == AppCompatActivity.RESULT_OK) {
            String deviceName = data.getStringExtra(DeviceScanActivity.DEVICE_NAME);
            if (deviceName!=null){
                mDeviceAddress = data.getStringExtra(DeviceScanActivity.DEVICE_ADDRESS);
                tileButton.setText(deviceName);
                tileButton.setBackgroundColor(0XFF00A000);
                switchTile.setVisibility(View.VISIBLE);

            }else{
                Toast.makeText(getActivity(), DEVICE_NOT_SUPPORTED, Toast.LENGTH_SHORT).show();
                tileButton.setText("Connexion");
                tileButton.setBackgroundColor(Color.LTGRAY);
                switchTile.setVisibility(View.INVISIBLE);
            }

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

    private boolean connexionTile(){

        return true;
    }


    private void displayData(byte[] tileData){
        Log.d("TAG", "Temperature format UINT16.");
        Log.d("TAG", "Pressure format UINT32.");
        final int temperature = NumberConversion.bytesToInt16(tileData,6);
        final int pressure = NumberConversion.bytesToInt32(tileData,2);
        Log.d("TAG", String.format("Received Temperature: %d", temperature));
        Log.d("TAG", String.format("Received Pressure: %d", pressure));
    }

}

