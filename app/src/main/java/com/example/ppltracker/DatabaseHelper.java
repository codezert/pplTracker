package com.example.ppltracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.text.ParseException;
import com.github.mikephil.charting.data.Entry;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;


// Extend SQLiteOpenHelper to create and manage the database
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database constants
    private static final String DATABASE_NAME = "PPLTracker.db"; // Database file name
    private static final int DATABASE_VERSION = 26; // Database version for upgrade management

    // Table and column names for exercises
    private static final String TABLE_EXERCISES = "exercises";
    private static final String EXERCISE_ID = "id";
    private static final String EXERCISE_NAME = "name";
    private static final String EXERCISE_REPS = "reps";
    private static final String EXERCISE_SETS = "sets";
    private static final String EXERCISE_ROUTINE = "routine";

    // Table and column names for weight entries
    private static final String TABLE_WEIGHT_ENTRIES = "weight_entries";
    private static final String WEIGHT_ID = "id";
    private static final String WEIGHT_EXERCISE_ID = "exercise_id";
    private static final String WEIGHT_WEIGHT = "weight";
    private static final String WEIGHT_REPS = "reps";
    private static final String WEIGHT_SETS = "sets";
    private static final String WEIGHT_DATE = "date"; // Date of the entry
    private static final String WEIGHT_TIMESTAMP = "timestamp"; // Timestamp for additional temporal tracking

    // Constructor to initialize the database helper object
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // onCreate is called to create the database tables when the database is created
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create exercises table
        String CREATE_EXERCISE_TABLE = "CREATE TABLE " + TABLE_EXERCISES + "("
                + EXERCISE_ID + " INTEGER PRIMARY KEY," + EXERCISE_NAME + " TEXT,"
                + EXERCISE_REPS + " TEXT," + EXERCISE_SETS + " TEXT,"
                + EXERCISE_ROUTINE + " TEXT" + ")";
        db.execSQL(CREATE_EXERCISE_TABLE); // Execute the SQL to create the table

        // SQL statement to create weight entries table with a foreign key linking to exercises table
        String CREATE_WEIGHT_ENTRY_TABLE = "CREATE TABLE " + TABLE_WEIGHT_ENTRIES + "("
                + WEIGHT_ID + " INTEGER PRIMARY KEY," + WEIGHT_EXERCISE_ID + " INTEGER,"
                + WEIGHT_WEIGHT + " REAL," + WEIGHT_REPS + " INTEGER," + WEIGHT_SETS + " INTEGER,"
                + WEIGHT_DATE + " TEXT," + WEIGHT_TIMESTAMP + " INTEGER,"
                + "FOREIGN KEY(" + WEIGHT_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + EXERCISE_ID + "))";
        db.execSQL(CREATE_WEIGHT_ENTRY_TABLE); // Execute the SQL to create the table
    }

    // onUpgrade is called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT_ENTRIES);
        onCreate(db); // Create tables again
    }

    // Method to add an exercise to the database
    public long addExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase(); // Get writable database
        ContentValues values = new ContentValues();

        // Put exercise details into ContentValues
        values.put(EXERCISE_NAME, exercise.getName());
        values.put(EXERCISE_REPS, exercise.getReps());
        values.put(EXERCISE_SETS, exercise.getSets());
        values.put(EXERCISE_ROUTINE, exercise.getRoutine());

        // Insert the values into the exercises table
        long id = db.insert(TABLE_EXERCISES, null, values);

        if (id != -1) {
            // Add a default weight entry for this exercise if insert was successful
            WeightEntry defaultWeightEntry = new WeightEntry(-1, id, 0.0, 0, 0, null, System.currentTimeMillis());
            addWeightEntry(defaultWeightEntry);
        }

        db.close(); // Close the database connection
        return id; // Return the ID of the newly inserted exercise row
    }

    // Updates the details of an existing exercise in the database.
    public void updateExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase(); // Open a writable database
        ContentValues contentValues = new ContentValues();
        // Setting new values for the exercise
        contentValues.put(EXERCISE_NAME, exercise.getName());
        contentValues.put(EXERCISE_ROUTINE, exercise.getRoutine());
        contentValues.put(EXERCISE_REPS, exercise.getReps());
        contentValues.put(EXERCISE_SETS, exercise.getSets());
        // Updating row in the exercise table
        db.update(TABLE_EXERCISES, contentValues, EXERCISE_ID + " = ?", new String[]{String.valueOf(exercise.getId())});
        db.close(); // Close the database connection
    }

    // Deletes an exercise from the database by ID.
    public boolean deleteExercise(int id) {
        SQLiteDatabase db = this.getWritableDatabase(); // Open a writable database
        // Delete associated weight entries first
        db.delete(TABLE_WEIGHT_ENTRIES, WEIGHT_EXERCISE_ID + " = ?", new String[]{String.valueOf(id)});
        // Delete the exercise entry and check if it was successful
        boolean success = db.delete(TABLE_EXERCISES, EXERCISE_ID + " = ?", new String[]{String.valueOf(id)}) > 0;
        db.close(); // Close the database
        return success;
    }

    // Retrieves a specific exercise by its ID.
    public Exercise getExerciseById(int exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase(); // Open a readable database
        Cursor cursor = db.query(TABLE_EXERCISES, new String[]{EXERCISE_ID, EXERCISE_NAME, EXERCISE_REPS, EXERCISE_SETS, EXERCISE_ROUTINE},
                EXERCISE_ID + " = ?", new String[]{String.valueOf(exerciseId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) { // Ensure the cursor is not null and move to first record
            Exercise exercise = new Exercise(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4));
            cursor.close(); // Close the cursor
            return exercise;
        }
        return null; // Return null if no exercise found
    }

    // Adds a new weight entry to the database.
    public long addWeightEntry(WeightEntry weightEntry) {
        SQLiteDatabase db = this.getWritableDatabase(); // Open a writable database
        ContentValues values = new ContentValues();
        // Set values for the weight entry
        values.put(WEIGHT_EXERCISE_ID, weightEntry.getExerciseId());
        values.put(WEIGHT_WEIGHT, weightEntry.getWeight());
        values.put(WEIGHT_REPS, weightEntry.getReps());
        values.put(WEIGHT_SETS, weightEntry.getSets());
        values.put(WEIGHT_DATE, weightEntry.getDate());
        values.put(WEIGHT_TIMESTAMP, weightEntry.getTimestamp());
        long result = db.insert(TABLE_WEIGHT_ENTRIES, null, values); // Insert the new row
        db.close(); // Close the database connection
        return result; // Return the row ID of the newly inserted row, or -1 if an error occurred
    }

    // Updates a specific weight entry in the database.
    public int updateWeightEntry(WeightEntry weightEntry) {
        SQLiteDatabase db = this.getWritableDatabase(); // Open a writable database
        ContentValues values = new ContentValues();
        // Update values for the weight entry
        values.put(WEIGHT_SETS, weightEntry.getSets());
        values.put(WEIGHT_EXERCISE_ID, weightEntry.getExerciseId());
        values.put(WEIGHT_WEIGHT, weightEntry.getWeight());
        values.put(WEIGHT_REPS, weightEntry.getReps());
        values.put(WEIGHT_DATE, weightEntry.getDate());
        int rowsAffected = db.update(TABLE_WEIGHT_ENTRIES, values, WEIGHT_ID + " = ?", new String[]{String.valueOf(weightEntry.getId())});
        db.close(); // Close the database
        return rowsAffected; // Return the number of database rows affected by the update
    }

    // Deletes a specific weight entry by its ID from the database.
    public boolean deleteWeightEntry(long id) {
        SQLiteDatabase db = this.getWritableDatabase(); // Open a writable database
        // Delete the weight entry and check if any rows were affected
        boolean isDeleted = db.delete(TABLE_WEIGHT_ENTRIES, WEIGHT_ID + "=?", new String[]{String.valueOf(id)}) > 0;
        db.close(); // Close the database
        return isDeleted;
    }

    // Retrieves all weight entries for a specific exercise, excluding the oldest entry.
    public List<WeightEntry> getWeightEntriesByExerciseId(int exerciseId) {
        List<WeightEntry> weightEntryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // Open a readable database

        // SQL query to select all entries except the oldest for a specific exercise
        String selectQuery = "SELECT * FROM " + TABLE_WEIGHT_ENTRIES + " WHERE " + WEIGHT_EXERCISE_ID + " = ? " +
                "AND " + WEIGHT_ID + " != (SELECT " + WEIGHT_ID + " FROM " + TABLE_WEIGHT_ENTRIES +
                " WHERE " + WEIGHT_EXERCISE_ID + " = ? ORDER BY " + WEIGHT_TIMESTAMP + " ASC LIMIT 1) " +
                "ORDER BY " + WEIGHT_TIMESTAMP + " DESC";

        Log.d("DatabaseHelper", "Executing query: " + selectQuery);
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(exerciseId), String.valueOf(exerciseId)}); // Execute the query

        if (cursor.moveToFirst()) {
            do {
                // Fetching values from cursor
                int id = cursor.getInt(cursor.getColumnIndex(WEIGHT_ID));
                double weight = cursor.getDouble(cursor.getColumnIndex(WEIGHT_WEIGHT));
                int reps = cursor.getInt(cursor.getColumnIndex(WEIGHT_REPS));
                int sets = cursor.getInt(cursor.getColumnIndex(WEIGHT_SETS));
                String date = cursor.getString(cursor.getColumnIndex(WEIGHT_DATE)); // Ensure WEIGHT_DATE is defined as a TEXT column
                long timestamp = cursor.getLong(cursor.getColumnIndex(WEIGHT_TIMESTAMP));

                // Creating weight entry object
                WeightEntry weightEntry = new WeightEntry(
                        id,
                        exerciseId,
                        weight,
                        reps,
                        sets,
                        date,
                        timestamp);
                weightEntryList.add(weightEntry);
            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseHelper", "No entries found for exerciseId: " + exerciseId);
        }

        cursor.close(); // Close the cursor
        db.close(); // Close the database
        return weightEntryList;
    }

    // Retrieves all exercises for a specific routine, each populated with its latest weight entry.
    public List<Exercise> getExercisesWithRepsAndSets(String routine) {
        List<Exercise> exerciseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // Open a readable database

        // SQL query to select exercises based on the routine
        String selectQuery = "SELECT * FROM " + TABLE_EXERCISES + " WHERE " + EXERCISE_ROUTINE + " = '" + routine + "'";

        Cursor cursor = db.rawQuery(selectQuery, null); // Execute the query

        if (cursor.moveToFirst()) {
            do {
                // Create exercise objects and fetch the latest weight entry for each
                Exercise exercise = new Exercise(
                        cursor.getInt(cursor.getColumnIndex(EXERCISE_ID)),
                        cursor.getString(cursor.getColumnIndex(EXERCISE_NAME)),
                        cursor.getString(cursor.getColumnIndex(EXERCISE_REPS)),
                        cursor.getString(cursor.getColumnIndex(EXERCISE_SETS)),
                        routine);

                WeightEntry latestWeightEntry = getLatestWeightEntryByExerciseId(exercise.getId());
                if (latestWeightEntry != null) {
                    exercise.setLatestWeightEntry(latestWeightEntry);
                }

                exerciseList.add(exercise);
            } while (cursor.moveToNext());
        }
        cursor.close(); // Close the cursor
        return exerciseList;
    }

    // Retrieves the latest weight entry for a specific exercise.
    public WeightEntry getLatestWeightEntryByExerciseId(int exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase(); // Open a readable database

        // SQL query to find the most recent weight entry for an exercise
        String selectQuery = "SELECT * FROM " + TABLE_WEIGHT_ENTRIES +
                " WHERE " + WEIGHT_EXERCISE_ID + " = " + exerciseId +
                " ORDER BY " + WEIGHT_DATE + " DESC LIMIT 1";

        Cursor cursor = db.rawQuery(selectQuery, null); // Execute the query

        if (cursor.moveToFirst()) {
            // Fetching values from cursor
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(WEIGHT_ID));
            double weight = cursor.getDouble(cursor.getColumnIndexOrThrow(WEIGHT_WEIGHT));
            int reps = cursor.getInt(cursor.getColumnIndexOrThrow(WEIGHT_REPS));
            int sets = cursor.getInt(cursor.getColumnIndexOrThrow(WEIGHT_SETS));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(WEIGHT_DATE)); // Ensure WEIGHT_DATE is defined as a TEXT column
            long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(WEIGHT_TIMESTAMP));

            // Creating weight entry object
            WeightEntry weightEntry = new WeightEntry(id,exerciseId,weight,reps,sets,date,timestamp);
            cursor.close(); // Close the cursor
            return weightEntry; // Return the found weight entry
        } else {
            cursor.close(); // Ensure the cursor is closed even if no data was found
            return null; // Return null if no entries were found
        }
    }


    // Checks if the exercises table is empty.
    public boolean isExercisesEmpty() {
        boolean isEmpty;
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_EXERCISES; // SQL query to count entries in the exercises table
        SQLiteDatabase db = this.getReadableDatabase(); // Open database in readable mode
        Cursor cursor = db.rawQuery(countQuery, null); // Execute the query

        cursor.moveToFirst(); // Move to the first row of the result
        isEmpty = cursor.getInt(0) == 0; // Check if the count is zero indicating the table is empty

        cursor.close(); // Close the cursor
        return isEmpty; // Return whether the table is empty
    }

    // Retrieves the most recent weight entry for a specific exercise.
    public WeightEntry getMostRecentWeightEntry(int exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase(); // Open database in readable mode
        // Query to fetch the most recent weight entry for the specified exercise
        Cursor cursor = db.query(
                TABLE_WEIGHT_ENTRIES, // Table to query
                new String[]{WEIGHT_ID, WEIGHT_EXERCISE_ID, WEIGHT_WEIGHT, WEIGHT_REPS, WEIGHT_SETS, WEIGHT_DATE, WEIGHT_TIMESTAMP}, // Columns to return
                WEIGHT_EXERCISE_ID + " = ?", // Selection criteria
                new String[]{String.valueOf(exerciseId)}, // Selection arguments
                null, // Group by clause
                null, // Having clause
                WEIGHT_TIMESTAMP + " DESC", // Order by clause, descending by timestamp
                "1" // Limit to 1 record
        );

        if (cursor.moveToFirst()) { // Check if the query returned any results
            WeightEntry weightEntry = new WeightEntry(
                    cursor.getInt(cursor.getColumnIndex(WEIGHT_ID)),
                    exerciseId,
                    cursor.getDouble(cursor.getColumnIndex(WEIGHT_WEIGHT)),
                    cursor.getInt(cursor.getColumnIndex(WEIGHT_REPS)),
                    cursor.getInt(cursor.getColumnIndex(WEIGHT_SETS)),
                    cursor.getString(cursor.getColumnIndex(WEIGHT_DATE)),
                    cursor.getLong(cursor.getColumnIndex(WEIGHT_TIMESTAMP)));
            cursor.close(); // Close the cursor
            return weightEntry; // Return the found weight entry
        } else {
            cursor.close(); // Ensure the cursor is closed even if no data was found
            return null; // Return null if no entries were found
        }
    }

    // Retrieves the most recent date for a specific routine across all associated weight entries.
    public String getMostRecentDateForRoutine(String routine) {
        SQLiteDatabase db = this.getReadableDatabase(); // Open database in readable mode
        // SQL query to find the latest date for a specific routine using an inner join between exercises and weight entries
        String query = "SELECT MAX(W." + WEIGHT_DATE + ") as latest_date " +
                "FROM " + TABLE_EXERCISES + " E " +
                "INNER JOIN " + TABLE_WEIGHT_ENTRIES + " W ON E." + EXERCISE_ID + " = W." + WEIGHT_EXERCISE_ID + " " +
                "WHERE E." + EXERCISE_ROUTINE + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{routine}); // Execute the query with the routine as the argument

        String latestDate = null;
        if (cursor.moveToFirst()) { // Check if the query returned any results
            latestDate = cursor.getString(cursor.getColumnIndex("latest_date")); // Retrieve the latest date
        }

        cursor.close(); // Close the cursor
        return latestDate; // Return the latest date found, or null if no entries
    }

    public void createDefaultWorkouts() {
        addExercise(new Exercise(0, "Bench Press", "5", "5", "Push"));
        addExercise(new Exercise(0, "Overhead Press", "5", "5", "Push"));
        addExercise(new Exercise(0, "Incline Dumbbell Press", "3", "8", "Push"));
        addExercise(new Exercise(0, "Tricep Dips", "3", "10", "Push"));
        addExercise(new Exercise(0, "Close Grip Bench Press", "3", "10", "Push"));
        addExercise(new Exercise(0, "Push Ups", "3", "10", "Push"));

        // Pull day exercises
        addExercise(new Exercise(0, "Deadlift", "5", "5", "Pull"));
        addExercise(new Exercise(0, "Pull Ups", "3", "10", "Pull"));
        addExercise(new Exercise(0, "Barbell Rows", "3", "10", "Pull"));
        addExercise(new Exercise(0, "Face Pulls", "3", "10", "Pull"));
        addExercise(new Exercise(0, "Bicep Curls", "3", "10", "Pull"));
        addExercise(new Exercise(0, "Hammer Curls", "3", "10", "Pull"));

        // Legs day exercises
        addExercise(new Exercise(0, "Squats", "5", "5", "Legs"));
        addExercise(new Exercise(0, "Leg Press", "3", "10", "Legs"));
        addExercise(new Exercise(0, "Lunges", "3", "10", "Legs"));
        addExercise(new Exercise(0, "Leg Curls", "3", "10", "Legs"));
        addExercise(new Exercise(0, "Calf Raises", "3", "10", "Legs"));
        addExercise(new Exercise(0, "Abdominal Crunches", "3", "10", "Legs"));
    }
    //****************************
    // USED FOR STATISTICS
    //****************************
    public List<Entry> getRoutineVolume(String routine) {
        List<Entry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // Open database in readable mode

        // SQL query to calculate total volume of weights lifted per date for a specific routine
        String sql = "SELECT date, SUM(weight * reps * sets) as TotalVolume " +
                "FROM weight_entries " +
                "WHERE exercise_id IN (SELECT id FROM exercises WHERE routine = ?) " +
                "AND date IS NOT NULL " +
                "GROUP BY date " +
                "ORDER BY date(date) ASC";

        Log.d("DatabaseHelper", "Executing query: " + sql + " with routine: " + routine);
        Cursor cursor = db.rawQuery(sql, new String[]{routine}); // Execute the query with the routine as the argument

        if (cursor.moveToFirst()) {
            do {
                String dateStr = cursor.getString(0);
                int totalVolume = cursor.getInt(1);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    Date date = sdf.parse(dateStr);
                    if (date != null) {
                        long millis = date.getTime();
                        entries.add(new Entry(millis, totalVolume));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseHelper", "No entries found for routine: " + routine);
        }

        cursor.close(); // Close the cursor
        db.close(); // Close the database
        return entries;
    }


    public List<Entry> getTotalWeight(String routine) {
        List<Entry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // Open database in readable mode

        // SQL query to calculate total weight lifted per date for a specific routine
        String sql = "SELECT date, SUM(weight) as TotalWeight " +
                "FROM weight_entries " +
                "WHERE exercise_id IN (SELECT id FROM exercises WHERE routine = ?) " +
                "AND date IS NOT NULL " +
                "GROUP BY date " +
                "ORDER BY date(date) ASC";

        Log.d("DatabaseHelper", "Executing query: " + sql + " with routine: " + routine);
        Cursor cursor = db.rawQuery(sql, new String[]{routine}); // Execute the query with the routine as the argument

        if (cursor.moveToFirst()) {
            do {
                String dateStr = cursor.getString(0);
                int totalWeight = cursor.getInt(1);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    Date date = sdf.parse(dateStr);
                    if (date != null) {
                        long millis = date.getTime();
                        entries.add(new Entry(millis, totalWeight));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseHelper", "No entries found for routine: " + routine);
        }

        cursor.close(); // Close the cursor
        db.close(); // Close the database
        return entries;
    }

    public float getImprovementRate(String routine, int days) {
        SQLiteDatabase db = this.getReadableDatabase(); // Open database in readable mode

        // Calculate the date threshold for the given timeframe
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        long dateThreshold = calendar.getTimeInMillis();

        // SQL query to fetch weight entries within the given timeframe, ignoring the first entry per exercise
        String sql = "SELECT exercise_id, weight, reps, sets, timestamp FROM " +
                "(SELECT exercise_id, weight, reps, sets, timestamp, ROW_NUMBER() OVER (PARTITION BY exercise_id ORDER BY timestamp ASC) row_num " +
                "FROM " + TABLE_WEIGHT_ENTRIES +
                " WHERE exercise_id IN (SELECT id FROM " + TABLE_EXERCISES + " WHERE routine = ?) " +
                " AND timestamp >= ?) " +
                " WHERE row_num > 1";

        Log.d("DatabaseHelper", "Executing query: " + sql + " with routine: " + routine + " and dateThreshold: " + dateThreshold);
        Cursor cursor = db.rawQuery(sql, new String[]{routine, String.valueOf(dateThreshold)}); // Execute the query with the routine and dateThreshold as arguments

        float totalImprovement = 0;
        int improvementCount = 0;

        Map<Integer, Double> previousVolumes = new HashMap<>();

        // Process the cursor results
        if (cursor.moveToFirst()) {
            do {
                int exerciseId = cursor.getInt(cursor.getColumnIndex(WEIGHT_EXERCISE_ID));
                double weight = cursor.getDouble(cursor.getColumnIndex(WEIGHT_WEIGHT));
                int reps = cursor.getInt(cursor.getColumnIndex(WEIGHT_REPS));
                int sets = cursor.getInt(cursor.getColumnIndex(WEIGHT_SETS));

                double currentVolume = weight * reps * sets;

                if (previousVolumes.containsKey(exerciseId)) {
                    double previousVolume = previousVolumes.get(exerciseId);
                    if (previousVolume > 0) {
                        double improvement = ((currentVolume - previousVolume) / previousVolume) * 100;
                        totalImprovement += improvement;
                        improvementCount++;
                    }
                }

                previousVolumes.put(exerciseId, currentVolume);
            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseHelper", "No entries found for routine: " + routine);
        }

        cursor.close(); // Close the cursor
        db.close(); // Close the database

        if (improvementCount > 0) {
            return totalImprovement / improvementCount; // Return the average improvement rate
        } else {
            return 0; // Return 0 if no valid entries were found
        }
    }
    public int getTotalWorkouts(String routine, int days) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Calculate the date threshold for the given timeframe
        long currentTime = System.currentTimeMillis();
        long dateThreshold = currentTime - TimeUnit.DAYS.toMillis(days);

        // SQL query to count distinct workout dates within the timeframe
        String sql = "SELECT COUNT(DISTINCT date) AS workoutCount " +
                "FROM " + TABLE_WEIGHT_ENTRIES + " " +
                "WHERE exercise_id IN (SELECT id FROM " + TABLE_EXERCISES + " WHERE routine = ?) " +
                "AND timestamp >= ?";

        Cursor cursor = db.rawQuery(sql, new String[]{routine, String.valueOf(dateThreshold)});
        int workoutCount = 0;

        if (cursor.moveToFirst()) {
            workoutCount = cursor.getInt(cursor.getColumnIndex("workoutCount"));
        }

        cursor.close();
        db.close();
        return workoutCount;
    }




}

