package irina.dam.rggoal.Profile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import irina.dam.rggoal.StartApp.MainActivity;
import irina.dam.rggoal.R;

public class QuizProfileActivity extends AppCompatActivity {

    EditText etName;
    ProgressBar loadProgress;
    EditText etBirthYear,etStartYear;
    Spinner spHours;
    CheckBox cb1, cb2, cb3, cb4, cb5;
    RadioGroup rgCompetition;
    RadioButton rb1, rb2, rb3;
    TextView btnSubmit;
    FirebaseFirestore db;
    RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_profil);

        layout=findViewById(R.id.layoutQuiz);
        etName=findViewById(R.id.etName);
        etBirthYear=findViewById(R.id.etBirthYear);
        etStartYear=findViewById(R.id.etStartYear);
        spHours=findViewById(R.id.spinnerHours);
        cb1=findViewById(R.id.cb1);
        cb2=findViewById(R.id.cb2);
        cb3=findViewById(R.id.cb3);
        cb4=findViewById(R.id.cb4);
        cb5=findViewById(R.id.cb5);
        rgCompetition=findViewById(R.id.rgCompete);
        rb1=findViewById(R.id.rb1);
        rb2=findViewById(R.id.rb2);
        rb3=findViewById(R.id.rb3);
        btnSubmit=findViewById(R.id.btnSubmit);

        loadProgress=findViewById(R.id.pbPic);

        List<String> mList=Arrays.asList(getString(R.string.hours1), getString(R.string.hours2), getString(R.string.hours3), getString(R.string.hours4));
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, mList);
        mArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spHours.setAdapter(mArrayAdapter);
    }

    public Gymnast createGymnast() {
        Gymnast gymnast = new Gymnast();

        if (etName.getText().toString().isEmpty()) {
            etName.setError(getString(R.string.nameErr));
            etName.requestFocus();
            return null;
        } else {
            gymnast.setName(etName.getText().toString());
        }

        int birthYear=0, startYear=0;

        if (etBirthYear.getText().toString().isEmpty()){
            etBirthYear.setError(getString(R.string.birthyearErr));
            etBirthYear.requestFocus();
            return null;
        } else {
            try{
                birthYear=Integer.parseInt(etBirthYear.getText().toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if(birthYear> LocalDate.now().getYear() || birthYear<1970){
                        etBirthYear.setError(getString(R.string.birthyearErr));
                        etBirthYear.requestFocus();
                        return null;
                    }
                }
            }catch(NumberFormatException ex){
                etBirthYear.setError(getString(R.string.birthyearErr));
                etBirthYear.requestFocus();
                return null;
            }
            gymnast.setBirthYear(birthYear);
        }

        if (etStartYear.getText().toString().isEmpty()){
            etStartYear.setError(getString(R.string.startYearErr));
            etStartYear.requestFocus();
            return null;
        } else {
            try{
                startYear=Integer.parseInt(etStartYear.getText().toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if(startYear> LocalDate.now().getYear() || startYear<1970 || startYear<birthYear){
                        etStartYear.setError(getString(R.string.startYearErr));
                        etStartYear.requestFocus();
                        return null;
                    }
                }
            }catch(NumberFormatException ex){
                etStartYear.setError(getString(R.string.startYearErr));
                etStartYear.requestFocus();
                return null;
            }
            gymnast.setStartYear(startYear);
        }

        gymnast.setLevel(calculateLevel(startYear));

        return gymnast;
    }


    private int calculateLevel(int startYear){
        int level=0;
        int position = spHours.getSelectedItemPosition() ;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int currentYear=LocalDate.now().getYear();
            int experience=currentYear-startYear;
            if(experience<3) {
                level += 2;
            }
            else if(experience>=3 && experience<6){
                level += 5;
            }
            else if(experience>=6 && experience<10){
                level += 8;
            }
            else {
                level += 10;
            }

        }

        switch (position){
            case 0:{
                level+=2;
                break;
            }
            case 1:{
                level+=5;
                break;
            }
            case 2:{
                level+=8;
                break;
            }
            case 3:{
                level+=10;
                break;
            }
        }

        if(rb1.isChecked()){
            level+=3;
        }else{
            if(rb2.isChecked()){
                level+=7;
            }else{
                level+=10;
            }
        }

        List<CheckBox> cbList=new ArrayList<CheckBox>();
        Collections.addAll(cbList, cb1,cb2,cb3,cb4,cb5);
        for(CheckBox cb: cbList){
            if(cb.isChecked()){
                level+=2;
            }
        }

        return Math.round(level/10);
    }

    private HashMap<String, Boolean> createDays(){
        HashMap<String,Boolean>trainingDays=new HashMap<String, Boolean>();
        List<String>days=new ArrayList<>();
        Collections.addAll(days,
                getString(R.string.monday),getString(R.string.tuesday),getString(R.string.wednesday),getString(R.string.thursday),
                getString(R.string.friday),getString(R.string.saturday),getString(R.string.sunday));
        for(String day:days){
            trainingDays.put(day, false);
        }
        return trainingDays;
    }

    String url="";
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedpreferences = getSharedPreferences("preferinte", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

            btnSubmit.setOnClickListener(view -> {
                loadProgress.setVisibility(View.VISIBLE);
                layout.setAlpha(0.5f);
                Gymnast gymnast = createGymnast();
                if (gymnast != null) {

                    db = FirebaseFirestore.getInstance();

                    Map<String, Object> user = new HashMap<>();
                    user.put("name", gymnast.getName());
                    user.put("url", "");
                    user.put("birthYear", gymnast.getBirthYear());
                    user.put("startYear", gymnast.getStartYear());
                    user.put("level", gymnast.getLevel());

                    HashMap<String, Boolean> trainingDays = createDays();
                    user.put("trainingDays", trainingDays);

                    db.collection("users").add(user)
                            .addOnSuccessListener(documentReference -> {
                                // Toast.makeText(this, gymnast.toString(), Toast.LENGTH_SHORT).show();

                                String id = documentReference.getId();
                                editor.putString("firebaseKey", id);
                                editor.apply();
                                goToMainActivity();
                                loadProgress.setVisibility(View.GONE);

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                            });
                }
            });
    }

    private void goToMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("schedule", true);
        setResult(RESULT_OK, intent);
        startActivity(intent);
        finish();
    }

}