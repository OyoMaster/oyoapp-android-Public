package com.sunny.oyoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sunny.oyoapp.api.NetworkClient;
import com.sunny.oyoapp.api.UserApi;
import com.sunny.oyoapp.config.Config;
import com.sunny.oyoapp.model.Res;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MenuActivity extends AppCompatActivity {

    TextView btnChatbot;
    TextView btnFridgeRecipe;
    TextView btnYoutube;
    TextView btnThem;
    TextView btnUserRecipe;
    TextView btnMyRecipe;
    TextView btnUserInfo;
    TextView btnLogout;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        getSupportActionBar().setTitle("전체 메뉴");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_ios);

        btnChatbot = findViewById(R.id.btnChatbot);
        btnFridgeRecipe = findViewById(R.id.btnFridgeRecipe);
        btnYoutube = findViewById(R.id.btnYoutube);
        btnThem = findViewById(R.id.btnThem);
        btnUserRecipe = findViewById(R.id.btnUserRecipe);
        btnMyRecipe = findViewById(R.id.btnMyRecipe);
        btnUserInfo = findViewById(R.id.btnUserInfo);
        btnLogout = findViewById(R.id.btnLogout);


        btnChatbot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ChatbotActivity.class);
                startActivity(intent);
            }
        });

        btnFridgeRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, FridgeRecipeActivity.class);
                startActivity(intent);
            }
        });

        btnYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, YoutubeActivity.class);
                startActivity(intent);
            }
        });

        btnUserRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, UserRecipeAllActivity.class);
                startActivity(intent);
            }
        });

        btnMyRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, MyRecipeActivity.class);
                startActivity(intent);

            }
        });

        btnUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, UpdateUserActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Retrofit retrofit = NetworkClient.getRetrofitClient(MenuActivity.this);
                UserApi api = retrofit.create(UserApi.class);
                SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                token = sp.getString("token", "");
                token = "Bearer " + token;

                Call<Res> call = api.logout(token);

                call.enqueue(new Callback<Res>() {
                    @Override
                    public void onResponse(Call<Res> call, Response<Res> response) {
                        if(response.isSuccessful()){

                            // 쉐어드프리퍼런스의 token 을 없애야 한다.
                            SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("token", "");
                            editor.apply();

                            // 로그인액티비티를 띄우고 메인종료!
                            Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                            startActivity(intent);

                            finish();

                        }else{

                        }
                    }

                    @Override
                    public void onFailure(Call<Res> call, Throwable t) {

                    }
                });
            }
        });


    }
}