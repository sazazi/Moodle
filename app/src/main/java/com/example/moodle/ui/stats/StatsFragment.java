package com.example.moodle.ui.stats;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.moodle.MonthYearPickerDialog;
import com.example.moodle.Mood;
import com.example.moodle.MoodMarkerView;
import com.example.moodle.R;
import com.example.moodle.YAxisRightValueFormatter;
import com.example.moodle.YAxisValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class StatsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private final String TAG = "Stats Fragment";
    private LineChart moodLineChart;
    private Button thisMonthBtn;
    private Calendar currentCalendar;
    private TextView monthText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stats, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        currentCalendar = Calendar.getInstance();

        monthText = root.findViewById(R.id.monthText);

        Button previousMonthBtn = root.findViewById(R.id.previousMonthBtn);
        previousMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "PREVIOUS MONTH button clicked");
                selectPreviousMonth();
            }
        });

        Button monthPickerBtn = root.findViewById(R.id.monthPickerBtn);
        monthPickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "SELECT MONTH button clicked");
                selectMonth();
            }
        });

        thisMonthBtn = root.findViewById(R.id.thisMonthBtn);
        thisMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "THIS MONTH button clicked");
                initialiseChart();
            }
        });

        // Initialise line chart
        moodLineChart = root.findViewById(R.id.moodLineChart);
        moodLineChart.setNoDataText("");

        // Prevent dragging, scaling and zooming interaction with chart
        moodLineChart.setDragEnabled(false);
        moodLineChart.setScaleEnabled(false);
        moodLineChart.setDoubleTapToZoomEnabled(false);

        moodLineChart.getLegend().setEnabled(false);
        moodLineChart.getDescription().setEnabled(false);

        // Format left y-axis
        moodLineChart.setExtraLeftOffset(5f);
        YAxis yAxisLeft = moodLineChart.getAxisLeft();
        yAxisLeft.setDrawZeroLine(true);
        yAxisLeft.setValueFormatter(new YAxisValueFormatter(getActivity()));
        yAxisLeft.setDrawGridLines(false);

        // Format right y-axis
        YAxis yAxisRight = moodLineChart.getAxisRight();
        yAxisRight.setDrawZeroLine(true);
        yAxisRight.setValueFormatter(new YAxisRightValueFormatter());

        // Format x-axis
        XAxis xAxis = moodLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceMax(0.5f);
        xAxis.setSpaceMin(0.5f);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1); //sets min x-axis intervals to be 1

        initialiseChart();

        return root;
    }

    public void initialiseChart(){
        currentCalendar = Calendar.getInstance();
        currentCalendar.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), 1, 0,0,0);
        thisMonthBtn.setEnabled(false);
        setData();
    }

    public void selectPreviousMonth(){
        currentCalendar.add(Calendar.MONTH, -1);
        thisMonthBtn.setEnabled(true);
        setData();
    }

    public void selectMonth(){
        Calendar c = Calendar.getInstance();
        final int currentMonth = c.get(Calendar.MONTH);
        final int currentYear = c.get(Calendar.YEAR);

        int monthSet = currentCalendar.get(Calendar.MONTH);
        int yearSet = currentCalendar.get(Calendar.YEAR);

        MonthYearPickerDialog mpd = new MonthYearPickerDialog(monthSet, yearSet);
        mpd.setListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if(month==currentMonth && year==currentYear){
                    thisMonthBtn.setEnabled(false);
                }
                else{
                    thisMonthBtn.setEnabled(true);
                }
                currentCalendar.set(year, month, 1, 0,0,0);
                setData();
            }
        });
        mpd.show(getFragmentManager(), "MonthYearPickerDialog");
    }


    /** Gets data from Firestore Database and sets it for the mood chart */
    private void setData(){
        //clear the previous mood chart that was displayed
        moodLineChart.clear();

        Date firstDate = currentCalendar.getTime();
        //add a month to existing month, to get the upper bound of the query search to come
        currentCalendar.add(Calendar.MONTH, 1);
        Date endDate = currentCalendar.getTime();
        currentCalendar.add(Calendar.MONTH, -1);

        monthText.setText(new SimpleDateFormat("MMMM").format(firstDate) + " " + currentCalendar.get(Calendar.YEAR));

        FirebaseUser user = mAuth.getCurrentUser();
        String uid = Objects.requireNonNull(user).getUid();

        final Date firstDateOfMonth = firstDate;

        CollectionReference dailyEntriesCol = db.collection("users").document(uid).collection("dailyEntries");

        dailyEntriesCol
                .whereGreaterThanOrEqualTo("date", firstDateOfMonth)
                .whereLessThan("date", endDate)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Entry> entries = new ArrayList<>();
                            Map<Float, Map<String, Object>> moodEntries = new HashMap<>();
                            float referenceDate = 0;
                            float firstDate = 1f;

                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Map<String, Object> data = document.getData();

                                float date;

                                Timestamp d = (Timestamp) data.get("date");

                                Date d2 = d.toDate();
                                long diffInMilliseconds = Math.abs(d2.getTime()-firstDateOfMonth.getTime());
                                float diffInDays = TimeUnit.DAYS.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);

                                date = firstDate + diffInDays;

                                float rating = (float)((double) data.get("moodRating"));

                                //Sets mood icon for data points on the chart
                                Mood mood = Mood.getByRatingRange(rating);
                                Drawable icon = getContext().getDrawable(mood.getImageResID());
                                icon.setBounds(0,0, (int)(icon.getIntrinsicWidth()*0.5), (int)(icon.getIntrinsicHeight()*0.5));
                                Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
                                Drawable iconScaledDown = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 15, 15, true));

                                entries.add(new Entry(date, rating, iconScaledDown));
                                moodEntries.put(date, data);
                            }
                            LineDataSet dataSet = new LineDataSet(entries, "Time series");
                            dataSet.setDrawValues(false);
                            dataSet.setDrawIcons(true);

                            // Create and format marker
                            // Create marker to display box when values are selected
                            MoodMarkerView mv = new MoodMarkerView(getActivity(), R.layout.custom_marker_view, moodEntries);

                            // Set the marker to the chart
                            mv.setChartView(moodLineChart);
                            moodLineChart.setMarker(mv);

                            //Set the line data to the chart and refresh it
                            LineData data = new LineData(dataSet);
                            moodLineChart.setData(data);
                            moodLineChart.notifyDataSetChanged();
                            moodLineChart.invalidate();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }
}