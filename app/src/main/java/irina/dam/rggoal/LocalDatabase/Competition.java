package irina.dam.rggoal.LocalDatabase;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.checkerframework.common.aliasing.qual.Unique;

import java.time.LocalDate;

@Entity(tableName="competitions")
public class Competition {
    @PrimaryKey(autoGenerate = true)
    int id;

    private String name;
    @Unique
    private LocalDate date;
    private int noApparatus;
    private LocalDate createdAt;

    @Ignore
    public Competition(){}

    public Competition(String name, LocalDate date, int noApparatus) {
        this.name = name;
        this.date = date;
        this.noApparatus = noApparatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getNoApparatus() {
        return noApparatus;
    }

    public void setNoApparatus(int noApparatus) {
        this.noApparatus = noApparatus;
    }

    @Override
    public String toString() {
        return "Competiton{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date=" + date +
                ", createdAt=" + createdAt +
                ", noApparatus=" + noApparatus +
                '}';
    }
}
