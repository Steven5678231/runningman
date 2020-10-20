package com.example.runningman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.runningman.service.StepCounterService;

public class StepCounterActivity extends AppCompatActivity {
    private TextView textView;
    private Button startButton;
    private Button resetButton;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        textView = findViewById(R.id.textView);
        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);
        broadcastReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                //System.out.println(" *********** "+intent.getIntExtra("steps",0));
                textView.setText(Integer.toString(intent.getIntExtra("steps",0)));
            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction(StepCounterService.STEP_COUNTER_BROADCAST);
        registerReceiver(broadcastReceiver, intentFilter);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerReceiver(broadcastReceiver, intentFilter);
                Intent intent = new Intent(StepCounterActivity.this, StepCounterService.class);
                startService(intent);
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent myService = new Intent(StepCounterActivity.this, StepCounterService.class);
                    stopService(myService);
                    textView.setText("0");
                    unregisterReceiver(broadcastReceiver);
                }catch (Exception e){
                    System.out.println("I don't care");
                }
            }
        });


    }
    @Override
    public void onStop() {
        try {
            unregisterReceiver(broadcastReceiver);
        }catch (Exception e){
            System.out.println("Remaining running");
        }
        super.onStop();
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

}
