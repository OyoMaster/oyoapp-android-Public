package com.sunny.oyoapp.api;

import android.content.Context;

import com.sunny.oyoapp.config.Config;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {

    public static Retrofit retrofit; // 라이브러리 사용을 위한 변수 사용

    public static Retrofit getRetrofitClient(Context context){
        if(retrofit == null){
            // 통신 로그 확인할때 필요한 코드
            HttpLoggingInterceptor loggingInterceptor =
                    new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC); // 이 부분만 변경 ★

            // 네트워크 연결관련 코드
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES) // 1분동안 작업을 하기 위해 연결 (사진 전송 등)
                    .readTimeout(1, TimeUnit.MINUTES) // 데이터를 가져올 때
                    .writeTimeout(1, TimeUnit.MINUTES) // 보낼 때
                    .addInterceptor(loggingInterceptor)
                    .build();
            // 네트워크로 데이터를 보내고 받는
            // 레트로핏 라이브러리 관련 코드
            retrofit = new Retrofit.Builder()
                    .baseUrl(Config.DOMAIN)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
