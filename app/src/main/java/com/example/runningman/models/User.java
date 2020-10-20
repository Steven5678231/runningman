package com.example.runningman.models;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String email;
    private String user_id;
    private String username;
    private String extraInfo;
    private int records_count;

    public User(){

    }

    public User(String email, String user_id, String username, String extraInfo, int records_count) {
        this.email = email;
        this.user_id = user_id;
        this.username = username;
        this.extraInfo = extraInfo;
        this.records_count = records_count;
    }

    protected User(Parcel in) {
        email = in.readString();
        user_id = in.readString();
        username = in.readString();
        extraInfo = in.readString();
        records_count = in.readInt();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getEmail() {
        return email;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public int getRecords_count(){return records_count;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(email);
        parcel.writeString(user_id);
        parcel.writeString(username);
        parcel.writeString(extraInfo);
        parcel.writeInt(records_count);
    }
}
