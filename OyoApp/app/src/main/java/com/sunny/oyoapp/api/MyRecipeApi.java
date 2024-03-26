package com.sunny.oyoapp.api;

import com.sunny.oyoapp.model.RecipeList;
import com.sunny.oyoapp.model.UserInfoRes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface MyRecipeApi {

    @GET("/myrecipe")
    Call<RecipeList> getMyPosting(
            @Header("Authorization") String token,
            @Query("order") String order,
            @Query("offset") int offset,
            @Query("limit") int limit
    );

    @GET("/myrecipe/myinfo")
    Call<UserInfoRes> getMyPostingInfo(
            @Header("Authorization") String token
    );
}
