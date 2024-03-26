package com.sunny.oyoapp;

import static com.sunny.oyoapp.config.Config.MY_SECRET_KEY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;


import com.sunny.oyoapp.adapter.MessageAdapter;
import com.sunny.oyoapp.model.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatbotActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText editMsg;
    ImageView btnSend;

    List<Message> messageList;
    MessageAdapter messageAdapter;



    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS) // 연결 Timeout 30초로 설정
            .readTimeout(300, TimeUnit.SECONDS) // 읽기 Timeout 30초로 설정
            .writeTimeout(300, TimeUnit.SECONDS) // 쓰기 Timeout 30초로 설정
            .build();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        getSupportActionBar().setTitle("음식 추천봇");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_ios);

        recyclerView = findViewById(R.id.recyclerView);
        editMsg = findViewById(R.id.editMsg);
        btnSend = findViewById(R.id.btnSend);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        recyclerView.setAdapter(messageAdapter);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // JSON 형식의 문자열을 생성하고 이스케이프 처리해야 합니다.


                String question = editMsg.getText().toString().trim();
                addToChat(question, Message.SENT_BY_ME);
                editMsg.setText("");
                // 프롬프트에만 jsonPrompt 추가하여 프롬프트를 줄입니다.
                callAPIWithRecipeQuestion(question);
            }
        });



    }

    void callAPIWithRecipeQuestion(String question) {
        // "레시피 알려줘" 형식인지 확인
        if (question.contains(" 음식 추천해줘")) {
            // "레시피 알려줘" 부분 추출
            String date = question.replace(" 음식 추천해줘", "").trim();

            // OpenAI API 호출
            String apiQuestion = date + " 음식,소개로만 된 json형식으로만 대답해줘 꼭 json형식을 지켜서 대답 해줘 음식이 여러개면  음식마다 음식,소개로 된 별도의 객체로 만들어 result로된 배열로 만들어줘";
            callAPI(apiQuestion);
        } else {
            // 다른 형식의 질문에 대한 처리 로직 추가
            addResponse("죄송합니다. '~~ 음식 추천해줘' 형식으로만 레시피를 제공할 수 있습니다.");
        }
    }

    void addToChat(String message, String sentBy) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
                // 추가된 메시지를 로그로 출력
                Log.d("ChatMessage", "Message: " + message + ", Sent By: " + sentBy);
            }
        });
    }

    void addResponse(String response) {
        messageList.remove(messageList.size() - 1);
        addToChat(response, Message.SENT_BY_BOT);
        // 응답 메시지를 로그로 출력
        Log.d("ChatResponse", "Response: " + response);
    }


    void callAPI(String question) {
        // okhttp
        messageList.add(new Message("음식을 찾고 있습니다", Message.SENT_BY_BOT));

        JSONObject object = new JSONObject();
        try {
            object.put("model", "gpt-3.5-turbo-instruct");
            object.put("prompt", question);
            object.put("max_tokens", 4000);
            object.put("temperature", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization", "Bearer " + MY_SECRET_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String errorMessage = "응답을 불러오지 못했습니다. 오류: " + e.getMessage();
                Log.e("API Failure", errorMessage);
                addResponse(errorMessage);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");
                        Log.i("AAA", result);
                        processRecipeResponse(result.trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("JSON Parsing", "JSON 파싱 오류: " + e.getMessage());
                    }
                } else {
                    String errorMessage = "응답을 불러오지 못했습니다. 오류: " + response.body().string();
                    Log.e("API Response", errorMessage);
                    addResponse(errorMessage);
                }
            }
        });
    }

    void processRecipeResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray jsonArray = jsonResponse.getJSONArray("result");

            addToChat("음식을 찾았습니다.", Message.SENT_BY_BOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String foodName = jsonObject.optString("음식");
                String introduction = jsonObject.optString("소개");
                String message = "음식: " + foodName + "\n소개: " + introduction;

                addToChat(message, Message.SENT_BY_BOT); // 각 음식에 대한 메시지 추가
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON Parsing", "JSON 파싱 오류: " + e.getMessage());
            addResponse("JSON 파싱 오류 발생: " + e.getMessage());
        }
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

            Intent intent = new Intent(ChatbotActivity.this, ScarpActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}