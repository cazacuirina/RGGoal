package irina.dam.rggoal.Updates;

import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import irina.dam.rggoal.LocalDatabase.Competition;
import irina.dam.rggoal.LocalDatabase.DatabaseManager;
import irina.dam.rggoal.R;

public class MorningBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        getCompetitionSoon(context);
        updatePrefs(context);
    }

    Competition competition;
    LocalDate today, threeDays;
    long daysLeft;
    private void getCompetitionSoon(Context context){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            today=LocalDate.now();
            threeDays= ChronoUnit.DAYS.addTo(LocalDate.now(), 3);
        }

        new Thread(()->{
            competition = DatabaseManager.getInstance(context).getDao().getCompetitionSoon(today, threeDays);
            String message="", text="";

            if(competition!=null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    daysLeft=ChronoUnit.DAYS.between(today, competition.getDate());

                    if(daysLeft>0){
                        text="Competition soon";
                        message="Next competition: "+competition.getName()+" in "+daysLeft+" days";
                    }else{
                        text="Competition today";
                        message="Good luck at "+competition.getName()+". You will do great!";
                    }
                }
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                        .setSmallIcon(R.drawable.ic_baseline_calendar_month_24)
                        .setContentTitle(text)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(0, builder.build());
            }
        }).start();
    }

    String formatted2days, formattedYesterday;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private void updatePrefs(Context context){
        prefs = context.getSharedPreferences("preferinte", MODE_PRIVATE);
        editor = prefs.edit();

        Map<String, ?> allPrefs = prefs.getAll();

        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            String key = entry.getKey();
            DateTimeFormatter formatter = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                formattedYesterday = today.minusDays(1).format(formatter);
                formatted2days = today.minusDays(2).format(formatter);
            }

            if((key.contains(formattedYesterday) && !key.contains("delays")) || (key.contains(formatted2days) && key.contains(formatted2days))){
                editor.remove(key);
                editor.apply();
            }
        }
    }
}