package com.sunny.oyoapp.api;

import com.sunny.oyoapp.model.Res;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoritesApi {

    // 좋아요 API
    @POST("/favorites/{postingId}")
    Call<Res> setFavorite(@Path("postingId") int postingId,
                          @Header("Authorization") String token);

    // 좋아요 해지 API
    @DELETE("/favorites/{postingId}")
    Call<Res> deleteFavorite(@Path("postingId") int postingId,
                             @Header("Authorization") String token);

}
