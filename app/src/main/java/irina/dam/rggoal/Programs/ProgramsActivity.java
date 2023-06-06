package irina.dam.rggoal.Programs;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import irina.dam.rggoal.LocalDatabase.Competition;
import irina.dam.rggoal.LocalDatabase.DatabaseManager;
import irina.dam.rggoal.LocalDatabase.EnrolledProgram;
import irina.dam.rggoal.R;

public class ProgramsActivity extends AppCompatActivity {

    FirebaseFirestore db;
    CollectionReference ref;
    ArrayList<Program> programsList=new ArrayList<Program>();
    ArrayList<Program> programsListFull=new ArrayList<Program>();
    Map<String, String> options=new HashMap<String, String>();
    RecyclerView rvPrograms;
    ProgramAdapter adapter;
    String level="";
    int lvl=0;
    String option="";
    EnrolledProgram program;
    TextView tvCategory;
    TextInputEditText etSearch;
    TextInputLayout layoutSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programe);

        tvCategory=findViewById(R.id.tvCategory);
        rvPrograms=findViewById(R.id.rvPrograms);
        adapter=new ProgramAdapter(this, programsList, programStatuses);
        rvPrograms.setAdapter(adapter);
        rvPrograms.setLayoutManager(new LinearLayoutManager(this));


        layoutSearch=findViewById(R.id.layoutSearch);
        etSearch=findViewById(R.id.etSearch);

        options=(HashMap<String, String>)getIntent().getSerializableExtra("options");
        level=options.get("level");
        option=options.get("option");

        lvl= Integer.parseInt(level.substring(level.length() - 1));
        tvCategory.setText("Category: "+option);

        db=FirebaseFirestore.getInstance();
        ref=db.collection("programs").document("programList")
                .collection("Levels").document(level)
                .collection("Courses");

        getPrograms();
        if(option!=null) {
            layoutSearch.setVisibility(View.GONE);
            getStatuses(option);
        }else{
            tvCategory.setVisibility(View.GONE);
            getAllPrograms();
        }

        adapter.setOnItemClickListener(new ProgramAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Program selectedProgram= programsList.get(position);

                if(currentProgram!=null && !selectedProgram.getKey().equals(currentProgram.getFirebaseKey())) {
                    AlertDialog.Builder message=new AlertDialog.Builder(ProgramsActivity.this);
                    message.setTitle(R.string.progrWarning).setMessage(R.string.progrNotAvailable)
                            .setIcon(R.drawable.prog1)
                            .setPositiveButton("Got it!", (dialog, which)->{});
                    message.show();
                }
                else if(currentProgram!=null && selectedProgram.getKey().equals(currentProgram.getFirebaseKey())){
                    AlertDialog.Builder message=new AlertDialog.Builder(ProgramsActivity.this);
                    message.setTitle(R.string.progrWarning).setMessage(R.string.progrCurrent)
                            .setIcon(R.drawable.prog1)
                            .setPositiveButton("Got it!", (dialog, which)->{});
                    message.show();

                }
                else if(programStatuses.containsKey(selectedProgram.getKey())){
                    AlertDialog.Builder message=new AlertDialog.Builder(ProgramsActivity.this);
                    message.setTitle(R.string.progrWarning).setMessage(R.string.progrFinished)
                            .setIcon(R.drawable.prog2)
                            .setPositiveButton("Got it!", (dialog, which)->{});
                    message.show();
                }
                else{
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        program=new EnrolledProgram(selectedProgram.getKey(), LocalDate.now(),  option, 1, lvl);
                        Toast.makeText(getApplicationContext(), program.toString(), Toast.LENGTH_SHORT).show();

                        new Thread(()->{
                            DatabaseManager.getInstance(getApplicationContext()).getDao().insertProgram(program);
                            programStatuses.put(program.getFirebaseKey(), getString(R.string.progEnrolled));
                            runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                            });
                        }).start();

                    }
                }
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                programsList.clear();

                if(charSequence.length()==0){
                    adapter.notifyDataSetChanged();
                } else {
                filterByName(charSequence);
            }}

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void getAllPrograms() {
        ref.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> categories = new ArrayList<String>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Program program = document.toObject(Program.class);
                        program.setKey(document.getId());
                        programsListFull.add(program);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProgramsActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                });
    }

    private void filterByName(CharSequence charSequence){
        String searchQuery = charSequence.toString().toLowerCase();
        programsList.clear();
        List<String>categories=new ArrayList<String>();

        for (Program program : programsListFull) {
            String name = program.getName().toLowerCase();
            String category = program.getCategory().toLowerCase();

            if (name.contains(searchQuery) || category.contains(searchQuery)) {
                programsList.add(program);
            }

            if(!categories.contains(program.getCategory())){
                categories.add(program.getCategory());
            }
        }

        for(String category:categories){
            getStatuses(category);
        }
            adapter.notifyDataSetChanged();
    }


    EnrolledProgram currentProgram;
    List<EnrolledProgram> finishedPrograms;
    HashMap<String, String>programStatuses=new HashMap<String, String>();
    private void getStatuses(String category){

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<EnrolledProgram> futureCurrentPrograms = executorService.submit(() -> {
            EnrolledProgram currentProgram= DatabaseManager.getInstance(getApplicationContext()).getDao().getCurrentProgramCateg(lvl, category);
            return currentProgram;
        });
        Future<List<EnrolledProgram>> futureFinishedPrograms = executorService.submit(() -> {
            List<EnrolledProgram> finishedPrograms= DatabaseManager.getInstance(getApplicationContext()).getDao().getFinishedCategoryPrograms(lvl, category);
            return finishedPrograms;
        });

        try{
            currentProgram = futureCurrentPrograms.get();
            finishedPrograms = futureFinishedPrograms.get();

            if(currentProgram!=null) {
                programStatuses.put(currentProgram.getFirebaseKey(), getString(R.string.progEnrolled)); //Enrolled
            }

            if(finishedPrograms.size()>0){
                for(int i=0; i<finishedPrograms.size(); i++){
                    programStatuses.put(finishedPrograms.get(i).getFirebaseKey(), getString(R.string.progFinished));  //Finished
                }
            }

            adapter.notifyDataSetChanged();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getPrograms(){
        ref.whereEqualTo("category", option).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for(QueryDocumentSnapshot doc: queryDocumentSnapshots){
                        Program program=doc.toObject(Program.class);
                        program.setKey(doc.getId());
                        programsList.add(program);

                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }
}