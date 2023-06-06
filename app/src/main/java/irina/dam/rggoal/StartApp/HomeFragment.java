package irina.dam.rggoal.StartApp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import irina.dam.rggoal.Programs.CategoryCount;
import irina.dam.rggoal.Programs.ProgramsActivity;
import irina.dam.rggoal.SharedPrefs.PreferencesKeys;
import irina.dam.rggoal.TrainingPlan.Exercises.Exercise;
import irina.dam.rggoal.TrainingPlan.Exercises.ExerciseAdapter;
import irina.dam.rggoal.LocalDatabase.DatabaseManager;
import irina.dam.rggoal.LocalDatabase.EnrolledProgram;
import irina.dam.rggoal.Profile.Gymnast;
import irina.dam.rggoal.Programs.Day;
import irina.dam.rggoal.Programs.Program;
import irina.dam.rggoal.R;
import irina.dam.rggoal.TrainingPlan.DailyPlanActivity;
import irina.dam.rggoal.TrainingPlan.MacroCycle;

public class HomeFragment extends Fragment {

    Button btnPlan;
    TextView tvPlan, tvHi, tvName, tvCateg, tvDay, tvStage, tvEquipment, tvDuration, tvCrt, tvSuggestion;
    RecyclerView rvExercises;
    ImageView imgProgram;
    Button btnOpen, btnView;
    ImageButton btnSearch;
    FirebaseFirestore db;
    DocumentReference refUser, refProgram, refDay;
    CollectionReference refPrograms;
    CardView crtDayLayout;
    CollectionReference refExercises;

    AlertDialog dialog;
    AlertDialog.Builder builder;
    ExerciseAdapter exerciseAdapter;
    Day currentDay;
    ArrayList<Exercise>execises=new ArrayList<Exercise>();

    Map<String,Boolean> trainingDays = new HashMap<String, Boolean>();
    Intent planIntent;
    Boolean trday=new Boolean(Boolean.FALSE);
    Gymnast gymnast;
    int level;
    HashMap<String, LocalDate>annualPhases=new LinkedHashMap<String, LocalDate>();
    HashMap<String, String> planValues=new HashMap<String , String>();
    MacroCycle macroCycle;

    PreferencesKeys preferencesKeys;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        btnPlan=(Button) view.findViewById(R.id.btnPlan);
        tvHi=(TextView)view.findViewById(R.id.tvHi);
        tvPlan=(TextView)view.findViewById(R.id.tvplan);

        tvName=(TextView)view.findViewById(R.id.tvProgramName);
        tvCateg=(TextView)view.findViewById(R.id.tvProgramCateg);
        tvDay=(TextView)view.findViewById(R.id.tvProgramDay);
        imgProgram=(ImageView) view.findViewById(R.id.imgProgramEnrolled);
        tvCrt=(TextView)view.findViewById(R.id.tvCurrent);
        btnOpen=(Button) view.findViewById(R.id.btnOpen);
        crtDayLayout=(CardView) view.findViewById(R.id.currentDay);

        btnSearch=(ImageButton) view.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(view1 -> {
            Map<String,String> options = new HashMap<String, String>();
            Intent intent=new Intent(getActivity(), ProgramsActivity.class);
            options.put("level", "level"+gymnast.getLevel());
            intent.putExtra("options", (Serializable) options);
            startActivity(intent);
        });

        preferencesKeys=new PreferencesKeys(getContext());

        db = FirebaseFirestore.getInstance();
        refUser=db.collection("users").document(preferencesKeys.getUserKey());

        checkTrainingDay();

        gymnast = (Gymnast) getArguments().getSerializable("gym");
        tvHi.setText("Hi, "+gymnast.getName()+"!");
        level=gymnast.getLevel();

        macroCycle=new MacroCycle(getContext(), level, (MainActivity)getContext());
        macroCycle.getNextLastCompetition();

        btnOpen.setOnClickListener(view1 -> {
            openDay();
        });

