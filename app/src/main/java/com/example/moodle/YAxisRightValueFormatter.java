package com.example.moodle;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class YAxisRightValueFormatter extends ValueFormatter {

    private final String TAG = "YAxisValueFormatter";

    @Override
    public String getPointLabel(Entry entry) {
        return super.getPointLabel(entry);
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        axis.setAxisMinimum(0);
        axis.setAxisMaximum(5);
        axis.setLabelCount(6, true);
        return super.getAxisLabel(value, axis);
    }

    @Override
    public String getFormattedValue(float value) {
        return "";
    }
}
