package com.example.ppltracker;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import android.content.DialogInterface;
import android.app.AlertDialog;

public class ExerciseListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExerciseAdapter exerciseAdapter;
    private List<Exercise> exerciseList;
    private DatabaseHelper dbHelper;
    private String routine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);

        dbHelper = new DatabaseHelper(this);
        routine = getIntent().getStringExtra("routine");

        initRecyclerView();
        initButtons();
        updateExerciseList();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initButtons() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ExerciseListActivity.this, EditExerciseActivity.class);
            intent.putExtra("routine", routine);
            startActivityForResult(intent, 1);
        });

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        Button btnRoutineStats = findViewById(R.id.btnRoutineStats);
        btnRoutineStats.setOnClickListener(v -> {
            Intent intent = new Intent(ExerciseListActivity.this, RoutineStatisticsActivity.class);
            intent.putExtra("routine", routine);
            startActivity(intent);
        });
    }

    private void updateExerciseList() {
        exerciseList = dbHelper.getExercisesWithRepsAndSets(routine);
        exerciseAdapter = new ExerciseAdapter(exerciseList, new ExerciseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Exercise exercise) {
                Intent intent = new Intent(ExerciseListActivity.this, ExerciseDetailsActivity.class);
                intent.putExtra("exerciseId", exercise.getId());
                startActivity(intent);
            }

            @Override
            public void onItemDelete(final Exercise exercise) {
                showDeleteConfirmationDialog(exercise);
            }

            @Override
            public void onItemEdit(Exercise exercise) {
                Intent intent = new Intent(ExerciseListActivity.this, EditExerciseActivity.class);
                intent.putExtra("selected_exercise_id", exercise.getId());
                startActivityForResult(intent, 2);
            }
        });
        recyclerView.setAdapter(exerciseAdapter);
    }

    private void showDeleteConfirmationDialog(final Exercise exercise) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Exercise")
                .setMessage("Are you sure you want to delete this exercise? All information will be lost.")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    if (dbHelper.deleteExercise(exercise.getId())) {
                        Toast.makeText(this, "Exercise deleted", Toast.LENGTH_SHORT).show();
                        updateExerciseList();
                    } else {
                        Toast.makeText(this, "Failed to delete exercise", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, requestCode == 1 ? "Exercise added" : "Exercise updated", Toast.LENGTH_SHORT).show();
            updateExerciseList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (exerciseAdapter != null) {
            exerciseAdapter.clearSelectedItem();
        }
    }
}
