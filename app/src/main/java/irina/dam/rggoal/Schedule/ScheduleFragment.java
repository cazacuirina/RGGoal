package irina.dam.rggoal.Schedule;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;

import irina.dam.rggoal.Calendar.CalendarActivity;
import irina.dam.rggoal.LocalDatabase.Competition;
import irina.dam.rggoal.LocalDatabase.DatabaseManager;
import irina.dam.rggoal.R;
import irina.dam.rggoal.StartApp.HomeFragment;
import irina.dam.rggoal.StartApp.MainActivity;
import irina.dam.rggoal.TrainingPlan.MacroCycle;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link CalendarFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class ScheduleFragment extends Fragment implements CompetitionAdapter.OnButtonClickListener{

    ArrayList<Competition> competitonList=new ArrayList<Competition>();
    FirebaseFirestore db;

    public ScheduleFragment() {

    }

    Map<String,Boolean> trainingDays = new HashMap<String, Boolean>();
    Map<String,Boolean> sortedDays = new TreeMap<String,Boolean>();
    List<TextView>tvList=new ArrayList<TextView>();
    Button btnUpdate;
    ImageButton btnCalendar;
    DocumentReference ref;

    CompetitionAdapter adapter;
    RecyclerView rvCompetiton;
    TextView tvNew;

    AlertDialog dialog;
    AlertDialog.Builder builder;
    TextInputEditText etCompName;
    TextInputEditText etCompApparatus;
    DatePicker dpCompDate;
    Button btnSubmit;
    FloatingActionButton btnAdd;
    Button btnClose;

    int level;
    MacroCycle macroCycle;
    boolean newFirstCompetition=false;

    private View.OnClickListener clickDay = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ColorDrawable viewColor = (ColorDrawable) view.getBackground();
            int colorId = viewColor.getColor();
            int lightPink = ContextCompat.getColor(getContext(), R.color.lightPink);
            int mediumPink = ContextCompat.getColor(getContext(), R.color.mediumPink);
            if(colorId==lightPink){
                view.setBackgroundColor(mediumPink);
                view.setAlpha(1.0f);
            } else {
                view.setBackgroundColor(lightPink);
                view.setAlpha(0.8f);
            }
            if(btnUpdate.getVisibility()==View.GONE){
                btnUpdate.setVisibility(View.VISIBLE);
            }
            int index=tvList.indexOf(view);
            List keys = new ArrayList(sortedDays.keySet());
            String selectedDay=(String) keys.get(index);
            trainingDays.put(selectedDay, !trainingDays.get(selectedDay) );
            Toast.makeText(getContext(), trainingDays.toString(), Toast.LENGTH_SHORT).show();

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_calendar, container, false);

        level=getArguments().getInt("level");
        macroCycle=new MacroCycle(getContext(), level, (MainActivity)getActivity());

        btnCalendar=view.findViewById(R.id.btnCalendar);
        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(), CalendarActivity.class);
                intent.putExtra("level", level);
                startActivity(intent);
            }
        });

        btnUpdate=view.findViewById(R.id.btnUpdateDays);
        btnUpdate.setOnClickListener(view1 -> {
            String key=getActivity().getSharedPreferences("preferinte", Context.MODE_PRIVATE).getString("firebaseKey", "");
            db = FirebaseFirestore.getInstance();
            ref=db.collection("users").document(key);
            updateDays();

        });

        TextView tv1, tv2, tv3, tv4, tv5, tv6, tv7;
        tv1=view.findViewById(R.id.tvMon);
        tv1.setOnClickListener(clickDay);
        tv2=view.findViewById(R.id.tvTue);
        tv2.setOnClickListener(clickDay);
        tv3=view.findViewById(R.id.tvWed);
        tv3.setOnClickListener(clickDay);
        tv4=view.findViewById(R.id.tvThu);
        tv4.setOnClickListener(clickDay);
        tv5=view.findViewById(R.id.tvFri);
        tv5.setOnClickListener(clickDay);
        tv6=view.findViewById(R.id.tvSat);
        tv6.setOnClickListener(clickDay);
        tv7=view.findViewById(R.id.tvSun);
        tv7.setOnClickListener(clickDay);
        tvNew=view.findViewById(R.id.tvNewCompetition);

        tvList.clear();
        Collections.addAll(tvList, tv5, tv1, tv6, tv7, tv4, tv2, tv3);
        getDays();

        getCompetitionList();

        rvCompetiton=view.findViewById(R.id.rvCompetition);
        adapter=new CompetitionAdapter(getContext(), competitonList, this);
        rvCompetiton.setAdapter(adapter);

        rvCompetiton.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAdd=view.findViewById(R.id.btnAddCompet);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog(null, 0);
            }
        });

        return view;
    }

    private void callComp(){
        if(newFirstCompetition){
            macroCycle.getNextLastCompetition();
        }
    }

    @Override
    public void onEditClick(int position) {
        Competition selectedCompetition= competitonList.get(position);
        createDialog(selectedCompetition, position);
    }


    @Override
    public void onDeleteClick(int position) {
        if(position==0){
            deletePrefsPhaseChanged();
            newFirstCompetition=true;
        }else{
            newFirstCompetition=false;
        }
        callComp();

        Competition selectedCompetition= competitonList.get(position);

        new Thread(()->{
            DatabaseManager.getInstance(getContext()).getDao().deleteCompetition(selectedCompetition);
            callComp();

            competitonList.remove(selectedCompetition);
            sortCompetitionsByDate();

            getActivity().runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void getCompetitionList(){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            new Thread(() -> {
                competitonList.clear();

                List<Competition> list = DatabaseManager.getInstance(getContext()).getDao().getFollowingCompetitions(LocalDate.now());
                competitonList.addAll(list);

                getActivity().runOnUiThread(() -> {
                    if (competitonList.size() == 0) {
                        tvNew.setVisibility(View.VISIBLE);
                        rvCompetiton.setVisibility(View.GONE);
                    } else {
                        tvNew.setVisibility(View.GONE);
                        rvCompetiton.setVisibility(View.VISIBLE);

                        sortCompetitionsByDate();
                        adapter.notifyDataSetChanged();
                    }

                });
            }).start();
            }
    }

    private void updateDays(){
         ref.update("trainingDays", trainingDays)
                .addOnSuccessListener(unused -> {
                    btnUpdate.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Fail", Toast.LENGTH_SHORT).show();
                });
    }

    private void sortCompetitionsByDate(){
        Comparator<Competition> comparator = new Comparator<Competition>() {
            @Override
            public int compare(Competition o1, Competition o2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return o1.getDate().compareTo(o2.getDate());
                }
                return 0;
            }
        };
        Collections.sort(competitonList, comparator);
    }

    private void insertNewCompetition(Competition competition){
        new Thread(()->{
            long id=DatabaseManager.getInstance(getContext()).getDao().insertCompetition(competition);
            competition.setId((int) id);
            callComp();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(competition.getDate().isEqual(LocalDate.now())||competition.getDate().isAfter(LocalDate.now()))
                {
                    competitonList.add(competition);
                }
            }
            sortCompetitionsByDate();
            int index=competitonList.indexOf(competition);

            getActivity().runOnUiThread(()-> {
                if(rvCompetiton.getVisibility()==View.GONE) {
                    tvNew.setVisibility(View.GONE);
                    rvCompetiton.setVisibility(View.VISIBLE);
                }
                adapter.notifyItemInserted(index);
            });
        }).start();
    }

    private void updateCompetition(Competition competition, int position){
        new Thread(()->{
            DatabaseManager.getInstance(getContext()).getDao().updateCompetition(competition);
            competitonList.set(position, competition);

            callComp();

            sortCompetitionsByDate();

            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), competition.toString(), Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void createDialog(Competition competition, int position){
        builder=new AlertDialog.Builder(getContext());
        final View popup=getLayoutInflater().inflate(R.layout.competition_form, null);
        etCompName=(TextInputEditText) popup.findViewById(R.id.etCompName);
        etCompApparatus=(TextInputEditText) popup.findViewById(R.id.etCompApp);
        dpCompDate=(DatePicker) popup.findViewById(R.id.dpCompDate);
        btnSubmit=(Button) popup.findViewById(R.id.btnSubmitComp);
        btnClose=(Button) popup.findViewById(R.id.btnClose);

        if(competition!=null){
            etCompName.setText(competition.getName());
            etCompApparatus.setText(String.valueOf(competition.getNoApparatus()));
            btnSubmit.setText("Update");

            Calendar calendar = Calendar.getInstance();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                calendar.set(competition.getDate().getYear(), competition.getDate().getMonthValue() - 1, competition.getDate().getDayOfMonth());
                dpCompDate.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
            }

        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Competition newcompetition=createCompetition();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if(competitonList.size()>0){
                        if(newcompetition.getDate().isBefore(competitonList.get(0).getDate())
                                || (competition!=null && competition.equals(competitonList.get(0)))){

                            deletePrefsPhaseChanged();

                            newFirstCompetition=true;
                        }else{
                            newFirstCompetition=false;
                        }
                    }
                }

                if(competition==null){
                    if(newcompetition!=null){
                        dialog.dismiss();
                        insertNewCompetition(newcompetition);
                        }
                    }

                else{
                    if(newcompetition!=null) {
                        dialog.dismiss();
                        newcompetition.setId(competition.getId());
                        updateCompetition(newcompetition, position);
                    }
                }
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        builder.setView(popup);
        dialog=builder.create();
        dialog.show();
    }

    private void deletePrefsPhaseChanged(){
        SharedPreferences prefs = getActivity().getSharedPreferences("preferinte", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String formattedToday="";

        Map<String, ?> allPrefs = prefs.getAll();

        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            String key = entry.getKey();
            DateTimeFormatter formatter = null;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                formattedToday = LocalDate.now().format(formatter);
            }

            if (key.contains(formattedToday)) {
                editor.remove(key);
                editor.apply();
            }
        }
    }

    private Competition createCompetition(){
        Competition competiton=new Competition();

        if (etCompName.getText().toString().isEmpty()) {
            etCompName.setError(getString(R.string.compErr));
            return null;
        } else {
            competiton.setName(etCompName.getText().toString());
        }

        if (etCompApparatus.getText().toString().isEmpty()){
            etCompApparatus.setError(getString(R.string.noAppErr));
            return null;
        } else{
            try{
                int noApparatus=Integer.parseInt(etCompApparatus.getText().toString());
                if(noApparatus<1 || noApparatus>5){
                    etCompApparatus.setError(getString(R.string.noAppErr));
                    return null;
                }
            }catch(NumberFormatException ex){
                etCompApparatus.setError(getString(R.string.noAppErr));
                return null;
            }
            competiton.setNoApparatus(Integer.parseInt(etCompApparatus.getText().toString()));
        }

        int year = dpCompDate.getYear();
        int month = dpCompDate.getMonth() + 1;
        int dayOfMonth = dpCompDate.getDayOfMonth();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            competiton.setDate(LocalDate.of(year, month, dayOfMonth));
            competiton.setCreatedAt(LocalDate.now()); //VEZI UPDATE
        }

        return competiton;
    }

    private void getDays() {
        trainingDays = (Map<String, Boolean>) getArguments().getSerializable("days");

        int lightPink = ContextCompat.getColor(getContext(), R.color.lightPink);
        int mediumPink = ContextCompat.getColor(getContext(), R.color.mediumPink);

        if (trainingDays.size() != 0) {
            sortedDays.putAll(trainingDays);

            for (Map.Entry<String, Boolean> entry : trainingDays.entrySet()) {
                String key = entry.getKey();
                Boolean value = entry.getValue();

                for (TextView tv : tvList) {
                    if (key.substring(0, 3).equals(tv.getText().toString().toLowerCase())) {
                        if (value.equals(true)) {
                            tv.setBackgroundColor(mediumPink);
                            tv.setAlpha(1.0f);
                        } else {
                            tv.setBackgroundColor(lightPink);
                            tv.setAlpha(0.8f);
                        }
                    }
                }
            }
        } else {
            for (TextView tv : tvList) {
                tv.setBackgroundColor(lightPink);
                tv.setAlpha(0.8f);
            }

        }
    }
}