package irina.dam.rggoal.Calendar;

import java.time.LocalDate;

enum GoalType{Competition, Program}
public class Goal {
    String name;
    LocalDate startDate;
    LocalDate completedDate;
    GoalType goalType;

    public Goal(){}
    public Goal(String name, LocalDate startDate, LocalDate completedDate, GoalType goalType) {
        this.name = name;
        this.startDate = startDate;
        this.completedDate = completedDate;
        this.goalType = goalType;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "name='" + name + '\'' +
                ", startDate=" + startDate +
                ", completedDate=" + completedDate +
                ", goalType=" + goalType +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public void setGoalType(GoalType goalType) {
        this.goalType = goalType;
    }
}
