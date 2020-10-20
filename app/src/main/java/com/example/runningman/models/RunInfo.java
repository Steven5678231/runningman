package com.example.runningman.models;

import java.util.Date;

public class RunInfo {
    private long timelast;
    private float distance;
    private float mean_speed;
    private float max_speed;
    private float earned_coin_value;
    private String date;

    public RunInfo(long timelast, float distance, float mean_speed, float max_speed, float earned_coin_value, String date) {
        this.timelast = timelast;
        this.distance = distance;
        this.mean_speed = mean_speed;
        this.max_speed = max_speed;
        this.earned_coin_value = earned_coin_value;
        this.date = date;
    }

    public float getTimelast() {
        return timelast;
    }

    public float getDistance() {
        return distance;
    }

    public float getMean_speed() {
        return mean_speed;
    }

    public float getMax_speed(){
        return max_speed;
    }

    public float getEarned_coin_value(){
        return earned_coin_value;
    }

    public String getDate() {
        return date;
    }
}
