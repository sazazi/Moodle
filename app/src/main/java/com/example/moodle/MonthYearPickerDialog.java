package com.example.moodle;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class MonthYearPickerDialog extends DialogFragment{

    private static final int MIN_YEAR = 2000;
    private DatePickerDialog.OnDateSetListener listener;
    private static int monthSet;
    private static int yearSet;

    public MonthYearPickerDialog(int monthSet, int yearSet){
        super();
        this.monthSet = monthSet+1;
        this.yearSet = yearSet;
    }

    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final Calendar cal = Calendar.getInstance();

        View dialog = inflater.inflate(R.layout.layout_month_picker_dialog, null);
        final NumberPicker monthPicker = (NumberPicker) dialog.findViewById(R.id.picker_month);
        final NumberPicker yearPicker = (NumberPicker) dialog.findViewById(R.id.picker_year);

        final int currentMonth = cal.get(Calendar.MONTH)+1;
        final int currentYear = cal.get(Calendar.YEAR);

        yearPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if(newVal==currentYear){
                    monthPicker.setMaxValue(currentMonth);
                }
                else{
                    monthPicker.setMaxValue(12);
                }
            }
        });

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(currentMonth);
        monthPicker.setValue(monthSet);

        yearPicker.setMinValue(MIN_YEAR);
        yearPicker.setMaxValue(currentYear);
        yearPicker.setValue(yearSet);

        builder.setView(dialog)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDateSet(null, yearPicker.getValue(), monthPicker.getValue()-1, 0);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MonthYearPickerDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
