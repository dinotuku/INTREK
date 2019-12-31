package com.example.intrek.DataModel;

import android.content.Intent;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.io.Serializable;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

// This class contains all the data of one recording and all the functions required to plot it on a analysis session.
// It is important to see that this class has lot of functionalities, but not all of them needs to be saved.
public class Recording implements Serializable {

    // MARK: - Fields

    ///// Generic information about the hike
    // todo
    private String startingTime ;
    private String endingTime ;
    private int grade; // Out of 5
    private String name;

    //// Collected data on the hike to be analysed

    // Arrays for the location and the distance
    private ArrayList<Long> distancesTimes = new ArrayList<>();
    private ArrayList<Double> distances = new ArrayList<>();

    // Arrays for the speed and altitude
    private ArrayList<Long> speedsTimes = new ArrayList<>();
    private ArrayList<Double> speeds = new ArrayList<>();
    private ArrayList<Double> altitudes = new ArrayList<>();


    // Arrays for the HR
    private ArrayList<Long> hrTimes = new ArrayList<>();
    private ArrayList<Integer> hrDataArrayList = new ArrayList<>();

    // MARK: - Public methods

    public Recording(ArrayList<Long> distancesTimes, ArrayList<Double> distances, ArrayList<Long> speedsTimes, ArrayList<Double> speeds, ArrayList<Double> altitudes, ArrayList<Long> hrTimes, ArrayList<Integer> hrDataArrayList) {
        this.distancesTimes = distancesTimes;
        this.distances = distances;
        this.speedsTimes = speedsTimes;
        this.speeds = speeds;
        this.altitudes = altitudes;
        this.hrTimes = hrTimes;
        this.hrDataArrayList = hrDataArrayList;
    }

    // This constructor is to build the statistics

    // Save recording to Firebase realtime database
    public void saveToFirebase(String uid) {
        // Get the recordings reference of the user in database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference profileGetRef = database.getReference("profiles");
        final DatabaseReference recordingRef = profileGetRef.child(uid).child("recordings").push();

        // Save to database using a handler
        recordingRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                // Save everything
                mutableData.child("startingTime").setValue(startingTime);
                mutableData.child("endingTime").setValue(endingTime);
                mutableData.child("grade").setValue(grade);
                mutableData.child("name").setValue(name);

                mutableData.child("distancesTimes").setValue(distancesTimes);
                mutableData.child("distances").setValue(distances);
                mutableData.child("speedsTimes").setValue(speedsTimes);
                mutableData.child("speeds").setValue(speeds);
                mutableData.child("altitudes").setValue(altitudes);
                mutableData.child("hrTimes").setValue(hrTimes);
                mutableData.child("hrDataArrayList").setValue(hrDataArrayList);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if (b) {
                    Log.e("Recording", "Saving to Firebase succeeded!");
                } else {
                    Log.e("Recording", "Saving to Firebase failed!");
                }
            }
        });
    }

    public String getStartingTime() { return this.startingTime; }

    public String getEndingTime() { return this.endingTime; }

    public String getName() { return this.name; }

    public ArrayList<RecordingData> getStatistics() {

        // 1. Convert the obtained data here to get distances in the x-channel
        // Construct the distances array
        ArrayList<Double> speedX = getDistancesFromTimes(speedsTimes);
        ArrayList<Double> hrX = getDistancesFromTimes(hrTimes);
        // Construct new HR and the pace
        ArrayList<Double> hrY = getHRAsDouble() ;
        ArrayList<Double> paces = getPace();

        // 2. Construct the data to be send and return it
        RecordingData s1 = new RecordingData("Pace",speedX,paces,"[min/km]") ;
        RecordingData s2 = new RecordingData("Speed",speedX,speeds,"[km/h]") ;
        RecordingData s3 = new RecordingData("Heart Rate",hrX,hrY,"[BPM]") ;
        RecordingData s4 = new RecordingData("Altitude",speedX,altitudes,"[m]") ;

        ArrayList<RecordingData> statistics = new ArrayList<>();
        statistics.add(s1);
        statistics.add(s2);
        statistics.add(s3);
        statistics.add(s4);
        return statistics;
    }

    public String getDistance() {
        Double inMeter = distances.get(distances.size()-1) ;
        Double inKm = inMeter / 1000 ;
        NumberFormat nf = new DecimalFormat("##.##");
        return nf.format(inKm) + " km";
    }

    public String getDuration() {
        Long lastTime = hrTimes.get(hrTimes.size()-1);
        Long seconds = lastTime/1000;
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day *24);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);
        String s = String.valueOf(hours) + "h - " + String.valueOf(minute) + "m - " + String.valueOf(second) + "s" ;
        return s ;
    }

    public void setGenericInformation(String startingTime, String endingTime, int grade, String name) {
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.grade = grade;
        this.name = name;
    }



    // MARK: - Private methods

    // This method returns an array containing all the paces.
    // The array of speeds needs to be in km/h already
    private ArrayList<Double> getPace() {
        ArrayList<Double> toReturn = new ArrayList<>();
        for (Double speed: speeds){
            if (speed >= 4) {
                toReturn.add(60/speed);
            } else {
                toReturn.add(10.0);
            }
        }
        return toReturn;
    }

    // Returns an array with the HR values as doubles
    private ArrayList<Double> getHRAsDouble() {
        ArrayList<Double> toReturn = new ArrayList<>();
        for (Integer i: hrDataArrayList) {
            toReturn.add((double) i) ;
        }
        return toReturn ;
    }

    // Returns an array of equivalent distances in meters for a given array of time steps.
    // Using this function, we can make sure than the x-values on the graphs is the distance and not the time
    private ArrayList<Double> getDistancesFromTimes(ArrayList<Long> times) {
        ArrayList<Double> toReturn = new ArrayList<>();
        for (Long time:times) {
            Double distance = getDistanceForTime(time);
            toReturn.add(distance);
        }
        return toReturn ;
    }

    // Returns the distance at the given time moment required
    private Double getDistanceForTime(Long time) {
        int index = 0 ;
        int maxIndex = distancesTimes.size()-1;
        while (distancesTimes.get(index) < time && index < maxIndex) {
            index ++ ;
        }
        // We now have the index of the lowest time
        return distances.get(index);
    }

}
