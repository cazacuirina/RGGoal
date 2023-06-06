package irina.dam.rggoal.SharedPrefs;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class PreferencesKeys {
    SharedPreferences sharedPreferences;
    Context context;

    public PreferencesKeys(Context context){
        this.context=context;
        this.sharedPreferences = context.getSharedPreferences("preferinte", MODE_PRIVATE);
    }

    public String getUserKey(){
        String gymnastKey=sharedPreferences.getString("firebaseKey", "");
        return gymnastKey;
    }

    public String getLastDelayKey() {
        String delayKey = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDate today = LocalDate.now();

        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            delayKey = entry.getKey();
            if (delayKey.endsWith("delays")) {

                    String dateString = delayKey.substring(0, 10);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    LocalDate date = LocalDate.parse(dateString, formatter);

                    if (date.isBefore(today)) {
                        return  delayKey;
                    }
                }
            }
        }
        return null;
    }

    public String getDelaysToday(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String prefsDelays=LocalDate.now().format(df);
            if(sharedPreferences.contains(prefsDelays+"delays")){
                return sharedPreferences.getString(prefsDelays+"delays", "");
            }
        }
        return null;
    }

    public String getExerciseIndexes(String stage){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String formattedDay = LocalDate.now().format(formatter);
            String exerciseKey=formattedDay+stage;
            return exerciseKey;
        }
        return  null;
    }

    public int[] getIntsFromPrefs(String prefsKey){
        String prefsString = sharedPreferences.getString(prefsKey, "");
        String[] prefsArray = prefsString.split(",\\s*");
        int[] intArray = new int[prefsArray.length];
        for (int i = 0; i < prefsArray.length; i++) {
            intArray[i] = Integer.parseInt(prefsArray[i]);
        }
        return intArray;
    }
}
