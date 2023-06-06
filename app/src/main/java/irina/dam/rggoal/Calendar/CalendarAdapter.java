package irina.dam.rggoal.Calendar;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import irina.dam.rggoal.R;

class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;
    Context context;
    private List<Goal> goals=new ArrayList<Goal>();
    private HashMap<String, Integer>days=new LinkedHashMap<String, Integer>();

    public CalendarAdapter(Context context, ArrayList<String> daysOfMonth, List<Goal> goals, OnItemListener onItemListener) //colored
    {
        this.context=context;
        this.daysOfMonth = daysOfMonth;
        this.goals = goals;
        this.onItemListener = onItemListener;

        for(int i=0; i<goals.size(); i++){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(goals.get(i).getCompletedDate()!=null){
                    days.put(String.valueOf(goals.get(i).getCompletedDate().getDayOfMonth()), i);
                }else{
                    days.put(String.valueOf(goals.get(i).getStartDate().getDayOfMonth()), i);
                }

            }
        }
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);

        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position)
    {
        holder.dayOfMonth.setText(daysOfMonth.get(position));
        
        if(goals.size()>0){
            for(String day: days.keySet()){
                if(day==daysOfMonth.get(position)){
                    if(goals.get(days.get(day)).getGoalType()==GoalType.Competition){
                        Drawable trophy = ContextCompat.getDrawable(context, R.drawable.trophy);
                        trophy.setColorFilter(ContextCompat.getColor(context, R.color.yellow), PorterDuff.Mode.SRC_IN);
                        //bgResource
                        holder.dayOfMonth.setBackground(trophy);
                    }else{
                        Drawable star = ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_24);
                        star.setColorFilter(ContextCompat.getColor(context, R.color.yellow), PorterDuff.Mode.SRC_IN);
                        holder.dayOfMonth.setBackground(star);
                    }

                }
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return daysOfMonth.size();
    }

    public interface  OnItemListener
    {
        void onCreate(Bundle savedInstanceState);

        void onItemClick(int position, String dayText);
    }
}
