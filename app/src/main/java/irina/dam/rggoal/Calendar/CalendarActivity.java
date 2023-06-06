package irina.dam.rggoal.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

//import com.github.sundeepk.compactcalendarview.CompactCalendarView;
//import com.github.sundeepk.compactcalendarview.EventsContainer;
//import com.github.sundeepk.compactcalendarview.domain.Event;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import irina.dam.rggoal.LocalDatabase.Competition;
import irina.dam.rggoal.LocalDatabase.DatabaseManager;
import irina.dam.rggoal.LocalDatabase.EnrolledProgram;
import irina.dam.rggoal.Programs.Program;
import irina.dam.rggoal.R;


public class CalendarActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener
{
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    ImageButton btnBack, btnFwd;

    List<Competition> competitions=new ArrayList<Competition>();
    List<EnrolledProgram> enrolledPrograms=new ArrayList<EnrolledProgram>();
    List<Goal>goals=new ArrayList<Goal>();

    FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);

        btnBack = findViewById(R.id.backBtnCalendar);
        btnBack.setOnClickListener(view -> {
            previousMonthAction();
        });
        btnFwd = findViewById(R.id.fwdBtnCalendar);
        btnFwd.setOnClickListener(view -> {
            nextMonthAction();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            selectedDate = LocalDate.now();
        }
        setMonthView(selectedDate);
    }

    CalendarAdapter calendarAdapter;
    DocumentReference refProgram;
    private void setMonthView(LocalDate currentDate)
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
            LocalDate lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());

            ExecutorService executorService = Executors.newFixedThreadPool(2);
            Future<List<Competition>> futureResultCompetition = executorService.submit(() -> {
                List<Competition> competitionList= DatabaseManager.getInstance(getApplicationContext()).getDao().getCompetitionsThisMonth(firstDayOfMonth, lastDayOfMonth);
                return competitionList;
            });

            Future<List<EnrolledProgram>> futureResultPrograms = executorService.submit(() -> {
                List<EnrolledProgram> programsList = DatabaseManager.getInstance(getApplicationContext()).getDao().getProgramsThisMonth(firstDayOfMonth, lastDayOfMonth);
                return programsList;
            });

            try {
                goals.clear();
                competitions = futureResultCompetition.get();
                for(Competition competition:competitions){
                    goals.add(new Goal(competition.getName(), competition.getCreatedAt(), competition.getDate(),GoalType.Competition));
                }
                enrolledPrograms = futureResultPrograms.get();

                if(enrolledPrograms.size()>0)
                {
                    db=FirebaseFirestore.getInstance();
                    for(int i=0; i<enrolledPrograms.size(); i++){
                    refProgram=db.collection("programs").document("programList")
                            .collection("Levels").document("level"+enrolledPrograms.get(i).getLevel())
                            .collection("Courses").document(enrolledPrograms.get(i).getFirebaseKey());
                    getPrograms(i, new FirestoreCallback() {
                        @Override
                        public void onCallbackProgram(int p, Program program) {
                            goals.add(new Goal(program.getName(), enrolledPrograms.get(p).getStartDate(), enrolledPrograms.get(p).getEndDate(), GoalType.Program));
                            if(p+1==enrolledPrograms.size()){
                                initializeAdapter();
                            }
                        }
                    });
                }
                }else{
                    initializeAdapter();
                }


            } catch (InterruptedException | ExecutionException e) {
               Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
            executorService.shutdown();

        }

    }

    private void initializeAdapter(){
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        calendarAdapter = new CalendarAdapter(getApplicationContext(), daysInMonth, goals, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(calendarRecyclerView.getContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private void getPrograms(int p,FirestoreCallback callback){
        refProgram.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Program program=documentSnapshot.toObject(Program.class);
                    callback.onCallbackProgram(p,program);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                });

    }

    private interface FirestoreCallback{
        void onCallbackProgram(int p, Program programs);
    }

    private ArrayList<String> daysInMonthArray(LocalDate date)
    {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = null;
        int daysInMonth = 0;
        LocalDate firstOfMonth = null;
        int dayOfWeek = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            yearMonth = YearMonth.from(date);
            daysInMonth = yearMonth.lengthOfMonth();
            firstOfMonth = selectedDate.withDayOfMonth(1);
            dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        }

        for(int i = 1; i <= 42; i++)
        {
            if(i <= dayOfWeek || i > daysInMonth + dayOfWeek)
            {
                daysInMonthArray.add("");
            }
            else
            {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return  daysInMonthArray;
    }

    private String monthYearFromDate(LocalDate date)
    {
        DateTimeFormatter formatter = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return date.format(formatter);
        }
        return null;
    }

    public void previousMonthAction()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            selectedDate = selectedDate.minusMonths(1);
        }
        setMonthView(selectedDate);
    }

    public void nextMonthAction()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            selectedDate = selectedDate.plusMonths(1);
        }
        setMonthView(selectedDate);
    }

    @Override
    public void onItemClick(int position, String dayText)
    {
        if(!dayText.equals("") && goals.size()>0)
        {
                for (Goal goal:goals) {
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if( (goal.getCompletedDate()!=null && String.valueOf(goal.getCompletedDate().getDayOfMonth())==dayText)
                            ||(goal.getCompletedDate()==null && String.valueOf(goal.getStartDate().getDayOfMonth())==dayText)){
                                 BottomSheetCalendar bottomSheet = new BottomSheetCalendar(goal);
                                 bottomSheet.show(getSupportFragmentManager(), "MyBottomSheetDialogFragment");
                            }
                        }
                    }
                }
        }
    }
}

