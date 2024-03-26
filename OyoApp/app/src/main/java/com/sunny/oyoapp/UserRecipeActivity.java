package com.sunny.oyoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sunny.oyoapp.adapter.RecipeAllAdapter;
import com.sunny.oyoapp.api.FollowApi;
import com.sunny.oyoapp.api.NetworkClient;
import com.sunny.oyoapp.api.RecipeApi;
import com.sunny.oyoapp.api.UserApi;
import com.sunny.oyoapp.config.Config;
import com.sunny.oyoapp.model.Follow;
import com.sunny.oyoapp.model.Posting;
import com.sunny.oyoapp.model.ProfileRes;
import com.sunny.oyoapp.model.RecipeList;
import com.sunny.oyoapp.model.Res;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UserRecipeActivity extends AppCompatActivity {

    TextView txtNickname;
    TextView txtId;
    TextView txtContentCnt;
    TextView txtFollower;
    TextView txtFollowee;
    Button btnFollow;
    RecyclerView recyclerView;
    CircleImageView ivProfile;
    RecipeAllAdapter adapter;
    ArrayList<Posting> postingArrayList = new ArrayList<>();
    int offset = 0;
    int limit = 10;
    int count = 0;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_recipe);

        txtNickname = findViewById(R.id.txtNickname);
        txtId = findViewById(R.id.txtId);
        txtContentCnt = findViewById(R.id.txtContentCnt);
        txtFollower = findViewById(R.id.txtFollower);
        txtFollowee = findViewById(R.id.txtFollowee);
        btnFollow = findViewById(R.id.btnFollow);
        ivProfile = findViewById(R.id.ivProfile);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(UserRecipeActivity.this));
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



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_ios);

        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                 팔로우 처리 네트워크로 DB 가져오기..

                // 버튼의 텍스트 값을 가져와서 비교합니다.
                String buttonText = btnFollow.getText().toString();

                int userId = getIntent().getIntExtra("userId", -1);

                Retrofit retrofit = NetworkClient.getRetrofitClient(UserRecipeActivity.this);

                FollowApi api = retrofit.create(FollowApi.class);

                SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
                String token = sp.getString("token", "");
                token = "Bearer " + token;

                if(buttonText.equals("팔로우 하기")){

                    btnFollow.setText("팔로우 헤제");
                    // SharedPreferences에 상태 저장
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("isFollowing" + userId, true);
                    editor.apply();

                    Call<Res> call = api.onFollow(userId,token);
                    call.enqueue(new Callback<Res>() {
                        @Override
                        public void onResponse(Call<Res> call, Response<Res> response) {
                            if (response.isSuccessful()){

                                // 팔로우 상태 변경 후 숫자 업데이트
                                updateFollowNumbers(userId);
                                adapter.notifyDataSetChanged();




                            }else {}
                        }

                        @Override
                        public void onFailure(Call<Res> call, Throwable t) {

                        }
                    });
                }else{
                    btnFollow.setText("팔로우 하기");
                    // SharedPreferences에 상태 저장
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("isFollowing" + userId, false);
                    editor.apply();
                    Call<Res> call = api.offFollow(userId,token);
                    call.enqueue(new Callback<Res>() {
                        @Override
                        public void onResponse(Call<Res> call, Response<Res> response) {
                            if (response.isSuccessful()){

                                // 팔로우 상태 변경 후 숫자 업데이트
                                updateFollowNumbers(userId);
                                adapter.notifyDataSetChanged();


                            }else {}
                        }

                        @Override
                        public void onFailure(Call<Res> call, Throwable t) {

                        }
                    });


                }

            }
        });
        // Load follow status when activity starts
        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
        int userId = getIntent().getIntExtra("userId", -1);
        boolean isFollowing = sp.getBoolean("isFollowing" + userId, false);
        if (isFollowing) {
            btnFollow.setText("팔로우 해제");
        } else {
            btnFollow.setText("팔로우 하기");
        }

    }
    @Override
    protected void onResume() {
        super.onResume();

        getNetworkData();
        getUserProfileData();

    }

    private void getNetworkData() {

        // 변수 초기화
        offset = 0;
        count = 0;



        // 네트워크로 API 호출한다.
        Retrofit retrofit = NetworkClient.getRetrofitClient(UserRecipeActivity.this);

        RecipeApi api = retrofit.create(RecipeApi.class);

        int userId = getIntent().getIntExtra("userId", -1);
        Log.i("AAAA" , "유저 액티이비 ID : " + userId);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
        token = sp.getString("token", "");
        token = "Bearer " + token;

        Call<RecipeList> call = api.getUserRecipe(userId, token, offset, limit);

        call.enqueue(new Callback<RecipeList>() {
            @Override
            public void onResponse(Call<RecipeList> call, Response<RecipeList> response) {


                if(response.isSuccessful()){

                    RecipeList recipeList = response.body();

                    count = recipeList.count;

                    postingArrayList.clear();

                    postingArrayList.addAll( recipeList.items );

                    adapter = new RecipeAllAdapter(UserRecipeActivity.this, postingArrayList);

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

    private void addNetworkData() {

        offset = offset + count;

        // 네트워크로 API 호출한다.
        Retrofit retrofit = NetworkClient.getRetrofitClient(UserRecipeActivity.this);

        RecipeApi api = retrofit.create(RecipeApi.class);
        int userId = getIntent().getIntExtra("userId", -1);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");
        token = "Bearer " + token;

        Call<RecipeList> call = api.getUserRecipe(userId, token,offset, limit);

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

    private void getUserProfileData() {

        // 변수 초기화
        offset = 0;
        count = 0;



        // 네트워크로 API 호출한다.
        Retrofit retrofit = NetworkClient.getRetrofitClient(UserRecipeActivity.this);

        UserApi api = retrofit.create(UserApi.class);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
        token = sp.getString("token", "");
        token = "Bearer " + token;

        // getIntent()를 통해 postingId를 가져와서 전달한다
        int postingId = getIntent().getIntExtra("postingId", -1);
        int userId = getIntent().getIntExtra("userId", -1);


        Call<ProfileRes> call = api.getUserProfile(userId,token);

        call.enqueue(new Callback<ProfileRes>() {
            @Override
            public void onResponse(Call<ProfileRes> call, Response<ProfileRes> response) {
                if (response.isSuccessful()){
                    ProfileRes profileRes = response.body();
                    if (profileRes != null && profileRes.items != null) {
                        // profileRes나 profileRes.items가 null이 아닌 경우에만 데이터를 사용합니다.



                        if (profileRes.items.profileUrl == null || profileRes.items.profileUrl =="") {
                            Glide.with(UserRecipeActivity.this).load(R.drawable.no_profile).into(ivProfile);
                        }else{
                            Glide.with(UserRecipeActivity.this).load(profileRes.items.profileUrl).into(ivProfile);

                        }

                        txtNickname.setText(profileRes.items.nickname);
                        txtId.setText(profileRes.items.email);
                        txtContentCnt.setText(String.valueOf(""+profileRes.items.cntPosting));
                        txtFollower.setText(String.valueOf(""+profileRes.items.cntFollower));
                        txtFollowee.setText(String.valueOf(""+profileRes.items.cntFollowing));

                    } else {
                        Log.e("ProfileData", "Profile data or items are null");
                        // profileRes나 profileRes.items가 null인 경우에 대한 처리를 여기에 추가합니다.
                    }


                }else {
                    Log.e("ProfileResponse", "Unsuccessful response: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ProfileRes> call, Throwable t) {

            }
        });



    }
    private void updateFollowNumbers(int userId) {
        // 네트워크로 팔로워 및 팔로잉 숫자를 가져오는 API 호출
        Retrofit retrofit = NetworkClient.getRetrofitClient(UserRecipeActivity.this);
        UserApi api = retrofit.create(UserApi.class);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
        String token = sp.getString("token", "");
        token = "Bearer " + token;

        Call<ProfileRes> call = api.getUserProfile(userId, token);

        call.enqueue(new Callback<ProfileRes>() {
            @Override
            public void onResponse(Call<ProfileRes> call, Response<ProfileRes> response) {
                if (response.isSuccessful()) {
                    ProfileRes profileRes = response.body();
                    if (profileRes != null && profileRes.items != null) {
                        // 업데이트된 숫자로 화면 업데이트
                        txtFollower.setText(String.valueOf(profileRes.items.cntFollower));
                        txtFollowee.setText(String.valueOf(profileRes.items.cntFollowing));
                    }
                } else {
                    // 실패 처리
                }
            }

            @Override
            public void onFailure(Call<ProfileRes> call, Throwable t) {
                // 실패 처리
            }
        });
    }

}