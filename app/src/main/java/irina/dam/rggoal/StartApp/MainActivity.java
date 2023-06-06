package irina.dam.rggoal.StartApp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import irina.dam.rggoal.Updates.EveningBroadcastReceiver;
import irina.dam.rggoal.Charts.ChartsFragment;
import irina.dam.rggoal.Profile.Gymnast;
import irina.dam.rggoal.Programs.ProgramsActivity;
import irina.dam.rggoal.R;
import irina.dam.rggoal.Schedule.ScheduleFragment;
import irina.dam.rggoal.SharedPrefs.PreferencesKeys;
import irina.dam.rggoal.databinding.ActivityMainBinding;
//import it.sephiroth.android.library.tooltip.Tooltip;

import irina.dam.rggoal.Updates.MorningBroadcastReceiver;

public class MainActivity extends AppCompatActivity{

    BottomNavigationView navBar;
    ProgressBar pbLoad;
    HomeFragment homeFragment=new HomeFragment();
    ChartsFragment graficFragment=new ChartsFragment();
    ScheduleFragment calendarFragment=new ScheduleFragment();

    DrawerLayout drawerLayout;
    MaterialToolbar toolbar;
    NavigationView navigationView;

    ActivityMainBinding binding;
    FirebaseFirestore db;
    FirebaseStorage storage;
    DocumentReference ref;
    String key="";

    Gymnast gymnast;
    Map<String,Boolean> trainingDays = new HashMap<String, Boolean>();
    Map<String,String> options = new HashMap<String, String>();

    AlarmManager alarmManagerMorning;
    NotificationChannel channel;
    NotificationManager notificationManager;

    de.hdodenhof.circleimageview.CircleImageView navProfilePic;
    ImageButton picButton;

    PreferencesKeys preferencesKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferencesKeys=new PreferencesKeys(this);

        setNotificationManager();
        setAlarmManagerMorning();
        setAlarmManagerEvening();

        binding.navBar.setOnNavigationItemSelectedListener(item -> {
           clickNavigationBar(item);
            return true;
        });

        boolean schedule=getIntent().getBooleanExtra("schedule", false);
        if(schedule==true){
            AlertDialog.Builder message=new AlertDialog.Builder(this);
            message.setTitle(R.string.schedule).setMessage(R.string.welcome).setIcon(R.drawable.ic_baseline_calendar_month_24)
                .setPositiveButton("Got it!", (dialog, which)->{});
            message.show();
        }

