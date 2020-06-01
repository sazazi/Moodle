package com.example.moodle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class DisplayActivitiesSelectionActivity extends AppCompatActivity {

    private static final String TAG = "Activities Activity";
    public static final String MOOD = "com.example.myfirstapp.MOOD";
    public static final String ACTIVITIES = "com.example.myfirstapp.ACTIVITIES";

    private ArrayList<String> activities = new ArrayList<>();
    private Mood mood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_activities_selection);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String name = intent.getStringExtra(MainActivity.MOODNAME);
        mood = Mood.getByName(name);
        int imageResID = mood.getImageResID();

        ImageView moodImg = findViewById(R.id.selectedMoodImg);
        moodImg.setImageResource(imageResID);
    }

    /** Called when the user selects an activity */
    public void selectActivity(View view){
        ImageButton activityBtn = (ImageButton) view;
        activityBtn.setSelected(!activityBtn.isSelected());
        String activityName = view.getTag().toString();
        Activity selectedActivity = Activity.getByName(activityName);
        if(selectedActivity!=null) {
            String selectedActivityName = getResources().getString(selectedActivity.getNameResID());
            if (activityBtn.isSelected()) {
                activities.add(selectedActivityName);
            } else {
                activities.remove(selectedActivityName);
            }
        }
        else {
            Log.d(TAG, "Unknown Activity");
        }
    }

    /** Called when the user taps the Next button */
    public void next(View view) {
        if(activities.isEmpty()){
            Toast.makeText(DisplayActivitiesSelectionActivity.this, "No activities selected. Please select at least one.", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent intent = new Intent(this, CommentsActivity.class);
            int nameResID = mood.getNameResID();
            String moodName = getResources().getString(nameResID);
            intent.putExtra(MOOD, moodName);
            intent.putExtra(ACTIVITIES, activities.toArray(new String[0]));
            startActivity(intent);
        }
    }

    /** Enable back button in top-left of action bar for going back  **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
