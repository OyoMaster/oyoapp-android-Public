package com.sunny.oyoapp.api;

import com.sunny.oyoapp.model.Follow;
import com.sunny.oyoapp.model.Res;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FollowApi {

    //유저 팔로우하기
    @POST("follow/{followee_id}")
    Call<Res> onFollow(@Path("followee_id")int followee_id,
                       @Header("Authorization") String token
    );

    @DELETE("follow/{followee_id}")
    Call<Res> offFollow(@Path("followee_id")int followee_id,
                        @Header("Authorization") String token);


}