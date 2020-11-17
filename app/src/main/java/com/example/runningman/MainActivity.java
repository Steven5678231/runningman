package com.example.runningman;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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


public class MainActivity extends Activity implements View.OnClickListener {


    private TextView tv_show_left;
    private TextView tv_title;
    private ImageView iv_head;
    private TextView tv_user_name;
    private TextView tv_go_userpage;
    private TextView tv_go_playground;
    private TextView welcome_text;
    private TextView tv_already_login;
    private TextView tv_go_login_or_out;
    private Button mainRegister;
    private Button mainSignin;


    private DrawerLayout dl_layout;

    FirebaseAuth mAuth;
    DatabaseReference usersDb;

    StorageReference profileDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        tv_go_login_or_out = (TextView) findViewById(R.id.tv_go_login_or_out);
        tv_already_login = (TextView) findViewById(R.id.tv_already_login);
        welcome_text = (TextView) findViewById(R.id.welcome);
        mainRegister = (Button) findViewById(R.id.mainRegister);
        mainSignin = (Button) findViewById(R.id.mainSignin);

        intiView();

        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/campus.ttf");
        welcome_text.setTypeface(typeface);
        tv_already_login.setTypeface(typeface);

        findViewById(R.id.mainRegister).setOnClickListener(this);
        findViewById(R.id.mainSignin).setOnClickListener(this);
        initViewAndSetDrawClick();

        if (mAuth.getCurrentUser()!=null) {
            usersDb = FirebaseDatabase.getInstance().getReference("users");
            tv_already_login.setVisibility(View.VISIBLE);
            mainSignin.setVisibility(View.INVISIBLE);
            mainRegister.setVisibility(View.INVISIBLE);

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
            imageRef.getBytes(1024*1024*2)
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

        } else {
            tv_already_login.setVisibility(View.INVISIBLE);
            mainSignin.setVisibility(View.VISIBLE);
            mainRegister.setVisibility(View.VISIBLE);
        }


    }

    public void intiView() {

        iv_head = findViewById(R.id.iv_head_image);//用户头像
        tv_user_name=findViewById(R.id.tv_user_name);//用户姓名

        if (mAuth.getCurrentUser() != null){
            tv_go_login_or_out.setText("logout");
            tv_already_login.setVisibility(View.VISIBLE);
            mainSignin.setVisibility(View.INVISIBLE);
            mainRegister.setVisibility(View.INVISIBLE);
        }else {
            tv_go_login_or_out.setText("login");
            tv_already_login.setVisibility(View.INVISIBLE);
            mainSignin.setVisibility(View.VISIBLE);
            mainRegister.setVisibility(View.VISIBLE);
        }
    }

    private void initViewAndSetDrawClick() {
        final Activity activity = this;

        TextView tv_title;
        final DrawerLayout dl_layout;

        tv_title = findViewById(R.id.text_title);
        tv_title.setText("Running man");
        dl_layout =  findViewById(R.id.dl_layout);

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
                if (mAuth.getCurrentUser() != null){
                    mAuth.signOut();
                    CharSequence text = "You have logout!";
                    Toast toast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG);
                    toast.show();
                    tv_go_login_or_out.setText("login");
                    tv_already_login.setVisibility(View.INVISIBLE);
                    mainSignin.setVisibility(View.VISIBLE);
                    mainRegister.setVisibility(View.VISIBLE);
                }else {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.mainRegister:{
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.mainSignin:{
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
            }


        }

    }
}
