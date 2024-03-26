package com.sunny.oyoapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sunny.oyoapp.adapter.MyRecipeListAdapter;
import com.sunny.oyoapp.api.MyRecipeApi;
import com.sunny.oyoapp.api.NetworkClient;
import com.sunny.oyoapp.config.Config;
import com.sunny.oyoapp.model.Posting;
import com.sunny.oyoapp.model.RecipeList;
import com.sunny.oyoapp.model.UserInfoRes;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyRecipeActivity extends AppCompatActivity {

    CircleImageView ivProfile;

    TextView txtNickname;
    TextView txtId;
    TextView txtContentCnt;
    TextView txtFollower;
    TextView txtFollowee;

    Button btnUpdateProfile;

    RecyclerView recyclerView;

    MyRecipeListAdapter adapter;
    ArrayList<Posting> postingArrayList = new ArrayList<>();

    int offset = 0;
    int limit = 20;
    int count = 0;

    String order = "p.createdAt";


    String token;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipe);

        getSupportActionBar().setTitle("나의 레시피");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_ios);


        ivProfile = findViewById(R.id.ivProfile);
        txtNickname = findViewById(R.id.txtNickname);
        txtId = findViewById(R.id.txtId);
        txtContentCnt = findViewById(R.id.txtContentCnt);
        txtFollower = findViewById(R.id.txtFollower);
        txtFollowee = findViewById(R.id.txtFollowee);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        recyclerView = findViewById(R.id.recyclerView);

        Retrofit retrofit = NetworkClient.getRetrofitClient(MyRecipeActivity.this);

        MyRecipeApi api = retrofit.create(MyRecipeApi.class);

        SharedPreferences sp = MyRecipeActivity.this.getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
        token = sp.getString("token", "");
        token = "Bearer " + token;

        Call<UserInfoRes> call = api.getMyPostingInfo(token);

        call.enqueue(new Callback<UserInfoRes>() {
            @Override
            public void onResponse(Call<UserInfoRes> call, Response<UserInfoRes> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // API 응답이 성공적으로 수행되었을 때

                    UserInfoRes userInfoRes = response.body();

                    if (userInfoRes.items != null) {
                        // 사용자 정보가 존재하면 사용자 정보 가져오기
                        UserInfoRes.UserItem userItem = userInfoRes.items;

                        // Log를 통해 사용자 정보 확인
                        Log.d("UpdateUserActivity", "User Nickname: " + userItem.nickname);

                        // 사용자 닉네임 설정
                        if(userItem.profileUrl == null){
                            Glide.with(MyRecipeActivity.this).load(R.drawable.no_profile).into(ivProfile);

                        }else {
                            Glide.with(MyRecipeActivity.this).load(userItem.profileUrl).into(ivProfile);
                        }
                        txtNickname.setText(userItem.nickname);
                        txtId.setText(userItem.email);
                        txtNickname.setText(userItem.nickname);
                        txtContentCnt.setText(userItem.postingCnt+"");
                        txtFollower.setText(userItem.follwerCnt+"");
                        txtFollowee.setText(userItem.followeeCnt+"");

                        // Log를 통해 txtNickname의 값 확인
                        Log.d("UpdateUserActivity", "txtNickname value: " + txtNickname.getText().toString());
                    } else {
                        // 사용자 정보가 비어있을 경우에 대한 처리
                        txtNickname.setText("사용자 정보가 없습니다.");
                    }
                } else {
                    // API 응답이 실패했을 때
                    txtNickname.setText("외안되");

                    // Log를 통해 실패한 응답 확인
                    Log.e("UpdateUserActivity", "API 응답 실패: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserInfoRes> call, Throwable t) {

            }
        });


        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyRecipeActivity.this, UpdateUserActivity.class);
                startActivity(intent);
                finish();
            }
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MyRecipeActivity.this));

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

    }
    @Override
    protected void onResume() {
        super.onResume();

        getNetworkData();

    }
    private void addNetworkData() {

        offset = offset + count;
        order = "p.createdAt";

        // 네트워크로 API 호출한다.
        Retrofit retrofit = NetworkClient.getRetrofitClient(MyRecipeActivity.this);

        MyRecipeApi api = retrofit.create(MyRecipeApi.class);

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

        // 네트워크로 API 호출한다.
        Retrofit retrofit = NetworkClient.getRetrofitClient(MyRecipeActivity.this);

        MyRecipeApi api = retrofit.create(MyRecipeApi.class);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
        token = sp.getString("token", "");
        token = "Bearer " + token;

        Call<RecipeList> call = api.getMyPosting(token, order, offset, limit);

        call.enqueue(new Callback<RecipeList>() {
            @Override
            public void onResponse(Call<RecipeList> call, Response<RecipeList> response) {
                if (response.isSuccessful()) {
                    RecipeList recipeList = response.body();

                    count = recipeList.count;

                    postingArrayList.clear();
                    postingArrayList.addAll(recipeList.items);

                    // 어댑터를 생성하고 RecyclerView에 설정
                    adapter = new MyRecipeListAdapter(MyRecipeActivity.this, postingArrayList);
                    recyclerView.setAdapter(adapter);

                } else {
                    // Handle unsuccessful response
                }
            }

            @Override
            public void onFailure(Call<RecipeList> call, Throwable t) {
                Log.e("NetworkError", "Network request failed", t);
            }
        });
    }
}
