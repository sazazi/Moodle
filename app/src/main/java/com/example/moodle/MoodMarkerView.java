package com.example.moodle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.common.base.Joiner;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MoodMarkerView extends MarkerView {
    private TextView tvContent;
    private Map<Float, Map<String, Object>> moodEntries;
    private final String TAG = "Mood Marker View";

    public MoodMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        tvContent = (TextView) findViewById(R.id.tvContent);
    }

    public MoodMarkerView(Context context, int layoutResource, Map<Float, Map<String, Object>> moodEntries) {
        super(context, layoutResource);

        tvContent = (TextView) findViewById(R.id.tvContent);
        this.moodEntries = moodEntries;

        Log.d(TAG, "moodEntries: " + moodEntries);
    }

    // callbacks every time the MarkerView is redrawn, used to update the content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String text;

        if (e instanceof CandleEntry) {
            CandleEntry ce = (CandleEntry) e;
            text = "Mood Rating: " + ce.getHigh();
        } else {
            float date = e.getX();
            Map<String, Object> entryDetails;

            if(moodEntries.containsKey(date)){
                entryDetails = moodEntries.get(date);

                //Get date
                Timestamp timestamp = (Timestamp) Objects.requireNonNull(entryDetails).get("date");
                Date fullDate = Objects.requireNonNull(timestamp).toDate();
                @SuppressLint("SimpleDateFormat")
                String dateFormatted = new SimpleDateFormat("EEE dd/MM/yy").format(fullDate);
                //Get activities
                List<Activity> activities = (List<Activity>) entryDetails.get("activities");
                String delim = ", ";
                String activitiesStr = Joiner.on(delim).join(Objects.requireNonNull(activities));

                //Get comment
                String comment = (String) entryDetails.get("comment");

                text = dateFormatted
                        + "\n\nMood Rating: " + Math.round(e.getY()*10.00)/10.00
                        + "\n\nActivities:\n" + activitiesStr//sb.toString()
                        + "\n\nComment:\n" + comment;
            }
            else{
                text = "Mood Rating: " + e.getY();
            }
        }

        tvContent.setText(text);

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
