package com.example.ppltracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExerciseDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WeightEntryAdapter weightEntryAdapter;
    private List<WeightEntry> weightEntryList;
    private DatabaseHelper dbHelper;
    private int exerciseId;
    private Exercise exercise;
    private WeightEntry selectedWeightEntry = null;

    private TextView txtExerciseName;
    private EditText edtReps, edtSets, edtWeight;
    private Button btnAddWeight, btnBack, btnDeleteWeight, btnAsUsual, btnIncreaseWeight, btnRestTimer;

    private CountDownTimer restTimer;
    private boolean isTimerRunning = false;
    private final long defaultRestTime = 60000; // 60 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_details);

        initializeViews();
        initializeDatabase();
        setupRecyclerView();
        setupButtons();

        loadExerciseDetails();
        updateWeightEntryList();
    }

    private void initializeViews() {
        txtExerciseName = findViewById(R.id.txtExerciseName);
        edtReps = findViewById(R.id.edtReps);
        edtSets = findViewById(R.id.edtSets);
        edtWeight = findViewById(R.id.edtWeight);
        recyclerView = findViewById(R.id.recyclerViewWeightEntries);
        btnAddWeight = findViewById(R.id.btnSaveWeight);
        btnBack = findViewById(R.id.btnBack);
        btnDeleteWeight = findViewById(R.id.btnDeleteWeight);
        btnAsUsual = findViewById(R.id.btn_as_usual);
        btnIncreaseWeight = findViewById(R.id.btn_increase);
        btnRestTimer = findViewById(R.id.btnRestTimer);
    }

    private void initializeDatabase() {
        dbHelper = new DatabaseHelper(this);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        weightEntryAdapter = new WeightEntryAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(weightEntryAdapter);
        weightEntryAdapter.setOnItemClickListener(weightEntry -> populateWeightEntryDetails(weightEntry));
    }

    private void setupButtons() {
        btnAddWeight.setOnClickListener(v -> saveOrUpdateWeightEntry());
        btnBack.setOnClickListener(v -> handleBackButton());
        btnDeleteWeight.setOnClickListener(v -> confirmDeleteWeightEntry());
        btnAsUsual.setOnClickListener(v -> fillRecentWeightEntry());
        btnIncreaseWeight.setOnClickListener(v -> increaseWeightBy5Percent());
        btnRestTimer.setOnClickListener(v -> handleRestTimer());
    }

    private void loadExerciseDetails() {
        Intent intent = getIntent();
        exerciseId = intent.getIntExtra("exerciseId", -1);
        exercise = dbHelper.getExerciseById(exerciseId);
        txtExerciseName.setText(exercise.getName());
        btnDeleteWeight.setVisibility(View.GONE);
        btnIncreaseWeight.setVisibility(View.GONE);
    }

    private void populateWeightEntryDetails(WeightEntry weightEntry) {
        selectedWeightEntry = weightEntry;
        edtWeight.setText(String.valueOf(weightEntry.getWeight()));
        edtSets.setText(String.valueOf(weightEntry.getSets()));
        edtReps.setText(String.valueOf(weightEntry.getReps()));
        btnAddWeight.setText("Update Weight");
        btnAsUsual.setVisibility(View.GONE);
        btnIncreaseWeight.setVisibility(View.GONE);
        btnDeleteWeight.setVisibility(View.VISIBLE);
    }

    private void fillRecentWeightEntry() {
        WeightEntry recentWeightEntry = dbHelper.getMostRecentWeightEntry(exerciseId);
        if (recentWeightEntry != null) {
            edtWeight.setText(String.format(Locale.US, "%.2f", recentWeightEntry.getWeight()));
            edtSets.setText(String.valueOf(recentWeightEntry.getSets()));
            edtReps.setText(String.valueOf(recentWeightEntry.getReps()));
            btnAsUsual.setVisibility(View.GONE);
            btnIncreaseWeight.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "No previous weight entries found", Toast.LENGTH_SHORT).show();
        }
    }

    private void increaseWeightBy5Percent() {
        String weightText = edtWeight.getText().toString();
        if (!weightText.isEmpty()) {
            double weight = Double.parseDouble(weightText) * 1.05;
            edtWeight.setText(String.format(Locale.US, "%.2f", weight));
        } else {
            Toast.makeText(this, "Please enter weight", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveOrUpdateWeightEntry() {
        if (validateInputs()) {
            double weight = (double) Math.round(Float.parseFloat(edtWeight.getText().toString()) * 100) / 100.0;
            int sets = Integer.parseInt(edtSets.getText().toString());
            int reps = Integer.parseInt(edtReps.getText().toString());
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            if (selectedWeightEntry == null) {
                addWeightEntry(weight, sets, reps, date);
            } else {
                updateWeightEntry(weight, sets, reps);
            }

            clearInputs();
            resetButtons();
            updateWeightEntryList();
        }
    }

    private boolean validateInputs() {
        if (edtWeight.getText().toString().isEmpty() || edtSets.getText().toString().isEmpty() || edtReps.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtWeight.getText().toString().length() > 8 || edtSets.getText().toString().length() > 5 || edtReps.getText().toString().length() > 8) {
            Toast.makeText(this, "Values are too large", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addWeightEntry(double weight, int sets, int reps, String date) {
        WeightEntry weightEntry = new WeightEntry(-1, exerciseId, weight, reps, sets, date, System.currentTimeMillis());
        long newWeightEntryId = dbHelper.addWeightEntry(weightEntry);
        if (newWeightEntryId > 0) {
            weightEntry.setId((int) newWeightEntryId);
            Toast.makeText(this, "Weight entry added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to add weight entry", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateWeightEntry(double weight, int sets, int reps) {
        selectedWeightEntry.setWeight(weight);
        selectedWeightEntry.setSets(sets);
        selectedWeightEntry.setReps(reps);
        dbHelper.updateWeightEntry(selectedWeightEntry);
        Toast.makeText(this, "Weight entry updated", Toast.LENGTH_SHORT).show();
    }

    private void clearInputs() {
        edtWeight.setText("");
        edtSets.setText("");
        edtReps.setText("");
        selectedWeightEntry = null;
    }

    private void resetButtons() {
        btnAddWeight.setText("Add Weight");
        btnDeleteWeight.setVisibility(View.GONE);
        btnAsUsual.setVisibility(View.VISIBLE);
        btnIncreaseWeight.setVisibility(View.GONE);
    }

    private void handleBackButton() {
        if (selectedWeightEntry == null) {
            finish();
        } else {
            clearInputs();
            resetButtons();
            weightEntryAdapter.deselectCurrentItem();
        }
    }

    private void confirmDeleteWeightEntry() {
        if (selectedWeightEntry != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete entry")
                    .setMessage("Are you sure you want to delete this entry? This action cannot be undone.")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteWeightEntry())
                    .setNegativeButton(android.R.string.no, (dialog, which) -> selectedWeightEntry = null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            Toast.makeText(this, "No weight entry selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteWeightEntry() {
        if (dbHelper.deleteWeightEntry(selectedWeightEntry.getId())) {
            Toast.makeText(this, "Weight entry deleted", Toast.LENGTH_SHORT).show();
            clearInputs();
            resetButtons();
            updateWeightEntryList();
        } else {
            Toast.makeText(this, "Failed to delete weight entry", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRestTimer() {
        if (isTimerRunning) {
            restTimer.cancel();
            isTimerRunning = false;
            btnRestTimer.setText("Start Rest Timer");
            Toast.makeText(this, "Rest timer stopped", Toast.LENGTH_SHORT).show();
        } else {
            startRestTimer(defaultRestTime);
        }
    }

    private void startRestTimer(long duration) {
        restTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnRestTimer.setText(String.format(Locale.getDefault(), "Cancel Rest Timer (%d)", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                try {
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(500); // Vibrate for 500 milliseconds
                    }
                    Toast.makeText(ExerciseDetailsActivity.this, "Rest time over", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    btnRestTimer.setText("Start Rest Timer");
                    isTimerRunning = false;
                }
            }
        }.start();
        isTimerRunning = true;
        Toast.makeText(this, "Rest timer started", Toast.LENGTH_SHORT).show();
    }

    private void updateWeightEntryList() {
        weightEntryList = dbHelper.getWeightEntriesByExerciseId(exerciseId);
        weightEntryAdapter.setWeightEntryList(weightEntryList);
        weightEntryAdapter.notifyDataSetChanged();
        weightEntryAdapter.deselectCurrentItem();

        if (weightEntryList.isEmpty()) {
            btnAsUsual.setVisibility(View.GONE);
            btnIncreaseWeight.setVisibility(View.GONE);
        } else {
            btnAsUsual.setVisibility(View.VISIBLE);
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
