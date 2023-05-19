package com.example.ppltracker;

public class Exercise {
    private int id;
    private String name;
    private String reps;
    private String sets;
    private String routine;
    private WeightEntry latestWeightEntry;

    public Exercise(int id, String name, String reps, String sets, String routine) {
        if (reps.length() > 20 || sets.length() > 20) {
            throw new IllegalArgumentException("Reps or Sets input is too long. It should not exceed 20 characters.");
        }
        this.id = id;
        this.name = name;
        this.reps = reps;
        this.sets = sets;
        this.routine = routine;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepsSets() {
        return reps + "x" + sets;
    }

    public String getRoutine() {
        return routine;
    }

    public void setRoutine(String routine) {
        this.routine = routine;
    }

    public WeightEntry getLatestWeightEntry() {
        return latestWeightEntry;
    }

    public void setLatestWeightEntry(WeightEntry latestWeightEntry) {
        this.latestWeightEntry = latestWeightEntry;
    }

    public String getReps() {
        return reps;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }

    public String getSets() {
        return sets;
    }

    public void setSets(String sets) {
        this.sets = sets;
    }
}