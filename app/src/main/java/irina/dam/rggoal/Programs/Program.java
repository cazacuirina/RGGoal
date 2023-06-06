package irina.dam.rggoal.Programs;

import java.io.Serializable;

public class Program implements Serializable {
    private int noDays;
    private String name;
    private String imageUrl;
    private String category;
    private String key;

    public Program(){}

    public Program(int noDays, String name, String imageUrl, String category, String key) {
        this.noDays = noDays;
        this.name = name;
        this.imageUrl = imageUrl;
        this.key = key;
        this.category = category;
    }

    @Override
    public String toString() {
        return "Programs{" +
                "noDays=" + noDays +
                ", name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", category='" + category + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getNoDays() {
        return noDays;
    }

    public void setNoDays(int noDays) {
        this.noDays = noDays;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
