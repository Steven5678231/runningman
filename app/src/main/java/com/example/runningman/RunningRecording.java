package com.example.runningman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import com.example.runningman.R;
import com.example.runningman.models.RunInfo;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.*;

import android.app.DatePickerDialog;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

public class RunningRecording extends AppCompatActivity {

    private static final String TAG = "Running recording";
    private LineChart lineChart;
    private TextView dis_distance;
    private TextView dis_speed;
    private TextView dis_duration;
    private TextView dis_date = null;
    private TextView condition_show;
    private String get_date;

    private String get_date_2before;
    private String get_date_before;
    private String get_date_after;
    private String get_date_2after;

    FirebaseDatabase mDatabase;
    FirebaseAuth mAuth;
    DatabaseReference usersDb;
    DatabaseReference recordsDb;
    RunInfo runinfoshow;
    RunInfo runinfoshow_b2;
    RunInfo runinfoshow_b;
    RunInfo runinfoshow_a;
    RunInfo runinfoshow_a2;

    ArrayList<String> distanceList = new ArrayList<String>();

    private String[] mList = new String[]{
            "Day1","Day2","Day3","Day4","Day5"
    };

    Calendar ca = Calendar.getInstance();
    int  mYear = ca.get(Calendar.YEAR);
    int  mMonth = ca.get(Calendar.MONTH);
    int  mDay = ca.get(Calendar.DAY_OF_MONTH);

    public RunningRecording() {
    }

    private void initChart(LineChart lineChart) {

        lineChart.setDrawBorders(true);
        lineChart.setDragEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(5,true);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(){
            @Override
            public String getFormattedValue(float value) {
                return mList[(int) value % mList.length];
            }
        });

