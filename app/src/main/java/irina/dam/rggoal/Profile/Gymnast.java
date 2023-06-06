package irina.dam.rggoal.Profile;

import java.io.Serializable;

public class Gymnast implements Serializable {
    private String name;
    private String url;
    private int birthYear;
    private int startYear;
    private int level;

    public Gymnast(){}

    public Gymnast(String name, String url,int birthYear, int startYear, int level) {
        this.name = name;
        this.url = url;
        this.birthYear = birthYear;
        this.startYear = startYear;
        this.level = level;
    }

    @Override
    public String toString() {
        return "Gymnast{" +
                "name='" + name + '\'' +
                ", url=" + url +
                ", birthYear=" + birthYear +
                ", startYear=" + startYear +
                ", level=" + level +
                '}';
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
