package com.example.intrek.DataModel;

import java.io.Serializable;

// Store user information and statistics.
// to send different type of data together (not one by one) through an Intent
public class Profile implements Serializable {
    private static final String TAG = "Profile";

    // Fields

    private String uid;
    private String username;
    private String email;
    private String photoPath;

    public long totalHikes;
    public Double avgDistance;
    public Double avgPace;
    public Double avgElevation;

    public Double totalDistance;
    public Double totalElevation;
    public Double farthestHike;
    public String longestHike;
    public Double highestElevation;

    public String farthestHikeDate;
    public String longestHikeDate;
    public String highestElevationDate;

    // Constructors

    public Profile(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    // Getters

    public String getUid() { return this.uid; }

    public String getUsername() { return this.username; }

    public String getEmail() { return this.email; }

    public String getPhotoPath() { return this.photoPath; }


    // Setters

    public void setUsername(String username) { this.username = username; }

    public void setPhotoPath(String path) { this.photoPath = path; }
}

