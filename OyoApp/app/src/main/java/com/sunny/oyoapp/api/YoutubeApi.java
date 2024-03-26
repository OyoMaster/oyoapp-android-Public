package com.sunny.oyoapp.api;

import com.sunny.oyoapp.model.VideoList;
import com.sunny.oyoapp.model.VideoList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YoutubeApi {
    @GET("search")
    Call<VideoList> searchVideos(
            @Query("part") String part,
            @Query("q") String query,
            @Query("type") String type,
            @Query("maxResults") int maxResults,
            @Query("order") String order,
            @Query("key") String apiKey,
            @Query("pageToken") String pageToken

    );

}

