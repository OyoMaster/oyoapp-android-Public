package com.sunny.oyoapp.api;

import com.sunny.oyoapp.model.RecipeDetail;
import com.sunny.oyoapp.model.RecipeList;
import com.sunny.oyoapp.model.Res;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RecipeApi {

    // 레시피 작성 api
    @Multipart
    @POST("/recipes/add")
    Call<Res> add(
            @Header("Authorization") String token,
            @Part MultipartBody.Part photo,
            @Part("title") RequestBody title,
            @Part("ingredients") RequestBody ingredients,
            @Part("recipe") RequestBody recipe);

    // 레시피 수정 API
    @Multipart
    @PUT("/myrecipes/{Myrecipes_id}")
    Call<Res> update(
            @Path("Myrecipes_id") int Myrecipes_id,
            @Header("Authorization") String token,
            @Part MultipartBody.Part photo,
            @Part("title") RequestBody title,
            @Part("ingredients") RequestBody ingredients,
            @Part("recipe") RequestBody recipe);

    // 레시피 삭제 API
    @DELETE("/myrecipes/{Myrecipes_id}")
    Call<Res> delete(
            @Path("Myrecipes_id") int Myrecipes_id,
            @Header("Authorization") String token
    );

    // 모든 레시피(간략히) 가져오는 API
    @GET("/recipe")
    Call<RecipeList> getMainRecipe(@Header("Authorization") String token,
                                   @Query("order") String order,
                                   @Query("offset") int offset,
                                   @Query("limit") int limit);


    // 모든 레시피(상세히) 가져오는 API
    @GET("/recipemore")
    Call<RecipeList> getMyPosting(@Header("Authorization") String token,
                                  @Query("order") String order,
                                  @Query("offset") int offset,
                                  @Query("limit") int limit);

    // 레시피 상세보기 API
    @GET("/recipe/{posting_id}")
    Call<RecipeDetail> getDetailRecipe(@Path("posting_id") int postingId,
                                       @Header("Authorization") String token);

    // 내가 즐겨찾기한 레시피 API
    @GET("/favoriterecipe")
    Call<RecipeList> getMyFavoriteRecipe(@Header("Authorization") String token);

    // 해당 유저의 레시피 API
    @GET("/userprofile/{userId}")
    Call<RecipeList> getUserRecipe(@Path("userId") int userId,
                                   @Header("Authorization") String token,
                                   @Query("offset") int offset,
                                   @Query("limit") int limit);
}
