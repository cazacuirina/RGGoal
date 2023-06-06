package irina.dam.rggoal.Schedule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import irina.dam.rggoal.LocalDatabase.Competition;
import irina.dam.rggoal.R;

public class CompetitionAdapter extends RecyclerView.Adapter<CompetitionAdapter.ViewHolderCompetitions> {
  Context context;
  ArrayList<Competition> competitons;

   public CompetitionAdapter(Context context, ArrayList<Competition> competitons, OnButtonClickListener listener){
        this.context=context;
        this.competitons=competitons;
        this.clickListener=listener;
   }

    @NonNull
    @Override
    public CompetitionAdapter.ViewHolderCompetitions onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.rv_competition_item, parent, false);
        return new CompetitionAdapter.ViewHolderCompetitions(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompetitionAdapter.ViewHolderCompetitions holder, @SuppressLint("RecyclerView") int position) {
        holder.tvName.setText(competitons.get(position).getName());
        holder.tvNoApparatus.setText(competitons.get(position).getNoApparatus()+" apparatuses");
        DateTimeFormatter dtf = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dtf = DateTimeFormatter.ofPattern("dd LLLL yyyy");
            String stringDate = competitons.get(position).getDate().format(dtf);
            holder.tvDate.setText(stringDate);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            long daysLeft= ChronoUnit.DAYS.between(LocalDate.now(), competitons.get(position).getDate());
            if(daysLeft<=5){
                if(daysLeft>0){
                    holder.tvDaysLeft.setText("SOON: "+daysLeft+" days left");
                    holder.layout.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(),R.color.mediumPink));
                }else{
                    holder.tvDaysLeft.setText("Good luck today!");
                    holder.layout.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(),R.color.intensePink));
                }
            }else{
                holder.tvDaysLeft.setText("");
                holder.layout.setBackgroundColor(Color.WHITE);
            }
        }

        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onEditClick(position);
                }
            }
        });
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onDeleteClick(position);
                }
            }
        });
    }

    public interface OnButtonClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }
    private OnButtonClickListener clickListener;

    @Override
    public int getItemCount() {
        return competitons.size();
    }

    public static class ViewHolderCompetitions extends RecyclerView.ViewHolder{
        TextView tvName, tvDate, tvNoApparatus, tvDaysLeft;
        ImageButton btnEdit, btnDelete;
        RelativeLayout layout;
        public ViewHolderCompetitions(@NonNull View itemView) {
            super(itemView);
            tvName=itemView.findViewById(R.id.tvNumeCompet);
            tvDate=itemView.findViewById(R.id.tvDataCompet);
            tvDaysLeft=itemView.findViewById(R.id.tvDaysLeft);
            tvNoApparatus=itemView.findViewById(R.id.tvNoAppCompet);
            btnEdit=itemView.findViewById(R.id.btnEdit);
            btnDelete=itemView.findViewById(R.id.btnDelete);
            layout=itemView.findViewById(R.id.layoutCompetiton);
        }
    }
}
