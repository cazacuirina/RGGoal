package irina.dam.rggoal.Programs;

import java.io.Serializable;
import java.util.ArrayList;

import irina.dam.rggoal.TrainingPlan.Exercises.Exercise;

public class Day implements Serializable {
    private String stage;
    private int duration;
    private String equipment;
    ArrayList<Exercise> exercises=new ArrayList<Exercise>();

    public Day(){}

    public Day(String stage, int duration, String equipment, ArrayList<Exercise> exercises) {
        this.stage = stage;
        this.duration = duration;
        this.equipment = equipment;
        this.exercises = exercises;
    }

    @Override
    public String toString() {
        return "Day{" +
                "stage='" + stage + '\'' +
                ", duration=" + duration +
                ", equipment='" + equipment + '\'' +
                ", exercises=" + exercises +
                '}';
    }

    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }
}
