package com.example.runningman;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfile extends Activity implements View.OnClickListener {

    EditText weight_text, address_text, phone_text, description_text, username_text;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference usersDb;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        weight_text = (EditText)findViewById(R.id.edit_weight);
        address_text = (EditText)findViewById(R.id.edit_address);
        phone_text = (EditText)findViewById(R.id.edit_phone);
        description_text = (EditText)findViewById(R.id.edit_description);
        username_text = (EditText)findViewById(R.id.edit_username);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        usersDb = mDatabase.getReference("users");
        uid = mAuth.getCurrentUser().getUid();

        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_update).setOnClickListener(this);

        usersDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mAuth.getCurrentUser() != null) {
                    String uid = mAuth.getCurrentUser().getUid();
                    String username = snapshot.child(uid).child("username").getValue(String.class);
                    TextView name_textview = (TextView) findViewById(R.id.tv_user_name);
                    name_textview.setText(username);

                    EditText username_textview = (EditText) findViewById(R.id.edit_username);
                    username_textview.setText(username);

                    String address = snapshot.child(uid).child("address").getValue(String.class);
                    EditText address_textview = (EditText) findViewById(R.id.edit_address);
                    address_textview.setText(address);

                    String weight = snapshot.child(uid).child("weight").getValue(String.class);
                    EditText weight_textview = (EditText) findViewById(R.id.edit_weight);
                    weight_textview.setText(weight);

                    String description = snapshot.child(uid).child("extraInfo").getValue(String.class);
                    EditText description_textview = (EditText) findViewById(R.id.edit_description);
                    description_textview.setText(description);

                    String phone = snapshot.child(uid).child("phone").getValue(String.class);
                    EditText phone_textview = (EditText) findViewById(R.id.edit_phone);
                    phone_textview.setText(phone);

                    String email = snapshot.child(uid).child("email").getValue(String.class);
                    TextView email_textview = (TextView) findViewById(R.id.edit_email);
                    email_textview.setText(email);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_back:{
                finish();
                break;
            }
            case R.id.btn_update:{
                usersDb.child(uid).child("weight").setValue(weight_text.getText().toString());
                usersDb.child(uid).child("address").setValue(address_text.getText().toString());
                usersDb.child(uid).child("phone").setValue(phone_text.getText().toString());
                usersDb.child(uid).child("extraInfo").setValue(description_text.getText().toString());
                usersDb.child(uid).child("username").setValue(username_text.getText().toString());
                finish();
                break;
            }
        }

    }
}