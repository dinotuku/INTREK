package com.example.intrek.ui.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.intrek.DataModel.Profile;
import com.example.intrek.DataModel.Recording;
import com.example.intrek.DataModel.RecordingData;
import com.example.intrek.R;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    public static final String UID = "UID";
    NumberFormat nf = new DecimalFormat("##.##");


    private View fragmentView;

    private Profile userProfile;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);

        String uid = getActivity().getIntent().getExtras().getString(UID);

        readUserProfile(uid);

        return fragmentView;
    }

    private void readUserProfile(final String uid) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference profileRef = database.getReference("profiles");
        profileRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email_db = dataSnapshot.child("email").getValue(String.class);
                String username_db = dataSnapshot.child("username").getValue(String.class);
                String photo = dataSnapshot.child("photo").getValue(String.class);

                userProfile = new Profile(uid, email_db);
                userProfile.setUsername(username_db);
                userProfile.setPhotoPath(photo);

                // read recording histories and show statistic
                final long[] totalHikes = {0};
                final ArrayList<Double> distances_list = new ArrayList<>();
                final ArrayList<Double> paces_list = new ArrayList<>();
                final ArrayList<Double> elev_gains_list = new ArrayList<>();

                profileRef.child(uid).child("recordings").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

                            Double inMeter = distances.get(distances.size()-1) ;
                            Double inKm = inMeter / 1000 ;
                            ArrayList<RecordingData> statistics = recording.getStatistics();
                            String pace_string = statistics.get(0).getAverage();
                            double elev_gain = statistics.get(3).getMaxY() - statistics.get(3).getMinY();

                            totalHikes[0] += 1;
                            distances_list.add(inKm);
                            paces_list.add(Double.valueOf(pace_string.substring(0, pace_string.length() - 8)));
                            elev_gains_list.add(elev_gain);
                        }

                        Double avgDistance = 0.0;
                        for (Double distance: distances_list) {
                            avgDistance += distance;
                        }
                        avgDistance /= distances_list.size();

                        Double avgPace = 0.0;
                        for (Double pace: paces_list) {
                            avgPace += pace;
                        }
                        avgPace /= paces_list.size();

                        Double avgElevGain = 0.0;
                        for (Double elev_gain: elev_gains_list) {
                            avgElevGain += elev_gain;
                        }
                        avgElevGain /= elev_gains_list.size();

                        userProfile.setStatistics(totalHikes[0], avgDistance, avgPace, avgElevGain);

                        setUserInfo();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUserInfo() {
        // reference to an image file in Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReferenceFromUrl(userProfile.getPhotoPath());
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                if (isAdded()) {
                    final Bitmap selectedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    ImageView imageView = fragmentView.findViewById(R.id.profile_picture);
                    imageView.setImageBitmap(selectedImage);
                }
            }
        });

        TextView mDisplayName = fragmentView.findViewById(R.id.display_name);
        mDisplayName.setText(userProfile.getUsername());

        TextView mTotalHikes = fragmentView.findViewById(R.id.total_hikes);
        mTotalHikes.setText(String.valueOf(userProfile.getTotalHikes()));

        TextView mAvgDistance = fragmentView.findViewById(R.id.avg_distance);
        mAvgDistance.setText(nf.format(userProfile.getAvgDistance()));

        TextView mAvgPace = fragmentView.findViewById(R.id.avg_pace);
        mAvgPace.setText(nf.format(userProfile.getAvgPace()));

        TextView mAvgElevGain = fragmentView.findViewById(R.id.avg_elev_gain);
        mAvgElevGain.setText(nf.format(userProfile.getAvgElevGain()));
    }
}
