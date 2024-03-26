package com.sunny.oyoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sunny.oyoapp.adapter.YoutubeAdapter;

import com.sunny.oyoapp.config.Config;
import com.sunny.oyoapp.model.Video;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class YoutubeActivity extends AppCompatActivity {

    ImageView btnBack;
    EditText editSearch;
    ImageView btnSearch;

    String keyword;

    RecyclerView recyclerView;
    YoutubeAdapter adapter;
    ArrayList<Video> videoArrayList = new ArrayList<>();

    // 페이징 처리에 필요한 변수
    String pageToken;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);

        getSupportActionBar().hide();

        btnBack = findViewById(R.id.btnBack);
        editSearch = findViewById(R.id.editSearch);
        btnSearch = findViewById(R.id.btnSearch);

        String foodName = getIntent().getStringExtra("foodName");
        editSearch.setText(foodName+ " 레시피");


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(YoutubeActivity.this));

        adapter = new YoutubeAdapter(YoutubeActivity.this, videoArrayList);
        recyclerView.setAdapter(adapter);


        // 페이징 처리
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 맨 마지막 데이터가, 화면에 나타나면
                // 네트워크 통해서 데이터를 추가로 받아오고, 화면에 표시.
                int lastPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                // 맨 마지막 위치값을 가져오는..
                int totalCount = recyclerView.getAdapter().getItemCount();


                // 스크롤을 맨 끝까지 한 상태 체크
                if(lastPosition +1 == totalCount){
                    //네트워크 통해서 데이터를 추가로 받아오고, 화면에 표시.
                    addNetworkData();
                }
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyword = editSearch.getText().toString().trim();

                if(keyword.isEmpty()){
                    Toast.makeText(YoutubeActivity.this,"키워드를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                } // 키보드 숨기기
                hideKeyboard();

                getNetworkData();
            }
        });



        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(YoutubeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void getNetworkData() {
        RequestQueue queue = Volley.newRequestQueue(YoutubeActivity.this);

        String url = Config.YDOMAIN+"?key="+Config.Y_API_KEY+"&part=snippet&q="+keyword+" &type=video&maxResults=25&order=date";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        videoArrayList.clear();
                        pageToken ="";

                        try{
                            pageToken = response.getString("nextPageToken");
                            JSONArray items = response.getJSONArray("items");

                            for(int i = 0; i<items.length();i++){
                                String videoId = items.getJSONObject(i).getJSONObject("id").getString("videoId");
                                String title = items.getJSONObject(i).getJSONObject("snippet").getString("title");
                                String chTitle = items.getJSONObject(i).getJSONObject("snippet").getString("channelTitle");
                                String highUrl = items.getJSONObject(i).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high").getString("url");
                                String pubTime = items.getJSONObject(i).getJSONObject("snippet").getString("publishTime");

                                Video video = new Video(videoId,title,highUrl,chTitle,pubTime);

                                videoArrayList.add(video);

                            }
                            adapter = new YoutubeAdapter(YoutubeActivity.this, videoArrayList);

                            recyclerView.setAdapter(adapter);


                        } catch (JSONException e) {
                        }
                    }
                },
                new Response.ErrorListener(){

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );


        queue.add(request);
    }

    //데이터 추가하기
    private void addNetworkData() {

        RequestQueue queue = Volley.newRequestQueue(YoutubeActivity.this);


        String url = Config.YDOMAIN+"?key="+Config.Y_API_KEY+"&part=snippet&q="+keyword+" &type=video&maxResults=25&order=date";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            pageToken = response.getString("nextPageToken");
                            JSONArray items = response.getJSONArray("items");

                            for(int i = 0; i < items.length();i++){

                                String videoId = items.getJSONObject(i).getJSONObject("id").getString("videoId");
                                String title = items.getJSONObject(i).getJSONObject("snippet").getString("title");
                                String chTitle = items.getJSONObject(i).getJSONObject("snippet").getString("channelTitle");
                                String highUrl = items.getJSONObject(i).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high").getString("url");
                                String pubTime = items.getJSONObject(i).getJSONObject("snippet").getString("publishTime");

                                Video video = new Video(videoId,title,highUrl,chTitle,pubTime);

                                videoArrayList.add(video);

                            }
                            adapter.notifyDataSetChanged();
                        }catch (JSONException e) {
                            //유저한테 알리기 //토스트 쓰기.
                            return;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );
        queue.add(request);
    }



}