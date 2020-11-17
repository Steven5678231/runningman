package com.example.runningman;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import com.example.runningman.models.TotalInfo;
import com.example.runningman.service.StepCounterService;

import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.runningman.models.RunInfo;
import com.example.runningman.models.User;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RunningActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = "RunningActivity";

    private User user;
    private String startDate;
    private RunInfo runInfo;
    private Date startTime, timeOne;
    private LatLng startlatLng, startPoint;
    private boolean startCount;
    private long history_timelast, total_timelast;
    private int history_step_count, total_step_count;
    private int total_coins;

    private float distance, history_distance, total_distance;
    private float mean_speed, history_mean_speed;
    private float recent_speed;
    private float min_speed;
    private float max_speed;
    private int seconds_spent;
    private int record_count;
    private int earned_coin_value;
    private int second_count;
    private String step_count;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private String [] mode_string = {"Standard","Night","Silver","Aubergine"};

    private TextView distance_textView;
    private TextView speed_textView;
    private TextView duration_textView;

    private TextView big_duration_textView;

    private boolean updateFlag, startFlag;
    List<Polyline> polylines = new ArrayList<Polyline>();

    private LatLng currentlatLng;

    SupportMapFragment mapFragment;
    FusedLocationProviderClient client;

    private FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference userDb, locationDb, runRecordDb;

    Runnable runnable;

    LineChart chart;
    CheckBox mCheckBox1;
    List<CheckBox> mCheckBoxList = new ArrayList<>();

    // 折线编号
    public static final int LINE_NUMBER_1 = 0;
    public static final int LINE_NUMBER_2 = 1;
    public static final int LINE_NUMBER_3 = 2;

    LineChart mLineChart; // 折线表，存线集合
    LineData mLineData; // 线集合，全部折现以数组的形式存到此集合中
    XAxis mXAxis; //X轴
    YAxis mLeftYAxis; //左侧Y轴
    YAxis mRightYAxis; //右侧Y轴
    Legend mLegend; //图例
    LimitLine mLimitline; //限制线

    //  Y值数据链表
    List<Float> mList1 = new ArrayList<>();
    List<Float> mList2 = new ArrayList<>();
    List<Float> mList3 = new ArrayList<>();

    // Chart须要的点数据链表
    List<Entry> mEntries1 = new ArrayList<>();
    List<Entry> mEntries2 = new ArrayList<>();
    List<Entry> mEntries3 = new ArrayList<>();

    // LineDataSet:点集合,即一条线
    LineDataSet mLineDataSet1 = new LineDataSet(mEntries1, "Speed");
    LineDataSet mLineDataSet2 = new LineDataSet(mEntries2, "");
    LineDataSet mLineDataSet3 = new LineDataSet(mEntries3, "unknown");


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
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
    protected void onPause() {
        super.onPause();

        getCurrentLocation();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        Spinner mode_choice = findViewById(R.id.mode_shifting);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, mode_string);
        mode_choice.setAdapter(arrayAdapter);
        mode_choice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateMode(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        updateFlag = false;
        distance_textView = (TextView) findViewById(R.id.running_distanceTextView);
        duration_textView = (TextView) findViewById(R.id.running_timerTextView);
        speed_textView = (TextView) findViewById(R.id.running_speedTextView);
        big_duration_textView = (TextView) findViewById(R.id.bigDuration);
        chart = (LineChart) findViewById(R.id.runningChart);


        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //System.out.println(" *********** "+intent.getIntExtra("steps",0));
                step_count = Integer.toString(intent.getIntExtra("steps", 0));
            }
        };
        intentFilter = new IntentFilter();
        intentFilter.addAction(StepCounterService.STEP_COUNTER_BROADCAST);
        registerReceiver(broadcastReceiver, intentFilter);

        //init
        startCount = false;
        //Database reference
        mDatabase = FirebaseDatabase.getInstance();
        locationDb = mDatabase.getReference("locations");
        runRecordDb = mDatabase.getReference("run_records");
        userDb = mDatabase.getReference("users");
        //Initial user information
        mAuth = FirebaseAuth.getInstance();

        client = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(RunningActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //When Permission granted
            getCurrentLocation();
        } else {
            //Request permission
            ActivityCompat.requestPermissions(RunningActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        findViewById(R.id.startRun).setOnClickListener(this);
        findViewById(R.id.stopRun).setOnClickListener(this);
        findViewById(R.id.goBack).setOnClickListener(this);
//        findViewById(R.id.mode_shifting).setOnClickListener(this);

        //init chart
        //init view
        mCheckBox1 = findViewById(R.id.demo_checkbox1);
//        mCheckBox2 = findViewById(R.id.demo_checkbox2);
//        mCheckBox3 = findViewById(R.id.demo_checkbox3);
        mCheckBoxList.add(mCheckBox1);
//        mCheckBoxList.add(mCheckBox2);
//        mCheckBoxList.add(mCheckBox3);

        mCheckBox1.setOnClickListener(this);
//        mCheckBox2.setOnClickListener(this);
//        mCheckBox3.setOnClickListener(this);
        initLineChart();
        showLine(LINE_NUMBER_1);

        //Run updateLocation every x minutes
        final int delay = 1000; //milliseconds
        final Handler handler = new Handler(Looper.getMainLooper());
        second_count = 0;
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {

                handler.postDelayed(runnable, delay);
                if (startCount) {
                    second_count++;
                    Log.d(TAG, "run: running");
                    updateLocation();
                    updateTime();
                    updateSpeed();
//                    addLine2Data(recent_speed+100);

                    Log.d(TAG, "run: " + distance);
                    Log.d(TAG, "run: " + recent_speed);

                } else {

                    //Reset all data
                    second_count = 0;
                    distance_textView.setText("0 m");
                    distance = 0;
                    duration_textView.setText("00 : 00");

                    speed_textView.setText("0 m/s");
                    big_duration_textView.setText("00:00:00");
                }

            }
        }, delay);


    }

    public void initLineChart() {
        mLineChart = findViewById(R.id.runningChart);
        mXAxis = mLineChart.getXAxis(); // 获得x轴
        mLeftYAxis = mLineChart.getAxisLeft(); // 获得侧Y轴
        mRightYAxis = mLineChart.getAxisRight(); // 获得右侧Y轴
        mLegend = mLineChart.getLegend(); // 获得图例
        mLineData = new LineData();
        mLineChart.setData(mLineData);

        // 设置图标基本属性
        setChartBasicAttr(mLineChart);

        // 设置XY轴
        setXYAxis(mLineChart, mXAxis, mLeftYAxis, mRightYAxis, 0, 0, 15);

        // 添加线条
        initLine();

        // 设置图例
        createLegend(mLegend);

        // 设置MarkerView
        setMarkerView(mLineChart);
    }

    void setChartBasicAttr(LineChart lineChart) {

        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setTouchEnabled(true);

        //lineChart.animateY(2500);
        lineChart.animateX(1500);
    }

    void setXYAxis(LineChart lineChart, XAxis xAxis, YAxis leftYAxis, YAxis rightYAxis, int second_count, float minY, float maxY) {

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(second_count + 5);
        xAxis.setLabelCount(second_count, false);
        xAxis.setGranularity(1f);
        lineChart.setVisibleXRangeMaximum(20);
        //
        leftYAxis.setAxisMinimum(minY);
        rightYAxis.setAxisMinimum(minY);
        leftYAxis.setAxisMaximum(maxY);
        rightYAxis.setAxisMaximum(maxY);
//        leftYAxis.setGranularity(1f);
//        rightYAxis.setGranularity(1f);

        leftYAxis.setLabelCount(second_count, false);
        lineChart.setVisibleYRangeMaximum(maxY, YAxis.AxisDependency.LEFT);
        lineChart.setVisibleYRangeMaximum(maxY, YAxis.AxisDependency.RIGHT);
        leftYAxis.setEnabled(false);

    }

    void initLine() {

        createLine(mList1, mEntries1, mLineDataSet1, Color.RED, mLineData, mLineChart);
        createLine(mList2, mEntries2, mLineDataSet2, Color.BLACK, mLineData, mLineChart);
        createLine(mList3, mEntries3, mLineDataSet3, Color.YELLOW, mLineData, mLineChart);



        for (int i = 0; i < mLineData.getDataSetCount(); i++) {
            mLineChart.getLineData().getDataSets().get(i).setVisible(false); //
        }
        showLine(LINE_NUMBER_1);
    }

    public void showLine(int index) {
        mLineChart
                .getLineData()
                .getDataSets()
                .get(index)
                .setVisible(mCheckBoxList.get(index).isChecked());
        mLineChart.invalidate();
    }


    private void createLine(List<Float> dataList, List<Entry> entries, LineDataSet lineDataSet, int color, LineData lineData, LineChart lineChart) {
        for (int i = 0; i < dataList.size(); i++) {

            Entry entry = new Entry(i, dataList.get(i));// Entry(x,y)
            entries.add(entry);
        }

        // 初始化线条
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.CUBIC_BEZIER);

        if (lineData == null) {
            lineData = new LineData();
            lineData.addDataSet(lineDataSet);
            lineChart.setData(lineData);
        } else {
            lineChart.getLineData().addDataSet(lineDataSet);
        }

        lineChart.invalidate();
    }

    private void initLineDataSet(LineDataSet lineDataSet, int color, LineDataSet.Mode mode) {
        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setValueTextSize(10f);

        lineDataSet.setDrawFilled(true);
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setFormSize(15.f);
        if (mode == null) {
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            lineDataSet.setMode(mode);
        }

    }

    private void createLegend(Legend legend) {


        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12f);

        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);

        legend.setDrawInside(false);
        legend.setEnabled(true);
    }


    public void setMarkerView(LineChart lineChart) {

    }

    public void addEntry(LineData lineData, LineChart lineChart, float yValues, int index) {

        // 经过索引获得一条折线，以后获得折线上当前点的数量
        int xCount = lineData.getDataSetByIndex(index).getEntryCount();


        Entry entry = new Entry(xCount, yValues); // 建立一个点
        lineData.addEntry(entry, index); // 将entry添加到指定索引处的折线中
        Log.d(TAG, "addEntry: " + yValues);

        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();

        lineChart.moveViewToAnimated(xCount - 4, yValues, YAxis.AxisDependency.LEFT, 1000);// TODO: 内存泄漏，异步 待修复
        lineChart.invalidate();
    }


    public void addLine1Data(float yValues) {
        addEntry(mLineData, mLineChart, yValues, LINE_NUMBER_1);
    }

    public void onButtonShowPopupWindowClick(View view) {
        // Pop up window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);


        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; //let taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        Button nav_login = (Button) popupWindow.getContentView().findViewById(R.id.nav_pop_login);
        Button nav_register = (Button) popupWindow.getContentView().findViewById(R.id.nav_pop_register);
        nav_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RunningActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        nav_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RunningActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


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

    private void updateSpeed() {
        if (startCount) {
            mean_speed = distance / seconds_spent;
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String speedString = decimalFormat.format(recent_speed);
            speed_textView.setText(speedString + "m/s");
            if (mean_speed > recent_speed) {
                max_speed = recent_speed;
            }
            if (min_speed > recent_speed) {
                min_speed = recent_speed;
            }
        }
    }

    private void updateTime() {
        //Time calculation
        if (startCount) {
            Calendar calendar = Calendar.getInstance();
            Date finishTime = calendar.getTime();
            Log.d(TAG, "onClick: " + finishTime);
            long diff = finishTime.getTime() - startTime.getTime();
            Log.d(TAG, "onClick: " + diff);


            int diffHours = (int) diff / (60 * 60 * 1000);
            int diffMinutes = (int) diff / (60 * 1000) - 60 * diffHours;
            int diffSeconds = (int) diff / 1000 - 60 * 60 * diffHours - 60 * diffMinutes;
            seconds_spent = (int) diff / 1000;
            String hourString = Integer.toString(diffHours);
            String minuteString = Integer.toString(diffMinutes);
            String secondString = Integer.toString(diffSeconds);
            if (diffSeconds < 10) {
                secondString = '0' + secondString;
            }
            if (diffMinutes < 10) {
                minuteString = '0' + minuteString;
            }
            if (diffHours < 10) {
                hourString = '0' + hourString;
            }
            String timeString = hourString + ':' + minuteString + ':' + secondString;
            duration_textView.setText(timeString);
            big_duration_textView.setText(timeString);
        }
    }

    private float getDistance(double lat_a, double lng_a, double lat_b, double lng_b) {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }

    private class MyLocationListener implements LocationListener {


        @Override
        public void onLocationChanged(Location loc) {
            Log.d(TAG, "onLocationChanged: "+loc.getLatitude() + " Lng: "
                    + loc.getLongitude());
            String longitude = "Longitude: " + loc.getLongitude();
            Log.v(TAG, longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            Log.v(TAG, latitude);

            currentlatLng = new LatLng(loc.getLatitude(),
                                    loc.getLongitude());
            updateFlag = true;

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(RunningActivity.this, "Please enable the gps", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: enable");
            Toast.makeText(RunningActivity.this, "GPS ENABLE", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    private void updateLocation() {

        if (updateFlag){
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {

                    float curr_distance = getDistance(startlatLng.latitude, startlatLng.longitude, currentlatLng.latitude, currentlatLng.longitude);
                    distance += curr_distance;
                    Calendar calendar = Calendar.getInstance();
                    Date finishTime = calendar.getTime();
                    Log.d(TAG, "onClick: "+finishTime);
                    long diff = finishTime.getTime() - timeOne.getTime();
                    timeOne = finishTime;
                    int diffSeconds = (int) diff/1000;
                    recent_speed = curr_distance/diffSeconds;
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    String distanceString = decimalFormat.format(distance);
                    distance_textView.setText(distanceString+" m");


                    Polyline line = googleMap.addPolyline(new PolylineOptions()
                            .add(startlatLng, currentlatLng)
                            .width(5)
                            .color(Color.RED));

                    polylines.add(line);
                    startlatLng = currentlatLng;
                    Log.d(TAG, "onMapReady: updating startlatLng"+startlatLng);
                    float zoom = 15;
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlatLng,zoom));

                    setXYAxis(mLineChart, mXAxis, mLeftYAxis, mRightYAxis, second_count, 0, 15);
                    if (recent_speed<15){
                        addLine1Data(recent_speed);
                    } else {
                        addLine1Data(0);
                    }

                }
            });
            updateFlag = false;
        }


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
    private int calculate(float distance, long diffTime){

        int result = (int) (distance/100);
        return result;
    }

    private void storeRecords(int count, long diffSeconds){
        //Store data in runRecordDb
        String uid = mAuth.getCurrentUser().getUid();
        if (count>=0){
            count++;
            Log.d(TAG, "storeRecords: "+count);
            String record_count_String = Integer.toString(count);


//-----------------------------------------------------------------------------------
//          total recording
            total_distance += distance;
            total_timelast += diffSeconds;
            total_step_count += Integer.parseInt(step_count);
            total_coins += earned_coin_value;

            TotalInfo totalRunInfo =  new TotalInfo(total_timelast, total_distance,String.valueOf(total_step_count), total_coins);
            runRecordDb.child(uid).child("total_record").setValue(totalRunInfo);
//-----------------------------------------------------------------------------------
//          current date recording
            history_distance += distance;
            history_mean_speed = (history_mean_speed*(count-1)+mean_speed)/count;
            history_timelast += diffSeconds;
            history_step_count += Integer.parseInt(step_count);
            RunInfo historyRunInfo = new RunInfo(history_timelast, history_distance, history_mean_speed, 0, 0, startDate, String.valueOf(history_step_count));
            runRecordDb.child(uid).child(startDate).child("0").setValue(historyRunInfo);
//-----------------------------------------------------------------------------------
//          current recording
            RunInfo runInfo1 = new RunInfo(diffSeconds, distance, mean_speed, max_speed, earned_coin_value, startDate, step_count);
            runRecordDb.child(uid).child(startDate).child(record_count_String).setValue(runInfo1);

            userDb.child(uid).child("records_count").child(startDate).setValue(count);
        }
    }

    private void updateMode(final int mode_num){
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                try {
                    // Customise the styling of the base map using a JSON object defined
                    // in a raw resource file.

                    switch (mode_num){
                        case 0:
                            googleMap.setMapStyle(
                                    MapStyleOptions.loadRawResourceStyle(
                                            RunningActivity.this, R.raw.style_standard_json));
                            break;
                        case 1:
                            googleMap.setMapStyle(
                                    MapStyleOptions.loadRawResourceStyle(
                                            RunningActivity.this, R.raw.style_night_json));
                            break;
                        case 2:
                            googleMap.setMapStyle(
                                    MapStyleOptions.loadRawResourceStyle(
                                            RunningActivity.this, R.raw.style_silver_json));
                            break;
                        case 3:
                            googleMap.setMapStyle(
                                    MapStyleOptions.loadRawResourceStyle(
                                            RunningActivity.this, R.raw.style_aubergine_json));
                            break;


                    }


                } catch (Resources.NotFoundException e) {
                    Log.e(TAG, "Can't find style. Error: ", e);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.goBack: {
                finish();
                break;
            }
            case R.id.startRun:{
                initLineChart();
                if (mAuth.getCurrentUser()==null){
                    Toast.makeText(RunningActivity.this, "Please Login first", Toast.LENGTH_LONG).show();
                    onButtonShowPopupWindowClick(view);

                }else {
                    LocationManager locationManager = (LocationManager)
                            getSystemService(Context.LOCATION_SERVICE);
                    LocationListener locationListener = new MyLocationListener();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    startDate = simpleDateFormat.format(calendar.getTime());
                    startTime = calendar.getTime();
                    timeOne = calendar.getTime();
                    startCount = true;
                    max_speed = 0;
                    min_speed = 100;


//                    startDate = "2020-11-02";
                    registerReceiver(broadcastReceiver, intentFilter);
                    Intent intent = new Intent(RunningActivity.this, StepCounterService.class);
                    startService(intent);

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

                    runRecordDb.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String uid = mAuth.getCurrentUser().getUid();

                            RunInfo runInfo = snapshot.child(uid).child(startDate).child("0").getValue(RunInfo.class);
                            if (runInfo !=null){
                                history_distance = runInfo.getDistance();
                                history_mean_speed = runInfo.getMean_speed();
                                history_timelast = runInfo.getTimelast();
                                history_step_count = Integer.parseInt(runInfo.getStep());
                            } else{
                                history_timelast = 0;
                                history_mean_speed = 0;
                                history_distance = 0;
                                history_step_count = 0;
                            }

                            TotalInfo totalInfo = snapshot.child(uid).child("total_record").getValue(TotalInfo.class);
                            if (totalInfo!=null){
                                total_distance = totalInfo.getDistance();
                                total_timelast = totalInfo.getTimelast();
                                total_step_count = Integer.parseInt(totalInfo.getStep());
                                total_coins = totalInfo.getCoins();
                            } else {
                                total_timelast = 0;
                                total_distance = 0;
                                total_step_count = 0;
                                total_coins = 0;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }


                break;
            }
            case R.id.stopRun:{
                if (startCount){
                    for(Polyline line : polylines)
                    {
                        line.remove();
                    }

                    polylines.clear();

                    initLine();
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

                    } else {
                        //calculate earned coins
                        earned_coin_value = calculate(this.distance, diffHours);
                        Log.d(TAG, "onClick: "+earned_coin_value);
                        DecimalFormat decimalFormat = new DecimalFormat("0.00");
                        String earned_coin_string = decimalFormat.format(earned_coin_value);

                        Intent myService = new Intent(RunningActivity.this, StepCounterService.class);
                        stopService(myService);

                        storeRecords(record_count, diffSeconds);

                        Intent intent = new Intent(RunningActivity.this, RunningFinished.class);
                        intent.putExtra("record_count", String.valueOf(record_count+1));
                        startActivity(intent);
                        break;
                    }



                }else{
                    Toast.makeText(RunningActivity.this,"Please start first", Toast.LENGTH_SHORT).show();
                }

                break;
            }
            case R.id.demo_checkbox1:{
                showLine(LINE_NUMBER_1);
                break;
            }

        }
    }

    public void onStop() {
        try {
            unregisterReceiver(broadcastReceiver);
        }catch (Exception e){
            System.out.println("Remaining running");
        }
        super.onStop();
        // Chart须要的点数据链表
        mEntries1 = new ArrayList<>();
        mEntries2 = new ArrayList<>();
        mEntries3 = new ArrayList<>();

        // LineDataSet:点集合,即一条线
        mLineDataSet1 = new LineDataSet(mEntries1, "Speed");
        mLineDataSet2 = new LineDataSet(mEntries2, "");
        mLineDataSet3 = new LineDataSet(mEntries3, "unknown");
        initLineChart();

    }

    @Override
    public void onResume() {
        try {
            registerReceiver(broadcastReceiver, intentFilter);
        }catch (Exception e){
            System.out.println("Remaining running");
        }
        super.onResume();
    }

    @Override
    public void onRestart(){
        super.onRestart();
    }

}