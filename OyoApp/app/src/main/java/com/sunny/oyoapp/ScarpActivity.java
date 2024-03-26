package com.sunny.oyoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;


import com.sunny.oyoapp.adapter.ScarpAdapter;
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

public class ScarpActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    ScarpAdapter adapter;
    ArrayList<Posting> postingArrayList = new ArrayList<>();

    // 페이징 관련 변수
    int offset = 0;
    int limit = 20;
    int count = 0;
    String token;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scarp);

        getSupportActionBar().setTitle("나의 스크랩");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_ios);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ScarpActivity.this));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int totalCount = recyclerView.getAdapter().getItemCount();

                if (lastPosition + 1 == totalCount) {
                    // 네트워크 통해서 데이터를 더 불러온다.
                    if (limit == count) {
                        // DB에 데이터가 더 존재할수 있으니까, 데이터를 불러온다.
                        addNetworkData();
                    }
                }
            }
        });
        // 어댑터를 초기화하고 리사이클러뷰에 설정
        adapter = new ScarpAdapter(ScarpActivity.this, postingArrayList);
        recyclerView.setAdapter(adapter);

        // 네트워크에서 데이터를 가져옴
        getNetworkData();

    }


    @Override
    protected void onResume() {
        super.onResume();

        getNetworkData();

    }

    private void addNetworkData() {

        offset = offset + count;

        // 네트워크로 API 호출한다.
        Retrofit retrofit = NetworkClient.getRetrofitClient(ScarpActivity.this);

        RecipeApi api = retrofit.create(RecipeApi.class);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");
        token = "Bearer " + token;

        Call<RecipeList> call = api.getMyFavoriteRecipe(token);

        call.enqueue(new Callback<RecipeList>() {
            @Override
            public void onResponse(Call<RecipeList> call, Response<RecipeList> response) {


                if (response.isSuccessful()) {

                    RecipeList recipeList = response.body();

                    count = recipeList.count;

//                    postingArrayList.clear();
                    postingArrayList.addAll(recipeList.items);

                    adapter.notifyDataSetChanged();



                } else {

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
        Retrofit retrofit = NetworkClient.getRetrofitClient(ScarpActivity.this);

        RecipeApi api = retrofit.create(RecipeApi.class);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
        token = sp.getString("token", "");
        token = "Bearer " + token;

        Call<RecipeList> call = api.getMyFavoriteRecipe(token);

        call.enqueue(new Callback<RecipeList>() {
            @Override
            public void onResponse(Call<RecipeList> call, Response<RecipeList> response) {


                if (response.isSuccessful()) {

                    RecipeList recipeList = response.body();

                    count = recipeList.count;

                    postingArrayList.clear();

                    postingArrayList.addAll(recipeList.items);

                    adapter = new ScarpAdapter(ScarpActivity.this, postingArrayList);

                    recyclerView.setAdapter(adapter);

                } else {

                }

            }

            @Override
            public void onFailure(Call<RecipeList> call, Throwable t) {
                Log.e("NetworkError", "Network request failed", t);


            }
        });


    }
}
