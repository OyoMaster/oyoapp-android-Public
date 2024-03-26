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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.sunny.oyoapp.adapter.ReviewAdapter;
import com.sunny.oyoapp.api.NetworkClient;
import com.sunny.oyoapp.api.RecipeApi;
import com.sunny.oyoapp.api.ReviewApi;
import com.sunny.oyoapp.api.UserApi;
import com.sunny.oyoapp.config.Config;

import com.sunny.oyoapp.model.RecipeDetail;
import com.sunny.oyoapp.model.Res;
import com.sunny.oyoapp.model.Review;
import com.sunny.oyoapp.model.ReviewList;
import com.sunny.oyoapp.model.UserInfoRes;
import com.sunny.oyoapp.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RecipeActivity extends AppCompatActivity {

    CircleImageView ivProfile;
    TextView txtNickname;
    TextView txtDate;
    TextView txtRecipeTitle;
    TextView txtRating;
    ImageView imageRecipe;
    TextView txtIngredients;
    TextView txtIngredientsView;
    TextView txtRecipe;
    TextView txtRecipeView;
    TextView txtComment;

    RecyclerView recyclerView;

    Spinner spinnerComment;
    EditText editComment;
    ImageView btnSend;

    Double[] items = {0.0,1.0,2.0,3.0,4.0,5.0};
    double rating = 0;

    // 페이징 관련 변수
    int offset = 0;
    int limit = 10;
    int count = 0;
    String token;

    ArrayList<Review> reviewArrayList = new ArrayList<>();
    ReviewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        ivProfile = findViewById(R.id.ivProfile);
        txtNickname = findViewById(R.id.txtNickname);
        txtDate = findViewById(R.id.txtDate);
        txtRecipeTitle = findViewById(R.id.txtRecipeTitle);
        txtRating = findViewById(R.id.txtRating);
        imageRecipe = findViewById(R.id.imageRecipe);
        txtIngredients = findViewById(R.id.txtIngredients);
        txtIngredientsView = findViewById(R.id.txtIngredientsView);
        txtRecipe = findViewById(R.id.txtRecipe);
        txtRecipeView = findViewById(R.id.txtRecipeView);
        txtComment = findViewById(R.id.txtComment);
        spinnerComment = findViewById(R.id.spinnerComment);
        editComment = findViewById(R.id.editComment);
        btnSend = findViewById(R.id.btnSend);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(RecipeActivity.this));
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

        ArrayAdapter<Double> adapter = new ArrayAdapter<Double>(
                this, android.R.layout.simple_spinner_item,items);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerComment.setAdapter(adapter);

        spinnerComment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Double selectedItem = items[position];
                // 선택된 아이템에 따라 조건문 추가
                if ("0.0".equals(selectedItem)) {

                    rating =0.0;

                } else if ("1.0".equals(selectedItem)) {
                    rating = 1.0;

                } else if ("2.0".equals(selectedItem)) {

                    rating = 2.0;

                } else if ("3.0".equals(selectedItem)) {
                    rating = 3.0;

                } else if ("4.0".equals(selectedItem)) {
                    rating = 4.0;

                } else if ("5.0".equals(selectedItem)) {
                    rating = 5.0;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });





        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String content = editComment.getText().toString();

                rating = (Double) spinnerComment.getSelectedItem();

                Retrofit retrofit = NetworkClient.getRetrofitClient(RecipeActivity.this);

                ReviewApi api = retrofit.create(ReviewApi.class);

                int postingId = getIntent().getIntExtra("postingId", -1);



                // 토큰 가져온다.
                SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                String token = sp.getString("token", "");
                token = "Bearer " + token;

                // body 에 보낼 json 을, 자바의 객체로 생성
                Review review = new Review(content, rating);

                // API 호출
                Call<Res> call = api.addReview(postingId, token, review);

                call.enqueue(new Callback<Res>() {
                    @Override
                    public void onResponse(Call<Res> call, Response<Res> response) {
                        if (response.isSuccessful()){

                            // 입력 후 버튼 누르면 상세페이지에서 바로 추가되는 것을 볼 수 있도록
                            onResume();

                            // 입력 후 버튼 누르면 키보드 내려가는 코드
                            InputMethodManager mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            mInputMethodManager.hideSoftInputFromWindow(editComment.getWindowToken(), 0);
//                            finish();

                            // 입력 후 버튼 누르면 입력창 초기화
                            editComment.setText(null);

                            // 입력 후 버튼 누르면 스피너 초기화
                            spinnerComment.setSelection(0);


                        }else{
                            return;

                        }
                    }

                    @Override
                    public void onFailure(Call<Res> call, Throwable t) {

                    }
                });

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

            Intent intent = new Intent(RecipeActivity.this, ScarpActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getNetworkData();
        getNetworkReviewData();

    }


    // 레시피 상세보기 불러오는 함수
    private void getNetworkData() {

        // 변수 초기화
        offset = 0;
        count = 0;



        // 네트워크로 API 호출한다.
        Retrofit retrofit = NetworkClient.getRetrofitClient(RecipeActivity.this);

        RecipeApi api = retrofit.create(RecipeApi.class);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
        token = sp.getString("token", "");
        token = "Bearer " + token;

        // getIntent()를 통해 postingId를 가져와서 전달한다
        int postingId = getIntent().getIntExtra("postingId", -1);
        int userId = getIntent().getIntExtra("userId", -1);


        Log.i("AAAA", "받아온 포스팅 두번째 ID : " + postingId);
        Log.i("AAAAA", "받아온 유저 ID : " + userId);

        Call<RecipeDetail> call = api.getDetailRecipe(postingId,token);

        call.enqueue(new Callback<RecipeDetail>() {
            @Override
            public void onResponse(Call<RecipeDetail> call, Response<RecipeDetail> response) {
                if (response.isSuccessful()){
                    RecipeDetail recipeDetail = response.body();

                    if (recipeDetail.items.profileUrl == null || recipeDetail.items.profileUrl =="") {
                        Glide.with(RecipeActivity.this).load(R.drawable.no_profile).into(ivProfile);
                    }else{
                        Glide.with(RecipeActivity.this).load(recipeDetail.items.profileUrl).into(ivProfile);

                    }


                    txtNickname.setText(recipeDetail.items.nickname);
                    txtRecipeTitle.setText(recipeDetail.items.title);
                    txtDate.setText(recipeDetail.items.createdAt);

                    // recipeDetail.items.createdAt으로부터 받은 문자열
                    String originalDateString = recipeDetail.items.createdAt;

                    // SimpleDateFormat을 사용하여 날짜 형식을 지정합니다.
                    SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy년MM월dd일 HH:mm");

                    try {
                        // 원본 문자열을 Date 객체로 파싱합니다.
                        Date date = originalFormat.parse(originalDateString);

                        // Date 객체를 목표 형식의 문자열로 변환합니다.
                        String formattedDateString = targetFormat.format(date);

                        // 변환된 문자열을 TextView에 설정합니다.
                        txtDate.setText(formattedDateString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }



                    txtRating.setText(String.format("%.1f", recipeDetail.items.avgRating));
                    txtIngredientsView.setText(recipeDetail.items.ingredients);
                    txtRecipeView.setText(recipeDetail.items.recipe);
                    Glide.with(RecipeActivity.this).load(recipeDetail.items.imageURL).into(imageRecipe);

                    ivProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            Retrofit retrofit = NetworkClient.getRetrofitClient(RecipeActivity.this);

                            UserApi api = retrofit.create(UserApi.class);

                            SharedPreferences sp = RecipeActivity.this.getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
                            token = sp.getString("token", "");

                            token = "Bearer " + token;

                            Call<UserInfoRes> call  = api.getUserInfo(token);

                            call.enqueue(new Callback<UserInfoRes>(){

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

                                            if(userItem.nickname.equals(txtNickname.getText().toString())){
                                                Intent intent = new Intent(RecipeActivity.this, MyRecipeActivity.class);
                                                startActivity(intent);
                                            }else{
                                                Intent intent = new Intent(RecipeActivity.this, UserRecipeActivity.class);
                                                intent.putExtra("userId", userId);
                                                startActivity(intent);
                                            }
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
                        }
                    });

                    txtNickname.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Retrofit retrofit = NetworkClient.getRetrofitClient(RecipeActivity.this);

                            UserApi api = retrofit.create(UserApi.class);

                            SharedPreferences sp = RecipeActivity.this.getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
                            token = sp.getString("token", "");

                            token = "Bearer " + token;

                            Call<UserInfoRes> call  = api.getUserInfo(token);

                            call.enqueue(new Callback<UserInfoRes>(){

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

                                            if(userItem.nickname.equals(txtNickname.getText().toString())){
                                                Intent intent = new Intent(RecipeActivity.this, MyRecipeActivity.class);
                                                startActivity(intent);
                                            }else{
                                                Intent intent = new Intent(RecipeActivity.this, UserRecipeActivity.class);
                                                intent.putExtra("userId", userId);
                                                startActivity(intent);
                                            }
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



                        }
                    });






                }
            }

            @Override
            public void onFailure(Call<RecipeDetail> call, Throwable t) {

            }
        });



    }

    // 해당 리뷰 불러오는 함수
    private void getNetworkReviewData() {

        // 변수 초기화
        offset = 0;
        count = 0;



        // 네트워크로 API 호출한다.
        Retrofit retrofit = NetworkClient.getRetrofitClient(RecipeActivity.this);

        ReviewApi api = retrofit.create(ReviewApi.class);


        int postingId = getIntent().getIntExtra("postingId", -1);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
        token = sp.getString("token", "");
        token = "Bearer " + token;

        // getIntent()를 통해 postingId를 가져와서 전달한다
//        int postingId = getIntent().getIntExtra("postingId", -1);
//        int rPostingId = getIntent().getIntExtra("r.postingId", -1);
//        Log.i("AAA", "받아온 포스팅ID" + rPostingId);
//        Log.i("AAAA", "받아온 포스팅 두번째 ID" + postingId);

        Call<ReviewList> call = api.getReview(postingId,token,offset,limit);

        call.enqueue(new Callback<ReviewList>() {
            @Override
            public void onResponse(Call<ReviewList> call, Response<ReviewList> response) {
                if (response.isSuccessful()){
                    ReviewList reviewList = response.body();


                    // 리뷰의 총 개수를 가져와서 txtComment에 표시


                    txtComment.setText("댓글   " + reviewList.count + " 개");

                    reviewArrayList.clear();
                    reviewArrayList.addAll(reviewList.items);

                    adapter = new ReviewAdapter(RecipeActivity.this, reviewArrayList);

                    recyclerView.setAdapter(adapter);



                }
            }

            @Override
            public void onFailure(Call<ReviewList> call, Throwable t) {

            }
        });



    }

    // 리뷰 추가하는 함수
    private void addNetworkData() {

        offset = offset + count;


        // 네트워크로 API 호출한다.
        Retrofit retrofit = NetworkClient.getRetrofitClient(RecipeActivity.this);

        ReviewApi api = retrofit.create(ReviewApi.class);


        int postingId = getIntent().getIntExtra("postingId", -1);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");
        token = "Bearer " + token;

        Call<ReviewList> call = api.getReview(postingId ,token , offset, limit);

        call.enqueue(new Callback<ReviewList>() {
            @Override
            public void onResponse(Call<ReviewList> call, Response<ReviewList> response) {


                if(response.isSuccessful()){

                    ReviewList reviewList = response.body();

                    count = reviewList.count;

                    reviewArrayList.clear();
                    reviewArrayList.addAll( reviewList.items );

                    adapter.notifyDataSetChanged();




                }else{

                }
            }

            @Override
            public void onFailure(Call<ReviewList> call, Throwable t) {


            }
        });


    }
}