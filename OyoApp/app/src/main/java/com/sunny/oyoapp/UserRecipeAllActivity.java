package com.sunny.oyoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sunny.oyoapp.adapter.RecipeAllAdapter;
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

public class UserRecipeAllActivity extends AppCompatActivity {

    Spinner spinnerRecipe;
    String[] items = {"최신순","별점순"};

    FloatingActionButton btnAdd;
    RecyclerView recyclerView;

    RecipeAllAdapter adapter;
    ArrayList<Posting> postingArrayList = new ArrayList<>();

    // 페이징 관련 변수
    int offset = 0;
    int limit = 20;
    int count = 0;
    String token;
    String order = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_recipe_all);


        getSupportActionBar().setTitle("회원님들의 레시피");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_ios);

        spinnerRecipe = findViewById(R.id.spinnerRecipe);
        btnAdd = findViewById(R.id.btnAdd);
        recyclerView = findViewById(R.id.recyclerVeiw);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(UserRecipeAllActivity.this));

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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,items);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerRecipe.setAdapter(adapter);

        spinnerRecipe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = items[position]; // 선택된 아이템 가져오기

                // 선택된 아이템에 따라 조건문 추가
                if ("최신순".equals(selectedItem)) {


                    order = "p.createdAt";

                    // 최신순에 대한 이벤트 처리
                    // 예: 최신순 정렬 로직 수행
                    // updateRecyclerView("최신순");
                } else if ("오래된 순".equals(selectedItem)) {
                    order = "p.createdAt";

                    // 오래된 순에 대한 이벤트 처리
                    // 예: 오래된 순 정렬 로직 수행
                    // updateRecyclerView("오래된 순");
                } else if ("별점순".equals(selectedItem)) {

                    order = "avgRating";




                    // 별점순에 대한 이벤트 처리
                    // 예: 별점순 정렬 로직 수행
                    // updateRecyclerView("별점순");
                }
                getNetworkData();

                // 추가적으로 필요한 작업을 수행할 수 있습니다.


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무것도 선택되지 않았을 때의 처리

                getNetworkData();

            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 저장 액티비티로 넘어가서 저장한 데이터 다시 양방향 데이터로 받아 와야함.

                Intent intent = new Intent(UserRecipeAllActivity.this, AddRecipeActivity.class);
                startActivity(intent);
            }
        });
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // 눌렀을 때 새로운 액티비티
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();

        if(itemId == R.id.btnScrapAt){

            Intent intent = new Intent(UserRecipeAllActivity.this, ScarpActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getNetworkData();

    }

    private void addNetworkData() {

        offset = offset + count;

        // 네트워크로 API 호출한다.
        Retrofit retrofit = NetworkClient.getRetrofitClient(UserRecipeAllActivity.this);

        RecipeApi api = retrofit.create(RecipeApi.class);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");
        token = "Bearer " + token;

        Call<RecipeList> call = api.getMyPosting(token, order ,offset, limit);

        call.enqueue(new Callback<RecipeList>() {
            @Override
            public void onResponse(Call<RecipeList> call, Response<RecipeList> response) {


                if(response.isSuccessful()){

                    RecipeList recipeList = response.body();

                    count = recipeList.count;

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
        Retrofit retrofit = NetworkClient.getRetrofitClient(UserRecipeAllActivity.this);

        RecipeApi api = retrofit.create(RecipeApi.class);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
        token = sp.getString("token", "");
        token = "Bearer " + token;

        Call<RecipeList> call = api.getMyPosting(token,order, offset, limit);

        call.enqueue(new Callback<RecipeList>() {
            @Override
            public void onResponse(Call<RecipeList> call, Response<RecipeList> response) {


                if(response.isSuccessful()){

                    RecipeList recipeList = response.body();

                    count = recipeList.count;

                    postingArrayList.clear();

                    postingArrayList.addAll( recipeList.items );

                    adapter = new RecipeAllAdapter(UserRecipeAllActivity.this, postingArrayList);

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