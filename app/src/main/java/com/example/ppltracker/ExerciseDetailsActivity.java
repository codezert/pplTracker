package com.example.ppltracker;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.Toast;
import android.widget.TextView;
import android.content.DialogInterface;
import android.app.AlertDialog;
import java.util.ArrayList;

public class ExerciseDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WeightEntryAdapter weightEntryAdapter;
    private List<WeightEntry> weightEntryList;
    private DatabaseHelper dbHelper;
    private int exerciseId;
    private Exercise exercise;
    private WeightEntry selectedWeightEntry = null;
    private Button btnDeleteWeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_details);

        TextView txtExerciseName = findViewById(R.id.txtExerciseName);
        final EditText edtReps = findViewById(R.id.edtReps);
        final EditText edtSets = findViewById(R.id.edtSets);
        final EditText edtWeight = findViewById(R.id.edtWeight);
        recyclerView = findViewById(R.id.recyclerViewWeightEntries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Button btnAddWeight = findViewById(R.id.btnSaveWeight);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnDeleteWeight = findViewById(R.id.btnDeleteWeight);
        Button btnAsUsual = findViewById(R.id.btn_as_usual);
        Button btnIncreaseWeight = findViewById(R.id.btn_increase);
        dbHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        exerciseId = intent.getIntExtra("exerciseId", -1);

        exercise = dbHelper.getExerciseById(exerciseId);
        txtExerciseName.setText(exercise.getName());
        // Set initial visibility for Delete button
        btnDeleteWeight.setVisibility(View.GONE);

// Initialize the adapter with an empty list first
        weightEntryAdapter = new WeightEntryAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(weightEntryAdapter);

        updateWeightEntryList();
// Set the click listener
        weightEntryAdapter.setOnItemClickListener(new WeightEntryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(WeightEntry weightEntry) {
                selectedWeightEntry = weightEntry;
                edtWeight.setText(String.valueOf(weightEntry.getWeight()));
                edtSets.setText(String.valueOf(weightEntry.getSets()));
                edtReps.setText(String.valueOf(weightEntry.getReps()));
                btnAddWeight.setText("Update Weight");
                btnAsUsual.setVisibility(View.GONE);  // HIDE "As Usual" button
                btnDeleteWeight.setVisibility(View.VISIBLE);  // Show Delete button
            }
        });

