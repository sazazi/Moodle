package com.example.moodle;

import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class XAxisValueFormatter extends ValueFormatter {

    private final String TAG = "XAxisValueFormatter";

    @Override
    public String getPointLabel(Entry entry) {
        return super.getPointLabel(entry);
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        return super.getAxisLabel(value, axis);
    }

    @Override
    public String getFormattedValue(float value) {
        return super.getFormattedValue(value);
    }
}
