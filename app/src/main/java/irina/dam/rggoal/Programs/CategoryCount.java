package irina.dam.rggoal.Programs;

public class CategoryCount {
    String category;
    int count;

    public CategoryCount(String category, int count) {
        this.category = category;
        this.count = count;
    }

    public String getcategory() {
        return category;
    }

    public void setcategory(String category) {
        this.category = category;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "CategoryCounts{" +
                "category=" + category +
                ", count=" + count +
                '}';
    }
}
