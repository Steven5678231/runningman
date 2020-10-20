package com.example.runningman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.runningman.models.RunInfo;
import com.example.runningman.models.User;
import com.example.runningman.models.UserLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.StringBufferInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RunningActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = "RunningActivity";
    private FirebaseAuth mAuth;
    private User user;
    private String startDate;
    private RunInfo runInfo;
    private Date startTime;
    private LatLng startlatLng, startPoint;
    private boolean startCount;
    private float distance;
    private float mean_speed;
    private float max_speed;
    private int seconds_spent;
    private int record_count;
    private float earned_coin_value;

    private TextView distance_textView;
    private TextView speed_textView;
    private TextView duration_textView;
    private TextView cal_textView;
    private TextView big_duration_textView;
    private TextView earned_coin_textView;

    GoogleMap map;
    SupportMapFragment mapFragment;
    FusedLocationProviderClient client;

    FirebaseDatabase mDatabase;
    DatabaseReference userDb, locationDb, runRecordDb;

    Runnable runnable;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()){
            case R.id.nav_home:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.nav_login:
            case R.id.nav_logout:
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;
            case R.id.nav_playground:
                intent = new Intent(this, PlayGround.class);
                startActivity(intent);
                return true;
            case R.id.nav_profile:
                intent = new Intent(this, UserPage.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        distance_textView = (TextView) findViewById(R.id.distanceTextView);
        duration_textView = (TextView) findViewById(R.id.timerTextView);
        speed_textView = (TextView) findViewById(R.id.speedTextView);
        cal_textView = (TextView) findViewById(R.id.calTextView);
        big_duration_textView = (TextView)findViewById(R.id.bigDuration);


        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //init
        startCount = false;
        //Database reference
        mDatabase = FirebaseDatabase.getInstance();
        locationDb = mDatabase.getReference("locations");
        runRecordDb = mDatabase.getReference("run_records");
        userDb = mDatabase.getReference("users");
        //Initial user information
        mAuth = FirebaseAuth.getInstance();

        max_speed = 0;

        client = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(RunningActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //When Permission granted
            getCurrentLocation();
        } else {
            //Request permission
            ActivityCompat.requestPermissions(RunningActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        findViewById(R.id.startRun).setOnClickListener(this);
        findViewById(R.id.stopRun).setOnClickListener(this);

        //Run updateLocation every x minutes
        final int delay = 1000; //milliseconds
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {

                handler.postDelayed(runnable, delay);
                if (startCount){
                    Log.d(TAG, "run: running");
                    updateLocation();
                    updateTime();
                    updateCal();
                    updateSpeed();
                }
                else{

                    //Reset all data

                    distance_textView.setText("0 km");
                    distance = 0;
                    duration_textView.setText("00 : 00");

                    speed_textView.setText("0 m/s");
                    cal_textView.setText("0 Kj");
                    big_duration_textView.setText("00:00:00");
                }

            }
        },delay);


    }

    private void updateCal(){

    }

    public void onButtonShowPopupWindowClick(View view, String earned_string) {
        // Pop up window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);


        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; //let taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        earned_coin_textView = (TextView) popupWindow.getContentView().findViewById(R.id.earned_coin);
        earned_coin_textView.setText(earned_string);

        Toast.makeText(this, earned_string, Toast.LENGTH_SHORT).show();
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                popupWindow.dismiss();
                return true;
            }

        });
    }

    private void updateSpeed(){
        if (startCount){
            mean_speed = distance * 1000 / seconds_spent;
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String speedString = decimalFormat.format(mean_speed);
            speed_textView.setText(speedString + "m/s");
            if (mean_speed>max_speed){
                max_speed = mean_speed;
            }
        }
    }

    private void updateTime(){
        //Time calculation
        if (startCount){
            Calendar calendar = Calendar.getInstance();
            Date finishTime = calendar.getTime();
            Log.d(TAG, "onClick: "+finishTime);
            long diff = finishTime.getTime() - startTime.getTime();
            Log.d(TAG, "onClick: "+diff);


            int diffHours = (int) diff / (60*60*1000);
            int diffMinutes = (int) diff / (60*1000) - 60 * diffHours;
            int diffSeconds = (int) diff / 1000 -60*60*diffHours - 60*diffMinutes;
            seconds_spent = (int) diff/ 1000;
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
    }

    private float getDistance(double lat_a, double lng_a, double lat_b, double lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }

    private void updateLocation(){
        @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location!=null&startCount){
                    Log.d(TAG, "onSuccess: updating");
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            // current location
                            LatLng currentlatLng = new LatLng(location.getLatitude(),
                                    location.getLongitude());
//                            LatLng currentlatLng = new LatLng(startlatLng.latitude+0.01,
//                                    startlatLng.longitude+0.01);

                            //distance calculation
                            float curr_distance = getDistance(startlatLng.latitude, startlatLng.longitude, currentlatLng.latitude, currentlatLng.longitude);
                            distance += curr_distance/1000;
                            DecimalFormat decimalFormat = new DecimalFormat("0.00");
                            String distanceString = decimalFormat.format(distance);
                            distance_textView.setText(distanceString+" km");


                            Polyline line = googleMap.addPolyline(new PolylineOptions()
                                    .add(startlatLng, currentlatLng)
                                    .width(5)
                                    .color(Color.RED));

                            startlatLng = currentlatLng;
                            Log.d(TAG, "onMapReady: updating startlatLng"+startlatLng);
                        }
                    });
                }
            }
        });
    }

    private void getCurrentLocation(){
        @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location!=null){
                    Log.d(TAG, "onSuccess: ");
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            //Store current location in database
                            UserLocation currentlocation = new UserLocation(location.getLatitude(),location.getLongitude());

                            //locate in the map
                            LatLng latLng = new LatLng(location.getLatitude(),
                                    location.getLongitude());
                            startlatLng = new LatLng(location.getLatitude(),
                                    location.getLongitude());
                            //Create marker options
                            MarkerOptions options = new MarkerOptions()
                                    .position(latLng)
                                    .title("You are here");
                            //Zoom map
                            float zoom = 15;
                            // zoom level: // 1:world // 5:landmass/continent //10: City // 15: Street // 20: Building
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
                            //Add marker
                            googleMap.addMarker(options);

                        }
                    });
                }
            }
        });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //When permission granted
                getCurrentLocation();
            }
        }
    }
    private float calculate(float distance, long diffTime){

        float result = (float) (distance * 0.5 + diffTime * 0.5);
        return result;
    }

    private void storeRecords(int count, long diffHours){
        //Store data in runRecordDb
        String uid = mAuth.getCurrentUser().getUid();
        if (count>=0){
            count++;
            Log.d(TAG, "storeRecords: "+count);
            String record_count_String = Integer.toString(count);
            RunInfo runInfo1 = new RunInfo(diffHours, distance, mean_speed, max_speed, earned_coin_value, startDate);
            runRecordDb.child(uid).child(record_count_String).setValue(runInfo1);
            runRecordDb.child(uid).child(startDate).child(record_count_String).setValue(runInfo1);
            userDb.child(uid).child("records_count").child(startDate).setValue(count);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.startRun:{
                Button Start_stop =
                        (Button)findViewById(R.id.startRun);

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                startDate = simpleDateFormat.format(calendar.getTime());
                startTime = calendar.getTime();
                startCount = true;
                max_speed = 0;

                Log.d(TAG, "onClick: "+startDate);
                Log.d(TAG, "onClick2: "+startTime);

                userDb.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String uid = mAuth.getCurrentUser().getUid();
                        long record = 0;
                        try{
                            record = snapshot.child(uid).child("records_count").child(startDate).getValue(Long.class);
                        }catch (Exception e){

                        }
                        record_count = (int) record;

                        Log.d(TAG, "onDataChange: "+record_count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;
            }
            case R.id.stopRun:{
                if (startCount){
                    startCount = false;
                    Calendar calendar = Calendar.getInstance();
                    Date finishTime = calendar.getTime();
                    Log.d(TAG, "onClick: "+finishTime);
                    long diff = finishTime.getTime() - startTime.getTime();
                    Log.d(TAG, "onClick: "+diff);

                    long diffSeconds = diff / 1000;
                    long diffMinutes = diff / (60*1000);
                    final long diffHours = diff / (60*60*1000);

                    if (mAuth.getCurrentUser()==null){
                        Toast.makeText(RunningActivity.this,"Please login first!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //calculate earned coins
                    earned_coin_value = calculate(this.distance, diffHours);
                    Log.d(TAG, "onClick: "+earned_coin_value);
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    String earned_coin_string = decimalFormat.format(earned_coin_value);

                    //popup
//                    onButtonShowPopupWindowClick(view, earned_coin_string);

                    //Store info in database
                    //Obtain current number of running records;


                    storeRecords(record_count, diffHours);

                    Intent intent = new Intent(RunningActivity.this, RunningFinished.class);
                    startActivity(intent);
                    break;

                }else{
                    Toast.makeText(RunningActivity.this,"Please start first", Toast.LENGTH_SHORT).show();
                }

                break;
            }
        }
    }
}