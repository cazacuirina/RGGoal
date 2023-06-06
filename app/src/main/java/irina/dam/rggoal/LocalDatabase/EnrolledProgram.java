package irina.dam.rggoal.LocalDatabase;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.checkerframework.common.aliasing.qual.Unique;

import java.time.LocalDate;

import javax.annotation.Nullable;

@Entity(tableName="programs")
public class EnrolledProgram {
    @PrimaryKey(autoGenerate = true)
    int id;

    @Unique
    private String firebaseKey;
    @Unique
    private LocalDate startDate;
    @Nullable
    private LocalDate endDate;
    private String category;
    private int currentDay;
    private int level;

    @Ignore
    public EnrolledProgram(){}

    public EnrolledProgram(String firebaseKey, LocalDate startDate, String category, int currentDay, int level) {
        this.firebaseKey = firebaseKey;
        this.startDate = startDate;
        this.currentDay=currentDay;
        this.category = category;
        this.level = level;
    }

    @Override
    public String toString() {
        return "EnrolledPrograms{" +
                "firebaseKey='" + firebaseKey + '\'' +
                ", startDate=" + startDate +
                ", currentDay=" + currentDay +
                ", endDate=" + endDate +
                ", category='" + category + '\'' +
                ", level=" + level +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
