package com.example.ppltracker;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
public class MainActivity extends AppCompatActivity {

    private Button btnPush, btnPull, btnLegs;
    private DatabaseHelper dbHelper;
    private TextView tvPushDate;
    private TextView tvPullDate;
    private TextView tvLegsDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPush = findViewById(R.id.btnPush);
        btnPull = findViewById(R.id.btnPull);
        btnLegs = findViewById(R.id.btnLegs);
        tvPushDate = findViewById(R.id.tvPushDate);
        tvPullDate = findViewById(R.id.tvPullDate);
        tvLegsDate = findViewById(R.id.tvLegsDate);

        btnPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExerciseListActivity.class);
                intent.putExtra("routine", "Push");
                startActivity(intent);
            }
        });

        btnPull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExerciseListActivity.class);
                intent.putExtra("routine", "Pull");
                startActivity(intent);
            }
        });

        btnLegs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExerciseListActivity.class);
                intent.putExtra("routine", "Legs");
                startActivity(intent);
            }
        });
        dbHelper = new DatabaseHelper(this);

        // Check if the exercises have already been added
        if (dbHelper.isExercisesEmpty()) {
            dbHelper.createDefaultWorkouts();
        }
    }
    private void updateRoutineDates() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        String pushDate = dbHelper.getMostRecentDateForRoutine("Push");
        String pullDate = dbHelper.getMostRecentDateForRoutine("Pull");
        String legsDate = dbHelper.getMostRecentDateForRoutine("Legs");

        tvPushDate.setText(pushDate != null ? pushDate : "No entries yet");
        tvPullDate.setText(pullDate != null ? pullDate : "No entries yet");
        tvLegsDate.setText(legsDate != null ? legsDate : "No entries yet");
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateRoutineDates();
    }
}
