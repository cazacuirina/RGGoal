package irina.dam.rggoal.Programs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import irina.dam.rggoal.R;

public class ProgramAdapter extends RecyclerView.Adapter<ProgramAdapter.ViewHolderPrograms>{

    Context context;
    ArrayList<Program> programs;
    HashMap<String ,String >programStatuses=new HashMap<String, String>();

    public ProgramAdapter(Context context, ArrayList<Program> programs, HashMap<String ,String >programStatuses){
        this.context=context;
        this.programs=programs;
        this.programStatuses=programStatuses;
    }

    @NonNull
    @Override
    public ProgramAdapter.ViewHolderPrograms onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.rv_program_item, parent, false);
        return new ProgramAdapter.ViewHolderPrograms(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramAdapter.ViewHolderPrograms holder, @SuppressLint("RecyclerView") int position) {
        holder.tvName.setText(programs.get(position).getName());
        holder.tvDays.setText(programs.get(position).getNoDays()+" Days");
        if(programs.get(position).getImageUrl() != null && !programs.get(position).getImageUrl().isEmpty()){
            Picasso.get().load(programs.get(position).getImageUrl()).placeholder(R.drawable.gym_flexibility).into(holder.imgProgram);
        }

        holder.tvEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(position);
                }
            }
        });

        String key=programs.get(position).getKey();
        if (programStatuses.containsKey(key)) {
            String sts = programStatuses.get(key);
            holder.tvEnroll.setText(sts);
            if(key.equals(context.getString(R.string.progFinished))){
                holder.itemView.setAlpha(0.5f);
            }

        }else{
            if(programStatuses.containsValue("Enrolled")){
               holder.tvEnroll.setText(R.string.progAvailable);
               holder.itemView.setAlpha(0.5f);
            }else{
                holder.tvEnroll.setText(R.string.progEnroll);
                holder.itemView.setAlpha(1.0f);
            }
        }

    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    private OnItemClickListener mListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return programs.size();
    }


    public static class ViewHolderPrograms extends RecyclerView.ViewHolder{
        TextView tvName, tvDays, tvEnroll;
        ImageView imgProgram;
        RecyclerView rvExercises;

        public ViewHolderPrograms(@NonNull View itemView) {
            super(itemView);
            tvName=itemView.findViewById(R.id.tvProgName);
            tvDays=itemView.findViewById(R.id.tvProgDays);
            tvEnroll=itemView.findViewById(R.id.tvEnroll);
            imgProgram=itemView.findViewById(R.id.imgProgram);
            rvExercises=itemView.findViewById(R.id.rvPlanExecises);
        }
    }
}
