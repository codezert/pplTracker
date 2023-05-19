/*
The ExerciseAdapter.java class is an implementation of a custom RecyclerView adapter for displaying a list of exercises. Here's a summary of its functionality:

    It takes a list of Exercise objects and an OnItemClickListener as input in the constructor.
    The onCreateViewHolder method inflates the row_exercise layout and creates a new instance of ExerciseViewHolder.
    The onBindViewHolder method binds the data from the Exercise objects to the corresponding views in the ExerciseViewHolder. It displays the exercise name and the latest weight entry information, including reps and weight, or "No data" if the latest weight entry is not available.
    The getItemCount method returns the size of the exercise list.
    The ExerciseViewHolder inner class holds references to the TextViews for the exercise name and reps/sets information. It also implements an OnClickListener for handling item clicks.
    The OnItemClickListener interface is used to define a callback method for handling item clicks.

 */
package com.example.ppltracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.widget.Button;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<Exercise> exerciseList;
    private OnItemClickListener onItemClickListener;
    private int selectedItem = -1; // Declare selectedItem here

    public ExerciseAdapter(List<Exercise> exerciseList, OnItemClickListener onItemClickListener) {
        this.exerciseList = exerciseList;
        this.onItemClickListener = onItemClickListener;
    }

    public void removeItem(int position) {
        exerciseList.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_exercise, parent, false);
        return new ExerciseViewHolder(itemView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);

        holder.txtExerciseName.setText(exercise.getName());

        // Display the reps and sets for the exercise
        String repsSets = exercise.getReps() + " reps / " + exercise.getSets() + " sets";
        holder.txtRepsSets.setText(repsSets);

        if (position == selectedItem) {
            holder.btnDeleteExercise.setVisibility(View.VISIBLE);
            holder.btnEditExercise.setVisibility(View.VISIBLE);
        } else {
            holder.btnDeleteExercise.setVisibility(View.GONE);
            holder.btnEditExercise.setVisibility(View.GONE);
        }
    }



    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView txtExerciseName;
        TextView txtRepsSets;
        Button btnDeleteExercise;
        Button btnEditExercise;

        ExerciseViewHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);
            txtExerciseName = itemView.findViewById(R.id.txtExerciseName);
            txtRepsSets = itemView.findViewById(R.id.txtRepsSets);
            btnDeleteExercise = itemView.findViewById(R.id.btnDeleteExercise);
            btnDeleteExercise.setVisibility(View.GONE);
            btnEditExercise = itemView.findViewById(R.id.btnEditExercise); // Bind the edit button

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (selectedItem != -1) {
                        notifyItemChanged(selectedItem);
                    }
                    selectedItem = position;
                    notifyItemChanged(selectedItem);
                    return true;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(exerciseList.get(position));
                    }
                }
            });

            btnDeleteExercise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemDelete(exerciseList.get(position));
                        btnDeleteExercise.setVisibility(View.GONE);
                        selectedItem = -1;
                    }
                }
            });
            btnEditExercise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemEdit(exerciseList.get(position));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Exercise exercise);
        void onItemDelete(Exercise exercise);
        void onItemEdit(Exercise exercise); // New onEdit method
    }
    public void clearSelectedItem() {
        selectedItem = -1;
        notifyDataSetChanged(); // Notify the adapter that data set has changed, this will refresh the list
    }

}
