package com.example.intrek.ui.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.intrek.DataModel.Profile;
import com.example.intrek.DataModel.Recording;
import com.example.intrek.DataModel.RecordingData;
import com.example.intrek.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

// Show user information and statistics.
// It will fetch data on Firebase and do some simple calculation.
public class ProfileFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    // Fields

    public static final String UID = "UID";

    private View fragmentView;

    private Profile userProfile;

    // Constructors

    public ProfileFragment() {
        // Required empty public constructor
    }

    // Method which will be called by SectionsPagerAdapter

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
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
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Get user ID from intent
        String uid = getActivity().getIntent().getExtras().getString(UID);

        // Read and set user information
        readUserProfile(uid);
        setUserInfo();

        return fragmentView;
    }

    // Methods

    // Fetch user information and statistics from Firebase
    private void readUserProfile(final String uid) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference profileRef = database.getReference("profiles");
        profileRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get basic information
                String email_db = dataSnapshot.child("email").getValue(String.class);
                String username_db = dataSnapshot.child("username").getValue(String.class);
                String photo = dataSnapshot.child("photo").getValue(String.class);

                // Create a Profile object
                userProfile = new Profile(uid, email_db);
                userProfile.setUsername(username_db);
                userProfile.setPhotoPath(photo);

                // For calculating statistics
                final long[] totalHikes = {0};
                final ArrayList<Double> distances_list = new ArrayList<>();
                final ArrayList<Double> paces_list = new ArrayList<>();
                final ArrayList<Double> elev_list = new ArrayList<>();

                // Get recording histories and show statistic
                profileRef.child(uid).child("recordings").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (final DataSnapshot rec : dataSnapshot.getChildren()) {
                            // For simply getting arrays using getValue
                            GenericTypeIndicator<ArrayList<Long>> l = new GenericTypeIndicator<ArrayList<Long>>() {};
                            GenericTypeIndicator<ArrayList<Double>> d = new GenericTypeIndicator<ArrayList<Double>>() {};
                            GenericTypeIndicator<ArrayList<Integer>> i = new GenericTypeIndicator<ArrayList<Integer>>() {};

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
                            final Recording recording = new Recording("",distancesTimes, distances, speedsTimes, speeds, altitudes, hrTimes, hrDataArrayList);

                            // Get statistics like distance, pace and elecation
                            Double inMeter = distances.get(distances.size()-1) ;
                            Double inKm = inMeter / 1000 ;
                            ArrayList<RecordingData> statistics = recording.getStatistics();
                            String pace_string = statistics.get(0).getAverage();
                            double elevation = statistics.get(3).getMaxY() - statistics.get(3).getMinY();

                            totalHikes[0] += 1;
                            distances_list.add(inKm);
                            paces_list.add(Double.valueOf(pace_string.substring(0, pace_string.length() - 8)));
                            elev_list.add(elevation);
                        }

                        // Calculate total, farthest and average distance
                        Double totalDistance = 0.0;
                        Double farthestDistance = Double.MIN_VALUE;
                        for (Double distance: distances_list) {
                            totalDistance += distance;
                            if (distance > farthestDistance) {
                                farthestDistance = distance;
                            }
                        }
                        Double avgDistance = totalDistance / distances_list.size();

                        // Calculate average pace
                        Double avgPace = 0.0;
                        for (Double pace: paces_list) {
                            avgPace += pace;
                        }
                        avgPace /= paces_list.size();

                        // Calculate total, highest and average elevation
                        Double totalElevation = 0.0;
                        Double highestElevation = Double.MIN_VALUE;
                        for (Double elevation: elev_list) {
                            totalElevation += elevation;
                            if (elevation > highestElevation) {
                                highestElevation = elevation;
                            }
                        }
                        Double avgElevation = totalElevation / elev_list.size();

                        // Save statistics to the Profile object
                        userProfile.totalHikes = totalHikes[0];
                        userProfile.avgDistance = avgDistance;
                        userProfile.avgPace = avgPace;
                        userProfile.avgElevation = avgElevation;

                        userProfile.totalDistance = totalDistance;
                        userProfile.totalElevation = totalElevation;
                        userProfile.farthestHike = farthestDistance;
                        userProfile.highestElevation = highestElevation;
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

    // Set and show user information
    private void setUserInfo() {
        // Reference to an image file in Firebase Storage
        // Set user profile picture
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

        // Set user display name
        TextView mDisplayName = fragmentView.findViewById(R.id.display_name);
        mDisplayName.setText(userProfile.getUsername());

        // Set total number of hikes
        TextView mTotalHikes = fragmentView.findViewById(R.id.total_hikes);
        mTotalHikes.setText(String.valueOf(userProfile.totalHikes));

        // Set average distance
        TextView mAvgDistance = fragmentView.findViewById(R.id.avg_distance);
        mAvgDistance.setText(String.valueOf(userProfile.avgDistance));

        // Set average pace
        TextView mAvgPace = fragmentView.findViewById(R.id.avg_pace);
        mAvgPace.setText(String.valueOf(userProfile.avgPace));

        // Set average elevation
        TextView mAvgElevation = fragmentView.findViewById(R.id.avg_elev);
        mAvgElevation.setText(String.valueOf(userProfile.avgElevation));

        // Set total distance
        TextView mTotalDistance = fragmentView.findViewById(R.id.total_distance);
        mTotalDistance.setText(String.format("%s km", String.valueOf(userProfile.totalDistance)));

        // Set total elevation
        TextView mTotalElevation = fragmentView.findViewById(R.id.total_elev);
        mTotalElevation.setText(String.format("%s m", String.valueOf(userProfile.totalElevation)));

        // Set farthest hike
        TextView mFarthestHike = fragmentView.findViewById(R.id.far_hike);
        mFarthestHike.setText(String.format("%s km", String.valueOf(userProfile.farthestHike)));
//        TextView mFarthestHikeDate = fragmentView.findViewById(R.id.date_far_hike);
//        mFarthestHikeDate.setText();

//        TextView mLongestHike = fragmentView.findViewById(R.id.long_hike);
//        mLongestHike.setText(String.valueOf());
//        TextView mLongestHikeDate = fragmentView.findViewById(R.id.date_long_hike);
//        mLongestHikeDate.setText();

        // Set highest elevation
        TextView mHighestElevation = fragmentView.findViewById(R.id.high_elev);
        mHighestElevation.setText(String.format("%s m", String.valueOf(userProfile.highestElevation)));
//        TextView mHighestElevationDate = fragmentView.findViewById(R.id.date_high_elev);
//        mHighestElevationDate.setText();

        // TODO: finish time related parts
    }
}
