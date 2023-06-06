package irina.dam.rggoal.TrainingPlan.TrainingPeriodization;

import java.io.Serializable;
import java.util.ArrayList;

import irina.dam.rggoal.TrainingPlan.Exercises.Exercise;

public class TrainingStage implements Serializable {
    String name;
    int duration;
    int stage;
    boolean expandable;
    int noExercises;
    ArrayList<Exercise>exercises=new ArrayList<Exercise>();

    public TrainingStage(){}

    public TrainingStage(String name, int duration, ArrayList<Exercise> exercises, int stage, int noExercises) {
        this.name = name;
        this.duration = duration;
        this.expandable = false;
        this.exercises = exercises;
        this.stage=stage;
        this.noExercises=noExercises;
    }

    @Override
    public String toString() {
        return "TrainingStage{" +
                "name='" + name + '\'' +
                ", duration=" + duration +
                ", expandable=" + expandable +
                ", stage=" + stage +
                ", noExercises=" + noExercises +
                ", exercises=" + exercises +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isExpandable() {
        return expandable;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public int getNoExercises() {
        return noExercises;
    }

    public void setNoExercises(int noExercises) {
        this.noExercises = noExercises;
    }
}
