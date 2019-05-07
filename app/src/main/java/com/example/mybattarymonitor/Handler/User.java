package com.example.mybattarymonitor.Handler;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("sinceHour")
    @Expose
    private int sinceHour;

    @SerializedName("sinceMin")
    @Expose
    private int sinceMin;

    @SerializedName("beforeHour")
    @Expose
    private int beforeHour;

    @SerializedName("beforeMin")
    @Expose
    private int beforeMin;

    @SerializedName("key")
    @Expose
    private String key;

    public int getSinceHour() {
        return sinceHour;
    }

    public int getSinceMin() {
        return sinceMin;
    }

    public int getBeforeHour() {
        return beforeHour;
    }

    public int getBeforeMin() {
        return beforeMin;
    }

    public String getKey() {
        return key;
    }

    public User(int sinceHour, int sinceMin, int beforeHour, int beforeMin, String key) {
        this.sinceHour = sinceHour;
        this.sinceMin = sinceMin;
        this.beforeHour = beforeHour;
        this.beforeMin = beforeMin;
        this.key = key;
    }

}