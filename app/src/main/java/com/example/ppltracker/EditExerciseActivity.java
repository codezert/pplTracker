package com.example.ppltracker;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

public class EditExerciseActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private EditText edtExerciseName;
    private String routine;
    private int selectedExerciseId; // CHANGED VARIABLE NAME
    private EditText edtReps;
    private EditText edtSets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_exercise);

        dbHelper = new DatabaseHelper(this);
        routine = getIntent().getStringExtra("routine");

        selectedExerciseId = getIntent().getIntExtra("selected_exercise_id", -1); // CHANGED LINE HERE

        edtReps = findViewById(R.id.etReps);
        edtSets = findViewById(R.id.etSets);
        edtExerciseName = findViewById(R.id.etExerciseName);

        if (selectedExerciseId != -1) { // CHANGED CONDITION HERE
            Exercise exercise = dbHelper.getExerciseById(selectedExerciseId); // CHANGED LINE HERE
            edtExerciseName.setText(exercise.getName());
            edtReps.setText(exercise.getReps());
            edtSets.setText(exercise.getSets());
        }

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String exerciseName = edtExerciseName.getText().toString();
                String reps = edtReps.getText().toString();
                String sets = edtSets.getText().toString();

                if (exerciseName.isEmpty() || reps.isEmpty() || sets.isEmpty()) {
                    Toast.makeText(EditExerciseActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else if (exerciseName.length() > 20 || reps.length() > 20 || sets.length() > 20) {
                    Toast.makeText(EditExerciseActivity.this, "Reps or Sets input is too long. It should not exceed 20 characters.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Exercise exercise;
                    if (selectedExerciseId != -1) {
                        exercise = dbHelper.getExerciseById(selectedExerciseId);
                        exercise.setName(exerciseName);
                        exercise.setReps(reps);
                        exercise.setSets(sets);
                        dbHelper.updateExercise(exercise);
                    } else {
                        exercise = new Exercise(0, exerciseName, reps, sets, routine);
                        dbHelper.addExercise(exercise);
                    }

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("exercise_updated", true);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }
        });


        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
