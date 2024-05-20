package com.example.ppltracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RoutineStatisticsActivity extends AppCompatActivity {

    private LineChart chartRoutineVolume;
    private LineChart chartTotalWeight;
    private DatabaseHelper databaseHelper;
    private String routine;
    private TextView tvImprovementRate;
    private TextView tvTimeframe;
    private TextView tvTotalWorkouts;
    private SeekBar timeframeSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_statistics);

        // Get the intent and extract the routine name
        Intent intent = getIntent();
        routine = intent != null ? intent.getStringExtra("routine") : "";

        // Initialize UI components
        chartRoutineVolume = findViewById(R.id.chartRoutineVolume);
        chartTotalWeight = findViewById(R.id.chartTotalWeight);
        tvImprovementRate = findViewById(R.id.tvImprovementRate);
        tvTimeframe = findViewById(R.id.tvTimeframe);
        tvTotalWorkouts = findViewById(R.id.tvTotalWorkouts);
        timeframeSeekBar = findViewById(R.id.timeframeSeekBar);

        // Initialize the database helper
        databaseHelper = new DatabaseHelper(this);

        // Get the data for the charts
        List<Entry> routineVolumeData = databaseHelper.getRoutineVolume(routine);
        List<Entry> totalWeightData = databaseHelper.getTotalWeight(routine);

        // Check if there is enough data to display
        if (routineVolumeData.size() < 2 || totalWeightData.size() < 2) {
            Toast.makeText(this, "Provide two training sessions to generate stats.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set the chart data
        setChartData(chartRoutineVolume, routineVolumeData, "Routine Volume");
        setChartData(chartTotalWeight, totalWeightData, "Total Weight");

        // Initial improvement rate calculation with default 30 days
        updateImprovementRate(30);
        updateTotalWorkouts(30);

        // SeekBar change listener to adjust the timeframe
        timeframeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Ensure the minimum value is 1
                int adjustedProgress = Math.max(1, progress);
                // Update the text view with the current timeframe
                tvTimeframe.setText(String.valueOf(adjustedProgress));
                // Update the improvement rate, total workouts, and adjust the chart axis
                updateImprovementRate(adjustedProgress);
                updateTotalWorkouts(adjustedProgress);
                adjustChartAxis(chartRoutineVolume, adjustedProgress);
                adjustChartAxis(chartTotalWeight, adjustedProgress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });
    }

    // Method to update the improvement rate
    private void updateImprovementRate(int days) {
        double improvementRate = databaseHelper.getImprovementRate(routine, days);
        if (improvementRate != 0) {
            tvImprovementRate.setText(String.format("Average Volume Change (%d days): %.2f%%", days, improvementRate));
        } else {
            tvImprovementRate.setText("Not enough data to compute improvement rate");
        }
    }

    // Method to update the total number of workouts within the given timeframe
    private void updateTotalWorkouts(int days) {
        int totalWorkouts = databaseHelper.getTotalWorkouts(routine, days);
        tvTotalWorkouts.setText(String.format("Total Workouts: %d", totalWorkouts));
    }

    // Method to adjust the chart axis based on the timeframe
    private void adjustChartAxis(LineChart chart, int days) {
        if (chart.getData() != null && !chart.getData().getDataSets().isEmpty()) {
            LineDataSet dataSet = (LineDataSet) chart.getData().getDataSets().get(0);
            float maxX = dataSet.getXMax(); // get the latest entry's x value
            float minX = maxX - TimeUnit.DAYS.toMillis(days); // calculate the minimum X value based on the slider value
            XAxis xAxis = chart.getXAxis();
            xAxis.setAxisMinimum(minX);
            xAxis.setAxisMaximum(maxX);

            chart.setVisibleXRangeMaximum(TimeUnit.DAYS.toMillis(days));
            chart.invalidate(); // refresh the chart
        }
    }

    // Method to set chart data and customize the appearance
    private void setChartData(LineChart chart, List<Entry> entries, String label) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(Color.GREEN);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.WHITE);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.GREEN);
        dataSet.setFillAlpha(50);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.getLegend().setTextColor(Color.WHITE);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setValueFormatter(new DateAxisValueFormatter());
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.WHITE);

        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisLeft().setDrawLabels(true);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(Color.WHITE);

        chart.getAxisRight().setEnabled(false);

        if (!entries.isEmpty()) {
            float maxX = entries.get(entries.size() - 1).getX(); // get the latest entry's x value
            float minX = maxX - TimeUnit.DAYS.toMillis(30); // default to 30 days
            xAxis.setAxisMinimum(minX);
            xAxis.setAxisMaximum(maxX);

            float rangeX = maxX - minX;

            float fiveMonthsInDays = TimeUnit.DAYS.toMillis(150);

            if (rangeX > fiveMonthsInDays) {
                chart.setVisibleXRangeMaximum(fiveMonthsInDays);
            } else {
                chart.setVisibleXRangeMaximum(rangeX);
            }
        }

        chart.invalidate();
    }
}
