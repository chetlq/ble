package com.example.mybattarymonitor.Handler;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkServiceHolder {
    private static NetworkServiceHolder mInstance;
    private static final String BASE_URL = "http://185.174.130.200";
    private Retrofit mRetrofit;

    private NetworkServiceHolder() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)//"https://enj0o4y5g3cn.x.pipedream.net/")//BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    public static NetworkServiceHolder getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkServiceHolder();
        }
        return mInstance;
    }
    public JSONUserHolderApi getJSONApi() {
        return mRetrofit.create(JSONUserHolderApi.class);
    }


}