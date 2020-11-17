package com.example.runningman;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.runningman.models.TotalInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unity3d.player.UnityPlayerActivity;

public class UnityPlayer extends Activity implements View.OnClickListener {

    private static final String TAG = "UnityPlayer";
    private FirebaseAuth mAuth;

    private FirebaseDatabase mDatabase;
    private DatabaseReference usersDb, runRecordsDb;
    private int coins = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unity_player);
        findViewById(R.id.goBack_unity).setOnClickListener(this);

        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");
        String coin_str = intent.getStringExtra("coins");

        intent = new Intent(UnityPlayer.this, UnityPlayerActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("coins", coin_str);
        startActivity(intent);


    }

    private String getUserID() {
        String uid = mAuth.getCurrentUser().getUid();

        return uid;
    }

    private void sendDataToUnity() {
        runRecordsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                TotalInfo totalRecord = snapshot.child(getUserID()).child("total_record").getValue(TotalInfo.class);
                coins = totalRecord.getCoins();
                Intent intent = new Intent(UnityPlayer.this, UnityPlayerActivity.class);
                intent.putExtra("uid", getUserID());
                intent.putExtra("coins", Integer.toString(coins));
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.goBack_unity:{
                Intent intent = new Intent(UnityPlayer.this, PlayGround.class);
                startActivity(intent);
                finish();
                break;
            }
        }
    }
}