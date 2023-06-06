package irina.dam.rggoal.TrainingPlan.Exercises;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import irina.dam.rggoal.R;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolderExercises> {
    ArrayList<Exercise> exercises;

    public ExerciseAdapter(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ExerciseAdapter.ViewHolderExercises onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_plan_exercises, parent, false);
        return new ExerciseAdapter.ViewHolderExercises(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseAdapter.ViewHolderExercises holder, int position) {
        holder.tvName.setText(exercises.get(position).getName());
        holder.tvReps.setText("x"+exercises.get(position).getReps());
        holder.tvSpecs.setText(exercises.get(position).getSpecs());
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public static class ViewHolderExercises extends RecyclerView.ViewHolder {

        TextView tvName, tvReps, tvSpecs;

        public ViewHolderExercises(@NonNull View itemView) {
            super(itemView);

            tvName=itemView.findViewById(R.id.tvExNamePlan);
            tvReps=itemView.findViewById(R.id.tvRepsPlan);
            tvSpecs=itemView.findViewById(R.id.tvSpecsPlan);

        }
    }
}
