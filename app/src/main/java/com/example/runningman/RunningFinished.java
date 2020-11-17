package com.example.runningman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.runningman.models.RunInfo;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unity3d.player.UnityPlayerActivity;
import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class RunningFinished extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "RunningFinished";

    private String startDate;
    private int record_count;
    private boolean getData;

    private TextView distance_textView;
    private TextView speed_textView;
    private TextView duration_textView;
    private TextView steps_textView;
    private TextView big_duration_textView;
    private TextView earned_coin_textView;
    private TextView congrate_textView;
    private TextView distanceShow;
    private TextView timeShow;
    private TextView stepShow;
    private TextView speedShow;

    RunInfo runInfoShow;

    private FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference userDb, runRecordDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_finished);

        String record_count = getIntent().getStringExtra("record_count");

        Log.d(TAG, "onCreate: "+record_count);

        findViewById(R.id.finishGoBack).setOnClickListener(this);
        findViewById(R.id.finishGoCity).setOnClickListener(this);

        distance_textView = (TextView) findViewById(R.id.distanceTextView);
        duration_textView = (TextView) findViewById(R.id.timerTextView);
        speed_textView = (TextView) findViewById(R.id.speedTextView);
        steps_textView = (TextView) findViewById(R.id.stepsTextView);
        big_duration_textView = (TextView) findViewById(R.id.bigDuration);
        earned_coin_textView = (TextView) findViewById(R.id.earned_coin);
        congrate_textView = (TextView) findViewById(R.id.congrate);
        //ShowText
        timeShow = (TextView) findViewById(R.id.timeShow);
        speedShow = (TextView) findViewById(R.id.speedShow);
        distanceShow = (TextView) findViewById(R.id.distanceShow);
        stepShow = (TextView) findViewById(R.id.stepShow);

        Drawable btnDrawable = ContextCompat.getDrawable(this,
                R.mipmap.finish_background);
        findViewById(R.id.finished_layout).setBackground(btnDrawable);


        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/mrsmonster.ttf");
        congrate_textView.setTypeface(typeface);
        Typeface typeface1 = Typeface.createFromAsset(getAssets(),"fonts/campus.ttf");
        timeShow.setTypeface(typeface1);
        speedShow.setTypeface(typeface1);
        distanceShow.setTypeface(typeface1);
        stepShow.setTypeface(typeface1);
        Typeface typeface2 = Typeface.createFromAsset(getAssets(),"fonts/Gameplay.ttf");
        distance_textView.setTypeface(typeface2);
        speed_textView.setTypeface(typeface2);
        steps_textView.setTypeface(typeface2);
        duration_textView.setTypeface(typeface2);


        //RatingBar
        ScaleRatingBar ratingBar = new ScaleRatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setMinimumStars(1);
        ratingBar.setRating(0);
        ratingBar.setStarPadding(10);
        ratingBar.setStepSize(0.5f);
        ratingBar.setStarWidth(105);
        ratingBar.setStarHeight(105);
        ratingBar.setIsIndicator(false);
        ratingBar.setClickable(true);
        ratingBar.setScrollable(true);
        ratingBar.setClearRatingEnabled(true);
        ratingBar.setEmptyDrawableRes(R.drawable.star_empty);
        ratingBar.setFilledDrawableRes(R.drawable.star_empty);
        ratingBar.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChange(BaseRatingBar ratingBar, float rating, boolean fromUser) {
                Log.e(TAG, "onRatingChange: " + rating);
            }

        });


        //Database reference
        mDatabase = FirebaseDatabase.getInstance();
        runRecordDb = mDatabase.getReference("run_records");
        userDb = mDatabase.getReference("users");
        //Initial user information
        mAuth = FirebaseAuth.getInstance();

        //get the exact date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        startDate = simpleDateFormat.format(calendar.getTime());


        Log.d(TAG, "onCreate finished: "+record_count);


        int record_count_int = Integer.parseInt(record_count);

        if (record_count_int>0){
            getRunInfo(record_count);
//            Toast.makeText(RunningFinished.this, String.valueOf(runInfoShow.getDistance()), Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(RunningFinished.this, "no Record", Toast.LENGTH_SHORT).show();
        }



    }

    private void getRunInfo(final String record_count){

        runRecordDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String record_num = record_count;
                String uid = mAuth.getCurrentUser().getUid();

                runInfoShow = snapshot.child(uid).child(startDate).child(record_num).getValue(RunInfo.class);

                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String speedString = decimalFormat.format(runInfoShow.getMean_speed());
                speed_textView.setText(speedString + " m/s");
                Log.d(TAG, "onDataChange: "+runInfoShow.getDistance());


                steps_textView.setText(String.valueOf(runInfoShow.getStep()));
                float distance = runInfoShow.getDistance();
                if (distance>1000) {
                    distance = distance / 1000;
                    String distanceString = decimalFormat.format(distance);
                    distance_textView.setText(distanceString+" km");
                } else {
                    String distanceString = decimalFormat.format(distance);
                    distance_textView.setText(distanceString+" m");
                }



                String earnedString = decimalFormat.format(runInfoShow.getEarned_coin_value());
                earned_coin_textView.setText(earnedString+" coins!");


                long diff = runInfoShow.getTimelast();
                Log.d(TAG, "onDataChange: "+diff);
                int diffHours = (int) diff / (60*60);
                int diffMinutes = (int) diff / (60) - 60 * diffHours;
                int diffSeconds = (int) diff  -60*60*diffHours - 60*diffMinutes;
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
                String timeString = hourString+':'+minuteString+':'+secondString;
                duration_textView.setText(timeString);
                big_duration_textView.setText(timeString);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.finishGoBack: {
                finish();
                break;
            }
            case R.id.finishGoCity: {
                Intent intent = new Intent(RunningFinished.this, UnityPlayer.class);
                startActivity(intent);
                break;

            }
        }
    }
}