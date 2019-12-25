package com.example.intrek.DataModel;

import java.sql.Time;
import java.util.ArrayList;

public class Recording {

    // MARK: - Fields

    // About time
    private Time startingTime ;
    private Time endingTime ;
    // All positions
    private ArrayList<Double> lats = new ArrayList<>();
    private ArrayList<Double> longs = new ArrayList<>();
    // Collected data
    private ArrayList<Float> HRs = new ArrayList<>();

    // todo : add all the sensors
    // Comments of the users
    private int grade; // Out of 5
    private String name;
    

    // MARK: - Public methods

    public Recording() {
        // todo
    }

    public void saveToFirebase() {
        // todo
    }

    // MARK: - Private methods


}