        tvSuggestion=(TextView)view.findViewById(R.id.tvSuggestion);
        return view;
    }

    private void countCategoriesRoom(OnCategoryCountsRetrievedListenerRoom listener) {
        new Thread(() -> {
            List<CategoryCount> categoryCounts = DatabaseManager.getInstance(getContext()).getDao().getCategoryCounts();
            listener.onCategoryCountsRetrievedRoom(categoryCounts);
        }).start();
    }

    public interface OnCategoryCountsRetrievedListenerRoom {
        void onCategoryCountsRetrievedRoom(List<CategoryCount> categoryCountRoom);
    }

    private void countCategoriesFirestore(){
        refPrograms=db.collection("programs").document("programList")
                .collection("Levels").document("level"+level)
                .collection("Courses");

        refPrograms.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> categoryCountFB = new HashMap<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String category = document.getString("category");
                        if (categoryCountFB.containsKey(category)) {
                            categoryCountFB.put(category, categoryCountFB.get(category) + 1);
                        } else {
                            categoryCountFB.put(category, 1);
                        }
                    }

                    countCategoriesRoom(categoryCountRoom -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            StringBuilder missingCategories = new StringBuilder();

                            if (categoryCountRoom.size() == 0) {
                                missingCategories.append(getResources().getString(R.string.tryPrograms));
                            } else {
                                if (categoryCountRoom.size() < categoryCountFB.size()) {

                                    missingCategories.append(getResources().getString(R.string.tryNext)+" "+getResources().getString(R.string.tryCategory)+" ");

                                    missingCategories.append(categoryCountFB.keySet().stream()
                                            .filter(key -> categoryCountRoom.stream()
                                                    .noneMatch(obj -> obj.getcategory().equals(key)))
                                            .collect(Collectors.joining(", ")));
                                } else {
                                    int minCount = categoryCountRoom.stream()
                                            .mapToInt(CategoryCount::getCount)
                                            .min()
                                            .orElseThrow(NoSuchElementException::new);

                                    missingCategories.append(categoryCountRoom.stream()
                                            .filter(obj -> obj.getCount() == minCount)
                                            .map(CategoryCount::getcategory)
                                            .collect(Collectors.joining(", ")));

                                }

                                missingCategories.append("!");

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvSuggestion.setText(missingCategories.toString());
                                    }
                                });
                            }
                        }
                    });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                });
    }

        @Override
    public void onResume() {
        super.onResume();

            countCategoriesFirestore();

            if(currentDay==null){
                getCurrentProgramDay();
                if(trday.equals(Boolean.TRUE)){
                    tvDay.setVisibility(View.VISIBLE);
                    btnOpen.setVisibility(View.VISIBLE);
            }else{
                    tvDay.setVisibility(View.GONE);
                    btnOpen.setVisibility(View.GONE);
                }
        }
    }

    private void getDay(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate today=LocalDate.now();
            String day=today.getDayOfWeek().toString().toLowerCase();
            trday= trainingDays.get(day);
        }
    }

    private void checkTrainingDay(){
        refUser.get().addOnSuccessListener(documentSnapshot -> {
                    trainingDays = (Map<String, Boolean>) documentSnapshot.get("trainingDays");

                    getDay();
                    getCurrentProgramDay();

                    if(trday.equals(Boolean.TRUE)) {
                            tvDay.setVisibility(View.VISIBLE);
                            btnOpen.setVisibility(View.VISIBLE);

                            tvPlan.setText(R.string.planzilnic);
                            btnPlan.setVisibility(View.VISIBLE);


                            getDaySpecs(day -> {
                                    MainActivity activity = (MainActivity) getActivity();
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("day", day);
                                    activity.getPhases(bundle);
                            });


                            btnPlan.setOnClickListener(view1 -> {
                                macroCycle.getNextLastCompetition();
                                planValues.put("phase", macroCycle.getTrainingStage());
                                planValues.put("level", String.valueOf(level));

                                planIntent=new Intent(getActivity(), DailyPlanActivity.class);
                                planIntent.putExtra("phase", planValues);

                                if(enrolledProgram==null){
                                    startActivity(planIntent);
                                }else{
                                    getDaySpecs(day -> {
                                        planIntent.putExtra("day", day);

                                        getExercises(exerciseList -> {
                                            planIntent.putExtra("exercises", exerciseList);
                                            startActivity(planIntent);
                                        });

                                    });
                                }
                            });
                    }else
                    if(trday.equals(Boolean.FALSE)){
                        tvPlan.setText(R.string.zilibera);
                        tvDay.setVisibility(View.GONE);
                        btnOpen.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Fail", Toast.LENGTH_SHORT).show();
                });
    }

    EnrolledProgram enrolledProgram;
    private void getCurrentProgramDay(){
        new Thread(()-> {
            enrolledProgram = DatabaseManager.getInstance(getContext()).getDao().getCurrentProgram(level);
            if(enrolledProgram!=null){
                refProgram=db.collection("programs").document("programList")
                        .collection("Levels").document("level"+level)
                        .collection("Courses").document(enrolledProgram.getFirebaseKey());
                loadDay();

            }

            getActivity().runOnUiThread(() -> {
                if(enrolledProgram==null)
                    tvCrt.setText(R.string.addProgram);
            });

        }).start();
    }

    private void loadDay(){
        refProgram.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Program program=documentSnapshot.toObject(Program.class);

                    if(program.getNoDays() >= enrolledProgram.getCurrentDay()) {
                        tvName.setText(program.getName());
                        tvCateg.setText("Category: " + program.getCategory());

                        if(preferencesKeys.checkDelaysToday()){
                            tvDay.setText("Current day: " + enrolledProgram.getCurrentDay());
                        }else{
                            tvDay.setText("Next day: " + enrolledProgram.getCurrentDay());
                        }

                        Picasso.get().load(program.getImageUrl()).into(imgProgram);

                        tvCrt.setText(R.string.currentEnroll);

                        crtDayLayout.setVisibility(View.VISIBLE);
                    }else{
                        crtDayLayout.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openDay(){
        builder=new AlertDialog.Builder(getContext());
        final View popup=getLayoutInflater().inflate(R.layout.enrolled_day, null);
        tvDuration=(TextView) popup.findViewById(R.id.tvDayDuraion);
        tvEquipment=(TextView) popup.findViewById(R.id.tvDayEquipment);
        tvStage=(TextView) popup.findViewById(R.id.tvDayStage);
        rvExercises=(RecyclerView) popup.findViewById(R.id.rvDayExercises);
        btnView=(Button) popup.findViewById(R.id.btnView);
        btnView.setOnClickListener(view1 -> {
            dialog.dismiss();
        });

        getDaySpecs(day -> {
            tvStage.setText("The following exercises will be added to: "+day.getStage());
            tvDuration.setText("Duration: "+day.getDuration()+" minutes");
            tvEquipment.setText("Required equipment: "+day.getEquipment());
        });

        getExercises(exerciseList -> {
            exerciseAdapter = new ExerciseAdapter(execises);
            rvExercises.setAdapter(exerciseAdapter);
            rvExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        });

        builder.setView(popup);
        dialog=builder.create();
        dialog.show();
    }

    private interface FirestoreCallbackDay{
        void onCallBackDay(Day day);
    }

    private void getDaySpecs(FirestoreCallbackDay firestoreCallbackDay){
        if(enrolledProgram!=null) {
            refDay = refProgram.collection("Days").document("day" + enrolledProgram.getCurrentDay());
            refDay.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        currentDay = documentSnapshot.toObject(Day.class);
                        firestoreCallbackDay.onCallBackDay(currentDay);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private interface FirestoreCallbackExercises{
        void onCallBackExercises(ArrayList<Exercise> exerciseList);
    }

    private void getExercises(FirestoreCallbackExercises firestoreCallbackExercises){
       execises.clear();
            refExercises = refDay.collection("Exercises");
            refExercises.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Exercise exercise = documentSnapshot.toObject(Exercise.class);
                            execises.add(exercise);
                        }

                        Comparator<Exercise> comparator = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            comparator = Comparator.comparingInt(o -> o.getStep());
                        }
                        Collections.sort(execises, comparator);

                        firestoreCallbackExercises.onCallBackExercises(execises);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    });
    }
}