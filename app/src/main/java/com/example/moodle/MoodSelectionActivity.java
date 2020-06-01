package com.example.moodle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MoodSelectionActivity extends AppCompatActivity {

    private static final String TAG = "Mood Selection Activity";
    public static final String MOODNAME = "com.example.myfirstapp.MOODNAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_selection);
    }

    /** Called when the user selects a Mood button */
    public void sendMood(View view) {
        Log.d(TAG, "MOOD BUTTON CLICKED");
        Intent intent = new Intent(this, DisplayActivitiesSelectionActivity.class);

        String moodName = view.getTag().toString();
        Mood selectedMood = Mood.getByName(moodName);

        if(selectedMood!=null) {
            Log.d(TAG, "Mood Name: " + moodName);
            intent.putExtra(MOODNAME, moodName);
            Log.d(TAG, "Going to start next activity");
            startActivity(intent);
        }
        else{
            Log.d(TAG, "Mood Unknown");
        }
    }

    /** Called when the user clicks Skip button */
    public void skip(View view) {
        //start Visualisations Activity
        Intent intent = new Intent(this, VisualisationsActivity.class);
        startActivity(intent);
    }
}
