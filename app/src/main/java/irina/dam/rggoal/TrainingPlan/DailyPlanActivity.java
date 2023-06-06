package irina.dam.rggoal.TrainingPlan;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import irina.dam.rggoal.SharedPrefs.PreferencesKeys;
import irina.dam.rggoal.TrainingPlan.Exercises.Exercise;
import irina.dam.rggoal.LocalDatabase.Competition;
import irina.dam.rggoal.LocalDatabase.DatabaseManager;
import irina.dam.rggoal.LocalDatabase.EnrolledProgram;
import irina.dam.rggoal.StartApp.MainActivity;
import irina.dam.rggoal.Programs.Day;
import irina.dam.rggoal.Programs.Program;
import irina.dam.rggoal.R;
import irina.dam.rggoal.TrainingPlan.TrainingPeriodization.AnnualPhase;
import irina.dam.rggoal.TrainingPlan.TrainingPeriodization.PlanAdapter;
import irina.dam.rggoal.TrainingPlan.TrainingPeriodization.TrainingStage;

public class DailyPlanActivity extends AppCompatActivity  {

    RecyclerView rvPlan;
    ArrayList<TrainingStage>stages=new ArrayList<TrainingStage>();
    ArrayList<Exercise>execises=new ArrayList<Exercise>();
    ArrayList<String> exercisesKeys=new ArrayList<String>();
    PlanAdapter adapter;
    FirebaseFirestore db;
    TextView tvPhase, tvIntensity, tvVolume, tvTech, tvTotalDuration;
    int i=0;
    DocumentReference refPhase, refProgram, refLevel, refUser;
    CollectionReference refStage;
    DocumentReference refExercises;
    HashMap<String, Object> planValues=new HashMap<String , Object>();
    int level=0;
    String phase="";
    int[] delays=new int[3];

    ArrayList<Exercise>programAddedExercises=new ArrayList<Exercise>();
    Day day;

