package com.example.intrek.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.intrek.R;

public class NewRecordingFragment extends Fragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_recording, container, false);
    }
}
