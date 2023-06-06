package irina.dam.rggoal.Updates;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.Map;

import irina.dam.rggoal.R;
import irina.dam.rggoal.SharedPrefs.PreferencesKeys;

public class EveningBroadcastReceiver extends BroadcastReceiver {

    PreferencesKeys preferencesKeys;
    @Override
    public void onReceive(Context context, Intent intent) {
        preferencesKeys=new PreferencesKeys(context);
        checkTrainingCompleted(context);
    }

    SharedPreferences prefs;
    DocumentReference refGymnast;
    FirebaseFirestore db;
    Map<String, Boolean>trainingDays;
    private void checkTrainingCompleted(Context context){
        db=FirebaseFirestore.getInstance();
        refGymnast=db.collection("users").document(preferencesKeys.getUserKey());
        refGymnast.get()
                .addOnSuccessListener(documentSnapshot -> {
                    trainingDays = (Map<String, Boolean>) documentSnapshot.get("trainingDays");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LocalDate today=LocalDate.now();
                        String day = today.getDayOfWeek().toString().toLowerCase();
                        boolean isTrainingDay= trainingDays.get(day);
                        if(isTrainingDay){
                            if(preferencesKeys.getDelaysToday()==null){
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                                        .setSmallIcon(R.drawable.ic_baseline_calendar_month_24)
                                        .setContentTitle(context.getString(R.string.trainingCompleted))
                                        .setContentText(context.getString(R.string.rescheduleTraining))
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setAutoCancel(true);

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                                notificationManager.notify(0, builder.build());
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                });
    }

}
