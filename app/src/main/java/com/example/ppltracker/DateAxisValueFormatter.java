package com.example.ppltracker;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateAxisValueFormatter extends ValueFormatter {

    private SimpleDateFormat sdf;

    public DateAxisValueFormatter() {
        sdf = new SimpleDateFormat("MM/dd", Locale.getDefault());
    }

    @Override
    public String getFormattedValue(float value) {
        return sdf.format(new Date((long) value));
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        return getFormattedValue(value);
    }
}
