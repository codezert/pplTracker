package com.example.ppltracker;

public class WeightEntry {
    private long id;
    private long exerciseId;
    private double weight;
    private int reps;
    private int sets;
    private String date;
    private long timestamp;

    // Default constructor
    public WeightEntry() {}

    // Parameterized constructor
    public WeightEntry(long id, long exerciseId, double weight, int reps, int sets, String date, long timestamp) {
        this.setId(id);
        this.setExerciseId(exerciseId);
        this.setWeight(weight);
        this.setReps(reps);
        this.setSets(sets);
        this.setDate(date);
        this.setTimestamp(timestamp);
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp should be greater than 0.");
        }
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        // You can add date validation here if needed
        this.date = date;
    }

}
