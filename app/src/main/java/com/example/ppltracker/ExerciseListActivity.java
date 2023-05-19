package com.example.ppltracker;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import android.widget.Toast;
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

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateExerciseList();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExerciseListActivity.this, EditExerciseActivity.class);
                intent.putExtra("routine", routine);
                startActivityForResult(intent, 1);
            }
        });

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
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
            // Handle delete event
            @Override
            public void onItemDelete(final Exercise exercise) {
                new AlertDialog.Builder(ExerciseListActivity.this)
                        .setTitle("Delete Exercise")
                        .setMessage("Are you sure you want to delete this exercise? All information will be lost.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (dbHelper.deleteExercise(exercise.getId())) {
                                    Toast.makeText(ExerciseListActivity.this, "Exercise deleted", Toast.LENGTH_SHORT).show();
                                    updateExerciseList();
                                } else {
                                    Toast.makeText(ExerciseListActivity.this, "Failed to delete exercise", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            // Handle edit event
            @Override
            public void onItemEdit(Exercise exercise) {
                Intent intent = new Intent(ExerciseListActivity.this, EditExerciseActivity.class);
                intent.putExtra("selected_exercise_id", exercise.getId());
                startActivityForResult(intent, 2);
            }
        });
        recyclerView.setAdapter(exerciseAdapter);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Toast.makeText(ExerciseListActivity.this, "Exercise added", Toast.LENGTH_SHORT).show();
            } else if (requestCode == 2) {
                Toast.makeText(ExerciseListActivity.this, "Exercise updated", Toast.LENGTH_SHORT).show();
            }
            updateExerciseList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(exerciseAdapter != null) {
            exerciseAdapter.clearSelectedItem(); // Clear the selected item when the activity resumes
        }
    }

}