        pbLoad=findViewById(R.id.pbLoad);
        navigationView=findViewById(R.id.navDrawer);
        drawerLayout = findViewById(R.id.drawer);
        toolbar=findViewById(R.id.programMenu);
        toolbar.setNavigationOnClickListener(view -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            clickNavigationDrawer(item);
            return true;
        });


            gymnast=new Gymnast();
            db = FirebaseFirestore.getInstance();
            ref=db.collection("users").document(preferencesKeys.getUserKey());
            getUser();

    }

    static  final int PICK_IMG_REQ=100;
    Uri imgUri;
    private void openPicture(){
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMG_REQ);

    }

    String extension="";
    boolean selected=false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMG_REQ && resultCode==RESULT_OK && data!=null){
            imgUri=data.getData();
            Picasso.get().load(imgUri).into(navProfilePic);

            selected=true;
            picButton.setImageResource(R.drawable.ic_baseline_check_24);

            ContentResolver contentResolver=getContentResolver();
            MimeTypeMap mime=MimeTypeMap.getSingleton();
            extension=mime.getExtensionFromMimeType(contentResolver.getType(imgUri));

        }
    }

    AlarmManager alarmManagerEvening;
    private void setAlarmManagerEvening(){
        alarmManagerEvening = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND, 0);
        calendar.setTimeInMillis(System.currentTimeMillis());
        Intent intent = new Intent(this, EveningBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);//flag0
        alarmManagerEvening.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }


    private void setAlarmManagerMorning(){
        alarmManagerMorning = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(this, MorningBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);//flag0
        alarmManagerMorning.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void setNotificationManager(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    Bundle bundleHome = new Bundle();
    private void getUser(){
        ref.get().addOnSuccessListener(documentSnapshot -> {
                    gymnast=documentSnapshot.toObject(Gymnast.class);
                    trainingDays = (Map<String, Boolean>) documentSnapshot.get("trainingDays");

                    loadHeader();

                    bundleHome.putSerializable("gym", gymnast);
                    homeFragment.setArguments(bundleHome);

                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, homeFragment, "HomeFragment").commit();

                    pbLoad.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
                });
    }

    Bundle phases=new Bundle();
    public void getPhases(Bundle bundle){
        phases.putAll(bundle);
    }


    private void clickNavigationBar(MenuItem item){
        switch (item.getItemId()) {

            case R.id.home:{
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, homeFragment, "HomeFragment").commit();
                break;
            }
            case R.id.calendar:{
                Bundle bundle = new Bundle();
                bundle.putSerializable("days", (Serializable) trainingDays);
                bundle.putInt("level", gymnast.getLevel());
                calendarFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction().replace(R.id.frame, calendarFragment, "CalendarFragment").commit();
                break;
            }
            case R.id.charts:{
                graficFragment.setArguments(phases);

                getSupportFragmentManager().beginTransaction().replace(R.id.frame, graficFragment, "GraficFragment").commit();
                break;
            }
        }
    }

    private void clickNavigationDrawer(MenuItem item){
        int id=item.getItemId();
        drawerLayout.closeDrawer(GravityCompat.START);

        options.put("level", "level"+gymnast.getLevel());
        switch (id){
            case R.id.flexibility:{
                options.put("option","flexibility");
                break;
            }
            case R.id.strength:{
                options.put("option","strength");
                break;
            }
            case R.id.bd:{
                options.put("option","BD");
                break;
            }
            case R.id.ad:{
                options.put("option","AD");
                break;
            }
        }

        Intent intent=new Intent(this, ProgramsActivity.class);
        intent.putExtra("options", (Serializable) options);
        startActivity(intent);
    }

    private void loadHeader(){
        View headerView = navigationView.getHeaderView(0);
        TextView navName = (TextView) headerView.findViewById(R.id.name);
        navName.setText(gymnast.getName());

        TextView navYear = (TextView) headerView.findViewById(R.id.born);
        navYear.setText(String.valueOf(gymnast.getBirthYear()));

        TextView navLevel = (TextView) headerView.findViewById(R.id.level);
        switch (gymnast.getLevel()){
            case 1: {
                navLevel.setText("Recreative");
                break;
            }
            case 2: {
                navLevel.setText("Intermediate");
                break;
            }
            case 3: {
                navLevel.setText("Competitive");
                break;
            }
            case 4: {
                navLevel.setText("Elite");
                break;
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int currentYear= LocalDate.now().getYear();
            TextView navExperience = (TextView) headerView.findViewById(R.id.experience);
            navExperience.setText((currentYear-gymnast.getStartYear())+" years");
        }

        navProfilePic = (CircleImageView) headerView.findViewById(R.id.profilePic);
        picButton=(ImageButton) headerView.findViewById(R.id.btnPic);

        if(gymnast.getUrl()!="") {
            picButton.setVisibility(View.GONE);
            StorageReference imageRef =storage.getInstance().getReference("users/")
                    .child(preferencesKeys.getUserKey()).child(gymnast.getUrl());
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Picasso.get().load(uri).into(navProfilePic);
            });
        }

        picButton.setOnClickListener(view -> {
            if(!selected){
                AlertDialog.Builder message = new AlertDialog.Builder(this);
                message.setTitle(R.string.profilePic).setMessage(R.string.chooseProfile).setIcon(R.drawable.ic_baseline_person_24)
                        .setPositiveButton("Ok", (dialog, which) -> {
                            openPicture();
                        })
                        .setNegativeButton("No, thanks", (dialog, which) -> {
                            dialog.dismiss();
                        });
                message.show();
            }else{
                picButton.setVisibility(View.GONE);
                uploadPic(preferencesKeys.getUserKey(), ref, new FirestoreCallback() {
                    @Override
                    public void onCallbackProfilePic(String url) {
                        gymnast.setUrl(url);
                    }
                });
            }
        });
    }

    String urlStorage="";
    private void uploadPic(String id, DocumentReference documentReference, FirestoreCallback callback){

        if(imgUri !=null){
            urlStorage=System.currentTimeMillis()+"."+extension;
            StorageReference imageRef =storage.getInstance().getReference("users/")
                    .child(id).child(urlStorage);

            imageRef.putFile(imgUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        updatePic(documentReference, urlStorage, callback);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updatePic(DocumentReference documentReference, String urlStorage, FirestoreCallback callback){
        documentReference.update("url", urlStorage)
                .addOnSuccessListener(unused -> {
                    callback.onCallbackProfilePic(urlStorage);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                });
    }

    private interface FirestoreCallback{
        void onCallbackProfilePic(String url);
    }
}