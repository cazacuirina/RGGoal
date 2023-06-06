package irina.dam.rggoal.LocalDatabase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.time.LocalDate;
import java.util.List;

import irina.dam.rggoal.Programs.CategoryCount;

@Dao
public interface DaoGoals {
    //COMPETITIONS
    @Query("SELECT COUNT(*) FROM competitions")
    int getCompetitionCount();

    @Query("select * from competitions")
    List<Competition> getAllCompetitions();

    @Query("select * from competitions WHERE date >= :today")
    List<Competition> getFollowingCompetitions(LocalDate today);

    @Insert
    long insertCompetition(Competition competiton);

    @Query("SELECT MIN(date) FROM competitions")
    LocalDate getMinDate();

    @Query("SELECT * FROM competitions WHERE date " +
            "= (SELECT MIN(date) FROM competitions WHERE date >= :day)")
    Competition getNextCompetition(LocalDate day);

    @Query("SELECT * FROM competitions WHERE date " +
            "= (SELECT MAX(date) FROM competitions WHERE date < :day)")
    Competition getLastCompetition(LocalDate day);

    @Update
    void updateCompetition(Competition competition);

    @Query("SELECT * FROM competitions WHERE date >= :today AND date <= :targetDate ORDER BY date - :today LIMIT 1")
    Competition getCompetitionSoon(LocalDate today, LocalDate targetDate);

    @Query("SELECT * FROM competitions WHERE date between :firstDay and :lastDay ORDER BY date")
    List<Competition> getCompetitionsThisMonth(LocalDate firstDay, LocalDate lastDay);

    @Delete
    void deleteCompetition(Competition competition);

    //PROGRAMS
    @Query("SELECT COUNT(*) FROM programs WHERE endDate IS NOT NULL AND level = :level")
    int getFinishedProgramCount(int level);

    @Insert
    public long insertProgram(EnrolledProgram program);

    @Query("SELECT * FROM programs WHERE endDate IS NULL AND level = :level")
    public EnrolledProgram getCurrentProgram(int level);

    @Query("SELECT * FROM programs WHERE endDate IS NULL AND level = :level AND category = :category")
    public EnrolledProgram getCurrentProgramCateg(int level, String category);

    @Query("SELECT * FROM programs WHERE endDate IS NOT NULL AND level = :level AND category = :category")
    public List<EnrolledProgram> getFinishedCategoryPrograms(int level, String category);

    @Update
    void updateNoDays(EnrolledProgram enrolledProgram);

    @Query("UPDATE programs SET endDate = :endDate WHERE firebaseKey = :firebaseKey")
    void updateDate(LocalDate endDate, String firebaseKey);

    @Query("SELECT * FROM programs WHERE (endDate IS NOT NULL AND endDate between :firstDay and :lastDay) OR (startDate between :firstDay and :lastDay) ")
    List<EnrolledProgram> getProgramsThisMonth(LocalDate firstDay, LocalDate lastDay);

    @Query("SELECT category, COUNT(*) AS count FROM programs GROUP BY category")
    List<CategoryCount> getCategoryCounts();
}
