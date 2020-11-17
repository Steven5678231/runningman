package com.example.runningman;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import com.example.runningman.models.TotalInfo;
import com.example.runningman.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.DecimalFormat;

public class UserPage extends Activity implements View.OnClickListener {

    private static final String TAG = "UserPage";
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView tv_go_login_or_out;

    private String username;

    TotalInfo totalInfo;


    private DrawerLayout dl_layout;
    private Button mBtnRecording;

    FirebaseDatabase mDatabase;
    FirebaseAuth mAuth;
    DatabaseReference usersDb, runRecordDb;

    StorageReference profileDb;
    private StorageTask storageTask;

    ImageView profilePic;
    private Uri imageUri;

    User user;

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
        setContentView(R.layout.activity_user_page);
        //refers to database
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btn_editing).setOnClickListener(this);
        tv_go_login_or_out = (TextView) findViewById(R.id.tv_go_login_or_out);
        initViewAndSetDrawClick();

        mDatabase = FirebaseDatabase.getInstance();
        usersDb = mDatabase.getReference("users");
        runRecordDb = mDatabase.getReference("run_records");



        if (mAuth.getCurrentUser()==null){
            Intent intent = new Intent(UserPage.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else {
            profileDb = FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid());
        }

        mBtnRecording = findViewById(R.id.btn_recording);
        mBtnRecording.setOnClickListener(this);

        profilePic = (ImageView) findViewById(R.id.profilePic);
        profilePic.setOnClickListener(this);


        usersDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mAuth.getCurrentUser() != null) {
                    String uid = mAuth.getCurrentUser().getUid();
                    String username = snapshot.child(uid).child("username").getValue(String.class);
                    TextView name_textview = (TextView) findViewById(R.id.tv_user_name);
                    name_textview.setText(username);

                    TextView username_textview = (TextView) findViewById(R.id.profile_username);
                    username_textview.setText(username);

                    String address = snapshot.child(uid).child("address").getValue(String.class);
                    TextView address_textview = (TextView) findViewById(R.id.profile_address);
                    address_textview.setText(address);

                    String weight = snapshot.child(uid).child("weight").getValue(String.class);
                    TextView weight_textview = (TextView) findViewById(R.id.profile_weight);
                    weight_textview.setText(weight);

                    String description = snapshot.child(uid).child("extraInfo").getValue(String.class);
                    TextView description_textview = (TextView) findViewById(R.id.profile_description);
                    description_textview.setText(description);

                    String phone = snapshot.child(uid).child("phone").getValue(String.class);
                    TextView phone_textview = (TextView) findViewById(R.id.profile_phone);
                    phone_textview.setText(phone);

                    String email = snapshot.child(uid).child("email").getValue(String.class);
                    TextView email_textview = (TextView) findViewById(R.id.profile_email);
                    email_textview.setText(email);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        runRecordDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mAuth.getCurrentUser()!= null){
                    String uid = mAuth.getCurrentUser().getUid();
                    totalInfo = snapshot.child(uid).child("total_record").getValue(TotalInfo.class);
                    updateInfo();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





        if (mAuth.getCurrentUser()!=null){
            StorageReference imageRef = profileDb.child("profile");
            imageRef.getBytes(1024*1024*5)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            profilePic.setImageBitmap(bitmap);
                            ImageView head_pic = (ImageView) findViewById(R.id.iv_head_image);
                            head_pic.setImageBitmap(bitmap);
                        }
                    })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UserPage.this, "You can upload a selfie by clicking the empty area above", Toast.LENGTH_SHORT).show();
                }
            });

        }




    }

    private void updateInfo(){
        if (totalInfo!=null){

            long diff = totalInfo.getTimelast();
            float distance = totalInfo.getDistance()/1000;
            String steps = totalInfo.getStep();
            int coins = totalInfo.getCoins();
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String distance_show =  decimalFormat.format(distance)+" km";



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
            String time_show = hourString+"h "+minuteString+"mins";


            String coins_show = String.valueOf(coins);

            TextView distance_text = (TextView)findViewById(R.id.distance_user);
            TextView duration_text = (TextView)findViewById(R.id.duration_user);
            TextView coins_text = (TextView)findViewById(R.id.coins_user);
            coins_text.setText(coins_show);
            distance_text.setText(distance_show);
            duration_text.setText(time_show);
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
                Intent intent = new Intent(UserPage.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        nav_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserPage.this, RegisterActivity.class);
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
                if (mAuth.getCurrentUser() != null){
                    mAuth.signOut();
                    CharSequence text = "logout 23333!";
                    Toast toast = Toast.makeText(UserPage.this, text, Toast.LENGTH_LONG);
                    toast.show();
                    tv_go_login_or_out.setText("login");
                }else {
                    Intent intent = new Intent(UserPage.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.profilePic:{
                if (mAuth.getCurrentUser()==null){
                    onButtonShowPopupWindowClick(view);

                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }

                break;
            }
            case R.id.btn_editing:{
                if (mAuth.getCurrentUser()==null){
                    onButtonShowPopupWindowClick(view);

                } else {
                    Intent intent = new Intent(UserPage.this, EditProfile.class);
                    startActivity(intent);
                    onRestart();
                }

                break;
            }
            case R.id.btn_recording:{

                if (mAuth.getCurrentUser()==null){
                    onButtonShowPopupWindowClick(view);

                } else {
                    // Open Record
                    Intent intent = new Intent(UserPage.this, RunningRecording.class);
                    startActivity(intent);
                }


                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data !=null && data.getData()!=null){
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(profilePic);
            uploadFile();
//            profilePic.setImageURI(imageUri);
        }
    }

    private void uploadFile(){
        if (imageUri != null){
            if (mAuth.getCurrentUser()!=null){
                String uid = mAuth.getCurrentUser().getUid();
                StorageReference fileReference = profileDb.child("profile");
                Bitmap bmp = null;
                try {
                    bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                byte[] data = baos.toByteArray();
                storageTask = fileReference.putBytes(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());


                            }
                        });

                Task<Uri> urlTask = storageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return profileDb.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            String uid = mAuth.getCurrentUser().getUid();
                            usersDb.child(uid).child("profileUri").setValue(downloadUri);
                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });



            } else {
                Toast.makeText(this, "please login first", Toast.LENGTH_SHORT).show();
            }


        } else {
            Toast.makeText(this,"no file!",Toast.LENGTH_SHORT).show();
        }
    }
}
