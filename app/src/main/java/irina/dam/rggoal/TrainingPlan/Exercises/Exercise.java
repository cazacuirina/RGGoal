package irina.dam.rggoal.TrainingPlan.Exercises;

import java.io.Serializable;

public class Exercise implements Serializable {
    String name;
    String specs;
    int reps;
    int step;

    public Exercise(){}

    public Exercise(String name, String specs, int reps, int step) {
        this.name = name;
        this.specs = specs;
        this.reps = reps;
        this.step=step;
    }

    @Override
    public String toString() {
        return "Exercise{" +
                "name='" + name + '\'' +
                ", specs='" + specs + '\'' +
                ", reps=" + reps +
                ", step=" + step +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
}
