package com.example.ppltracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import java.util.ArrayList;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.List;
import java.util.concurrent.TimeUnit;
import android.widget.Toast;
import android.content.DialogInterface;
import android.app.AlertDialog;

public class RoutineStatisticsActivity extends AppCompatActivity {

    private LineChart chartRoutineVolume;
    private LineChart chartTotalWeight;
    private DatabaseHelper databaseHelper;
    private String routine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_statistics);

        Intent intent = getIntent();
        routine = intent != null ? intent.getStringExtra("routine") : "";

        chartRoutineVolume = findViewById(R.id.chartRoutineVolume);
        chartTotalWeight = findViewById(R.id.chartTotalWeight);

        databaseHelper = new DatabaseHelper(this);

        List<Entry> routineVolumeData = databaseHelper.getRoutineVolume(routine);
        List<Entry> totalWeightData = databaseHelper.getTotalWeight(routine);

        if(routineVolumeData.size() < 3 || totalWeightData.size() < 3) {
            Toast.makeText(this, "Provide more than two training sessions to generate stats.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setChartData(chartRoutineVolume, routineVolumeData, "Routine Volume");
        setChartData(chartTotalWeight, totalWeightData, "Total Weight");

        TextView tvImprovementRate = findViewById(R.id.tvImprovementRate);
        float improvementRate = databaseHelper.getImprovementRate(routine);
        if (improvementRate != 0) {
            tvImprovementRate.setText(String.format("Monthly Volume Change: %.2f%%", improvementRate));
        } else {
            tvImprovementRate.setText("Not enough data to compute improvement rate");
        }
    }


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
            float minX = entries.get(0).getX(); // get the first x value (minimum)
            float maxX = entries.get(entries.size() - 1).getX(); // get the last x value (maximum)
            float rangeX = maxX - minX; // calculate the range of x values

            xAxis.setAxisMinimum(minX);
            xAxis.setAxisMaximum(maxX);

            float fiveMonthsInDays = TimeUnit.DAYS.toMillis(150); // roughly five months

            // Set the visible range to maximum of five months or the actual range, whichever is smaller
            if (rangeX > fiveMonthsInDays) {
                chart.setVisibleXRangeMaximum(fiveMonthsInDays);
            } else {
                chart.setVisibleXRangeMaximum(rangeX);
            }
        }

        chart.invalidate(); // refresh the chart
    }




}
