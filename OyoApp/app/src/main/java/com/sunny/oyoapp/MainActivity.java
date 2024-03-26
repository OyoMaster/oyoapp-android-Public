package com.sunny.oyoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sunny.oyoapp.adapter.RecipeAdapter;
import com.sunny.oyoapp.api.NetworkClient;
import com.sunny.oyoapp.api.RecipeApi;
import com.sunny.oyoapp.config.Config;
import com.sunny.oyoapp.model.Posting;
import com.sunny.oyoapp.model.RecipeList;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    LinearLayout btnHome;
    LinearLayout btnChat;
    LinearLayout btnScrap;
    LinearLayout btnMenu;

    Button btnAi;
    Button btnAi2;

    TextView btnMoreRecipe;
    // 리사이클러뷰 관련 멤버변수
    RecyclerView recyclerView;
    RecipeAdapter adapter;
    ArrayList<Posting> postingArrayList = new ArrayList<>();

    // 페이징 관련 변수
    int offset = 0;
    int limit = 8;
    int count = 0;
    String token;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnHome = findViewById(R.id.btnScrapAt);
        btnChat = findViewById(R.id.btnChat);
        btnScrap = findViewById(R.id.btnScrap);
        btnMenu = findViewById(R.id.btnMenu);
        btnAi = findViewById(R.id.btnAi);
        btnAi2 = findViewById(R.id.btnAi2);
        btnMoreRecipe = findViewById(R.id.btnMoreRecipe);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);


        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
//        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int totalCount = recyclerView.getAdapter().getItemCount();

                if(lastPosition + 1 == totalCount){
                    // 네트워크 통해서 데이터를 더 불러온다.
                    if( limit == count){
                        // DB에 데이터가 더 존재할수 있으니까, 데이터를 불러온다.
                        addNetworkData();
                    }
                }
            }
        });

        btnAi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
                startActivity(intent);
            }
        });

        btnAi2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FridgeRecipeActivity.class);
                startActivity(intent);
            }
        });

        btnMoreRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserRecipeAllActivity.class);
                startActivity(intent);

            }
        });



        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
                startActivity(intent);

            }
        });

        btnScrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ScarpActivity.class);
                startActivity(intent);

            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(intent);

            }
        });




        getSupportActionBar().hide();


        // TODO:토큰 생기면 그때 진행.

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
        String token = sp.getString("token","");

        if(token.isEmpty()){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        getNetworkData();

    }

    private void addNetworkData() {

//        offset = offset + count;


        // 네트워크로 API 호출한다.
        Retrofit retrofit = NetworkClient.getRetrofitClient(MainActivity.this);

        RecipeApi api = retrofit.create(RecipeApi.class);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");
        token = "Bearer " + token;

        Call<RecipeList> call = api.getMainRecipe(token, "p.createdAt" ,offset, limit);

        call.enqueue(new Callback<RecipeList>() {
            @Override
            public void onResponse(Call<RecipeList> call, Response<RecipeList> response) {


                if(response.isSuccessful()){

                    RecipeList recipeList = response.body();

//                    count = recipeList.count;

                    postingArrayList.clear();
                    postingArrayList.addAll( recipeList.items );

                    adapter.notifyDataSetChanged();




                }else{

                }
            }

            @Override
            public void onFailure(Call<RecipeList> call, Throwable t) {


            }
        });


    }

    private void getNetworkData() {

        // 변수 초기화
        offset = 0;
        count = 0;



        // 네트워크로 API 호출한다.
        Retrofit retrofit = NetworkClient.getRetrofitClient(MainActivity.this);

        RecipeApi api = retrofit.create(RecipeApi.class);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
        token = sp.getString("token", "");
        token = "Bearer " + token;

        Call<RecipeList> call = api.getMainRecipe(token,"p.createdAt", offset, limit);

        call.enqueue(new Callback<RecipeList>() {
            @Override
            public void onResponse(Call<RecipeList> call, Response<RecipeList> response) {


                if(response.isSuccessful()){

                    RecipeList recipeList = response.body();

                    count = recipeList.count;

                    postingArrayList.clear();

                    postingArrayList.addAll( recipeList.items );

                    adapter = new RecipeAdapter(MainActivity.this, postingArrayList);

                    recyclerView.setAdapter(adapter);

                }else{

                }

            }

            @Override
            public void onFailure(Call<RecipeList> call, Throwable t) {
                Log.e("NetworkError", "Network request failed", t);


            }
        });


    }


}