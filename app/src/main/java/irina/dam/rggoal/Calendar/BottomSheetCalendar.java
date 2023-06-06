package irina.dam.rggoal.Calendar;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import irina.dam.rggoal.R;

public class BottomSheetCalendar extends BottomSheetDialogFragment {
    Goal goal;
    TextView tvName, tvFirstDate, tvLastDate, tvStarted, tvFinished;
    ImageView imgGoal;

    public BottomSheetCalendar(Goal goal){
        this.goal=goal;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_calendar, container, false);

        tvName = view.findViewById(R.id.tvTitle);
        tvFirstDate = view.findViewById(R.id.tvFirstDate);
        tvLastDate = view.findViewById(R.id.tvLastDate);
        tvStarted = view.findViewById(R.id.tvStarted);
        tvFinished = view.findViewById(R.id.tvCompleted);
        imgGoal = view.findViewById(R.id.imgCalendar);

        tvName.setText(goal.getName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tvFirstDate.setText(goal.getStartDate().format(DateTimeFormatter.ofPattern("dd LLLL yyyy")));

            if(goal.getCompletedDate()==null){
                tvFinished.setVisibility(View.GONE);
                tvLastDate.setText(R.string.stillworking);
            }else{
                tvLastDate.setText(goal.getCompletedDate().format(DateTimeFormatter.ofPattern("dd LLLL yyyy")));
                if(goal.getCompletedDate().isAfter(LocalDate.now())){
                    tvFinished.setText(R.string.willcomplete);
                }else{
                    tvFinished.setText(R.string.completed);
                }
            }
        }
        
        if(goal.getGoalType()==GoalType.Competition){
            imgGoal.setImageResource(R.drawable.trophy);
        }else{
            imgGoal.setImageResource(R.drawable.goal);
        }

        return view;
    }
}