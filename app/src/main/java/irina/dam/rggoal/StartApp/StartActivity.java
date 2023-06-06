package irina.dam.rggoal.StartApp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import irina.dam.rggoal.Profile.QuizProfileActivity;
import irina.dam.rggoal.R;

public class StartActivity extends AppCompatActivity {

    TextView tvStart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        tvStart=findViewById(R.id.btnStart);
        tvStart.setOnClickListener(view -> {
            SharedPreferences sharedpreferences = getSharedPreferences("preferinte", Context.MODE_PRIVATE);

            String name=sharedpreferences.getString("firebaseKey", "");

            if (name.equals("")) {
                Intent intent=new Intent(this, QuizProfileActivity.class);
                startActivity(intent);
                finish();
            }else{
                Intent intent=new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}