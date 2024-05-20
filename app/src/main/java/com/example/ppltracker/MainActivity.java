// Define the package for the application.
package com.example.ppltracker;

// Import necessary Android and Java libraries.
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

// Main activity class that inherits from AppCompatActivity, which provides compatibility features.
public class MainActivity extends AppCompatActivity {

    // Declare private variables for buttons and text views.
    private Button btnPush, btnPull, btnLegs;
    private DatabaseHelper dbHelper;
    private TextView tvPushDate, tvPullDate, tvLegsDate;

    // onCreate method is called when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the content view to your layout.

        // Link the Java objects with XML elements by their ID.
        btnPush = findViewById(R.id.btnPush);
        btnPull = findViewById(R.id.btnPull);
        btnLegs = findViewById(R.id.btnLegs);
        tvPushDate = findViewById(R.id.tvPushDate);
        tvPullDate = findViewById(R.id.tvPullDate);
        tvLegsDate = findViewById(R.id.tvLegsDate);

        // Set onClick listeners for the Push button to handle click events.
        btnPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExerciseListActivity.class);
                intent.putExtra("routine", "Push"); // Pass "Push" as extra info to intent.
                startActivity(intent); // Start the ExerciseListActivity.
            }
        });

        // Set onClick listeners for the Pull button.
        btnPull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExerciseListActivity.class);
                intent.putExtra("routine", "Pull"); // Pass "Pull" as extra info to intent.
                startActivity(intent); // Start the ExerciseListActivity.
            }
        });

        // Set onClick listeners for the Legs button.
        btnLegs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExerciseListActivity.class);
                intent.putExtra("routine", "Legs"); // Pass "Legs" as extra info to intent.
                startActivity(intent); // Start the ExerciseListActivity.
            }
        });

        // Initialize the database helper object.
        dbHelper = new DatabaseHelper(this);

        // Check if the exercises database is empty, and populate it if necessary.
        if (dbHelper.isExercisesEmpty()) {
            dbHelper.createDefaultWorkouts();
        }
    }

    // Method to update the dates displayed for each routine.
    private void updateRoutineDates() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Fetch the most recent dates from the database for each routine.
        String pushDate = dbHelper.getMostRecentDateForRoutine("Push");
        String pullDate = dbHelper.getMostRecentDateForRoutine("Pull");
        String legsDate = dbHelper.getMostRecentDateForRoutine("Legs");

        // Update the TextViews for each routine date. Display "No entries yet" if no date is found.
        tvPushDate.setText(pushDate != null ? pushDate : "No entries yet");
        tvPullDate.setText(pullDate != null ? pullDate : "No entries yet");
        tvLegsDate.setText(legsDate != null ? legsDate : "No entries yet");
    }

    // onResume is called when the activity is about to start interacting with the user.
    @Override
    protected void onResume() {
        super.onResume();
        updateRoutineDates(); // Update the routine dates whenever the activity resumes.
    }
}
