package com.example.moodle;

import android.content.Context;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

public class YAxisValueFormatter extends ValueFormatter {

    private Context context;
    private final String TAG = "YAxisValueFormatter";
    private DecimalFormat YAxisFormat = new DecimalFormat("###,##");

    public YAxisValueFormatter(Context context){
        super();
        this.context = context;
    }

    @Override
    public String getPointLabel(Entry entry) {
        return super.getPointLabel(entry);
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        axis.setAxisMinimum(0);
        axis.setAxisMaximum(5);
        axis.setLabelCount(21, true);
        return super.getAxisLabel(value, axis);
    }

    @Override
    public String getFormattedValue(float value) {

        if(Mood.moodExists(value)){
            Mood mood = Mood.getByRating(value);
            return context.getResources().getString(mood.getNameResID());
        }

        return "";
    }
}
