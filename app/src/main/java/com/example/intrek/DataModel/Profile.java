package com.example.intrek.DataModel;

import java.io.Serializable;

// to send different type of data together (not one by one) through an Intent
public class Profile implements Serializable {
    private static final String TAG = "Profile";

    private String uid;
    private String username;
    private String email;
    private String photoPath;

    private long totalHikes;
    private Double avgDistance;
    private Double avgPace;
    private Double avgElevGain;

    public Profile(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    // Getter

    public String getUid() { return this.uid; }

    public String getUsername() { return this.username; }

    public String getEmail() { return this.email; }

    public String getPhotoPath() { return this.photoPath; }

    public long getTotalHikes() { return this.totalHikes; }

    public Double getAvgDistance() { return this.avgDistance; }

    public Double getAvgPace() { return this.avgPace; }

    public Double getAvgElevGain() { return  this.avgElevGain; }

    // Setter

    public void setUsername(String username) { this.username = username; }

    public void setEmail(String email) { this.email = email; }

    public void setPhotoPath(String path) { this.photoPath = path; }

    public void setStatistics(long totalHikes, Double avgDistance, Double avgPace, Double avgElevGain) {
        this.totalHikes = totalHikes;
        this.avgDistance = avgDistance;
        this.avgPace = avgPace;
        this.avgElevGain = avgElevGain;
    }
}