// Initialize the visibility for the Increase button
        btnIncreaseWeight.setVisibility(View.GONE);

        btnAsUsual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeightEntry recentWeightEntry = dbHelper.getMostRecentWeightEntry(exerciseId);
                if (recentWeightEntry != null) {
                    edtWeight.setText(String.format(Locale.getDefault(), "%.2f", recentWeightEntry.getWeight()));
                    edtSets.setText(String.valueOf(recentWeightEntry.getSets()));
                    edtReps.setText(String.valueOf(recentWeightEntry.getReps()));
                    btnAsUsual.setVisibility(View.GONE);  // Hide "As Usual" button
                    btnIncreaseWeight.setVisibility(View.VISIBLE);  // Show "5%" button
                } else {
                    Toast.makeText(ExerciseDetailsActivity.this, "No previous weight entries found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnIncreaseWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String weightText = edtWeight.getText().toString();
                if (!weightText.isEmpty()) {
                    double weight = Double.parseDouble(weightText);
                    weight = weight*1.05; // Increase by 5%
                    edtWeight.setText(String.format(Locale.getDefault(), "%.2f", weight));
                } else {
                    Toast.makeText(ExerciseDetailsActivity.this, "Please enter weight", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnAddWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String weightText = edtWeight.getText().toString();
                if (weightText.isEmpty()) {
                    Toast.makeText(ExerciseDetailsActivity.this, "Please enter weight", Toast.LENGTH_SHORT).show();
                    return;
                }
                String setsText = edtSets.getText().toString();
                if (setsText.isEmpty()) {
                    Toast.makeText(ExerciseDetailsActivity.this, "Please enter sets", Toast.LENGTH_SHORT).show();
                    return;
                }
                String repsText = edtReps.getText().toString();
                if (repsText.isEmpty()) {
                    Toast.makeText(ExerciseDetailsActivity.this, "Please enter reps", Toast.LENGTH_SHORT).show();
                    return;
                }
                if ((weightText.length() > 8) || (repsText.length() > 8) || (setsText.length() > 5)) {
                    Toast.makeText(ExerciseDetailsActivity.this, "Values are too large.", Toast.LENGTH_SHORT).show();
                    return;
                }
                double weight = (double)Math.round(Float.parseFloat(weightText) * 100) / 100.0;
                int sets = Integer.parseInt(setsText);
                int reps = Integer.parseInt(repsText);
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                if (selectedWeightEntry == null) {
                    WeightEntry weightEntry = new WeightEntry(-1, exerciseId, weight, reps, sets, date, System.currentTimeMillis());
                    long newWeightEntryId = dbHelper.addWeightEntry(weightEntry);
                    if (newWeightEntryId > 0) {
                        weightEntry.setId((int) newWeightEntryId);
                        edtWeight.setText("");
                        edtSets.setText("");
                        edtReps.setText("");
                        updateWeightEntryList();
                        selectedWeightEntry = null;
                        btnAddWeight.setText("Add Weight");
                        btnDeleteWeight.setVisibility(View.GONE);  // Hide Delete button
                    } else {
                        // handle failure to insert new WeightEntry
                    }
                } else {
                    selectedWeightEntry.setWeight((double) weight);
                    selectedWeightEntry.setSets(sets);
                    selectedWeightEntry.setReps(reps);
                    dbHelper.updateWeightEntry(selectedWeightEntry);
                    selectedWeightEntry = null;
                    edtWeight.setText("");
                    edtSets.setText("");
                    edtReps.setText("");
                    btnAddWeight.setText("Add Weight");
                    btnDeleteWeight.setVisibility(View.GONE);  // Hide Delete button
                    }
                updateWeightEntryList();
                btnAsUsual.setVisibility(View.VISIBLE);  // Show "As Usual" button
                btnIncreaseWeight.setVisibility(View.GONE);  // Hide "5%" button
            }
        });


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedWeightEntry == null) {
                    finish();
                }else {
                    selectedWeightEntry = null;
                    edtWeight.setText("");
                    edtSets.setText("");
                    edtReps.setText("");
                    btnAddWeight.setText("Add Weight");
                    btnDeleteWeight.setVisibility(View.GONE);
                    weightEntryAdapter.deselectCurrentItem();  // Assuming weightEntryAdapter is your adapter instance
                }
            }
        });

        btnDeleteWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedWeightEntry != null) {
                    new AlertDialog.Builder(ExerciseDetailsActivity.this)
                            .setTitle("Delete entry")
                            .setMessage("Are you sure you want to delete this entry? This action cannot be undone.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Continue with delete operation
                                    if (dbHelper.deleteWeightEntry(selectedWeightEntry.getId())) {
                                        Toast.makeText(ExerciseDetailsActivity.this, "Weight entry deleted", Toast.LENGTH_SHORT).show();
                                        selectedWeightEntry = null;
                                        updateWeightEntryList();
                                        edtWeight.setText("");
                                        edtSets.setText("");
                                        edtReps.setText("");
                                        btnAddWeight.setText("Add Weight");

                                        btnDeleteWeight.setVisibility(View.GONE);  // Hide Delete button
                                    } else {
                                        Toast.makeText(ExerciseDetailsActivity.this, "Failed to delete weight entry", Toast.LENGTH_SHORT).show();
                                    }
                                    btnAsUsual.setVisibility(View.VISIBLE);  // Show "As Usual" button
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Clear selection if deletion is cancelled
                                    selectedWeightEntry = null;
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    Toast.makeText(ExerciseDetailsActivity.this, "No weight entry selected", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void updateWeightEntryList() {
        weightEntryList = dbHelper.getWeightEntriesByExerciseId(exerciseId);

        // Update the data in the adapter
        weightEntryAdapter.setWeightEntryList(weightEntryList);
        // Notify the adapter of the changes
        weightEntryAdapter.notifyDataSetChanged();

        Button btnAddWeight = findViewById(R.id.btnSaveWeight);
        Button btnAsUsual = findViewById(R.id.btn_as_usual);
        Button btnIncreaseWeight = findViewById(R.id.btn_increase);

        if (weightEntryList.size() == 0) {
            // If there are no weights, show 'Add Weight' button and hide others
            btnAddWeight.setText("Add Weight");
            btnAsUsual.setVisibility(View.GONE);
            btnIncreaseWeight.setVisibility(View.GONE);
        } else {
            // If there is at least one weight, show 'Save Weight' and 'As Usual' buttons
            btnAddWeight.setText("Save Weight");
            btnAsUsual.setVisibility(View.VISIBLE);
            btnIncreaseWeight.setVisibility(View.VISIBLE);  // Optional depending on your requirements
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data.getBooleanExtra("deleted", false)) {
                Toast.makeText(this, "Exercise deleted", Toast.LENGTH_SHORT).show();
            }
            updateWeightEntryList();
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Toast.makeText(this, "Exercise updated", Toast.LENGTH_SHORT).show();
            updateWeightEntryList();
        }
    }


}



