package irina.dam.rggoal.Charts;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointD;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import irina.dam.rggoal.Programs.Day;
import irina.dam.rggoal.R;
import irina.dam.rggoal.SharedPrefs.PreferencesKeys;
import irina.dam.rggoal.TrainingPlan.DailyPlanActivity;
import irina.dam.rggoal.TrainingPlan.TrainingPeriodization.AnnualPhase;
import irina.dam.rggoal.TrainingPlan.TrainingPeriodization.TrainingStage;


public class ChartsFragment extends Fragment {

    public ChartsFragment() {
        // Required empty public constructor
    }

    DocumentReference refPhase;
    CollectionReference refStage;
    FirebaseFirestore db;
    int level;

    LinkedHashMap<String, LocalDate> phases;
    ArrayList<AnnualPhase> annualPhases=new ArrayList<AnnualPhase>();
    String currentPhase="";
    Day day;

    BarChart barChart;
    ArrayList<IBarDataSet> barDataSets = new ArrayList<>();
    float space=0.3f;

    float mLastX = 0f;
    TextView mTooltip;

    int poz=0;
    int i = 0;

    LineChart lineChart;

    Button btnBarChart, btnLineChart;
    boolean openBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_charts, container, false);

        btnBarChart=view.findViewById(R.id.btnBar);
        btnLineChart=view.findViewById(R.id.btnLine);
        btnBarChart.setOnClickListener(view1 -> {
            lineChart.setVisibility(View.GONE);
            mTooltip.setVisibility(View.GONE);
            openBar=true;
            createBarChart();
        });
        btnLineChart.setOnClickListener(view1 -> {
            barChart.setVisibility(View.GONE);
            mTooltip.setVisibility(View.GONE);
            openBar=false;
            createLineChart();
        });

        mTooltip=view.findViewById(R.id.tooltip);
        barChart=view.findViewById(R.id.barChart);
        lineChart=view.findViewById(R.id.lineChart);

        getBundle();

        db=FirebaseFirestore.getInstance();

        return view;
    }

    private void getBundle(){
        Bundle bundle = getArguments();
        phases=(LinkedHashMap<String, LocalDate>) bundle.getSerializable("phases");
        level=bundle.getInt("level");
        currentPhase=bundle.getString("currentPhase");
        day=(Day) bundle.getSerializable("day");
    }

    List<TrainingStage> stages=new ArrayList<TrainingStage>();
    private void readStages(FirestoreCallbackStage callback){
        refStage.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                List<TrainingStage> unsorted=new ArrayList<TrainingStage>();

                for(DocumentSnapshot document:task.getResult()){
                    TrainingStage stage =document.toObject(TrainingStage.class);
                    unsorted.add(stage);
                }

                stages=sortStages(unsorted);
                callback.onCallBackStage(stages);
            }else{
                Toast.makeText(getContext(),task.getException().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private interface FirestoreCallbackStage{
        void onCallBackStage(List<TrainingStage>stages);
    }

    private List<TrainingStage> sortStages(List<TrainingStage>stages){
        Comparator<TrainingStage> comparator = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            comparator = Comparator.comparingInt(o -> o.getStage());
            Collections.sort(stages, comparator);
        }
        return stages;
    }

    List<ILineDataSet> lineDataSets = new ArrayList<>();
    private void addLineEntry(List<Entry> durations, String label, int color){
        LineDataSet lineDataSetExpected = new LineDataSet(durations, label);
        lineDataSetExpected.setColor(color);
        lineDataSetExpected.setLineWidth(2f);
        lineDataSetExpected.setCircleColor(color);
        lineDataSetExpected.setCircleRadius(5f);
        lineDataSetExpected.setDrawCircleHole(false);
        lineDataSets.add(lineDataSetExpected);
    }

    int[] oldDelays;
    int[] newDelays;

    private void createLineChart(){
        PreferencesKeys preferencesKeys=new PreferencesKeys(getActivity());
        oldDelays=new int[3];
        String lastDelay =preferencesKeys.getLastDelayKey();
        if(lastDelay!=null && !lastDelay.isEmpty()){
            oldDelays=preferencesKeys.getIntsFromPrefs(lastDelay);
        }
        newDelays=new int[3];
        String newDelay =preferencesKeys.getDelaysToday();
        if(newDelay!=null && !newDelay.isEmpty()){
            newDelays=preferencesKeys.getIntsFromPrefs(newDelay);
        }

        refStage=db.collection("annualPhases").document("AnnualPhases")
                .collection("Level" + level).document(currentPhase).collection("TrainingStages");

        List<Entry> expectedDurations = new ArrayList<>();
        List<Entry> delaysTodayEntries = new ArrayList<>();
        List<Entry> delaysLastDayEntries = new ArrayList<>();

        readStages((stages) -> {
            String data="";
            for(int i=0; i<stages.size(); i++){

                if(day!=null && day.getStage().equals(stages.get(i).getName())){
                    stages.get(i).setDuration(stages.get(i).getDuration()+day.getDuration());
                }

                data=stages.get(i).getName().substring(0, 1).toUpperCase()+stages.get(i).getName().substring(1)
                            +"\nProgrammed Duration: "+ stages.get(i).getDuration()+"m";
                expectedDurations.add(new Entry(i, stages.get(i).getDuration(),data));

                if(newDelays!=null){
                    data=stages.get(i).getName().substring(0, 1).toUpperCase()+stages.get(i).getName().substring(1)
                            +"\nEffective Duration: "+ (stages.get(i).getDuration()+newDelays[i])+"m"
                            +"\nFinished: "+ Math.abs(newDelays[i])+"m "+(newDelays[i]<0?"earlier":"later");
                    delaysTodayEntries.add(new Entry(i, stages.get(i).getDuration()+newDelays[i],data));
                }
                if(oldDelays!=null){
                    data=stages.get(i).getName().substring(0, 1).toUpperCase()+stages.get(i).getName().substring(1)
                            +"\nEffective Duration: "+ (stages.get(i).getDuration()+oldDelays[i])+"m"
                            +"\nFinished: "+ Math.abs(oldDelays[i])+"m "+(oldDelays[i]<0?"earlier":"later");
                    delaysLastDayEntries.add(new Entry(i, stages.get(i).getDuration()+oldDelays[i],data));
                }
            }

            addLineEntry(expectedDurations, "Programmed durations", getContext().getResources().getColor(R.color.mediumPink));

            if(delaysTodayEntries.size()>0){
                addLineEntry(delaysTodayEntries, "Today's effected durations", getContext().getResources().getColor(R.color.darkPink));
            }

            if(delaysLastDayEntries.size()>0) {
                addLineEntry(delaysLastDayEntries, "Last training's durations", getContext().getResources().getColor(R.color.mauve));
            }

            LineData lineData = new LineData(lineDataSets);
            lineChart.setData(lineData);

            lineChart.getXAxis().setAxisMinimum(0);
            lineChart.getAxisLeft().setAxisMinimum(0);

            lineChart.getDescription().setEnabled(false);
            lineChart.setTouchEnabled(true);
            lineChart.setDragEnabled(true);
            lineChart.setScaleEnabled(true);
            lineChart.setPinchZoom(true);

            lineChart.animateXY(2000, 2000, Easing.EaseInOutQuad);
            lineChart.setVisibility(View.VISIBLE);

            lineChart.setOnChartValueSelectedListener(
                    new OnChartValueSelectedListener() {
                        @Override
                        public void onValueSelected(Entry e, Highlight h) {
                            mTooltip.setVisibility(View.VISIBLE);
                            clickBarEntry(e, h, openBar);
                        }

                        @Override
                        public void onNothingSelected() {
                            mTooltip.setVisibility(View.GONE);
                        }
                    });

            lineChart.invalidate();

        });
    }

    private void createBarChart(){
        int size = phases.size();

        if(poz>0){
            poz=0;
            i=0;
            annualPhases.clear();
        }

        for (Map.Entry<String, LocalDate> entry : phases.entrySet()) {
            String key = entry.getKey();

            if (++i == size) {
                break;
            }

            refPhase = db.collection("annualPhases").document("AnnualPhases")
                    .collection("Level" + level).document(key);

            getPhaseParams((dbPhases, poz) -> {
                if (poz == phases.size() - 2) {
                    sortPhases(dbPhases);
                    barDataSets=createEntries(dbPhases, poz);

                    BarData barData = new BarData(barDataSets);
                    barData.setBarWidth(space);

                    setXaxis();

                    barChart.setData(barData);
                    barChart.getDescription().setEnabled(false);
                    barChart.animateY(1000);

                    barChart.setVisibility(View.VISIBLE);

                    barChart.setOnChartValueSelectedListener(
                            new OnChartValueSelectedListener() {
                                @Override
                                public void onValueSelected(Entry e, Highlight h) {
                                    mTooltip.setVisibility(View.VISIBLE);
                                    clickBarEntry(e, h, openBar);
                                }

                                @Override
                                public void onNothingSelected() {
                                    mTooltip.setVisibility(View.GONE);
                                }
                            });
                }

            });

        }
        barChart.invalidate();
    }

    private void getPhaseParams(FirestoreCallbackPhase callback){
        refPhase.get()
                .addOnCompleteListener(task -> {
                    AnnualPhase annualPhase=task.getResult().toObject(AnnualPhase.class);
                    if(task.isSuccessful()){
                        annualPhases.add(annualPhase);

                        callback.onCallBackPhaseParams(annualPhases, poz++);
                    }else{
                        Toast.makeText(getContext(),task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private interface FirestoreCallbackPhase{
        void onCallBackPhaseParams(ArrayList<AnnualPhase> phaseList, int poz);
    }


    private ArrayList<IBarDataSet> createEntries(ArrayList<AnnualPhase> DBphases, int poz){
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();

        for(int i=0; i<DBphases.size(); i++) {
            ArrayList<BarEntry> barEntries = new ArrayList<>();

            List<String> keys = new ArrayList<>(phases.keySet());
            String crtPhase = keys.get(i);
            crtPhase=crtPhase.substring(0, 1).toUpperCase()+crtPhase.substring(1);

            barEntries.add(new BarEntry(i + space, DBphases.get(i).getIntensity(),
                    crtPhase+"\n Intensity: "+DBphases.get(i).getIntensity()+"%"));
            barEntries.add(new BarEntry(i + space * 2, DBphases.get(i).getVolume(),
                    crtPhase+"\n Volume: "+DBphases.get(i).getVolume()+"%"));
            barEntries.add(new BarEntry(i + space * 3, DBphases.get(i).getTechnicalPreparation(),
                    crtPhase+"\n Tech. Prep.: "+DBphases.get(i).getTechnicalPreparation()+"%"));

            String key = phases.keySet().toArray(new String[0])[i];

            BarDataSet barDataSet = new BarDataSet(barEntries, key);
            barDataSet.setColors(new int[] {
                    getContext().getResources().getColor(R.color.mediumPink),
                    getContext().getResources().getColor(R.color.darkPink),
                    getContext().getResources().getColor(R.color.mauve),
            });

            dataSets.add(barDataSet);
        }

        return dataSets;
    }

    private void sortPhases(ArrayList<AnnualPhase> phases){
        Comparator<AnnualPhase> comparator = new Comparator<AnnualPhase>() {
            @Override
            public int compare(AnnualPhase o1, AnnualPhase o2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return o1.getPhase() - o2.getPhase();
                }
                return 0;
            }
        };
        Collections.sort(phases, comparator);
    }

    private void clickBarEntry(Entry e, Highlight h, boolean isBar){
        if (e == null) {
            return;
        }

        float x = e.getX();
        float y = e.getY();
        MPPointD pixel;
        if(isBar){
            pixel  = barChart.getPixelForValues(x, y-13, YAxis.AxisDependency.LEFT);
        }else{
            pixel  = lineChart.getPixelForValues(x, y-7, YAxis.AxisDependency.LEFT);
        }
        float[] pos = {(float) pixel.x, (float) pixel.y};

        float offsetX = 0f;
        if (x > mLastX) {
            offsetX = -mTooltip.getWidth();
        }

        mTooltip.setX(pos[0] + offsetX);
        mTooltip.setY(pos[1]);

        DataSet dataSet;

        if(isBar){
            dataSet = (BarDataSet) barChart.getData().getDataSetByIndex(h.getDataSetIndex());
        }else{
            dataSet = (LineDataSet) lineChart.getData().getDataSetByIndex(h.getDataSetIndex());
        }


        int barIndex = dataSet.getEntryIndex(e);
        int color = dataSet.getColor(barIndex);
        mTooltip.setBackgroundColor(color);

        String message=e.getData().toString();
        int previousDataSetIndex = h.getDataSetIndex() - 1;
        if (previousDataSetIndex>=0) {

            DataSet previousDataSet;

            if(isBar){
                previousDataSet = (BarDataSet) barChart.getData().getDataSetByIndex(previousDataSetIndex);
                BarEntry previousBarEntry = (BarEntry) previousDataSet.getEntryForIndex(barIndex);
                float previousXValue = previousBarEntry.getY();
                if(y-previousXValue>0){
                    message+="\n Growth: "+(y-previousXValue)+"%";
                }

                else{
                    message+="\n Decrease: "+(y-previousXValue)+"%";
                }
            }
        }

        mTooltip.setText(message);
        mLastX = x;
    }

    private void setXaxis(){
        List<String> xAxisLabels = new ArrayList<String>();
        for (LocalDate date : phases.values()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                xAxisLabels.add(date.format(DateTimeFormatter.ofPattern("dd LLLL")));
            }
        }

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
    }
}