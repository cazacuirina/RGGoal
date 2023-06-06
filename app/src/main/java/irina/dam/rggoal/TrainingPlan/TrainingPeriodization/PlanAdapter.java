package irina.dam.rggoal.TrainingPlan.TrainingPeriodization;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import irina.dam.rggoal.TrainingPlan.Exercises.Exercise;
import irina.dam.rggoal.TrainingPlan.Exercises.ExerciseAdapter;
import irina.dam.rggoal.R;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolderStages>{

    Context context;
    ArrayList<Exercise> exercises;
    ArrayList<TrainingStage> stages;
    int currentPosition;
    int[] delays;

    public PlanAdapter(Context context, ArrayList<TrainingStage>stages, ArrayList<Exercise> exercises, int[] delays){
        this.context=context;
        this.exercises=exercises;
        this.stages=stages;
        this.currentPosition= RecyclerView.NO_POSITION;
        this.delays=delays;
    }

    @NonNull
    @Override
    public PlanAdapter.ViewHolderStages onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.rv_plan_stage, parent, false);
        return new PlanAdapter.ViewHolderStages(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanAdapter.ViewHolderStages holder, @SuppressLint("RecyclerView") int position) {
        holder.tvStage.setText(stages.get(position).getName());
        holder.tvDuration.setText(stages.get(position).getDuration()+" minutes");

        if (delays != null && delays[position] != 0) {
            holder.tvDelays.setText((delays[position]>0?"-":"+" )+Math.abs(delays[position])+"m");
        }


        ExerciseAdapter exerciseAdapter=new ExerciseAdapter(exercises);
        LinearLayoutManager layoutManager=new LinearLayoutManager(context);//activity
        holder.rvExercises.setLayoutManager(layoutManager);
        holder.rvExercises.setAdapter(exerciseAdapter);


        boolean isExpandable=stages.get(position).isExpandable();

        holder.layout.setVisibility(isExpandable? View.VISIBLE : View.GONE);
        holder.tvExercises.setVisibility(isExpandable? View.VISIBLE : View.GONE);
        holder.tvReps.setVisibility(isExpandable? View.VISIBLE : View.GONE);
        holder.tvSpecs.setVisibility(isExpandable? View.VISIBLE : View.GONE);

        if(isExpandable){
            holder.btnExpand.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
        }else{
            holder.btnExpand.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
        }

        holder.btnExpand.setOnClickListener(view->{
            expandStage(position, !stages.get(position).isExpandable());
            notifyItemChanged(holder.getAdapterPosition());
        });

    }
    public void expandStage(int position, boolean isExpandable) {
        stages.get(position).setExpandable(isExpandable);

        if(stages.get(position).isExpandable()){
            exercises = stages.get(position).getExercises();
        }

    }

    @Override
    public int getItemCount() {
        return stages.size();
    }

    public static class ViewHolderStages extends RecyclerView.ViewHolder {

        TextView tvStage, tvDuration, tvDelays, tvExercises, tvReps, tvSpecs;
        ImageButton btnExpand;
        RelativeLayout layout;
        RecyclerView rvExercises;

        public ViewHolderStages(@NonNull View itemView) {
            super(itemView);

            tvStage=itemView.findViewById(R.id.tvStage);
            tvDuration=itemView.findViewById(R.id.tvDuration);
            tvExercises=itemView.findViewById(R.id.nameEx);
            tvDelays=itemView.findViewById(R.id.tvDelay);
            tvReps=itemView.findViewById(R.id.repsEx);
            tvSpecs=itemView.findViewById(R.id.specsEx);
            btnExpand=itemView.findViewById(R.id.btnExpand);
            layout=itemView.findViewById(R.id.expandable);
            rvExercises=itemView.findViewById(R.id.rvPlanExecises);


        }
    }
}