    PreferencesKeys preferencesKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_plan);

        sharedPreferences = getSharedPreferences("preferinte", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        rvPlan=findViewById(R.id.rvPlan);
        tvPhase=findViewById(R.id.tvPhase);
        tvIntensity=findViewById(R.id.tvIntensity);
        tvVolume=findViewById(R.id.tvVolume);
        tvTech=findViewById(R.id.tvTechnical);

        tvTimer=findViewById(R.id.tvTimer);
        tvStageTimer=findViewById(R.id.tvTimerStage);
        btnStartPause=findViewById(R.id.btnStartPause);
        btnReset=findViewById(R.id.btnReset);
        btnNext=findViewById(R.id.btnNextStage);
        btnStartPause.setOnClickListener(view -> {
            startTimer();
        });
        btnReset.setOnClickListener(view -> {
            resetTimer();
        });
        btnNext.setOnClickListener(view -> {
            getNextStage();
        });
        timer=new Timer();
        tvTotalDuration=findViewById(R.id.tvTotalDuration);

        preferencesKeys=new PreferencesKeys(this);
        adjustDuration();

            if(checkAlreadyFinishedTraining()){
                hideTimer();
                tvTotalDuration.setText(getString(R.string.trainingCompleted));
            }

        getPlanValues();


        tvPhase.setText("Current phase: "+phase.substring(0, 1).toUpperCase() + phase.substring(1));

        db=FirebaseFirestore.getInstance();
         refPhase=db.collection("annualPhases").document("AnnualPhases")
                .collection("Level"+level).document(phase);
        refStage=refPhase.collection("TrainingStages");

        adapter=new PlanAdapter(this, stages, execises, delays);

        rvPlan.setAdapter(adapter);
        rvPlan.setLayoutManager(new LinearLayoutManager(this));


        getPhase();

        readStages((keys, stages1) -> {

        for(i=0; i<keys.size(); i++)
        {
            refExercises=refStage.document(exercisesKeys.get(i));
            readExercises((exercise, poz) -> {
                List<Exercise> selectedExercises=generateExerciseList(exercise, stages.get(poz));

                Comparator<Exercise> comparator = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    comparator = Comparator.comparingInt(o -> o.getStep());
                }
                Collections.sort(selectedExercises, comparator);

                if(phase.equals("precompetitional") || phase.equals("competitional")){
                    Exercise exercise1=getCompetitionApparatus(stages1.get(poz));
                    if(exercise1!=null){
                        selectedExercises.add(exercise1);
                    }
                }

                if(day!=null && programAddedExercises.size()>0 && stages1.get(poz).getName().equals(day.getStage())){
                    selectedExercises.addAll(programAddedExercises);
                    stages1.get(poz).setDuration(stages1.get(poz).getDuration()+day.getDuration());
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if(Math.abs(delays[poz]) > 0.5 * stages1.get(poz).getDuration() ){
                        selectedExercises.stream()
                                .forEach(obj -> obj.setReps(obj.getReps() - obj.getReps()/ 4));
                    }
                }

                delays[poz]=(Math.abs(delays[poz]) > 0.25 * stages1.get(poz).getDuration() ) ? (delays[poz]/4) : 0;
                durations[poz]=stages1.get(poz).getDuration() + delays[poz];

                if(!checkAlreadyFinishedTraining()){
                    setTotalDuration();
                }
                stages1.get(poz).setExercises((ArrayList)selectedExercises);
                adapter.notifyDataSetChanged();
            });
        }
        });
    }
    
    private void hideTimer(){
        btnNext.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
        btnStartPause.setVisibility(View.GONE);
        tvTimer.setVisibility(View.GONE);
    }

  private void adjustDuration(){
      String delayKey=preferencesKeys.getLastDelayKey();
      if(delayKey!=null){
          delays=preferencesKeys.getIntsFromPrefs(delayKey);
      }
  }

    private boolean checkAlreadyFinishedTraining() {
        boolean done=false;
        String delayToday=preferencesKeys.getDelaysToday();
        if( delayToday!=null && !delayToday.isEmpty()){
            done=true;
        }
        return done;
    }

    private void setTotalDuration(){
        int totalMinutes = 0;
        for (int i = 0; i < durations.length; i++) {
            totalMinutes += durations[i];
        }

        StringBuilder duration=new StringBuilder();
        duration.append("Training duration ");
        int hours = totalMinutes / 60;
        if(hours>0){
            duration.append(hours+"h : ");
        }
        int remainingMinutes = totalMinutes % 60;
        duration.append(remainingMinutes+"m");
        tvTotalDuration.setText(duration.toString());
    }

    private void getPlanValues(){
        planValues=(HashMap<String, Object>) getIntent().getSerializableExtra("phase");
        Object lvlMap=planValues.get("level");
        level=Integer.parseInt(String.valueOf(lvlMap));
        Object phaseMap=planValues.get("phase");
        phase=String.valueOf(phaseMap);

        day=(Day) getIntent().getSerializableExtra("day");
        programAddedExercises=(ArrayList<Exercise>) getIntent().getSerializableExtra("exercises");
    }

    private void getPhase(){
        refPhase.get()
                .addOnSuccessListener(documentSnapshot -> {
                    AnnualPhase phase=documentSnapshot.toObject(AnnualPhase.class);

                    tvIntensity.setText(phase.getIntensity()+"%");
                    tvVolume.setText(phase.getVolume()+"%");
                    tvTech.setText(phase.getTechnicalPreparation()+"%");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    String exName="", exSpecs="";
    private Exercise getCompetitionApparatus(TrainingStage stage){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Exercise>exerciseFuture=executor.submit(() -> {
            if(stage.getName().equals("main part")){

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        Competition competition=DatabaseManager.getInstance(getApplicationContext()).getDao().getNextCompetition(LocalDate.now());
                        if(competition!=null){
                            exName=phase.equals("precompetitional")?"learn competitional routines":"rehearse competitional routines";
                            exSpecs=phase.equals("precompetitional")?"without music":"with music";

                            Exercise exercise=new Exercise(exName, exSpecs, competition.getNoApparatus(), 0);
                            return exercise;
                        }
                    }
             }
            return null;
        });

        Exercise routine=null;
        try{
            routine=exerciseFuture.get();
        }catch  (InterruptedException | ExecutionException e) {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
        executor.shutdown();

        return routine;

    }

    private String formattedDay(LocalDate date){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String formattedDay = date.format(formatter);
            return formattedDay;
        }
        return null;
    }
    
    String exerciseKeyPrefs;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private List<Exercise> generateExerciseList(List<Exercise>exercises, TrainingStage trainingStage){

        sharedPreferences = getSharedPreferences("preferinte", MODE_PRIVATE);
        List<Exercise> selectedExercises=new ArrayList<Exercise>();

        exerciseKeyPrefs=preferencesKeys.getExerciseIndexes(trainingStage.getName());
        if(sharedPreferences.contains(exerciseKeyPrefs)){
            int[] selectedIndexes = preferencesKeys.getIntsFromPrefs(exerciseKeyPrefs);
            for(int selectedIndex: selectedIndexes){
                selectedExercises.add(exercises.get(selectedIndex));
            }

        }else{
            Random random = new Random();
            Set<Integer> selectedIndexes = new HashSet<>();
            StringBuilder stringIndexes = new StringBuilder();

            while (selectedIndexes.size() < trainingStage.getNoExercises()) {
                int randomIndex = random.nextInt(exercises.size());
                if (!selectedIndexes.contains(randomIndex)) {
                    selectedIndexes.add(randomIndex);
                    stringIndexes.append(randomIndex+",");
                    selectedExercises.add(exercises.get(randomIndex));
                }
            }
            editor = sharedPreferences.edit();
            editor.putString(exerciseKeyPrefs, String.valueOf(stringIndexes));
            editor.apply();
        }
        return selectedExercises;
    }

    private void readStages(FirestoreCallbackStages callback){
        refStage.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                for(DocumentSnapshot document:task.getResult()){
                    TrainingStage stage =document.toObject(TrainingStage.class);

                    exercisesKeys.add(document.getId());
                    stages.add(stage);
                }

                sortStages();

                callback.onCallBackStage(exercisesKeys, stages);
            }else{
                Toast.makeText(this,task.getException().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortStages(){
        ArrayList<String> auxkeys=new ArrayList<>();
        ArrayList<TrainingStage> auxStages=new ArrayList<>();
        Comparator<TrainingStage> comparator = null;

        for(int i=0; i<stages.size(); i++){
            auxStages.add(stages.get(i));
            auxkeys.add(exercisesKeys.get(i));
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            comparator = Comparator.comparingInt(o -> o.getStage());
            Collections.sort(stages, comparator);


            Map<Integer, Integer> indexMap = new HashMap<>();
            for (int i = 0; i < stages.size(); i++) {
                int sortedIndex = i;
                int originalIndex = auxStages.indexOf(stages.get(i));
                indexMap.put(sortedIndex, originalIndex);
            }

            for(int i=0; i<exercisesKeys.size(); i++){
                int index=indexMap.get(i);
                exercisesKeys.set(i, auxkeys.get(index));
            }
        }
    }

    private void readExercises(FirestoreCallbackExercises callback2){
        int stagePosition=exercisesKeys.indexOf(refExercises.getId());

            refExercises.collection("Exercises").get()
                    .addOnCompleteListener(task -> {
                        ArrayList<Exercise>execisesList=new ArrayList<Exercise>();
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document:task.getResult()) {
                                Exercise exercise = document.toObject(Exercise.class);
                                execisesList.add(exercise);
                            }
                            callback2.onCallBackExercise(execisesList, stagePosition);
                        } else{
                            Toast.makeText(this,task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
    }

    private interface FirestoreCallbackStages{
        void onCallBackStage(List<String>keys, List<TrainingStage>stages);
    }
    private interface FirestoreCallbackExercises{
        void onCallBackExercise(List<Exercise>exercises, int poz);
    }

    TextView tvTimer, tvStageTimer;
    ImageButton btnStartPause, btnReset, btnNext;
    long timeLeftInMillis;
    boolean timerRunning=false;
    Timer timer;
    TimerTask timerTask;
    int[] durations= new int[3];

    private void startTimer() {
        if (!timerRunning) {
            timerTask=new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timeLeftInMillis+=1000;
                            updateTimer();
                        }
                    });
                }
            };

            if(currentStage==0){
                tvStageTimer.setText("Current stage: "+stages.get(0).getName());
                adapter.expandStage(currentStage, true);
                adapter.notifyItemChanged(0);

            }
            timer.scheduleAtFixedRate(timerTask, 0, 1000);
            timerRunning = true;
            btnStartPause.setImageResource(R.drawable.ic_baseline_pause_24);

        }else{
            timerTask.cancel();
            timerRunning = false;
            btnStartPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    private void resetTimer() {
        timeLeftInMillis = 0;
        updateTimer();
    }

    private void setDialog(String title, String stageMessage){

        AlertDialog.Builder message=new AlertDialog.Builder(this);
        message.setTitle(title).setMessage(stageMessage)
                .setIcon(R.drawable.ic_baseline_av_timer_24)
                .setPositiveButton("Yes", (dialog, which)->{

                    navigateStage();

                })
                .setNegativeButton("Cancel", (dialog, which)->{
                    dialog.dismiss();
                });
        message.show();
    }

    private void navigateStage(){
        delays[currentStage]=(int) (timeLeftInMillis/1000 - durations[currentStage]);

        if(currentStage<stages.size()-1){
            currentStage++;

            if(currentStage<stages.size()){
                adapter.expandStage(currentStage, true);
                adapter.notifyItemChanged(currentStage);
            }

            if(currentStage-1>=0){
                adapter.expandStage(currentStage-1, false);
                adapter.notifyItemChanged(currentStage-1);
            }

            tvStageTimer.setText("Current stage: "+stages.get(currentStage).getName());

            if(currentStage==stages.size()-1){
                btnNext.setImageResource(R.drawable.ic_baseline_check_circle_24);
            }


        }else{
            tvStageTimer.setText("Training completed for today. Well done!");
            addDelaysToPrefs();
            updateDays();
            hideTimer();
        }

        tvTimer.setBackgroundColor(Color.TRANSPARENT);
        resetTimer();
    }

    private void getNextStage(){

        //E PE MINUTEEEEE / (1000*60)
        if(timeLeftInMillis / 1000 < durations[currentStage]){
            startTimer();
            String title="Early finish";
            String stageMessage="You finished "+stages.get(currentStage).getName() +" eralier than expected. Do you want to continue?";
            setDialog(title, stageMessage);
        }
        else if(timeLeftInMillis / 1000 > durations[currentStage]){
            startTimer();
            String title="Late finish";
            String stageMessage="You finished "+stages.get(currentStage).getName()+" later than expected. Do you want to continue?";
            setDialog(title, stageMessage);
        }

        if(isLate){
            isLate=false;
        }

    }

    boolean isLate=false;
    int currentStage=0;

    private void updateTimer() {
        int hours = (int) ((timeLeftInMillis / (1000*60*60)) % 24);
        int minutes = (int) ((timeLeftInMillis / (1000*60)) % 60);
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

        //E PE MINUTEEEEE / (1000*60)

        if(timeLeftInMillis / 1000 > durations[currentStage] && !isLate){
            tvTimer.setBackground(getResources().getDrawable(R.drawable.tv_rounded));
            startTimer();

            String stageMessage="";
            if(currentStage<stages.size()-1){
                stageMessage="Allocated time for "+stages.get(currentStage).getName()
                        +" is over. Do you want to continue with "+stages.get(currentStage+1).getName()+"?";
            }else{
                stageMessage="Allocated time for "+stages.get(currentStage).getName()
                        +" is over. Do you want to finish the training?";
            }

            AlertDialog.Builder message=new AlertDialog.Builder(this);
            message.setTitle("Time's up!").setMessage(stageMessage)
                    .setIcon(R.drawable.ic_baseline_av_timer_24)
                    .setPositiveButton("Yes", (dialog, which)->{
                        navigateStage();
                    })
                    .setNegativeButton("No, let me finish", (dialog, which)->{
                        dialog.dismiss();
                        isLate=true;
                        startTimer();
                    });
            message.show();
        }

        tvTimer.setText(timeLeftFormatted);
    }

    private void addDelaysToPrefs(){
        StringBuilder stringDelays = new StringBuilder();

        for(int delay:delays){
            stringDelays.append(delay+",");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            editor.putString(formattedDay(LocalDate.now())+"delays", String.valueOf(stringDelays));
        }
        editor.apply();
    }

    EnrolledProgram enrolledProgram;
    private void updateDays(){
        new Thread(()-> {
            enrolledProgram = DatabaseManager.getInstance(getApplicationContext()).getDao().getCurrentProgram(level);

            new Thread(()->{
                enrolledProgram.setCurrentDay(enrolledProgram.getCurrentDay()+1);
                DatabaseManager.getInstance(getApplicationContext()).getDao().updateNoDays(enrolledProgram);

                getFinishedPrograms();
            }).start();

        }).start();
    }

    private void getFinishedPrograms(){
        refProgram=db.collection("programs").document("programList")
                .collection("Levels").document("level"+level)
                .collection("Courses").document(enrolledProgram.getFirebaseKey());
        refProgram.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Program program=documentSnapshot.toObject(Program.class);

                    if(program.getNoDays()==enrolledProgram.getCurrentDay()){

                        checkFinishedProgramsLevel();
                        updateEndDate();

                        AlertDialog.Builder message=new AlertDialog.Builder(DailyPlanActivity.this);
                        message.setTitle(R.string.completedProgram).setMessage(R.string.congratsProgram)
                                .setIcon(R.drawable.ic_baseline_done_outline_24)
                                .setPositiveButton("Got it!", (dialog, which)-> {
                                });
                        message.show();
                    }

                    Intent intent=new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEndDate(){
        new Thread(()->{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enrolledProgram.setEndDate(LocalDate.now());
            }

            DatabaseManager.getInstance(getApplicationContext()).getDao().updateDate(enrolledProgram.getEndDate(), enrolledProgram.getFirebaseKey());
        }).start();
    }

    private void updateLevel(){
        refUser=db.collection("users").document(preferencesKeys.getUserKey());
        refUser.update("level", level+1)
                .addOnSuccessListener(unused -> {

                    AlertDialog.Builder message=new AlertDialog.Builder(DailyPlanActivity.this);
                    message.setTitle(R.string.nextLevel).setMessage(R.string.congratsLevel)
                            .setIcon(R.drawable.ic_baseline_done_outline_24)
                            .setPositiveButton("Got it!", (dialog, which)-> {
                            });
                    message.show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkFinishedProgramsLevel(){
        refLevel=db.collection("programs").document("programList")
                .collection("Levels").document("level"+level);
        refLevel.get()
                .addOnSuccessListener(documentSnapshot -> {
                    int noPrograms=documentSnapshot.getLong("noPrograms").intValue();
                    new Thread(()->{
                        int finishedPrograms=DatabaseManager.getInstance(getApplicationContext()).getDao().getFinishedProgramCount(level);
                        if(noPrograms==finishedPrograms){
                            updateLevel();
                        }
                    }).start();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                });
    }
}