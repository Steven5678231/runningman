package com.example.runningman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.runningman.models.TotalInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class PlayGround extends Activity implements View.OnClickListener{

    private Context context;
    private FirebaseAuth mAuth;

    private FirebaseDatabase mDatabase;
    private DatabaseReference usersDb, runRecordsDb;

    private TextView tv_go_login_or_out;

    StorageReference profileDb;
    private int coins = 0;

    private static final String TAG = "PlayGround";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_ground);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        tv_go_login_or_out = (TextView) findViewById(R.id.tv_go_login_or_out);
        findViewById(R.id.go_city_page).setOnClickListener(this);
        findViewById(R.id.go_run_page).setOnClickListener(this);
        initViewAndSetDrawClick();

        if (mAuth.getCurrentUser()!=null){
            usersDb = FirebaseDatabase.getInstance().getReference("users");

            runRecordsDb = FirebaseDatabase.getInstance().getReference("run_records");

            usersDb.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (mAuth.getCurrentUser() != null) {
                        String uid = mAuth.getCurrentUser().getUid();
                        String username = snapshot.child(uid).child("username").getValue(String.class);
                        TextView name_textview = (TextView) findViewById(R.id.tv_user_name);
                        name_textview.setText(username);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            profileDb = FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid());
            StorageReference imageRef = profileDb.child("profile");
            imageRef.getBytes(1024*1024*5)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            ImageView head_pic = (ImageView) findViewById(R.id.iv_head_image);
                            head_pic.setImageBitmap(bitmap);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

        }
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
                Intent intent = new Intent(PlayGround.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        nav_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PlayGround.this, RegisterActivity.class);
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

    private void initViewAndSetDrawClick() {
        final Activity activity = this;

        TextView tv_title;
        final DrawerLayout dl_layout;

        tv_title = findViewById(R.id.text_title);
        tv_title.setText("Running man");
        dl_layout =  findViewById(R.id.dl_layout);

        if (mAuth.getCurrentUser() != null){
            tv_go_login_or_out.setText("logout");
        }else {
            tv_go_login_or_out.setText("login");
        }

        //设置左上角的图标和监听事件
        findViewById(R.id.show_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dl_layout.isDrawerOpen(Gravity.LEFT)) {
                    dl_layout.closeDrawer(Gravity.LEFT);
                } else {//如果已经是关闭状态
                    dl_layout.openDrawer(Gravity.LEFT);
                }
            }
        });
        //跳转到usepage页面
        findViewById(R.id.tv_go_usepage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity,UserPage.class);
                dl_layout.closeDrawer(Gravity.LEFT);
                startActivity(intent);

            }
        });
        //跳转到playground页面
        findViewById(R.id.tv_go_playground).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity,PlayGround.class);
                dl_layout.closeDrawer(Gravity.LEFT);
                startActivity(intent);
            }
        });

        //跳转到login页面
        tv_go_login_or_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser()!=null){
                    tv_go_login_or_out.setText("login");
                    mAuth.signOut();
                    Intent intent = new Intent(PlayGround.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    tv_go_login_or_out.setText("logout");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.go_city_page:{

                if (mAuth.getCurrentUser()==null){
                    onButtonShowPopupWindowClick(view);
                } else{
                    sendDataToUnity();

                }

                break;
            }
            case R.id.go_run_page:{
                if (mAuth.getCurrentUser()==null){
                    onButtonShowPopupWindowClick(view);
                } else{
                    Intent intent = new Intent(PlayGround.this, RunningActivity.class);
                    startActivity(intent);
                }

                break;
            }

        }

    }

    private void sendDataToUnity() {
        runRecordsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                TotalInfo totalRecord = snapshot.child(getUserID()).child("total_record").getValue(TotalInfo.class);
                if (totalRecord!=null){
                    coins = totalRecord.getCoins();

                } else {
                    coins = 0;
                }
                Intent intent = new Intent(PlayGround.this, UnityPlayer.class);
                intent.putExtra("uid", getUserID());
                intent.putExtra("coins", Integer.toString(coins));
                startActivity(intent);

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }


    private String getUserID() {
        String uid = mAuth.getCurrentUser().getUid();

        return uid;
    }

    public static void ReceiveData(String uid, String coins) {
        Log.i("TAG", "The uid is " + uid);
        Log.i("TAG", "The coins is " + coins);
        DatabaseReference runRecordsDb = FirebaseDatabase.getInstance().getReference("run_records");
        runRecordsDb.child(uid).child("total_record").child("coins").setValue(Integer.parseInt(coins));
    }
}