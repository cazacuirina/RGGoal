package irina.dam.rggoal.TrainingPlan;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import irina.dam.rggoal.LocalDatabase.Competition;
import irina.dam.rggoal.LocalDatabase.DatabaseManager;
import irina.dam.rggoal.StartApp.MainActivity;

public class MacroCycle {

    Context context;
    Competition nextCompetition, lastCompetition;
    HashMap<String, LocalDate> annualPhases=new LinkedHashMap<String, LocalDate>();
    int level;
    MainActivity mainActivity;
    String trainingStage="";

    public MacroCycle(Context context, int level, MainActivity mainActivity){
        this.context=context;
        this.level=level;
        this.mainActivity=mainActivity;
    }

    public String getTrainingStage() {
        return trainingStage;
    }

    public void getNextLastCompetition(){
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<Competition> futureNextCompetition = executorService.submit(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Competition competition= DatabaseManager.getInstance(context).getDao().getNextCompetition(LocalDate.now());
                return competition;
            }
            return null;

        });
        Future<Competition> futureLastCompetition = executorService.submit(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Competition competition= DatabaseManager.getInstance(context).getDao().getLastCompetition(LocalDate.now());
                return competition;
            }
            return null;
        });

        try{
            nextCompetition=futureNextCompetition.get();
            lastCompetition=futureLastCompetition.get();

            LocalDate lastRevovery=null, lastCompDate=null;
            if(lastCompetition!=null){
                lastRevovery= checkRecovery(lastCompetition.getCreatedAt(), lastCompetition.getDate());
                lastCompDate=lastCompetition.getDate();
            }

            if(nextCompetition!=null){
                getTrainingStage(lastCompDate, lastRevovery, nextCompetition.getCreatedAt(), nextCompetition.getDate());
            }else{
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    LocalDate today = LocalDate.now();
                    LocalDate firstDayOfYear = today.with(TemporalAdjusters.firstDayOfYear());
                    LocalDate lastDayOfYear = today.with(TemporalAdjusters.lastDayOfYear());

                    getTrainingStage(lastCompDate, lastRevovery, firstDayOfYear, lastDayOfYear);
                }
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private LocalDate checkRecovery(LocalDate firstDate, LocalDate secondDate){
        LocalDate lastRevovery=null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            long daysBetween = ChronoUnit.DAYS.between(firstDate, secondDate);
            int microcycleDuration=Math.round((float)daysBetween/13);
            lastRevovery=secondDate.plusDays(microcycleDuration);
        }
        return lastRevovery;
    }

    private void getTrainingStage(LocalDate lastCompDate, LocalDate lastRecovery, LocalDate firstDate, LocalDate secondDate){
        long daysBetween = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate today=LocalDate.now();
            daysBetween = ChronoUnit.DAYS.between(firstDate, secondDate);
            LocalDate recoveryDate, adaptationDate, learningDate, precompDate;
            int microcycleDuration=Math.round((float)daysBetween/13);

            if(lastCompDate!=null && lastRecovery!=null){
                long delay = ChronoUnit.DAYS.between(lastCompDate, lastRecovery);
                firstDate  = firstDate.plusDays(delay);
            }

            adaptationDate  = firstDate.plusDays(4 * microcycleDuration);
            learningDate = adaptationDate.plusDays(4 * microcycleDuration);
            precompDate  = learningDate.plusDays(3 * microcycleDuration);
            recoveryDate  = secondDate.plusDays(microcycleDuration);

            annualPhases.put("adaptation", firstDate);
            annualPhases.put("learning", adaptationDate);
            annualPhases.put("precompetitional", learningDate);
            annualPhases.put("competitional", precompDate);
            annualPhases.put("recovery", secondDate);
            annualPhases.put("endPhase", recoveryDate);

            if(lastCompDate!=null){
                if(today.isAfter(lastCompDate) && (today.isBefore(lastRecovery)||today.isEqual(lastRecovery)))
                    trainingStage="recovery";
            }

            else if((today.isEqual(firstDate)||today.isAfter(firstDate))
                    && (today.isBefore(adaptationDate)||today.isEqual(adaptationDate)))
                trainingStage="adaptation";
            else if(today.isAfter(adaptationDate) && (today.isBefore(learningDate)||today.isEqual(learningDate)))
                trainingStage="learning";
            else if(today.isAfter(learningDate) && (today.isBefore(precompDate)||today.isEqual(precompDate)))
                trainingStage="precompetitional";
            else if(today.isAfter(precompDate) && (today.isBefore(secondDate)||today.isEqual(secondDate)))
                trainingStage="competitional";
           sendBundle();
        }

    }

    private void sendBundle(){
        Bundle bundle = new Bundle();
        bundle.putInt("level", level);
        bundle.putSerializable("phases", annualPhases);
        bundle.putString("currentPhase", trainingStage);
        mainActivity.getPhases(bundle);
    }
}
