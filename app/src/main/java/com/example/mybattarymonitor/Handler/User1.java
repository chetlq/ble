package com.example.mybattarymonitor.Handler;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class User1 {
    public ArrayList<User> getUser() {
        return user;
    }

    public void setUser(ArrayList<User> user) {
        this.user = user;
    }

    @SerializedName("user")
    @Expose
    private ArrayList<User> user;


}
