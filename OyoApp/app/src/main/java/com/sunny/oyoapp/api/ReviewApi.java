package com.sunny.oyoapp.api;

import com.sunny.oyoapp.model.Res;
import com.sunny.oyoapp.model.Review;
import com.sunny.oyoapp.model.ReviewList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ReviewApi {


    // 리뷰 작성하는 API
    @POST("/review/{postingId}")
    Call<Res> addReview(@Path("postingId") int postingId,
                        @Header("Authorization") String token,
                        @Body Review review);

    // 리뷰 불러오기 API
    @GET("/review/{postingId}")
    Call<ReviewList> getReview(@Path("postingId") int postingId,
                               @Header("Authorization") String token,
                               @Query("offset") int offset,
                               @Query("limit") int limit);

}
