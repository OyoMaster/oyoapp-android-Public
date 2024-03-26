package com.sunny.oyoapp.api;

import com.sunny.oyoapp.model.ProfileRes;
import com.sunny.oyoapp.model.Res;
import com.sunny.oyoapp.model.User;
import com.sunny.oyoapp.model.UserInfoRes;
import com.sunny.oyoapp.model.UserRes;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserApi {
    // 회원가입 Api
    @POST("/user/register")
    Call<UserRes> register(@Body User user);

    // 로그인 Api
    @POST("/user/login")
    Call<UserRes> login(@Body User user);

    // 로그아웃 Api
    @DELETE("/user/logout")
    Call<Res> logout(@Header("Authorization") String token);

    // 회원정보 변경 Api
    @GET("/user/passwordUpdate")
    Call<UserInfoRes> getUserInfo(@Header("Authorization") String token);

    @Multipart
    @PUT("/user/passwordUpdate")
    Call<UserRes> updateUserInfo(@Header("Authorization") String token,
                                 @Part MultipartBody.Part photo,
                                 @Part("nickname") RequestBody nickname,
                                 @Part("password") RequestBody password);

    @POST("/user/googlelogin")
    Call<UserRes> googlelogin(@Body User user);

    @POST("/user/kakaologin")
    Call<UserRes> kakaologin(@Body User user);

    //유저의 프로필(게시물,팔로워 수)
    @GET("userfollow/{userId}")
    Call<ProfileRes> getUserProfile(@Path("userId") int userId,
                                    @Header("Authorization") String token);

}
