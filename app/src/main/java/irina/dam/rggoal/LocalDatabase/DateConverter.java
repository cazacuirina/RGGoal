package irina.dam.rggoal.LocalDatabase;

import android.os.Build;

import androidx.room.TypeConverter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateConverter {
    @TypeConverter
    public static LocalDate toDate(Long timestamp) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(timestamp!=null){
                return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
            }
        }
        return null;
    }

    @TypeConverter
    public static Long toTimestamp(LocalDate date) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(date!=null){
                return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
        }
        return null;
    }
}
