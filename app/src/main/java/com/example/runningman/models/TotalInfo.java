package com.example.runningman.models;


public class TotalInfo {
    private long timelast;
    private float distance;
    private String step;
    private int coins;

    public TotalInfo(){

    }

    public TotalInfo(long timelast, float distance, String step, int coins) {
        this.timelast = timelast;
        this.distance = distance;
        this.coins = coins;
        this.step = step;
    }

    public long getTimelast() {
        return timelast;
    }

    public float getDistance() {
        return distance;
    }

    public int getCoins(){
        return coins;
    }

    public String getStep(){
        return step;
    }

}