        YAxis leftYAxis = lineChart.getAxisLeft();
        YAxis rightYaxis = lineChart.getAxisRight();
        leftYAxis.setAxisMinimum(0f);
        rightYaxis.setAxisMinimum(0f);
        rightYaxis.setEnabled(false);
        rightYaxis.setDrawGridLines(false);
        leftYAxis.setDrawGridLines(true);
        leftYAxis.enableGridDashedLine(10f, 10f, 0f);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);

        Description description = new Description();
        description.setText("Running Distance");
        description.setEnabled(true);

        lineChart.setDescription(description);

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initLineDataSet(LineDataSet lineDataSet) {

        lineDataSet.setColor(Color.rgb(102, 163, 255));
        lineDataSet.setCircleColor(Color.rgb(102, 163, 255));
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);

        lineDataSet.setDrawCircleHole(false);
        lineChart.invalidate();
        lineDataSet.setValueTextSize(10f);

        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillDrawable(getResources().getDrawable(R.drawable.fade_color));

        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setFormSize(15.f);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

    }


    public void showLineChart(){

        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < distanceList.size(); i++) {
            String data = distanceList.get(i);
            Entry entry = new Entry(i, Float.parseFloat(data));
            entries.add(entry);
        }
        for (int i = 0; i<5-distanceList.size();i++) {
            Entry entry = new Entry(i+distanceList.size(),0);
            entries.add(entry);
        }


        LineDataSet lineDataSet = new LineDataSet(entries, " ");
        initLineDataSet(lineDataSet);
        LineData data = new LineData(lineDataSet);
        lineChart.setData(data);
    }

    private void initDate(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        String init_date = formatter.format(date);
        dis_date.setText(init_date);
        get_date = (mMonth+1)+"-"+mDay;
        get_date_2before = (mMonth+1) + "-" + (mDay-2) ;
        get_date_before =  (mMonth+1) + "-" + (mDay-1);
        get_date_after = (mMonth+1) + "-" + (mDay+1);
        get_date_2after = (mMonth+1) + "-" + (mDay+2);
        mList = new String[]{ get_date_2before, get_date_before,get_date, get_date_after, get_date_2after};
        if (mDay>9){
            get_date = mYear + "-" + (mMonth+1) + "-" + mDay;
            get_date_2before = mYear + "-" + (mMonth+1) + "-" + (mDay-2) ;
            get_date_before = mYear + "-" + (mMonth+1) + "-" + (mDay-1);
            get_date_after = mYear + "-" + (mMonth+1) + "-" + (mDay+1);
            get_date_2after = mYear + "-" + (mMonth+1) + "-" + (mDay+2);
        } else {
            get_date = mYear + "-" + (mMonth+1) + "-0" + mDay;
            get_date_2before = mYear + "-" + (mMonth+1) + "-0" + (mDay-2) ;
            get_date_before = mYear + "-" + (mMonth+1) + "-0" + (mDay-1);
            get_date_after = mYear + "-" + (mMonth+1) + "-0" + (mDay+1);
            get_date_2after = mYear + "-" + (mMonth+1) + "-0" + (mDay+2);
        }



    }

    public void getDate(View v) {

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.MyDatePickerDialogTheme,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mYear = year;
                        mMonth = month;
                        mDay = dayOfMonth;
                        final String date = mYear + "-" + (mMonth+1) + "-" + mDay;
                        get_date = (mMonth+1) +"-"+mDay;
                        get_date_2before = (mMonth+1) + "-" + (mDay-2) ;
                        get_date_before =  (mMonth+1) + "-" + (mDay-1);
                        get_date_after = (mMonth+1) + "-" + (mDay+1);
                        get_date_2after = (mMonth+1) + "-" + (mDay+2);
                        mList = new String[]{ get_date_2before, get_date_before,get_date, get_date_after, get_date_2after};
                        if (mDay>9){
                            get_date = mYear + "-" + (mMonth+1) + "-" + mDay;
                            get_date_2before = mYear + "-" + (mMonth+1) + "-" + (mDay-2) ;
                            get_date_before = mYear + "-" + (mMonth+1) + "-" + (mDay-1);
                            get_date_after = mYear + "-" + (mMonth+1) + "-" + (mDay+1);
                            get_date_2after = mYear + "-" + (mMonth+1) + "-" + (mDay+2);
                        } else {
                            get_date = mYear + "-" + (mMonth+1) + "-0" + mDay;
                            get_date_2before = mYear + "-" + (mMonth+1) + "-0" + (mDay-2) ;
                            get_date_before = mYear + "-" + (mMonth+1) + "-0" + (mDay-1);
                            get_date_after = mYear + "-" + (mMonth+1) + "-0" + (mDay+1);
                            get_date_2after = mYear + "-" + (mMonth+1) + "-0" + (mDay+2);
                        }


                        dis_date.setText(date);

                        getRunInfo();
                    }
                },
                mYear, mMonth, mDay);
        datePickerDialog.show();

    }

    public void getRunCount(){
        usersDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String uid = (String) mAuth.getCurrentUser().getUid();
                String date = get_date;
                Long count ;
                count = snapshot.child(uid).child("records_count").child(date).getValue(Long.class);
                int recordCount = 0;
                if (count!=null){
                    recordCount = Integer.parseInt(String.valueOf(count));
                }
                Log.d(TAG, "onDataChange: "+recordCount);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getRunInfo(){
        distanceList = new ArrayList<>();
        recordsDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String uid = (String) mAuth.getCurrentUser().getUid();
                String date = get_date;
                String date_2b =  get_date_2before;
                String date_b = get_date_before;
                String date_a = get_date_after;
                String date_a2 = get_date_2after;

                String record_num ="0";

                runinfoshow = snapshot.child(uid).child(date).child(record_num).getValue(RunInfo.class);
                runinfoshow_b2 = snapshot.child(uid).child(date_2b).child(record_num).getValue(RunInfo.class);
                runinfoshow_b = snapshot.child(uid).child(date_b).child(record_num).getValue(RunInfo.class);
                runinfoshow_a = snapshot.child(uid).child(date_a).child(record_num).getValue(RunInfo.class);
                runinfoshow_a2 = snapshot.child(uid).child(date_a2).child(record_num).getValue(RunInfo.class);

                DecimalFormat decimalFormat = new DecimalFormat("0.00");

                distanceList = new ArrayList<>();
                if(runinfoshow_b2 != null)
                    distanceList.add(decimalFormat.format(runinfoshow_b2.getDistance()));
                else
                    distanceList.add("0.00");

                if(runinfoshow_b != null)
                    distanceList.add(decimalFormat.format(runinfoshow_b.getDistance()));
                else
                    distanceList.add("0.00");

                if(runinfoshow != null){

//                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    String distance = decimalFormat.format(runinfoshow.getDistance());
                    distanceList.add(distance);


                    String speed = decimalFormat.format(runinfoshow.getMean_speed());
                    long diff = runinfoshow.getTimelast();
                    int diffHours = (int) diff / (60*60);
                    int diffMinutes = (int) diff / (60) - 60 * diffHours;
                    int diffSeconds = (int) diff -60*60*diffHours - 60*diffMinutes;

                    String hourString = Integer.toString(diffHours);
                    String minuteString = Integer.toString(diffMinutes);
                    String secondString = Integer.toString(diffSeconds);
                    if (diffSeconds<10){
                        secondString = '0'+secondString;
                    }
                    if (diffMinutes<10){
                        minuteString = '0'+minuteString;
                    }
                    if (diffHours<10){
                        hourString = '0'+hourString;
                    }
                    String duration = hourString+" : "+minuteString+" : "+diffSeconds;



                    condition_show.setText("Have a Good Running Day!");
                    if (runinfoshow.getDistance()<1000){
                        dis_distance.setText(distance+" m");
                    } else {
                        dis_distance.setText(decimalFormat.format(runinfoshow.getDistance()/1000)+ " km");
                    }

                    dis_speed.setText(speed + " m/s");
                    dis_duration.setText(duration);
                } else {
                    condition_show.setText("No Running Today !");
                    dis_distance.setText("0 m");
                    dis_speed.setText("0 m/s ");
                    dis_duration.setText("0 s");
                    Log.d(TAG, "before add");
                    distanceList.add("0.00");
                    Log.d(TAG, "after add");
                    System.out.println(distanceList);
                }

                if(runinfoshow_a != null)
                    distanceList.add(decimalFormat.format(runinfoshow_a.getDistance()));
                else
                    distanceList.add("0.00");

                if(runinfoshow_a2 != null)
                    distanceList.add(decimalFormat.format(runinfoshow_a2.getDistance()));
                else
                    distanceList.add("0.00");

                showLineChart();
            }

            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_recording);

        lineChart = findViewById(R.id.lineChart);
        dis_date = findViewById(R.id.dialog_tv_date);
        dis_distance = findViewById(R.id.input_distance);
        dis_speed = findViewById(R.id.input_speed);
        dis_duration = findViewById(R.id.input_timelast);
        condition_show = findViewById(R.id.dialog_condition);

        mDatabase = FirebaseDatabase.getInstance();
        usersDb = mDatabase.getReference("users");
        recordsDb = mDatabase.getReference("run_records");
        mAuth = FirebaseAuth.getInstance();

        initDate();
        getRunInfo();
        initChart(lineChart);


    }


}
