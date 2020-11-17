package com.example.runningman.models;


public class RunInfo {
    private long timelast;
    private float distance;
    private float mean_speed;
    private float max_speed;
    private int earned_coin_value;
    private String date;
    private String step;

    public RunInfo(){

    }
    public RunInfo(long timelast, float distance, float mean_speed, float max_speed,
                   int earned_coin_value, String date, String step) {
        this.timelast = timelast;
        this.distance = distance;
        this.mean_speed = mean_speed;
        this.max_speed = max_speed;
        this.earned_coin_value = earned_coin_value;
        this.date = date;
        this.step = step;
    }

    public long getTimelast() {
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

    public int getEarned_coin_value(){
        return earned_coin_value;
    }

    public String getStep(){
        return step;
    }

    public String getDate() {
        return date;
    }
}
