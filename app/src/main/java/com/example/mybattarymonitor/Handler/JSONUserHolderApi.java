package com.example.mybattarymonitor.Handler;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface JSONUserHolderApi {
//    @FormUrlEncoded
//    @POST("/massHandler.php")
//    Call<User> getHandler(@Field("andreq") String andreq);

    @FormUrlEncoded
    @POST("/massHandler.php")
    Call<User1> getHandler1(@Field("andreq") String andreq);
}