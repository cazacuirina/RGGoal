package irina.dam.rggoal.LocalDatabase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@TypeConverters(DateConverter.class)

@Database(entities = {Competition.class, EnrolledProgram.class}, version = 1)
public abstract class DatabaseManager extends RoomDatabase{
    public static volatile DatabaseManager instance;
    public static synchronized DatabaseManager getInstance(Context context){
        if(instance==null){
            instance= Room.databaseBuilder(context, DatabaseManager.class, "goals.db")
                    .fallbackToDestructiveMigration().build();
        }
        return instance;
    }

    public abstract DaoGoals getDao();
}
