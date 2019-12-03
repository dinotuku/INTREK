package com.example.intrek.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.intrek.MainActivity;
import com.example.intrek.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TypePickerPopUp extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_type_picker);

        // Set the dimensions of this new activity (it's a pop-up)
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width*.8), (int) (height*.6));

        // Set the button action
        Button doneButton = findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the activity
                Intent i = new Intent(TypePickerPopUp.this, MainActivity.class);
                setResult(AppCompatActivity.RESULT_OK,i);
                finish();
            }
        });

        // Set the list and its item
        ListView listView = findViewById(R.id.typeListView);
        TypesArrayAdapter adapter = new TypesArrayAdapter(TypePickerPopUp.this,R.layout.row_activity_type);
        listView.setAdapter(adapter);
        for (String s: NewRecordingFragment.activityTypes) {
            adapter.add(s);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
            }
        });
    }


    private class TypesArrayAdapter extends ArrayAdapter<String> {

        public TypesArrayAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            // Reference to the row View
            View row = convertView;

            if (row == null) {
                // Inflate it from layout
                row = LayoutInflater.from(getContext()).inflate(R.layout.row_activity_type, parent, false);
            }

            // Set the text
            ((TextView) row.findViewById(R.id.activityTextView)).setText(getItem(position));

            return row ;
        }
    }

}
