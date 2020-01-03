package com.example.intrek.DataModel;

import android.content.Intent;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.maps.android.PolyUtil;

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

    private String startingTime ;
    private String duration;
    private String name;
    private String mapUrl ;
    private Double elevationGain ;


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

    // Arrays for the microcontroller
    private ArrayList<Double> temperaturesArray = new ArrayList<>();
    private ArrayList<Double> pressuresArray = new ArrayList<>();
    private ArrayList<Long> temperaturesTimesArray = new ArrayList<>();
    private ArrayList<Long> pressuresTimesArray = new ArrayList<>();


    // MARK: - Public methods

    public Recording(String duration, ArrayList<Long> distancesTimes, ArrayList<Double> distances, ArrayList<Long> speedsTimes, ArrayList<Double> speeds, ArrayList<Double> altitudes, ArrayList<Long> hrTimes, ArrayList<Integer> hrDataArrayList,ArrayList<Long> temperaturesTimesArray, ArrayList<Double> temperaturesArray, ArrayList<Long> pressuresTimesArray, ArrayList<Double> pressuresArray) {
        this.duration = duration;
        this.distancesTimes = distancesTimes;
        this.distances = distances;
        this.speedsTimes = speedsTimes;
        this.speeds = speeds;
        this.altitudes = altitudes;
        this.hrTimes = hrTimes;
        this.hrDataArrayList = hrDataArrayList;
        this.temperaturesTimesArray = temperaturesTimesArray ;
        this.temperaturesArray = temperaturesArray ;
        this.pressuresTimesArray = pressuresTimesArray;
        this. pressuresArray = pressuresArray ;
    }

    // To save the image of the map, we only keep the url of the google map link
    // This method construct the URL and save it for this recording, using the array of locations
    // It uses the array of all locations to contruct the path
    public void constructURLFromLocations(ArrayList<LatLng> averagedLocations) {
        LatLng firstLocation = averagedLocations.get(0);
        String zoom = "15" ;
        String size = "600x400";
        String path = PolyUtil.encode(averagedLocations);
        String url = "http://maps.google.com/maps/api/staticmap?"
                + "center=" + String.valueOf(firstLocation.latitude) + "," + String.valueOf(firstLocation.longitude)
                + "&zoom=" + zoom
                + "&size=" + size
                + "&path=color:0x0000ff|weight:5|enc:" + path
                + "&sensor=false&key=AIzaSyCjDSiAyIqt1YApD1rCTgUTAFeO6Udcixs"
                ;
        this.mapUrl = url ;
    }

    // This method is called when receivng the data from firebase
    public void setGenericInformation(String startingTime, String name, String mapUrl, String duration, Double elevationGain) {
        this.startingTime = startingTime;
        this.name = name ;
        this.mapUrl = mapUrl ;
        this.duration = duration ;
        this.elevationGain = elevationGain ;
    }

    // Save recording to Firebase realtime database. The user Id is reauired in order to save the data to the correct user.
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
                mutableData.child("name").setValue(name);
                mutableData.child("elevationGain").setValue(elevationGain);
                mutableData.child("mapUrl").setValue(mapUrl);
                mutableData.child("duration").setValue(duration);
                mutableData.child("distancesTimes").setValue(distancesTimes);
                mutableData.child("distances").setValue(distances);
                mutableData.child("speedsTimes").setValue(speedsTimes);
                mutableData.child("speeds").setValue(speeds);
                mutableData.child("altitudes").setValue(altitudes);
                mutableData.child("hrTimes").setValue(hrTimes);
                mutableData.child("hrDataArrayList").setValue(hrDataArrayList);
                mutableData.child("temperaturesArray").setValue(temperaturesArray);
                mutableData.child("pressuresArray").setValue(pressuresArray);
                mutableData.child("temperaturesTimesArray").setValue(temperaturesTimesArray);
                mutableData.child("pressuresTimesArray").setValue(pressuresTimesArray);
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

    // Returns the list of recording datas to be displayed in plots.
    public ArrayList<RecordingData> getStatistics() {

        //// 1. Convert the obtained data here to get distances in the x-channel
        // Indeed, we have the arrays as function of time but we want them as function of distance...
        //// 2. Construct the data to be send and return it

        // Construct the distances array for the pace, the speed and the altitude
        ArrayList<Double> gpsX = getDistancesFromTimes(speedsTimes);
        ArrayList<Double> hrY = getHRAsDouble() ;
        ArrayList<Double> paces = getPace();

        ArrayList<RecordingData> statistics = new ArrayList<>();

        RecordingData s1 = new RecordingData("Pace",gpsX,paces,"[min/km]") ;
        statistics.add(s1);
        RecordingData s2 = new RecordingData("Speed",gpsX,speeds,"[km/h]") ;
        statistics.add(s2);
        RecordingData s3 = new RecordingData("Altitude",gpsX,altitudes,"[m]") ;
        statistics.add(s3);

        if (hrY.size()>0) {
            ArrayList<Double> hrX = getDistancesFromTimes(hrTimes);
            RecordingData s4 = new RecordingData("Heart Rate",hrX,hrY,"[BPM]");
            statistics.add(s4);
        }

        if (pressuresArray != null && pressuresArray.size()>0) {
            ArrayList<Double> pressureX = getDistancesFromTimes(pressuresTimesArray);
            RecordingData s5 = new RecordingData("Pressure",pressureX,pressuresArray,"[Pa]");
            statistics.add(s5);
        }

        if (temperaturesArray != null && temperaturesArray.size()>0) {
            ArrayList<Double> tempX = getDistancesFromTimes(temperaturesTimesArray);
            RecordingData s6 = new RecordingData("Temperature",tempX,temperaturesArray,"[C]");
            statistics.add(s6);
        }

        return statistics;
    }

    //// Setters and getters

    public void setName(String name) {
        this.name = name;
    }

    public void setStartingTime(String startingTime) {
        this.startingTime = startingTime;
    }

    public void setElevationGain(Double elevationGain) {
        this.elevationGain = elevationGain;
    }

    public Double getElevationGain() {
        return elevationGain;
    }

    public String getName() { return this.name; }

    public String getDistance() {
        Double inMeter = distances.get(distances.size()-1) ;
        Double inKm = inMeter / 1000 ;
        NumberFormat nf = new DecimalFormat("##.##");
        return nf.format(inKm) + " km";
    }

    public String getMapUrl() {
        return this.mapUrl;
    }

    public String getDuration() {
        return this.duration;
    }

    public Double getAvePace() {
        // 1. Compute the average speed
        Double aveSpeed = 0.0 ;
        for (Double s: speeds) {
            aveSpeed += s ;
        }
        aveSpeed = aveSpeed / speeds.size() ;
        // 2. display the average speed
        Double avePace = 60 / aveSpeed ;
        if (avePace > 20) {
            avePace = 0.0 ;
        }
        return avePace;
    }

    public String getStartingTime() { return this.startingTime; }

    //// Private methods for processing the data

    // This method returns an array containing all the paces.
    // The array of speeds needs to be in km/h already
    private ArrayList<Double> getPace() {
        ArrayList<Double> toReturn = new ArrayList<>();
        for (Double speed: speeds){
            Double pace = 60 / speed ;
            if (pace > 20) {
                toReturn.add(20.0);
            } else {
                toReturn.add(pace);
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
