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

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PPLTracker.db";
    private static final int DATABASE_VERSION = 25;

    private static final String TABLE_EXERCISES = "exercises";
    private static final String EXERCISE_ID = "id";
    private static final String EXERCISE_NAME = "name";
    private static final String EXERCISE_REPS = "reps";
    private static final String EXERCISE_SETS = "sets";
    private static final String EXERCISE_ROUTINE = "routine";

    private static final String TABLE_WEIGHT_ENTRIES = "weight_entries";
    private static final String WEIGHT_ID = "id";
    private static final String WEIGHT_EXERCISE_ID = "exercise_id";
    private static final String WEIGHT_WEIGHT = "weight";
    private static final String WEIGHT_REPS = "reps";
    private static final String WEIGHT_DATE = "date";

    public static final String WEIGHT_ENTRY_TABLE = "weight_entries";
    private static final String WEIGHT_TIMESTAMP = "timestamp";
    private static final String WEIGHT_SETS = "sets";
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EXERCISE_TABLE = "CREATE TABLE " + TABLE_EXERCISES + "("
                + EXERCISE_ID + " INTEGER PRIMARY KEY," + EXERCISE_NAME + " TEXT,"
                + EXERCISE_REPS + " TEXT," + EXERCISE_SETS + " TEXT,"
                + EXERCISE_ROUTINE + " TEXT" + ")";
        db.execSQL(CREATE_EXERCISE_TABLE);

        String CREATE_WEIGHT_ENTRY_TABLE = "CREATE TABLE " + TABLE_WEIGHT_ENTRIES + "("
                + WEIGHT_ID + " INTEGER PRIMARY KEY," + WEIGHT_EXERCISE_ID + " INTEGER,"
                + WEIGHT_WEIGHT + " REAL," + WEIGHT_REPS + " INTEGER," + WEIGHT_SETS + " INTEGER,"
                + WEIGHT_DATE + " TEXT," + WEIGHT_TIMESTAMP + " INTEGER,"
                + "FOREIGN KEY(" + WEIGHT_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + EXERCISE_ID + "))";
        db.execSQL(CREATE_WEIGHT_ENTRY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT_ENTRIES);
        onCreate(db);
    }

    public long addExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(EXERCISE_NAME, exercise.getName());
        values.put(EXERCISE_REPS, exercise.getReps());
        values.put(EXERCISE_SETS, exercise.getSets());
        values.put(EXERCISE_ROUTINE, exercise.getRoutine());

        long id = db.insert(TABLE_EXERCISES, null, values);

        if (id != -1) {
            // Add a default weight entry for this exercise
            //String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            WeightEntry defaultWeightEntry = new WeightEntry(-1, id, 0.0, 0, 0, null, System.currentTimeMillis());
            addWeightEntry(defaultWeightEntry);
        }

        db.close();
        return id;
    }

    public void updateExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EXERCISE_NAME, exercise.getName());
        contentValues.put(EXERCISE_ROUTINE, exercise.getRoutine());
        contentValues.put(EXERCISE_REPS, exercise.getReps());
        contentValues.put(EXERCISE_SETS, exercise.getSets());
        db.update(TABLE_EXERCISES, contentValues, EXERCISE_ID + " = ?", new String[]{String.valueOf(exercise.getId())});
        db.close();
    }

    public boolean deleteExercise(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WEIGHT_ENTRIES, WEIGHT_EXERCISE_ID + " = ?", new String[]{String.valueOf(id)});
        return db.delete(TABLE_EXERCISES, EXERCISE_ID + " = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public Exercise getExerciseById(int exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXERCISES, new String[]{EXERCISE_ID, EXERCISE_NAME, EXERCISE_REPS, EXERCISE_SETS, EXERCISE_ROUTINE},
                EXERCISE_ID + " = ?", new String[]{String.valueOf(exerciseId)}, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Exercise exercise = new Exercise(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4));
        cursor.close();
        return exercise;
    }

    public long addWeightEntry(WeightEntry weightEntry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(WEIGHT_EXERCISE_ID, weightEntry.getExerciseId());
        values.put(WEIGHT_WEIGHT, weightEntry.getWeight());
        values.put(WEIGHT_REPS, weightEntry.getReps());
        values.put(WEIGHT_SETS, weightEntry.getSets());
        values.put(WEIGHT_DATE, weightEntry.getDate());
        values.put(WEIGHT_TIMESTAMP, weightEntry.getTimestamp());

        return db.insert(TABLE_WEIGHT_ENTRIES, null, values);
    }
    public int updateWeightEntry(WeightEntry weightEntry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WEIGHT_SETS, weightEntry.getSets());
        values.put(WEIGHT_EXERCISE_ID, weightEntry.getExerciseId());
        values.put(WEIGHT_WEIGHT, weightEntry.getWeight());
        values.put(WEIGHT_REPS, weightEntry.getReps());
        values.put(WEIGHT_DATE, weightEntry.getDate());
        // removed the line where WEIGHT_TIMESTAMP was set
        return db.update(TABLE_WEIGHT_ENTRIES, values, WEIGHT_ID + " = ?", new String[]{String.valueOf(weightEntry.getId())});
    }


    public boolean deleteWeightEntry(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_WEIGHT_ENTRIES, WEIGHT_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public WeightEntry getWeightEntryById(int weightEntryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHT_ENTRIES, new String[]{WEIGHT_ID, WEIGHT_WEIGHT, WEIGHT_REPS, WEIGHT_DATE, WEIGHT_EXERCISE_ID},
                WEIGHT_ID + " = ?", new String[]{String.valueOf(weightEntryId)}, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        WeightEntry weightEntry = new WeightEntry(
                cursor.getInt(cursor.getColumnIndex(WEIGHT_ID)),
                cursor.getInt(cursor.getColumnIndex(WEIGHT_EXERCISE_ID)),
                cursor.getDouble(cursor.getColumnIndex(WEIGHT_WEIGHT)),
                cursor.getInt(cursor.getColumnIndex(WEIGHT_REPS)),
                cursor.getInt(cursor.getColumnIndex(WEIGHT_SETS)),
                cursor.getString(cursor.getColumnIndex(WEIGHT_DATE)),
                cursor.getLong(cursor.getColumnIndex(WEIGHT_TIMESTAMP)));
        cursor.close();
        return weightEntry;
    }

    public List<WeightEntry> getAllWeightEntries() {
        List<WeightEntry> weightEntries = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_WEIGHT_ENTRIES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                WeightEntry weightEntry = new WeightEntry(
                        cursor.getInt(cursor.getColumnIndex(WEIGHT_ID)),
                        cursor.getInt(cursor.getColumnIndex(WEIGHT_EXERCISE_ID)),
                        cursor.getDouble(cursor.getColumnIndex(WEIGHT_WEIGHT)),
                        cursor.getInt(cursor.getColumnIndex(WEIGHT_REPS)),
                        cursor.getInt(cursor.getColumnIndex(WEIGHT_SETS)),
                        cursor.getString(cursor.getColumnIndex(WEIGHT_DATE)),
                        cursor.getLong(cursor.getColumnIndex(WEIGHT_TIMESTAMP)));
                weightEntries.add(weightEntry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return weightEntries;
    }

    public List<Exercise> getAllExercises() {
        List<Exercise> exercises = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EXERCISES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Exercise exercise = new Exercise(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4));
                exercises.add(exercise);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return exercises;
    }
    public List<WeightEntry> getWeightEntriesByExerciseId(int exerciseId) {
        List<WeightEntry> weightEntryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_WEIGHT_ENTRIES + " WHERE " + WEIGHT_EXERCISE_ID + " = " + exerciseId +
                " AND " + WEIGHT_ID + " != (SELECT " + WEIGHT_ID + " FROM " + TABLE_WEIGHT_ENTRIES +
                " WHERE " + WEIGHT_EXERCISE_ID + " = " + exerciseId + " ORDER BY " + WEIGHT_TIMESTAMP + " ASC LIMIT 1)" +
                " ORDER BY " + WEIGHT_TIMESTAMP + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(WEIGHT_ID));
                double weight = cursor.getDouble(cursor.getColumnIndex(WEIGHT_WEIGHT));
                int reps = cursor.getInt(cursor.getColumnIndex(WEIGHT_REPS));
                String date = cursor.getString(cursor.getColumnIndex(WEIGHT_DATE));

                WeightEntry weightEntry = new WeightEntry(
                        id,
                        exerciseId,
                        weight,
                        reps,
                        cursor.getInt(cursor.getColumnIndex(WEIGHT_SETS)),
                        date,
                        cursor.getLong(cursor.getColumnIndex(WEIGHT_TIMESTAMP)));
                weightEntryList.add(weightEntry);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return weightEntryList;
    }


    public List<Exercise> getExercisesWithLatestWeightEntries(String routine) {
        List<Exercise> exerciseList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + EXERCISE_ID + ", " + EXERCISE_NAME + ", " + EXERCISE_REPS + ", " + EXERCISE_SETS +
                " FROM " + TABLE_EXERCISES +
                " WHERE " + EXERCISE_ROUTINE + " = '" + routine + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(EXERCISE_ID));
                String name = cursor.getString(cursor.getColumnIndex(EXERCISE_NAME));
                String reps = cursor.getString(cursor.getColumnIndex(EXERCISE_REPS));
                String sets = cursor.getString(cursor.getColumnIndex(EXERCISE_SETS));

                Exercise exercise = new Exercise(id, name, reps, sets, routine);
                WeightEntry latestWeightEntry = getLatestWeightEntryByExerciseId(id);
                exercise.setLatestWeightEntry(latestWeightEntry);
                exerciseList.add(exercise);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return exerciseList;
    }
    public List<Exercise> getExercisesWithRepsAndSets(String routine) {
        List<Exercise> exerciseList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_EXERCISES +
                " WHERE " + EXERCISE_ROUTINE + " = '" + routine + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(EXERCISE_ID));
                String name = cursor.getString(cursor.getColumnIndex(EXERCISE_NAME));
                String reps = cursor.getString(cursor.getColumnIndex(EXERCISE_REPS));
                String sets = cursor.getString(cursor.getColumnIndex(EXERCISE_SETS));

                Exercise exercise = new Exercise(id, name, reps, sets, routine);

                WeightEntry latestWeightEntry = getLatestWeightEntryByExerciseId(id);
                if (latestWeightEntry != null) {
                    exercise.setLatestWeightEntry(latestWeightEntry);
                }

                exerciseList.add(exercise);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return exerciseList;
    }


    public WeightEntry getLatestWeightEntryByExerciseId(int exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT " + WEIGHT_ID + ", " + WEIGHT_EXERCISE_ID + ", " + WEIGHT_WEIGHT + ", " + WEIGHT_REPS + ", " + WEIGHT_DATE + ", " + WEIGHT_SETS + ", " + WEIGHT_TIMESTAMP +
                " FROM " + TABLE_WEIGHT_ENTRIES +
                " WHERE " + WEIGHT_EXERCISE_ID + " = " + exerciseId +
                " ORDER BY " + WEIGHT_DATE + " DESC LIMIT 1";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(WEIGHT_ID));
            int exercise_id = cursor.getInt(cursor.getColumnIndex(WEIGHT_EXERCISE_ID));
            double weight = cursor.getDouble(cursor.getColumnIndex(WEIGHT_WEIGHT));
            int reps = cursor.getInt(cursor.getColumnIndex(WEIGHT_REPS));
            String date = cursor.getString(cursor.getColumnIndex(WEIGHT_DATE));

            WeightEntry weightEntry = new WeightEntry(
                    id,
                    exercise_id,
                    weight,
                    reps,
                    cursor.getInt(cursor.getColumnIndex(WEIGHT_SETS)),
                    date,
                    cursor.getLong(cursor.getColumnIndex(WEIGHT_TIMESTAMP)));
            cursor.close();
            return weightEntry;
        } else {
            cursor.close();
            return null;
        }
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
    public boolean isExercisesEmpty() {
        boolean isEmpty;
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_EXERCISES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        cursor.moveToFirst();
        isEmpty = cursor.getInt(0) == 0; // Returns true if the count is 0 (i.e., the table is empty)

        cursor.close();
        return isEmpty;
    }
    public WeightEntry getMostRecentWeightEntry(int exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_WEIGHT_ENTRIES, // table name
                new String[]{WEIGHT_ID, WEIGHT_EXERCISE_ID, WEIGHT_WEIGHT, WEIGHT_REPS, WEIGHT_SETS, WEIGHT_DATE, WEIGHT_TIMESTAMP}, // columns to return
                WEIGHT_EXERCISE_ID + " = ?", // selection criteria
                new String[]{String.valueOf(exerciseId)}, // selection arguments
                null, // group by
                null, // having
                WEIGHT_TIMESTAMP + " DESC", // order by
                "1" // limit
        );

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(WEIGHT_ID));
            double weight = cursor.getDouble(cursor.getColumnIndex(WEIGHT_WEIGHT));
            int reps = cursor.getInt(cursor.getColumnIndex(WEIGHT_REPS));
            int sets = cursor.getInt(cursor.getColumnIndex(WEIGHT_SETS));
            String date = cursor.getString(cursor.getColumnIndex(WEIGHT_DATE));
            long timestamp = cursor.getLong(cursor.getColumnIndex(WEIGHT_TIMESTAMP));

            cursor.close();
            return new WeightEntry(id, exerciseId, weight, reps, sets, date, timestamp);
        } else {
            cursor.close();
            return null;
        }
    }

    public String getMostRecentDateForRoutine(String routine) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT MAX(W." + WEIGHT_DATE + ") as latest_date " +
                "FROM " + TABLE_EXERCISES + " E " +
                "INNER JOIN " + TABLE_WEIGHT_ENTRIES + " W ON E." + EXERCISE_ID + " = W." + WEIGHT_EXERCISE_ID + " " +
                "WHERE E." + EXERCISE_ROUTINE + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{routine});

        String latestDate = null;
        if (cursor.moveToFirst()) {
            latestDate = cursor.getString(cursor.getColumnIndex("latest_date"));
        }

        cursor.close();
        return latestDate;
    }

    public List<Entry> getRoutineVolume(String routine) {
        List<Entry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT date, SUM(weight * reps * sets) as TotalVolume FROM weight_entries WHERE exercise_id IN (SELECT id FROM exercises WHERE routine = ?) AND date IS NOT NULL GROUP BY date ORDER BY date(date) ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{routine});

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
        }

        cursor.close();
        return entries;
    }

    public List<Entry> getTotalWeight(String routine) {
        List<Entry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT date, SUM(weight) as TotalWeight FROM weight_entries WHERE exercise_id IN (SELECT id FROM exercises WHERE routine = ?) AND date IS NOT NULL GROUP BY date ORDER BY date(date) ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{routine});

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
        }

        cursor.close();
        return entries;
    }


    public float getImprovementRate(String routine) {
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "SELECT exercise_id, weight, reps, sets FROM " +
                "(SELECT exercise_id, weight, reps, sets, date, ROW_NUMBER() OVER (PARTITION BY exercise_id ORDER BY date DESC) row_num FROM " + TABLE_WEIGHT_ENTRIES +
                " WHERE exercise_id IN (SELECT id FROM " + TABLE_EXERCISES + " WHERE routine = ?))" +
                " WHERE row_num <= 5 ORDER BY exercise_id, date ASC";

        Cursor cursor = db.rawQuery(sql, new String[]{routine});

        float previousVolume = 0;
        float totalIncrease = 0;
        int count = 0;

        if (cursor.moveToFirst()) {
            do {
                float currentWeight = cursor.getFloat(cursor.getColumnIndex(WEIGHT_WEIGHT));
                int currentReps = cursor.getInt(cursor.getColumnIndex(WEIGHT_REPS));
                int currentSets = cursor.getInt(cursor.getColumnIndex(WEIGHT_SETS));
                float currentVolume = currentWeight * currentReps * currentSets;

                if (previousVolume != 0) {
                    float increase = ((currentVolume - previousVolume) / previousVolume) * 100;
                    totalIncrease += increase;
                    count++;
                }

                previousVolume = currentVolume;
            } while (cursor.moveToNext());
        }

        cursor.close();

        if (count > 0) {
            return totalIncrease / count;
        } else {
            return 0;
        }
    }









}

